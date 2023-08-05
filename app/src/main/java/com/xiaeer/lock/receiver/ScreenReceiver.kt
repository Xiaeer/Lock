package com.xiaeer.lock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.xiaeer.lock.utils.DeviceMethod
import timber.log.Timber

class ScreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("%s:%s", intent.action, isLockScreen)
        if (intent.action == null) {
            return
        }
        when (intent.action) {
            Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> if (isLockScreen) {
                Timber.d("屏幕开启了")
                lockNow()
            }
        }
    }

    companion object {
        var isLockScreen = false

        fun lockNow() {
            Timber.d("lock_now")
            DeviceMethod.getInstance().lockNow()
        }
    }
}