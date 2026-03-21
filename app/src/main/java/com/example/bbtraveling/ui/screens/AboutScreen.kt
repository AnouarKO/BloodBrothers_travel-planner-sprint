package com.example.bbtraveling.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bbtraveling.R
import com.example.bbtraveling.ui.preview.PreviewScreenContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: (() -> Unit)?,
    onOpenTerms: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val bannerGradient = listOf(Color(0xFF6417F4), Color(0xFFFFCB2D))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_about)) },
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
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = scheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(bannerGradient)
                            )
                            .padding(20.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.bb_logo),
                            contentDescription = null,
                            modifier = Modifier.height(90.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.about_banner_subtitle),
                            color = Color.White.copy(alpha = 0.92f)
                        )
                    }
                }
            }

            item {
                InfoCard(
                    title = stringResource(R.string.about_team_title),
                    body = stringResource(R.string.about_team_body)
                )
            }

            item {
                InfoCard(
                    title = stringResource(R.string.about_stack_title),
                    body = stringResource(R.string.about_stack_body)
                )
            }

            item {
                InfoCard(
                    title = stringResource(R.string.about_version_title),
                    body = stringResource(R.string.about_version_body)
                )
            }

            item {
                InfoCard(
                    title = stringResource(R.string.about_license_title),
                    body = stringResource(R.string.about_license_body)
                )
            }

            item {
                Button(onClick = onOpenTerms, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.Gavel, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.action_open_terms))
                }
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AboutScreenPreview() {
    PreviewScreenContainer {
        AboutScreen(
            onBack = {},
            onOpenTerms = {}
        )
    }
}
