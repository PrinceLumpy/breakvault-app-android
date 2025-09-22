package com.princelumpy.breakvault.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.princelumpy.breakvault.data.transfer.AppDataExport
import com.princelumpy.breakvault.ui.theme.ComboGeneratorTheme
import com.princelumpy.breakvault.viewmodel.FakeMoveViewModel
import com.princelumpy.breakvault.viewmodel.IMoveViewModel
import com.princelumpy.breakvault.viewmodel.MoveViewModel
import com.princelumpy.breakvault.R
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    moveViewModel: IMoveViewModel = viewModel<MoveViewModel>()
) {
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val exportDataLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri: Uri? ->
            uri?.let {
                scope.launch {
                    try {
                        val appData = moveViewModel.getAppDataForExport()
                        if (appData != null) {
                            val jsonString =
                                Json.encodeToString(appData)
                            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                                outputStream.write(jsonString.toByteArray())
                            }
                            snackbarHostState.showSnackbar(context.getString(R.string.settings_export_success_snackbar, uri.toString()))
                            Log.i("SettingsScreen", "Data exported to $uri")
                        } else {
                            snackbarHostState.showSnackbar(context.getString(R.string.settings_export_error_retrieving_data_snackbar))
                        }
                    } catch (e: Exception) {
                        Log.e("SettingsScreen", "Error exporting data", e)
                        snackbarHostState.showSnackbar(context.getString(R.string.settings_action_failed_snackbar, context.getString(R.string.settings_export_action_label), e.message ?: "Unknown error"))
                    }
                }
            }
        }
    )

    val importDataLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                scope.launch {
                    try {
                        val jsonString =
                            context.contentResolver.openInputStream(it)?.use { inputStream ->
                                BufferedReader(InputStreamReader(inputStream)).readText()
                            }
                        if (jsonString != null) {
                            val appData =
                                Json.decodeFromString<AppDataExport>(jsonString)
                            val success = moveViewModel.importAppData(appData)
                            if (success) {
                                snackbarHostState.showSnackbar(context.getString(R.string.settings_import_success_snackbar))
                                Log.i("SettingsScreen", "Data imported from $uri")
                            } else {
                                snackbarHostState.showSnackbar(context.getString(R.string.settings_import_failed_logs_snackbar))
                            }
                        } else {
                            snackbarHostState.showSnackbar(context.getString(R.string.settings_import_error_reading_file_snackbar))
                        }
                    } catch (e: Exception) {
                        Log.e("SettingsScreen", "Error importing data", e)
                        snackbarHostState.showSnackbar(context.getString(R.string.settings_action_failed_snackbar, context.getString(R.string.settings_import_action_label), e.message ?: "Unknown error"))
                    }
                }
            }
        }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.settings_title)) })
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
                onClick = { showResetConfirmDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(id = R.string.settings_reset_database_button))
            }
        }
    }

    if (showResetConfirmDialog) {
        ResetConfirmationDialog(
            onConfirm = {
                Log.i("SettingsScreen", "Database reset confirmed")
                scope.launch {
                    moveViewModel.resetDatabase()
                    snackbarHostState.showSnackbar(context.getString(R.string.settings_database_reset_success_snackbar))
                }
                showResetConfirmDialog = false
            },
            onDismiss = {
                showResetConfirmDialog = false
            }
        )
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

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    ComboGeneratorTheme {
        // Note: SAF launchers won't work in Preview, so interaction will be limited.
        SettingsScreen(moveViewModel = FakeMoveViewModel())
    }
}

@Preview
@Composable
fun ResetConfirmationDialogPreview() {
    ComboGeneratorTheme {
        ResetConfirmationDialog(onConfirm = {}, onDismiss = {})
    }
}
