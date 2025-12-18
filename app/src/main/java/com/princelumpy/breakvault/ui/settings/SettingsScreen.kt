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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.ui.theme.ComboGeneratorTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Listen for one-time events from the ViewModel
    LaunchedEffect(Unit) {
        settingsViewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
            }
        }
    }

    val exportDataLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri: Uri? ->
            // Just notify the ViewModel. The logic is handled there.
            uri?.let { settingsViewModel.exportData(it) }
        }
    )

    val importDataLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            // Just notify the ViewModel.
            uri?.let { settingsViewModel.importData(it) }
        }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                // Just notify the ViewModel.
                settingsViewModel.resetDatabase()
                showResetConfirmDialog = false
            },
            onDismiss = {
                showResetConfirmDialog = false
            }
        )
    }
}

// Private helper composables remain unchanged as they are pure UI
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
        // Your preview can be simplified as it doesn't need a real ViewModel
        // You may need a Fake ViewModel implementation if you want to preview UI states
        // For now, this will render the initial layout.
        SettingsScreen(navController = rememberNavController())
    }
}

@Preview
@Composable
fun ResetConfirmationDialogPreview() {
    ComboGeneratorTheme {
        ResetConfirmationDialog(onConfirm = {}, onDismiss = {})
    }
}
