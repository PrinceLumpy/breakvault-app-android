// Modified by Claude Code - 2026-09-25
package com.princelumpy.breakvault.data.local.entity

import kotlinx.serialization.Serializable

/**
 * Fixed palette for move and battle tag colors. Declaration order is the top-to-bottom order of the
 * color strip on a battle combo card.
 *
 * Entries are persisted by name (Room TEXT column and export JSON), so never rename or remove
 * one without migrating stored values. Reordering is safe.
 */
@Serializable
enum class TagColor(val argb: Long) {
    RED(0xFFE53935),
    ORANGE(0xFFFB8C00),
    YELLOW(0xFFFDD835),
    GREEN(0xFF43A047),
    TEAL(0xFF00897B),
    BLUE(0xFF1E88E5),
    PURPLE(0xFF8E24AA),
    PINK(0xFFD81B60)
}
