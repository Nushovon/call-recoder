package com.myapp.callrecorder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

/**
 * Fires whenever the phone's call state changes.
 * We use this purely to know WHEN to start/stop the recording service
 * and to capture the number involved - no audio handling happens here.
 */
class CallReceiver : BroadcastReceiver() {

    companion object {
        private var lastOutgoingNumber: String? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {

            // Captures the dialed number for outgoing calls
            Intent.ACTION_NEW_OUTGOING_CALL -> {
                lastOutgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
            }

            TelephonyManager.ACTION_PHONE_STATE_CHANGED -> {
                val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

                when (state) {
                    // Call is active (either just answered, or outgoing call connected)
                    TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                        val number = incomingNumber ?: lastOutgoingNumber ?: "Unknown"
                        val direction = if (incomingNumber != null) "INCOMING" else "OUTGOING"

                        val serviceIntent = Intent(context, RecordingService::class.java).apply {
                            action = RecordingService.ACTION_START
                            putExtra(RecordingService.EXTRA_NUMBER, number)
                            putExtra(RecordingService.EXTRA_DIRECTION, direction)
                        }
                        context.startForegroundService(serviceIntent)
                    }

                    // Call ended or was never answered
                    TelephonyManager.EXTRA_STATE_IDLE -> {
                        val serviceIntent = Intent(context, RecordingService::class.java).apply {
                            action = RecordingService.ACTION_STOP
                        }
                        context.startService(serviceIntent)
                        lastOutgoingNumber = null
                    }
                }
            }
        }
    }
}
