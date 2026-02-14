package com.princelumpy.breakvault.ui.common

import AppStyleDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.princelumpy.breakvault.R

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

/**
 * A shared component for displaying and selecting tags in a scrollable card.
 * Used across Move editing, Battle Combo editing, and Combo Generator screens.
 *
 * @param T The type of tag (must have id and name properties)
 * @param allTags List of all available tags
 * @param selectedTags Set of selected tag identifiers
 * @param isLoading Whether tags are currently being loaded
 * @param emptyMessage Message to display when no tags are available
 * @param onTagSelected Callback when a tag is selected/deselected
 * @param getTagId Function to extract the ID from a tag
 * @param getTagName Function to extract the name from a tag
 * @param height Optional height for the card (defaults to SpacingExtraLarge * 8)
 * @param modifier Optional modifier for the card
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> TagSelectionCard(
    allTags: List<T>,
    selectedTags: Set<String>,
    isLoading: Boolean,
    emptyMessage: String,
    onTagSelected: (String) -> Unit,
    getTagId: (T) -> String,
    getTagName: (T) -> String,
    height: androidx.compose.ui.unit.Dp = AppStyleDefaults.SpacingExtraLarge * 8,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppStyleDefaults.SpacingMedium),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(AppStyleDefaults.SpacingExtraLarge)
                    )
                }

                allTags.isEmpty() -> {
                    Text(
                        emptyMessage,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            allTags.forEach { tag ->
                                val tagId = getTagId(tag)
                                val isSelected = selectedTags.contains(tagId)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onTagSelected(tagId) },
                                    label = { Text(getTagName(tag)) },
                                    leadingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                Icons.Filled.Done,
                                                stringResource(id = R.string.add_edit_move_selected_chip_description),
                                                Modifier.size(FilterChipDefaults.IconSize)
                                            )
                                        }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * A shared dialog for adding or editing a tag.
 * Used in Move Tag Management, Battle Tag Management, and Add/Edit screens.
 *
 * @param title Dialog title text
 * @param labelText Label for the input field
 * @param confirmButtonText Text for the confirm button (e.g., "Add" or "Save")
 * @param tagName Current value of the tag name input
 * @param characterLimit Maximum character limit for tag names (null for no limit)
 * @param isError Whether there's an error state
 * @param errorMessage Error message to display
 * @param onTagNameChange Callback when tag name changes
 * @param onConfirm Callback when confirm button is clicked
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
fun TagDialog(
    title: String,
    labelText: String,
    confirmButtonText: String,
    tagName: String,
    characterLimit: Int? = null,
    isError: Boolean,
    errorMessage: String?,
    onTagNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = tagName,
                onValueChange = { newValue ->
                    if (characterLimit == null || newValue.length <= characterLimit) {
                        onTagNameChange(newValue)
                    }
                },
                label = { Text(labelText) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (tagName.isNotBlank()) {
                            onConfirm()
                        }
                    }
                ),
                isError = isError,
                supportingText = {
                    if (isError) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = tagName.isNotBlank()
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.common_cancel))
            }
        }
    )
}
