package io.gitee.jesse205.activitylauncher.app

import android.app.Application
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import io.gitee.jesse205.activitylauncher.R
import io.gitee.jesse205.activitylauncher.theme.AppTheme
import io.gitee.jesse205.activitylauncher.theme.ThemeRegistry
import io.gitee.jesse205.activitylauncher.utils.disableDeathOnFileUriExposure
import io.gitee.jesse205.activitylauncher.utils.isDeviceSettingsThemeSupported
import io.gitee.jesse205.activitylauncher.utils.isDeviceThemeSupported
import io.gitee.jesse205.activitylauncher.utils.isGingerbreadThemeNoBugs
import io.gitee.jesse205.activitylauncher.utils.isHoloThemeSupported
import io.gitee.jesse205.activitylauncher.utils.isMaterialThemeSupported


class ActivityLauncherApp : Application() {
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        setupStrictMode()
        registerThemes()
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

    @Suppress("DEPRECATION")
    private fun registerThemes() {
        if (isDeviceSettingsThemeSupported) {
            ThemeRegistry.registerTheme(
                AppTheme(
                    group = "device_default",
                    id = "device_settings_light",
                    style = android.R.style.Theme_DeviceDefault_Settings,
                    overlayStyle = R.style.ThemeOverlay_ActivityLauncher_DeviceDefault_Settings,
                    displayNames = intArrayOf(
                        R.string.settings_theme_device_settings,
                    )
                )
            )
        }
        if (isDeviceThemeSupported) {
            ThemeRegistry.registerTheme(
                AppTheme(
                    group = "device_default",
                    id = "device_default_light",
                    style = android.R.style.Theme_DeviceDefault_Light,
                    overlayStyle = R.style.ThemeOverlay_ActivityLauncher_DeviceDefault_Light,
                    displayNames = intArrayOf(
                        R.string.settings_theme_device_default,
                        R.string.settings_theme_light
                    )
                )
            )
            ThemeRegistry.registerTheme(
                AppTheme(
                    group = "device_default",
                    id = "device_default_dark",
                    style = android.R.style.Theme_DeviceDefault,
                    overlayStyle = R.style.ThemeOverlay_ActivityLauncher_DeviceDefault_Dark,
                    displayNames = intArrayOf(
                        R.string.settings_theme_device_default,
                        R.string.settings_theme_dark
                    )
                )
            )
        }

        if (isMaterialThemeSupported) {
            ThemeRegistry.registerTheme(
                AppTheme(
                    group = "material",
                    id = "material_light",
                    style = android.R.style.Theme_Material_Light,
                    overlayStyle = R.style.ThemeOverlay_ActivityLauncher_Material_Light,
                    displayNames = intArrayOf(
                        R.string.settings_theme_material,
                        R.string.settings_theme_light
                    )
                )
            )
            ThemeRegistry.registerTheme(
                AppTheme(
                    group = "material",
                    id = "material_light_dark_action_bar",
                    style = android.R.style.Theme_Material_Light_DarkActionBar,
                    overlayStyle = R.style.ThemeOverlay_ActivityLauncher_Material_Light_DarkActionBar,
                    displayNames = intArrayOf(
                        R.string.settings_theme_material,
                        R.string.settings_theme_light,
                        R.string.settings_theme_dark_action_bar
                    )
                )
            )
            ThemeRegistry.registerTheme(
                AppTheme(
                    group = "material",
                    id = "material_dark",
                    style = android.R.style.Theme_Material,
                    overlayStyle = R.style.ThemeOverlay_ActivityLauncher_Material_Dark,
                    displayNames = intArrayOf(
                        R.string.settings_theme_material,
                        R.string.settings_theme_dark
                    )
                )
            )
        }
        if (isHoloThemeSupported) {
            ThemeRegistry.registerTheme(
                AppTheme(
                    group = "holo",
                    id = "holo_light",
                    style = android.R.style.Theme_Holo_Light,
                    overlayStyle = R.style.ThemeOverlay_ActivityLauncher_Holo_Light,
                    displayNames = intArrayOf(
                        R.string.settings_theme_holo,
                        R.string.settings_theme_light
                    )
                )
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                ThemeRegistry.registerTheme(
                    AppTheme(
                        group = "holo",
                        id = "holo_light_dark_action_bar",
                        style = android.R.style.Theme_Holo_Light_DarkActionBar,
                        overlayStyle = R.style.ThemeOverlay_ActivityLauncher_Holo_Light_DarkActionBar,
                        displayNames = intArrayOf(
                            R.string.settings_theme_holo,
                            R.string.settings_theme_light,
                            R.string.settings_theme_dark_action_bar
                        )
                    )
                )
            }
            ThemeRegistry.registerTheme(
                AppTheme(
                    group = "holo",
                    id = "holo_dark",
                    style = android.R.style.Theme_Holo,
                    overlayStyle = R.style.ThemeOverlay_ActivityLauncher_Holo_Dark,
                    displayNames = intArrayOf(
                        R.string.settings_theme_holo,
                        R.string.settings_theme_dark
                    )
                )
            )

        }
        if (isGingerbreadThemeNoBugs) {
            ThemeRegistry.registerTheme(
                AppTheme(
                    group = "holo",
                    id = "gingerbread_light",
                    style = android.R.style.Theme_Light,
                    overlayStyle = R.style.ThemeOverlay_ActivityLauncher_Gingerbread_Light,
                    displayNames = intArrayOf(
                        R.string.settings_theme_gingerbread,
                        R.string.settings_theme_light
                    )
                )
            )
            ThemeRegistry.registerTheme(
                AppTheme(
                    group = "holo",
                    id = "gingerbread_dark",
                    style = android.R.style.Theme,
                    overlayStyle = R.style.ThemeOverlay_ActivityLauncher_Gingerbread_Dark,
                    displayNames = intArrayOf(
                        R.string.settings_theme_gingerbread,
                        R.string.settings_theme_dark
                    )
                )
            )
        }


    }

    companion object {
        lateinit var INSTANCE: ActivityLauncherApp
        private const val TAG = "App"
    }
}