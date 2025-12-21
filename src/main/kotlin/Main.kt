@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@file:Suppress("LocalVariableName", "FunctionName", "ConvertToStringTemplate", "SpellCheckingInspection", "UnusedReceiverParameter", "LiftReturnOrAssignment", "CascadeIf", "SENSELESS_COMPARISON", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "UNUSED_ANONYMOUS_PARAMETER", "REDUNDANT_ELSE_IN_WHEN", "ReplaceSizeCheckWithIsNotEmpty", "ReplaceRangeToWithRangeUntil", "ReplaceGetOrSet", "SimplifyBooleanWithConstants")

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import com.zoffcc.applications.trifa.CustomSemaphore
import kotlinx.coroutines.DelicateCoroutinesApi
import org.briarproject.briar.desktop.ui.AboutScreen

val CONTACTITEM_HEIGHT = 50.dp
val GROUPITEM_HEIGHT = 50.dp
val GROUP_PEER_HEIGHT = 33.dp
val SETTINGS_HEADER_SIZE = 56.dp
val CONTACT_COLUMN_WIDTH = 230.dp
const val CONTACT_COLUMN_CONTACTNAME_LEN_THRESHOLD = 13
const val PUSHURL_SHOW_LEN_THRESHOLD = 60
val GROUPS_COLUMN_WIDTH = 190.dp
val GROUPS_COLLAPSED_COLUMN_WIDTH = 50.dp
const val GROUPS_COLUMN_GROUPNAME_LEN_THRESHOLD = 13
val GROUP_PEER_COLUMN_WIDTH = 165.dp
val GROUP_COLLAPSED_PEER_COLUMN_WIDTH = 45.dp
const val GROUP_PEER_COLUMN_PEERNAME_LEN_THRESHOLD = 12
val MESSAGE_INPUT_LINE_HEIGHT = 58.dp
const val IMAGE_PREVIEW_SIZE = 70f
const val AVATAR_SIZE = 40f
const val MAX_AVATAR_SIZE = 70f
val SPACE_AFTER_LAST_MESSAGE = 2.dp
val SPACE_BEFORE_FIRST_MESSAGE = 10.dp
const val LAST_MSG_SCROLL_TO_SCROLL_OFFSET = 10000
val MESSAGE_BOX_BOTTOM_PADDING = 4.dp
const val MSG_TEXT_FONT_SIZE_MIXED = 14.0f
const val MSG_TEXT_FONT_SIZE_EMOJI_ONLY = 55.0f
const val MAX_EMOJI_POP_SEARCH_LEN = 20
const val MAX_EMOJI_POP_RESULT = 15
const val MAX_ONE_ON_ONE_MESSAGES_TO_SHOW = 20000
const val MAX_GROUP_MESSAGES_TO_SHOW = 20000
const val BG_COLOR_RELAY_CONTACT_ITEM = 0x448ABEB9
const val BG_COLOR_OWN_RELAY_CONTACT_ITEM = 0x44FFFFB9
var global_semaphore_contactlist_ui = CustomSemaphore(1)
var global_semaphore_grouppeerlist_ui = CustomSemaphore(1)
var global_semaphore_grouplist_ui = CustomSemaphore(1)

@OptIn(DelicateCoroutinesApi::class)
var DefaultFont: FontFamily? = null

@OptIn(DelicateCoroutinesApi::class)
fun main(args: Array<String>) = application(exitProcessOnExit = true) {
    MainAppStart()
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun MainAppStart()
{
    // ----------- main app screen -----------
    // ----------- main app screen -----------
    // ----------- main app screen -----------
    AboutScreen()
    // ----------- main app screen -----------
    // ----------- main app screen -----------
    // ----------- main app screen -----------
}

