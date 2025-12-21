@file:Suppress("SpellCheckingInspection", "ConvertToStringTemplate", "RemoveSingleExpressionStringTemplate")

package org.briarproject.briar.desktop.ui
// import com.zoffcc.applications.trifa_material.trifa_material.BuildConfig
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.zoffcc.applications.trifa_material.trifa_material.BuildConfig
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n

@Composable
fun AboutScreen() = Box {
    Column() {
        GeneralInfo()
    }
}

@Composable
private fun GeneralInfo()
{
    val lines = buildList {
        add(Entry(i18n("about.trifa_material_version"), BuildConfig.APP_VERSION))
        add(Entry(i18n("about.git_commit_branch"), BuildConfig.GIT_BRANCH))
        add(Entry(i18n("about.git_commit_hash"), BuildConfig.GIT_COMMIT_HASH))
        add(Entry(i18n("about.git_commit_date"), BuildConfig.GIT_COMMIT_DATE))
        add(Entry(i18n("about.git_commit_msg"), BuildConfig.GIT_COMMIT_MSG))
        add(Entry(i18n("about.compose_version"), BuildConfig.COMPOSE_VERSION))
        add(Entry(i18n("about.kotlin_compiler_used_version"), BuildConfig.KOTLIN_VERSION))
    }

    Text(
        text = "" + lines,
        modifier = Modifier.padding(10.dp)
    )
    println("xx: " + lines)
}

private data class Entry(
    val label: String,
    val value: String,
    val showCopy: Boolean = false,
)

// sizes of the two columns in the general tab
private val colSizesGeneral = listOf(0.3f, 0.7f)

@Composable
private fun AboutEntry(entry: Entry) =
    Row(
        Modifier
            .fillMaxWidth()
            // this is required for Divider between Boxes to have appropriate size
            .height(IntrinsicSize.Min)
            .semantics(mergeDescendants = true) {
                // manual text setting can be removed if Compose issue resolved
                // https://github.com/JetBrains/compose-jb/issues/2111
                text = buildAnnotatedString { append("${entry.label}: ${entry.value}") }
            }
    ) {
        Cell(colSizesGeneral[0], entry.label)
        VerticalDivider()
        Box(modifier = Modifier.weight(colSizesGeneral[1]).fillMaxHeight()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = CenterVertically
            ) {
                SelectionContainer {
                    Text(
                        text = entry.value,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                if (entry.showCopy)
                {
                    val clipboardManager = LocalClipboardManager.current
                    IconButton(
                        icon = Icons.Filled.ContentCopy,
                        contentDescription = i18n("ui.copy"),
                        onClick = {
                            clipboardManager.setText(AnnotatedString(entry.value))
                        }
                    )
                }
            }
        }
    }

@Composable
private fun Libraries()
{
    VerticallyScrollableArea { scrollState ->
        LazyColumn(
            modifier = Modifier.semantics {
                contentDescription = i18n("ui.about.list_dependencies")
            },
            state = scrollState
        ) {
            item {
                HorizontalDivider()
            }
            /*
            items(BuildData.ARTIFACTS) { artifact ->
                LibraryEntry(artifact)
                HorizontalDivider()
            }
            */
        }
    }
}

@Composable
fun show_report_bug_dialog(show_link_click: Boolean, link_str: String, setLinkVars: (Boolean, String) -> Unit)
{
    var show_link_click1 = show_link_click
    var link_str1 = link_str
    if (show_link_click1)
    {
        AlertDialog(onDismissRequest = { link_str1 = ""; show_link_click1 = false; setLinkVars(show_link_click1, link_str1) },
            title = { Text("Open this URL ?") },
            confirmButton = {
                Button(onClick = { }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { link_str1 = ""; show_link_click1 = false;setLinkVars(show_link_click1, link_str1) }) {
                    Text("No")
                }
            },
            text = { Text("This could be potentially dangerous!" + "\n\n" + link_str1) })
    }
}

// sizes of the four columns in the dependencies tab
val colSizesLibraries = listOf(0.3f, 0.3f, 0.15f, 0.25f)
/*
@Composable
private fun LibraryEntry(artifact: Artifact) =
    SelectionContainer {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Add tiny cells in between so that one can select a row, copy and paste
            // it somewhere and appear like "group:artifact:version license".
            Cell(colSizesLibraries[0], artifact.group)
            Cell(MIN_VALUE, ":")
            VerticalDivider()
            Cell(colSizesLibraries[1], artifact.artifact)
            Cell(MIN_VALUE, ":")
            VerticalDivider()
            Cell(colSizesLibraries[2], artifact.version)
            Cell(MIN_VALUE, "\t")
            VerticalDivider()
            Cell(colSizesLibraries[3], artifact.license)
        }
    }
*/

@Composable
private fun RowScope.Cell(size: Float, text: String) =
    Box(modifier = Modifier.weight(size).fillMaxHeight()) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth().padding(8.dp).align(CenterStart)
        )
    }
