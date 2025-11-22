package io.gitee.jesse205.activitylauncher.app

import android.app.Application
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import io.gitee.jesse205.activitylauncher.util.disableDeathOnFileUriExposure


class ActivityLauncherApp : Application() {
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        setupStrictMode()
    }

    private fun setupStrictMode() {
        val policy = VmPolicy.Builder().let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                it.permitUnsafeIntentLaunch()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                it.permitNonSdkApiUsage()
            }
            it.build()
        }
        StrictMode.setVmPolicy(policy)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            disableDeathOnFileUriExposure()
        }
    }


    companion object {
        lateinit var INSTANCE: ActivityLauncherApp
        private const val TAG = "App"
    }
}