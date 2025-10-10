package io.gitee.jesse205.activitylauncher.theme

import android.app.Activity
import android.os.Build
import io.gitee.jesse205.activitylauncher.R
import io.gitee.jesse205.activitylauncher.preferences.AppPreferences
import io.gitee.jesse205.activitylauncher.utils.isDeviceSettingsThemeSupported
import io.gitee.jesse205.activitylauncher.utils.isDeviceThemeSupported
import io.gitee.jesse205.activitylauncher.utils.isGingerbreadThemeNoBugs
import io.gitee.jesse205.activitylauncher.utils.isHoloThemeSupported
import io.gitee.jesse205.activitylauncher.utils.isMaterialThemeSupported

object ThemeManager {
    private const val PREF_THEME_KEY = "app_theme"
    val themes: List<AppTheme> = mutableListOf<AppTheme>().apply {
        if (isDeviceSettingsThemeSupported) {
            add(
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
            addAll(
                arrayListOf(
                    AppTheme(
                        group = "device_default",
                        id = "device_default_light",
                        style = android.R.style.Theme_DeviceDefault_Light,
                        overlayStyle = R.style.ThemeOverlay_ActivityLauncher_DeviceDefault_Light,
                        displayNames = intArrayOf(
                            R.string.settings_theme_device_default,
                            R.string.settings_theme_light
                        )
                    ),
                    AppTheme(
                        group = "device_default",
                        id = "device_default_light_dark_action_bar",
                        style = android.R.style.Theme_DeviceDefault_Light_DarkActionBar,
                        overlayStyle = R.style.ThemeOverlay_ActivityLauncher_DeviceDefault_Light_DarkActionBar,
                        displayNames = intArrayOf(
                            R.string.settings_theme_device_default,
                            R.string.settings_theme_light,
                            R.string.settings_theme_dark_action_bar
                        )
                    ),
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
            )

        }

        if (isMaterialThemeSupported) {
            addAll(
                arrayListOf(
                    AppTheme(
                        group = "material",
                        id = "material_light",
                        style = android.R.style.Theme_Material_Light,
                        overlayStyle = R.style.ThemeOverlay_ActivityLauncher_Material_Light,
                        displayNames = intArrayOf(
                            R.string.settings_theme_material,
                            R.string.settings_theme_light
                        )
                    ),
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
                    ),
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
            )
        }
        if (isHoloThemeSupported) {
            add(
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
                add(
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
            add(
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
            addAll(
                arrayListOf(
                    AppTheme(
                        group = "holo",
                        id = "gingerbread_light",
                        style = android.R.style.Theme_Light,
                        overlayStyle = R.style.ThemeOverlay_ActivityLauncher_Gingerbread_Light,
                        displayNames = intArrayOf(
                            R.string.settings_theme_gingerbread,
                            R.string.settings_theme_light
                        )
                    ),
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
            )
        }
    }

    val defaultTheme = themes.first()

    fun getThemeById(id: String): AppTheme? {
        return themes.find { it.id == id }
    }


    fun isThemeCompatible(id: String): Boolean {
        return getThemeById(id) != null
    }

    fun getCurrentTheme(): AppTheme {
        return AppPreferences.themeId?.let { getThemeById(it) }
            ?: defaultTheme
    }

    fun setTheme(themeId: String) {
        if (isThemeCompatible(themeId)) {
            AppPreferences.themeId = themeId
        }
    }

    fun applyTheme(activity: Activity, appTheme: AppTheme = getCurrentTheme()) {
        activity.apply {
            theme.applyStyle(R.style.ThemeReset_ActivityLauncher, true)
            setTheme(appTheme.style)
            theme.applyStyle(appTheme.overlayStyle, true)
        }
    }
}