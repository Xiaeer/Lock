package com.xiaeer.lock.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import androidx.core.app.NotificationCompat
import com.drake.tooltip.toast
import com.xiaeer.lock.R
import com.xiaeer.lock.data.room.AppDatabase
import com.xiaeer.lock.data.room.model.LockTask
import com.xiaeer.lock.receiver.ScreenReceiver
import com.xiaeer.lock.ui.activity.SplashActivity
import com.xiaeer.lock.utils.DeviceMethod
import timber.log.Timber
import java.util.Date

class LockService : Service() {

    private lateinit var builder: NotificationCompat.Builder

    private var isServiceEnd = false
    private var screenReceiver: ScreenReceiver? = null

    var isActuallyEnable: Boolean = false

    private lateinit var serviceThread: ServiceThread

    private var lockTasks: List<LockTask> = ArrayList()

    inner class ServiceThread internal constructor() : Thread() {
        init {
            // 初始化数据
            isActuallyEnable = DeviceMethod.getInstance().isSelfEnable()
        }

        override fun run() {
            while (!isServiceEnd) {
                if (lockTasks.isNotEmpty()) {
                    loopTasks()
                }
                // 五秒刷新一次
                try {
                    sleep(5000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }

        private fun loopTasks() {
            Timber.d(Date().toString() + ":循环查看任务, " + lockTasks.size)
            for (lockTask in lockTasks) {
                Timber.d(lockTask.toString())
                if (!lockTask.enable) {
                    continue
                }
                val startTime = lockTask.startTime ?: continue
                val endTime = lockTask.endTime ?: continue
                val c = Calendar.getInstance()
                c.time = startTime
                val startTimeNum = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
                c.time = endTime
                val endTimeNum = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
                c.time = Date()
                val curTimeNum = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
                if (startTimeNum <= endTimeNum) {
                    Timber.d("同天时间段")
                    if (curTimeNum in startTimeNum..endTimeNum) {
                        processLockScreen()
                        if (isActuallyEnable) {
                            hideMySelfHandler?.sendEmptyMessage(HIDE_SELF)
                        }
                        return
                    } else {
                        if (!isActuallyEnable) {
                            hideMySelfHandler?.sendEmptyMessage(ENABLE_SELF)
                        }
                        processUnLockScreen()
                    }
                } else {
                    Timber.d("跨天时间段")
                    if (curTimeNum <= endTimeNum || curTimeNum >= startTimeNum) {
                        processLockScreen()
                        if (isActuallyEnable) {
                            hideMySelfHandler?.sendEmptyMessage(HIDE_SELF)
                        }
                        return
                    } else {
                        if (!isActuallyEnable) {
                            hideMySelfHandler?.sendEmptyMessage(ENABLE_SELF)
                        }
                        processUnLockScreen()
                    }
                }
            }
        }

        private fun processLockScreen() {
            // 如果屏幕还没锁
            if (!ScreenReceiver.isLockScreen) {
                Timber.d("lock screen")
                ScreenReceiver.isLockScreen = true
                ScreenReceiver.lockNow()
            }
        }

        private fun processUnLockScreen() {
            ScreenReceiver.isLockScreen = false
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        builder = NotificationCompat.Builder(
            applicationContext,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_lock_black_24dp)
            .setContentTitle(getString(R.string.notification_keep_running_title))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        createNotificationChannel()

        startForeground(NOTIFICATION_ID, builder.build());

        // 注册广播
        registerBroadCast()

        hideMySelfHandler = object : Handler(
            Looper.getMainLooper()
        ) {
            private fun showMe() {
                if (isActuallyEnable) {
                    return
                }
                toast(R.string.calm_down_end_text)
                val componentName = ComponentName(
                    application, SplashActivity::class.java
                )
                packageManager.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                isActuallyEnable = true
            }

            private fun hideMe() {
                if (!isActuallyEnable) {
                    return
                }
                val componentName = ComponentName(
                    application, SplashActivity::class.java
                )
                packageManager.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                isActuallyEnable = false
            }

            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    ENABLE_SELF -> showMe()
                    HIDE_SELF -> hideMe()
                }
            }
        }

        AppDatabase.getInstance(this).lockTaskDao().getAllTasksLive().observeForever { value ->
            Timber.d("数据更新 从 " + lockTasks.size + " 到 " + value.size)
            // 如果删掉了一个任务，必须先设置为false，否则当遍历不到被删掉的任务时，屏幕保持锁定状态
            ScreenReceiver.isLockScreen = false
            lockTasks = value
        }

        serviceThread = ServiceThread()
        serviceThread.start()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.channel_name_keep_running)
            val description = getString(R.string.channel_description_keep_running)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                CHANNEL_ID,
                name,
                importance
            )
            channel.description = description
            channel.enableVibration(false)
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 动态注册广播
     */
    private fun registerBroadCast() {
        // 屏幕开启/关闭状态监听
        val intentFilterScreen = IntentFilter(Intent.ACTION_SCREEN_ON)
        intentFilterScreen.addAction(Intent.ACTION_SCREEN_OFF)
        intentFilterScreen.addAction(Intent.ACTION_USER_PRESENT)
        screenReceiver = ScreenReceiver()
        registerReceiver(screenReceiver, intentFilterScreen)
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceEnd = true
        Timber.d("服务结束！")
        unregisterReceiver(screenReceiver)
    }

    companion object {
        private const val CHANNEL_ID = "channel_id1"
        private const val NOTIFICATION_ID = 0x1001

        var hideMySelfHandler: Handler? = null
            private set

        const val ENABLE_SELF = 0x100
        const val HIDE_SELF = 0x101
    }
}