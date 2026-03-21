package com.example.bbtraveling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bbtraveling.R
import com.example.bbtraveling.ui.preview.PreviewScreenContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.title_terms)) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp)
                ) {
                    Text(stringResource(R.string.terms_notice_title), style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.terms_intro))
                    Spacer(Modifier.height(16.dp))
                    TermsSection(
                        title = stringResource(R.string.terms_scope_title),
                        body = stringResource(R.string.terms_scope_body)
                    )
                    TermsSection(
                        title = stringResource(R.string.terms_content_title),
                        body = stringResource(R.string.terms_content_body)
                    )
                    TermsSection(
                        title = stringResource(R.string.terms_privacy_title),
                        body = stringResource(R.string.terms_privacy_body)
                    )
                    TermsSection(
                        title = stringResource(R.string.terms_acceptance_title),
                        body = stringResource(R.string.terms_acceptance_body)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.action_reject))
                }
                Spacer(Modifier.width(12.dp))
                Button(onClick = onAccept, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.action_accept))
                }
            }
        }
    }
}

@Composable
private fun TermsSection(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Spacer(Modifier.height(12.dp))
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun TermsScreenPreview() {
    PreviewScreenContainer {
        TermsScreen(
            onAccept = {},
            onReject = {}
        )
    }
}
