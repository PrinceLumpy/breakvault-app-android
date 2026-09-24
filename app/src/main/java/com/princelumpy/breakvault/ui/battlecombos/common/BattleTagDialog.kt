// Modified by Claude Code - 2026-09-24
package com.princelumpy.breakvault.ui.battlecombos.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.princelumpy.breakvault.common.Constants.BATTLE_TAG_CHARACTER_LIMIT
import com.princelumpy.breakvault.data.local.entity.TagColor
import com.princelumpy.breakvault.ui.common.TagDialog

/**
 * Name + color popup used everywhere a battle tag is created or edited
 * (manage battle tags screen and the inline "new tag" on add/edit battle combo).
 */
@Composable
fun BattleTagDialog(
    title: String,
    labelText: String,
    confirmButtonText: String,
    tagName: String,
    tagColor: TagColor?,
    isError: Boolean,
    errorMessage: String?,
    onTagNameChange: (String) -> Unit,
    onTagColorChange: (TagColor?) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    TagDialog(
        title = title,
        labelText = labelText,
        confirmButtonText = confirmButtonText,
        tagName = tagName,
        characterLimit = BATTLE_TAG_CHARACTER_LIMIT,
        isError = isError,
        errorMessage = errorMessage,
        onTagNameChange = onTagNameChange,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        extraContent = {
            TagColorPicker(selectedColor = tagColor, onColorSelected = onTagColorChange)
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun BattleTagDialogPreview() {
    MaterialTheme {
        BattleTagDialog(
            title = "Add New Tag",
            labelText = "Tag Name",
            confirmButtonText = "Add",
            tagName = "Power",
            tagColor = TagColor.RED,
            isError = false,
            errorMessage = null,
            onTagNameChange = {},
            onTagColorChange = {},
            onConfirm = {},
            onDismiss = {}
        )
    }
}
