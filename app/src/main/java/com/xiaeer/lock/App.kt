package com.xiaeer.lock

import android.app.Application
import android.content.pm.ApplicationInfo
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import kotlin.properties.Delegates

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        application = this
        isDebuggable = 0 != (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE)

        if (isDebuggable) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }
    }

    companion object {
        lateinit var application: App

        var isDebuggable by Delegates.notNull<Boolean>()
    }

    private class CrashReportingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {

        }
    }
}