/*
 * Briar Desktop
 * Copyright (C) 2021-2023 The Briar Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.briarproject.briar.desktop.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.Badge
import androidx.compose.material.BadgedBox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.briarproject.briar.desktop.navigation.SidebarButtonState.None
import org.briarproject.briar.desktop.navigation.SidebarButtonState.UnreadMessages
import org.briarproject.briar.desktop.navigation.SidebarButtonState.Warning
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n

val SIDEBAR_WIDTH = 56.dp

@Composable
fun BriarSidebar(
) {
    @Composable
    fun BriarSidebarButton(
        messageCount: Int = 0,
    ) = BriarSidebarButton(
        selected = false,
        onClick = {},
        icon = Icons.Filled.Add,
        contentDescription = "",
        sideBarButtonState = if (messageCount == 0) None else UnreadMessages(messageCount),
    )

    Column(
        modifier = Modifier
            .width(SIDEBAR_WIDTH).fillMaxHeight()
            .padding(vertical = 4.dp)
            .selectableGroup(),
        verticalArrangement = spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // profile button
        Spacer(Modifier.height(4.dp))
        /*
        BriarSidebarButton(UiMode.CONTACTS, messageCount.privateMessages)
        if (configuration.shouldEnablePrivateGroups())
            BriarSidebarButton(UiMode.GROUPS, messageCount.privateGroupMessages)
        if (configuration.shouldEnableForums()) BriarSidebarButton(UiMode.FORUMS, messageCount.forumPosts)
        if (configuration.shouldEnableBlogs()) BriarSidebarButton(UiMode.BLOGS, messageCount.blogPosts)

        Spacer(Modifier.weight(1f))

        if (configuration.shouldEnableTransportSettings()) BriarSidebarButton(UiMode.TRANSPORTS)
        BriarSidebarButton(
            selected = uiMode == UiMode.MAILBOX,
            onClick = { setUiMode(UiMode.MAILBOX) },
            icon = UiMode.MAILBOX.icon,
            contentDescription = i18n(UiMode.MAILBOX.contentDescriptionKey),
            sideBarButtonState = if (hasMailboxProblem) Warning else None,
        )
        BriarSidebarButton(UiMode.SETTINGS)
        BriarSidebarButton(UiMode.ABOUT)
        */
    }
}

sealed class SidebarButtonState {
    object None : SidebarButtonState()
    class UnreadMessages(val messageCount: Int) : SidebarButtonState()
    object Warning : SidebarButtonState()
}

@Composable
fun BriarSidebarButton(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    sideBarButtonState: SidebarButtonState,
) = BadgedBox(
    badge = {
        if (sideBarButtonState is UnreadMessages && sideBarButtonState.messageCount > 0) {
            Badge(
                modifier = Modifier.offset((-12).dp, 12.dp),
                backgroundColor = MaterialTheme.colors.secondary,
            )
        } else if (sideBarButtonState is Warning) {
            Icon(
                Icons.Default.Error,
                i18n("mailbox.status.problem"),
                modifier = Modifier.offset((-12).dp, 12.dp).size(16.dp),
                tint = MaterialTheme.colors.error
            )
        }
    },
) {
    val tint = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
    IconButton(
        content = {},
        onClick = onClick
    )
}