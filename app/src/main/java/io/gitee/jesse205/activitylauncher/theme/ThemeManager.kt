package io.gitee.jesse205.activitylauncher.theme

import android.app.Activity
import io.gitee.jesse205.activitylauncher.R
import io.gitee.jesse205.activitylauncher.preferences.AppPreferences
import io.gitee.jesse205.activitylauncher.utils.isDeviceSettingsThemeSupported
import io.gitee.jesse205.activitylauncher.utils.isDeviceThemeSupported
import io.gitee.jesse205.activitylauncher.utils.isGingerbreadThemeNoBugs
import io.gitee.jesse205.activitylauncher.utils.isHoloDarkActionBarThemeSupported
import io.gitee.jesse205.activitylauncher.utils.isHoloThemeSupported
import io.gitee.jesse205.activitylauncher.utils.isMaterialThemeSupported
import io.gitee.jesse205.activitylauncher.utils.isNightModeCompat

object ThemeManager {
    const val THEME_DEVICE_DEFAULT_SETTINGS = "device_default_settings"
    const val THEME_DEVICE_DEFAULT = "device_default"
    const val THEME_MATERIAL = "material"
    const val THEME_HOLO = "holo"
    const val THEME_GINGERBREAD = "gingerbread"
    const val THEME_VARIANT_LIGHT = "light"
    const val THEME_VARIANT_DARK = "dark"
    const val THEME_VARIANT_DARK_ACTION_BAR = "dark_action_bar"
    val themes: List<AppTheme> = mutableListOf<AppTheme>().apply {
        if (isDeviceSettingsThemeSupported) {
            add(
                NormalTheme(
                    id = THEME_DEVICE_DEFAULT_SETTINGS,
                    style = android.R.style.Theme_DeviceDefault_Settings,
                    overlayStyle = R.style.ThemeOverlay_ActivityLauncher_DeviceDefault_Settings,
                    displayNames = listOf(
                        R.string.settings_theme_device_default,
                        R.string.settings_theme_settings,
                    )
                )
            )
        }
        if (isDeviceThemeSupported) {
            add(
                DayNightTheme(
                    id = THEME_DEVICE_DEFAULT,
                    light = NormalThemeVariant(
                        id = THEME_VARIANT_LIGHT,
                        style = android.R.style.Theme_DeviceDefault_Light,
                        overlayStyle = R.style.ThemeOverlay_ActivityLauncher_DeviceDefault_Light,
                    ),
                    lightDarkActionBar = NormalThemeVariant(
                        id = THEME_VARIANT_DARK_ACTION_BAR,
                        style = android.R.style.Theme_DeviceDefault_Light_DarkActionBar,
                        overlayStyle = R.style.ThemeOverlay_ActivityLauncher_DeviceDefault_Light_DarkActionBar,
                    ),
                    dark = NormalThemeVariant(
                        id = THEME_VARIANT_DARK,
                        style = android.R.style.Theme_DeviceDefault,
                        overlayStyle = R.style.ThemeOverlay_ActivityLauncher_DeviceDefault_Dark,
                    ),
                    displayNames = listOf(R.string.settings_theme_device_default)
                )
            )
        }
        if (isMaterialThemeSupported) {
            add(
                DayNightTheme(
                    id = THEME_MATERIAL,
                    light = NormalThemeVariant(
                        id = THEME_VARIANT_LIGHT,
                        style = android.R.style.Theme_Material_Light,
                        overlayStyle = R.style.ThemeOverlay_ActivityLauncher_Material_Light,
                    ),
                    lightDarkActionBar = NormalThemeVariant(
                        id = THEME_VARIANT_DARK_ACTION_BAR,
                        style = android.R.style.Theme_Material_Light_DarkActionBar,
                        overlayStyle = R.style.ThemeOverlay_ActivityLauncher_Material_Light_DarkActionBar,
                    ),
                    dark = NormalThemeVariant(
                        id = THEME_VARIANT_DARK,
                        style = android.R.style.Theme_Material,
                        overlayStyle = R.style.ThemeOverlay_ActivityLauncher_Material_Dark,
                    ),
                    displayNames = listOf(R.string.settings_theme_material)
                )
            )
        }
        @Suppress("DEPRECATION")
        if (isHoloThemeSupported) {
            add(
                DayNightTheme(
                    id = THEME_HOLO,
                    light = NormalThemeVariant(
                        id = THEME_VARIANT_LIGHT,
                        style = android.R.style.Theme_Holo_Light,
                        overlayStyle = R.style.ThemeOverlay_ActivityLauncher_Holo_Light,
                    ),
                    lightDarkActionBar = when {
                        isHoloDarkActionBarThemeSupported -> NormalThemeVariant(
                            id = THEME_VARIANT_DARK_ACTION_BAR,
                            style = android.R.style.Theme_Holo_Light_DarkActionBar,
                            overlayStyle = R.style.ThemeOverlay_ActivityLauncher_Holo_Light_DarkActionBar,
                        )

                        else -> null
                    },
                    dark = NormalThemeVariant(
                        id = THEME_VARIANT_DARK,
                        style = android.R.style.Theme_Holo,
                        overlayStyle = R.style.ThemeOverlay_ActivityLauncher_Holo_Dark,
                    ),
                    displayNames = listOf(R.string.settings_theme_holo)
                )
            )
        }
        if (isGingerbreadThemeNoBugs) {
            add(
                DayNightTheme(
                    id = THEME_GINGERBREAD,
                    light = NormalThemeVariant(
                        id = THEME_VARIANT_LIGHT,
                        style = android.R.style.Theme_Light,
                        overlayStyle = R.style.ThemeOverlay_ActivityLauncher_Gingerbread_Light,
                    ),
                    dark = NormalThemeVariant(
                        id = THEME_VARIANT_DARK,
                        style = android.R.style.Theme,
                        overlayStyle = R.style.ThemeOverlay_ActivityLauncher_Gingerbread_Dark,
                    ),
                    displayNames = listOf(R.string.settings_theme_gingerbread)
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

    fun getCurrentVariantId(activity: Activity, appTheme: AppTheme): String? {
        if (appTheme is DayNightTheme) {
            return when {
                activity.resources.configuration.isNightModeCompat && AppPreferences.darkMode == AppPreferences.THEME_DARK_MODE_SYSTEM -> THEME_VARIANT_DARK
                AppPreferences.darkMode == AppPreferences.THEME_DARK_MODE_ON -> THEME_VARIANT_DARK
                appTheme.lightDarkActionBar != null && AppPreferences.isDarkActionBar -> THEME_VARIANT_DARK_ACTION_BAR
                else -> THEME_VARIANT_LIGHT
            }
        }
        return null
    }

    fun setTheme(themeId: String) {
        if (isThemeCompatible(themeId)) {
            AppPreferences.themeId = themeId
        }
    }

    fun applyTheme(
        activity: Activity,
        appTheme: AppTheme = getCurrentTheme(),
        variantId: String? = getCurrentVariantId(activity, appTheme)
    ) {
        activity.apply {
            theme.applyStyle(R.style.ThemeReset_ActivityLauncher, true)
            when (appTheme) {
                is DayNightTheme -> applyDayNightThemeInternal(activity, appTheme, variantId)
                is NormalTheme -> applyNormalThemeInternal(activity, appTheme)
            }
        }
    }

    private fun applyDayNightThemeInternal(
        activity: Activity,
        appTheme: DayNightTheme,
        variantId: String? = getCurrentVariantId(activity, appTheme)
    ) {
        val themeVariant = when (variantId) {
            THEME_VARIANT_DARK -> appTheme.dark
            THEME_VARIANT_DARK_ACTION_BAR -> appTheme.lightDarkActionBar ?: appTheme.light
            else -> appTheme.light
        }
        activity.apply {
            setTheme(themeVariant.style)
            theme.applyStyle(themeVariant.overlayStyle, true)
        }
    }

    private fun applyNormalThemeInternal(activity: Activity, appTheme: NormalTheme) {
        activity.apply {
            setTheme(appTheme.style)
            theme.applyStyle(appTheme.overlayStyle, true)
        }
    }
}