package com.flixclusive.feature.mobile.player.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

@Composable
internal fun PlayerPipReceiver(
    action: String,
    onReceive: (intent: Intent?) -> Unit,
) {
    val context = LocalContext.current
    val currentOnReceive by rememberUpdatedState(newValue = onReceive)

    DisposableEffect(context, action) {
        val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent,
            ) {
                currentOnReceive(intent)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                /* receiver = */ broadcastReceiver,
                /* filter = */ IntentFilter(action),
                /* flags = */ Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            context.registerReceiver(
                broadcastReceiver, IntentFilter(action)
            )
        }

        onDispose {
            context.unregisterReceiver(broadcastReceiver)
        }
    }
}