package com.example.bbtraveling.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bbtraveling.R
import com.example.bbtraveling.domain.Trip
import com.example.bbtraveling.domain.TripStatus
import com.example.bbtraveling.ui.formatEuro
import com.example.bbtraveling.ui.preview.PreviewScreenContainer
import com.example.bbtraveling.ui.preview.previewTrips
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    trips: List<Trip>,
    onTripClick: (String) -> Unit,
    onOpenTrips: () -> Unit
) {
    val nextTrip = trips
        .filterNot { it.status == TripStatus.Completed }
        .minByOrNull { it.startDate }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.title_dashboard)) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            HeroCard(tripCount = trips.size)

            if (nextTrip != null) {
                NextTripCard(
                    trip = nextTrip,
                    onOpenTrip = { onTripClick(nextTrip.id) },
                    onOpenTrips = onOpenTrips
                )
            }

            StatsRow(trips = trips)
        }
    }
}

@Composable
private fun HeroCard(tripCount: Int) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF6A1CF7), Color(0xFFFFC928))
                    )
                )
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.bb_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.home_hero_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.home_hero_subtitle, tripCount),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.92f)
                )
            }
        }
    }
}

@Composable
private fun NextTripCard(
    trip: Trip,
    onOpenTrip: () -> Unit,
    onOpenTrips: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF6A1CF7).copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(stringResource(R.string.home_next_trip), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                text = trip.title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = trip.destination,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${trip.startDate.format(DATE_FORMAT)} - ${trip.endDate.format(DATE_FORMAT)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(
                    R.string.trip_status_value,
                    stringResource(tripStatusLabelResId(trip.status))
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = onOpenTrip,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.action_open),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                OutlinedButton(
                    onClick = onOpenTrips,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.action_see_all_trips),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun tripStatusLabelResId(status: TripStatus): Int {
    return when (status) {
        TripStatus.Draft -> R.string.trip_status_draft
        TripStatus.Planning -> R.string.trip_status_planning
        TripStatus.Upcoming -> R.string.trip_status_upcoming
        TripStatus.Completed -> R.string.trip_status_completed
    }
}

@Composable
private fun StatsRow(trips: List<Trip>) {
    val totalBudget = trips.sumOf { it.budgetEur }
    val totalSpent = trips.sumOf { it.spentEur }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SmallStatCard(
            label = stringResource(R.string.home_stat_trips),
            value = trips.size.toString(),
            modifier = Modifier.weight(1f)
        )
        SmallStatCard(
            label = stringResource(R.string.home_stat_budget),
            value = formatEuro(totalBudget),
            modifier = Modifier.weight(1f)
        )
        SmallStatCard(
            label = stringResource(R.string.home_stat_spent),
            value = formatEuro(totalSpent),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SmallStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(PaddingValues(horizontal = 12.dp, vertical = 14.dp)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    PreviewScreenContainer {
        HomeScreen(
            trips = previewTrips(),
            onTripClick = {},
            onOpenTrips = {}
        )
    }
}
