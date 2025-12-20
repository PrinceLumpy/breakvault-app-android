import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

object AppStyleDefaults {
    val SpacingSmall = 4.dp
    val SpacingMedium = 8.dp
    val SpacingLarge = 16.dp
    val SpacingExtraLarge = 24.dp
    val LazyListPadding = PaddingValues(horizontal = SpacingLarge, vertical = SpacingMedium)
}

object GoalInputDefaults {
    const val MAX_TITLE_LENGTH = 100
    val DESCRIPTION_FIELD_HEIGHT = 120.dp
}
