@file:Suppress("unused", "FunctionName", "SpellCheckingInspection")

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Compact floating hover action bar for chat messages (1-on-1 and NGC group chats).
 *
 * HINT: design goals
 *  - much less vertical height than the old bar
 *  - sits in the LOWER part of the bubble, on the OPPOSITE side of the date/time stamp
 *  - still a floating overlay: it must NOT change the size/layout of the message bubble
 *
 * HINT: why not "IconButton"?
 * Material's IconButton applies "minimumInteractiveComponentSize()", which floors the
 * interactive area at 48dp and therefore overrides the requested Modifier.size(28.dp).
 * That floor is what made the old bar ~50dp tall. Here we use an explicitly sized
 * clickable Box instead, so the height is exactly what we ask for.
 */

/** Button footprint. Kept small on purpose; the whole bar is only ~20dp tall at ui_scale 1.0. */
private const val ACTION_BUTTON_SIZE = 18f
private const val ACTION_ICON_SIZE = 12f
private const val ACTION_EMOJI_SIZE = 11f
private const val ACTION_SEPARATOR_HEIGHT = 12f

/** Vertical padding of the bar container. Total height = ACTION_BUTTON_SIZE + 2 * this. */
private val BAR_PADDING_VERTICAL = 1.dp
private val BAR_PADDING_HORIZONTAL = 4.dp

/**
 * Distance of the bar from the edge of the message list view.
 *
 * HINT: the bar lives OUTSIDE the message bubble, in the full-width message row.
 * Anchoring it to the bubble itself was wrong: a short bubble is narrower than the
 * bar, so the bar got horizontally clipped and lost items.
 */
val MESSAGE_ACTION_BAR_EDGE_PADDING = 16.dp

/** Small vertical inset so the bar does not sit flush on the bottom of the row. */
val MESSAGE_ACTION_BAR_INSET_VERTICAL = 2.dp

/**
 * Alignment of the bar inside the full-width message row.
 *
 * Own messages have their bubble on the right, so the bar goes to the LEFT edge of the
 * message list view; incoming messages have their bubble on the left, so the bar goes to
 * the RIGHT edge. Vertically it is always bottom aligned, level with the bottom of the
 * bubble, and it never overlaps the message text.
 */
fun messageActionBarAlignment(isMyMessage: Boolean): Alignment =
    if (isMyMessage) Alignment.BottomStart else Alignment.BottomEnd

/**
 * One compact, exactly sized action button.
 *
 * Uses Box + clickable instead of IconButton to avoid the 48dp minimum touch target.
 */
@Composable
private fun ActionBarItem(
    ui_scale: Float,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size((ACTION_BUTTON_SIZE * ui_scale).dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * The floating action bar.
 *
 * @param onDismiss called on right click, so the caller can hide the bar until the next hover
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun MessageActionBar(
    ui_scale: Float,
    isMyMessage: Boolean,
    onEmojiSelected: (String) -> Unit,
    onCopySelected: () -> Unit,
    onReplySelected: () -> Unit,
    onDeleteSelected: () -> Unit,
    onDismiss: () -> Unit,
) {
    Surface(
        elevation = 2.dp,
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colors.surface,
        border = BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.08f)),
        modifier = Modifier
            .pointerInput(Unit) {
                // Right-click anywhere on the bar container hides it
                detectTapGestures(
                    matcher = PointerMatcher.mouse(PointerButton.Secondary),
                    onTap = { onDismiss() }
                )
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                horizontal = BAR_PADDING_HORIZONTAL,
                vertical = BAR_PADDING_VERTICAL
            )
        ) {
            // --- EMOJI REACTIONS SECTION ---
            val emojis = listOf("👍", "👎", "❤️")
            emojis.forEach { emoji ->
                ActionBarItem(ui_scale = ui_scale, onClick = { onEmojiSelected(emoji) }) {
                    Text(
                        text = emoji,
                        fontSize = (ACTION_EMOJI_SIZE * ui_scale).sp
                    )
                }
            }

            // Subtle vertical separator line between reactions and actions
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .width(1.dp)
                    .height((ACTION_SEPARATOR_HEIGHT * ui_scale).dp)
                    .background(MaterialTheme.colors.onSurface.copy(alpha = 0.12f))
            )

            // --- COPY ACTION BUTTON ---
            TooltipArea(
                tooltip = { ActionTooltip(text = "Copy", ui_scale = ui_scale) },
                tooltipPlacement = TooltipPlacement.CursorPoint(
                    alignment = Alignment.BottomCenter,
                    offset = DpOffset(0.dp, 8.dp)
                ),
                delayMillis = 400
            ) {
                ActionBarItem(
                    ui_scale = ui_scale,
                    onClick = { onCopySelected() }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.size((ACTION_ICON_SIZE * ui_scale).dp)
                    )
                }
            }

            // --- REPLY ACTION BUTTON ---
            TooltipArea(
                tooltip = { ActionTooltip(text = "Reply", ui_scale = ui_scale) },
                tooltipPlacement = TooltipPlacement.CursorPoint(
                    alignment = Alignment.BottomCenter,
                    offset = DpOffset(0.dp, 8.dp)
                ),
                delayMillis = 400
            ) {
                ActionBarItem(
                    ui_scale = ui_scale,
                    onClick = { onReplySelected() }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Reply,
                        contentDescription = "Reply",
                        tint = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size((ACTION_ICON_SIZE * ui_scale).dp)
                            .graphicsLayer(
                                scaleX = if (isMyMessage) 1f else -1f
                            )
                    )
                }
            }

            // --- DELETE ACTION BUTTON ---
            TooltipArea(
                tooltip = { ActionTooltip(text = "Delete", ui_scale = ui_scale) },
                tooltipPlacement = TooltipPlacement.CursorPoint(
                    alignment = Alignment.BottomCenter,
                    offset = DpOffset(0.dp, 8.dp)
                ),
                delayMillis = 400
            ) {
                ActionBarItem(
                    ui_scale = ui_scale,
                    onClick = { onDeleteSelected() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colors.error.copy(alpha = 0.8f),
                        modifier = Modifier.size((ACTION_ICON_SIZE * ui_scale).dp)
                    )
                }
            }
        }
    }
}
