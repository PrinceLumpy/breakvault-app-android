package com.princelumpy.breakvault.ui.settings

import android.net.Uri
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
import com.princelumpy.breakvault.ui.theme.BreakVaultTheme

/**
 * The main, stateful screen composable that holds the ViewModel and state.
 */
@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            settingsViewModel.onSnackbarShown()
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

    SettingsScaffold(
        onNavigateUp = onNavigateUp,
        snackbarHostState = snackbarHostState,
        onExportClick = { exportDataLauncher.launch("combos_backup.json") },
        onImportClick = { importDataLauncher.launch(arrayOf("application/json")) },
        onResetClick = { settingsViewModel.onResetDatabaseClicked() }
    )

    if (uiState.showResetConfirmDialog) {
        ResetConfirmationDialog(
            onConfirm = { settingsViewModel.onResetDatabaseConfirm() },
            onDismiss = { settingsViewModel.onResetDatabaseDismiss() }
        )
    }
}

/**
 * A stateless scaffold that handles the overall layout for the Settings screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScaffold(
    onNavigateUp: () -> Unit,
    snackbarHostState: SnackbarHostState,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.common_back_button_description)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        SettingsContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            onExportClick = onExportClick,
            onImportClick = onImportClick,
            onResetClick = onResetClick
        )
    }
}

/**
 * The main, stateless content of the screen containing the settings buttons.
 */
@Composable
private fun SettingsContent(
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsButton(
            text = stringResource(id = R.string.settings_export_data_button),
            onClick = onExportClick
        )

        SettingsButton(
            text = stringResource(id = R.string.settings_import_data_button),
            onClick = onImportClick
        )

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = onResetClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text(stringResource(id = R.string.settings_reset_database_button))
        }
    }
}


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

//region Previews

@Preview(showBackground = true)
@Composable
private fun SettingsContentPreview() {
    BreakVaultTheme {
        SettingsContent(
            onExportClick = {},
            onImportClick = {},
            onResetClick = {}
        )
    }
}

@Preview
@Composable
private fun ResetConfirmationDialogPreview() {
    BreakVaultTheme {
        ResetConfirmationDialog(onConfirm = {}, onDismiss = {})
    }
}

//endregion
