package com.xiaeer.lock.utils

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ComponentInfo
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import com.drake.tooltip.toast
import com.xiaeer.lock.App
import com.xiaeer.lock.receiver.MyDeviceAdminReceiver
import com.xiaeer.lock.ui.activity.MainActivity
import com.xiaeer.lock.ui.activity.SplashActivity
import java.util.Collections

class DeviceMethod {

    private val devicePolicyManager: DevicePolicyManager =
        App.application.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val componentName: ComponentName =
        ComponentName(App.application, MyDeviceAdminReceiver::class.java)

    val isAdmin: Boolean
        get() = devicePolicyManager.isAdminActive(componentName)

    // 激活程序
    fun onActivate(launcher: ActivityResultLauncher<Intent>) {
        // 判断是否激活  如果没有就启动激活设备
        if (!isAdmin) {
            val intent = Intent(
                DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN
            )
            intent.putExtra(
                DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                componentName
            )
            launcher.launch(intent)
        } else {
            toast("已激活此设备管理员")
        }
    }

    /**
     * 移除程序 如果不移除程序 APP无法被卸载
     */
    fun onRemoveActivate() {
        devicePolicyManager.removeActiveAdmin(componentName)
    }

    /**
     * 立刻锁屏
     */
    fun lockNow() {
        if (devicePolicyManager.isAdminActive(componentName)) {
            devicePolicyManager.lockNow()
        } else {
            toast("请先激活此设备管理员")
        }
    }

    fun isSelfEnable(): Boolean {
        return isComponentEnabled(
            App.application.packageManager, componentName.packageName,
            SplashActivity::class.java.name
        )
    }

    private fun isComponentEnabled(pm: PackageManager, pkgName: String, clsName: String): Boolean {
        val componentName = ComponentName(pkgName, clsName)
        return when (pm.getComponentEnabledSetting(componentName)) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> false
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT ->
                try {
                    val packageInfo = pm.getPackageInfo(
                        pkgName, PackageManager.GET_ACTIVITIES
                                or PackageManager.GET_RECEIVERS
                                or PackageManager.GET_SERVICES
                                or PackageManager.GET_PROVIDERS
                                or PackageManager.GET_DISABLED_COMPONENTS
                    )
                    val components = mutableListOf<ComponentInfo>()
                    if (packageInfo.activities != null) Collections.addAll(
                        components,
                        *packageInfo.activities
                    )
                    if (packageInfo.services != null) Collections.addAll(
                        components,
                        *packageInfo.services
                    )
                    if (packageInfo.providers != null) Collections.addAll(
                        components,
                        *packageInfo.providers
                    )
                    for (componentInfo in components) {
                        if (componentInfo.name == clsName) {
                            return componentInfo.isEnabled
                        }
                    }

                    false
                } catch (e: PackageManager.NameNotFoundException) {
                    false
                }
            else -> try {
                val packageInfo = pm.getPackageInfo(
                    pkgName, PackageManager.GET_ACTIVITIES
                            or PackageManager.GET_RECEIVERS
                            or PackageManager.GET_SERVICES
                            or PackageManager.GET_PROVIDERS
                            or PackageManager.GET_DISABLED_COMPONENTS
                )
                val components = mutableListOf<ComponentInfo>()
                if (packageInfo.activities != null) Collections.addAll(
                    components,
                    *packageInfo.activities
                )
                if (packageInfo.services != null) Collections.addAll(
                    components,
                    *packageInfo.services
                )
                if (packageInfo.providers != null) Collections.addAll(
                    components,
                    *packageInfo.providers
                )
                for (componentInfo in components) {
                    if (componentInfo.name == clsName) {
                        return componentInfo.isEnabled
                    }
                }
                false
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    companion object {
        @Volatile private var mDeviceMethod: DeviceMethod? = null

        fun getInstance(): DeviceMethod {
            return mDeviceMethod ?: synchronized(this) {
                mDeviceMethod ?: DeviceMethod().also { mDeviceMethod = it }
            }
        }
    }
}