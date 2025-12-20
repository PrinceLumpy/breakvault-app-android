package com.princelumpy.breakvault.ui.settings

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.ui.theme.ComboGeneratorTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    // UPDATED: Use collectAsStateWithLifecycle for better lifecycle management.
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // UPDATED: LaunchedEffect now reacts to changes in the snackbarMessage from the UiState.
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            settingsViewModel.onSnackbarShown() // Notify ViewModel that snackbar was shown
        }
    }

    val exportDataLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri: Uri? ->
            uri?.let { settingsViewModel.exportData(it) }
        }
    )

    val importDataLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let { settingsViewModel.importData(it) }
        }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = { onNavigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.common_back_button_description)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsButton(text = stringResource(id = R.string.settings_export_data_button)) {
                Log.i("SettingsScreen", "Export Data Clicked")
                exportDataLauncher.launch("combos_backup.json")
            }

            SettingsButton(text = stringResource(id = R.string.settings_import_data_button)) {
                Log.i("SettingsScreen", "Import Data Clicked")
                importDataLauncher.launch(arrayOf("application/json"))
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                // UPDATED: onClick now calls the ViewModel function.
                onClick = { settingsViewModel.onResetDatabaseClicked() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(id = R.string.settings_reset_database_button))
            }
        }
    }

    // UPDATED: Dialog visibility is now controlled by the UiState from the ViewModel.
    if (uiState.showResetConfirmDialog) {
        ResetConfirmationDialog(
            onConfirm = {
                Log.i("SettingsScreen", "Database reset confirmed")
                // UPDATED: onConfirm now calls the ViewModel function.
                settingsViewModel.onResetDatabaseConfirm()
            },
            onDismiss = {
                // UPDATED: onDismiss now calls the ViewModel function.
                settingsViewModel.onResetDatabaseDismiss()
            }
        )
    }
}

// Private helper composable remain unchanged as they are pure UI
@Composable
private fun SettingsButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}

@Composable
private fun ResetConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.settings_reset_dialog_title)) },
        text = { Text(stringResource(id = R.string.settings_reset_dialog_message)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(id = R.string.settings_reset_dialog_confirm_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.common_cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    ComboGeneratorTheme {
        SettingsScreen(onNavigateUp = {})
    }
}

@Preview
@Composable
fun ResetConfirmationDialogPreview() {
    ComboGeneratorTheme {
        ResetConfirmationDialog(onConfirm = {}, onDismiss = {})
    }
}
