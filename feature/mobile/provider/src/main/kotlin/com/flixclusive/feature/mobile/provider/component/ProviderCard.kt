package com.flixclusive.feature.mobile.provider.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flixclusive.core.theme.FlixclusiveTheme
import com.flixclusive.core.ui.mobile.util.getFeedbackOnLongPress
import com.flixclusive.gradle.entities.Author
import com.flixclusive.gradle.entities.Language
import com.flixclusive.gradle.entities.PluginData
import com.flixclusive.gradle.entities.PluginType
import com.flixclusive.gradle.entities.Status

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun ProviderCard(
    pluginData: PluginData,
    enabled: Boolean,
    isSearching: Boolean,
    displacementOffset: Float?,
    openSettings: () -> Unit,
    uninstallProvider: () -> Unit,
    onToggleProvider: () -> Unit,
) {
    val hapticFeedBack = getFeedbackOnLongPress()
    val clipboardManager = LocalClipboardManager.current

    val isBeingDragged = remember(displacementOffset) {
        displacementOffset != null
    }
    
    val isNotMaintenance = pluginData.status != Status.Maintenance

    val color = if (isBeingDragged && isNotMaintenance && !isSearching) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = Modifier
            .graphicsLayer { translationY = if (isSearching) 0F else displacementOffset ?: 0f }
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Card(
            enabled = isNotMaintenance,
            onClick = onToggleProvider,
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = color,
                contentColor = contentColorFor(backgroundColor = color)
            ),
            border = if (isBeingDragged && !isNotMaintenance)
                BorderStroke(
                    width = 2.dp,
                    color = contentColorFor(color)
                ) else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp)
                .combinedClickable(
                    onClick = onToggleProvider,
                    onLongClick = {
                        hapticFeedBack()

                        clipboardManager.setText(
                            AnnotatedString(
                                pluginData.repositoryUrl ?: pluginData.buildUrl
                                ?: return@combinedClickable
                            )
                        )
                    },
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 15.dp)
            ) {
                TopCardContent(
                    isSearching = isSearching,
                    pluginData = pluginData
                )

                Divider(thickness = 0.5.dp)

                BottomCardContent(
                    pluginData = pluginData,
                    enabled = enabled,
                    openSettings = openSettings,
                    unloadPlugin = uninstallProvider,
                    toggleUsage = onToggleProvider
                )
            }
        }
    }
}

@Preview
@Composable
private fun ProviderCardPreview() {
    val pluginData = PluginData(
        authors = listOf(Author("FLX")),
        repositoryUrl = null,
        buildUrl = null,
        changelog = null,
        changelogMedia = null,
        versionName = "1.0.0",
        versionCode = 10000,
        description = null,
        iconUrl = null,
        language = Language.Multiple,
        name = "123Movies",
        pluginType = PluginType.All,
        status = Status.Working
    )

    FlixclusiveTheme {
        Surface {
            ProviderCard(
                pluginData = pluginData,
                enabled = true,
                isSearching = false,
                displacementOffset = null,
                openSettings = { /*TODO*/ },
                uninstallProvider = { /*TODO*/ }) {

            }
        }
    }
}