package com.flixclusive.presentation.mobile.common.composables.film

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.flixclusive.R
import com.flixclusive.common.UiText
import com.flixclusive.domain.model.VideoDataDialogState
import com.flixclusive.presentation.mobile.common.composables.GradientCircularProgressIndicator

@Composable
fun VideoPlayerDialog(
    videoDataDialogState: VideoDataDialogState,
    onConsumeDialog: () -> Unit,
) {
    LaunchedEffect(key1 = videoDataDialogState) {
        if (videoDataDialogState is VideoDataDialogState.Success) {
            onConsumeDialog()
        }
    }

    Dialog(
        onDismissRequest = onConsumeDialog
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 300.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Box(
                modifier = Modifier.padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = videoDataDialogState !is VideoDataDialogState.Error && videoDataDialogState !is VideoDataDialogState.Unavailable,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier.matchParentSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(30.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            GradientCircularProgressIndicator()
                        }

                        Text(
                            text = videoDataDialogState.message.asString(),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                AnimatedVisibility(
                    visible = videoDataDialogState is VideoDataDialogState.Error || videoDataDialogState is VideoDataDialogState.Unavailable,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier.matchParentSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(30.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.round_error_outline_24),
                            contentDescription = "Error icon",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .size(80.dp)
                                .padding(bottom = 15.dp)
                        )

                        Text(
                            text = videoDataDialogState.message.asString(),
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun VideoPlayerDialogPreview() {
    VideoPlayerDialog(videoDataDialogState = VideoDataDialogState.Error(UiText.StringValue("An unknown error occurred while obtaining episode data from api"))) {

    }
}