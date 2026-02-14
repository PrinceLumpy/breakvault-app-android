package com.princelumpy.breakvault.ui.common

import AppStyleDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

/**
 * A shared list item card component for displaying items in list screens.
 * Supports edit and optional delete icons, with flexible content composition.
 *
 * @param onClick Callback when the card is clicked
 * @param onEditClick Callback when the edit icon is clicked
 * @param onDeleteClick Optional callback when the delete icon is clicked (null hides delete button)
 * @param modifier Optional modifier for the card
 * @param content Composable content to display in the card
 */
@Composable
fun ListItemCard(
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = AppStyleDefaults.SpacingSmall),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppStyleDefaults.SpacingLarge,
                    vertical = AppStyleDefaults.SpacingMedium
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                content()
            }

            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(id = R.string.common_edit),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            if (onDeleteClick != null) {
                Spacer(modifier = Modifier.width(AppStyleDefaults.SpacingSmall))
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(id = R.string.common_delete),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * A shared generic list component for displaying items with edit and delete actions.
 * Used in tag management screens for both Move and Battle tags.
 *
 * @param T The type of items in the list
 * @param items List of items to display
 * @param onItemClick Callback when an item card is clicked
 * @param onEditClick Callback when the edit icon is clicked
 * @param onDeleteClick Callback when the delete icon is clicked
 * @param getItemKey Function to extract a unique key from an item
 * @param getItemName Function to extract the display name from an item
 * @param modifier Optional modifier for the list
 */
@Composable
fun <T> GenericItemList(
    items: List<T>,
    onItemClick: (T) -> Unit,
    onEditClick: (T) -> Unit,
    onDeleteClick: (T) -> Unit,
    getItemKey: (T) -> String,
    getItemName: (T) -> String,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = AppStyleDefaults.SpacingLarge,
            vertical = AppStyleDefaults.SpacingLarge
        ),
        verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium)
    ) {
        items(
            items = items,
            key = { getItemKey(it) }
        ) { item ->
            ListItemCard(
                onClick = { onItemClick(item) },
                onEditClick = { onEditClick(item) },
                onDeleteClick = { onDeleteClick(item) }
            ) {
                Text(
                    text = getItemName(item),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingExtraLarge * 3))
        }
    }
}
