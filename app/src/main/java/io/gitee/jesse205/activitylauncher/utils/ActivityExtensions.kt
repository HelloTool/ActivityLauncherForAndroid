package io.gitee.jesse205.activitylauncher.utils

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.preference.Preference
import android.preference.PreferenceActivity
import android.view.View
import android.view.WindowInsetsController
import androidx.annotation.RequiresApi

fun Activity.enableEdgeToEdge() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        window.apply {
            setDecorFitsSystemWindowsCompat(false)
            // 安卓 10 才引入手势导航，之前的版本没必要启用半透明
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                isNavigationBarContrastEnforced = true
                @Suppress("DEPRECATION")
                navigationBarColor = Color.TRANSPARENT
            }
        }
    }
}

@Suppress("DEPRECATION")
inline fun <reified T : Preference> PreferenceActivity.findPreferenceCompat(key: String): T? {
    return findPreference(key) as? T?
}

@RequiresApi(Build.VERSION_CODES.M)
fun Activity.setSystemBarsAppearance(isLightSystemBars: Boolean? = null, isLightNavigationBar: Boolean? = null) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.decorView.windowInsetsController?.apply {
            var mask = 0
            var appearance = 0
            if (isLightSystemBars != null) {
                mask = mask or WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                if (isLightSystemBars) {
                    appearance = appearance or WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                }
            }
            if (isLightNavigationBar != null) {
                mask = mask or WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                if (isLightNavigationBar) {
                    appearance = appearance or WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                }
            }

            setSystemBarsAppearance(appearance, mask)
        }
    } else {
        @Suppress("DEPRECATION")
        window.decorView.apply {
            var visibility = systemUiVisibility
            if (isLightSystemBars != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                visibility = if (isLightSystemBars) {
                    visibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    visibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
            }
            if (isLightNavigationBar != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                visibility = if (isLightNavigationBar) {
                    visibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                } else {
                    visibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
                }
            }
            systemUiVisibility = visibility
        }
    }
}

object ActivityCompat {
    const val WINDOW_HIERARCHY_TAG: String = "android:viewHierarchyState"
}