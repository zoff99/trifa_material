@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@file:Suppress("LocalVariableName", "FunctionName", "ConvertToStringTemplate", "SpellCheckingInspection", "UnusedReceiverParameter", "LiftReturnOrAssignment", "CascadeIf", "SENSELESS_COMPARISON", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "UNUSED_ANONYMOUS_PARAMETER", "REDUNDANT_ELSE_IN_WHEN", "ReplaceSizeCheckWithIsNotEmpty", "ReplaceRangeToWithRangeUntil", "ReplaceGetOrSet", "SimplifyBooleanWithConstants")

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import kotlinx.coroutines.DelicateCoroutinesApi
import org.briarproject.briar.desktop.ui.AboutScreen

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
    Window(onCloseRequest = { },
        title = "test",
        focusable = true
    ) {
        AboutScreen()
    }
    // ----------- main app screen -----------
    // ----------- main app screen -----------
    // ----------- main app screen -----------
}

