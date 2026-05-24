package com.example.bbtraveling.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bbtraveling.R
import com.example.bbtraveling.domain.Photo
import com.example.bbtraveling.domain.Trip
import com.example.bbtraveling.domain.TripImage
import com.example.bbtraveling.ui.preview.PreviewScreenContainer
import com.example.bbtraveling.ui.preview.previewTrips

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    tripId: String?,
    trips: List<Trip>,
    tripImages: List<TripImage> = emptyList(),
    onAddTripImage: ((String, String) -> Unit)? = null,
    onDeleteTripImage: ((String) -> Unit)? = null,
    onBack: (() -> Unit)?
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val currentTrip = trips.firstOrNull { it.id == tripId }
    val defaultTitle = stringResource(R.string.title_gallery)
    val title = if (tripId == null) defaultTitle else (currentTrip?.title ?: defaultTitle)
    val photos: List<Photo> = if (tripId == null) trips.flatMap { it.photos } else currentTrip?.photos.orEmpty()
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null && tripId != null && onAddTripImage != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            onAddTripImage(uri.toString(), title)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = scheme.secondaryContainer.copy(alpha = 0.72f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(title, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.gallery_items_ready, tripImages.size + photos.size),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { imagePicker.launch(arrayOf("image/*")) },
                    enabled = tripId != null && onAddTripImage != null
                ) {
                    Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.gallery_add_trip_image))
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.gallery_section_title), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tripImages) { image ->
                    TripImageTile(
                        image = image,
                        onDelete = onDeleteTripImage?.let { delete -> { delete(image.id) } }
                    )
                }
                items(photos) { photo ->
                    PhotoTile(photo = photo)
                }
                if (tripImages.isEmpty() && photos.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.gallery_no_trip_images),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TripImageTile(
    image: TripImage,
    onDelete: (() -> Unit)?
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(180.dp)
        ) {
            AsyncImage(
                model = image.uri,
                contentDescription = image.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            if (onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = stringResource(R.string.cd_delete_image),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoTile(photo: Photo) {
    val scheme = MaterialTheme.colorScheme
    val tileGradient = listOf(Color(0xFF6A1CF7), Color(0xFFFFCE2E))

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(tileGradient)
                )
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = photo.resId),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.height(34.dp))
            Text(photo.title, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(Modifier.height(4.dp))
            Text(photo.spot, color = Color.White.copy(alpha = 0.9f))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun GalleryScreenPreview() {
    val trips = previewTrips()
    PreviewScreenContainer {
        GalleryScreen(
            tripId = trips.firstOrNull()?.id,
            trips = trips,
            onBack = {}
        )
    }
}
