package com.princelumpy.breakvault.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppLinearProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
) {
    LinearProgressIndicator(
        progress = progress,
        modifier = modifier.fillMaxWidth(), // Always fill width by default
        color = MaterialTheme.colorScheme.primary, // Use primary color for progress
        trackColor = MaterialTheme.colorScheme.outlineVariant, // High-contrast track
    )
}
