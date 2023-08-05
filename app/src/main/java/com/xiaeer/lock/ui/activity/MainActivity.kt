package com.xiaeer.lock.ui.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.drake.statusbar.immersive
import com.drake.tooltip.toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.xiaeer.lock.R
import com.xiaeer.lock.databinding.ActivityMainBinding
import com.xiaeer.lock.service.LockService
import com.xiaeer.lock.utils.DeviceMethod
import com.xiaeer.lock.viewmodel.LockTaskViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    val lockTaskViewModel: LockTaskViewModel by viewModels()

    private var requestDeviceAdmin = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                toast("已激活此设备管理员")
                Timber.d("已激活此设备管理员，开启服务")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(Intent(this, LockService::class.java))
                } else {
                    startService(Intent(this, LockService::class.java))
                }
            } else {
                toast("取消激活此设备管理员，退出应用")
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersive(darkMode = true)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        if (!DeviceMethod.getInstance().isAdmin) {
            DeviceMethod.getInstance().onActivate(requestDeviceAdmin)
        } else {
            Timber.d("已激活此设备管理员，开启服务")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(this, LockService::class.java))
            } else {
                startService(Intent(this, LockService::class.java))
            }
        }
    }
}