package io.gitee.jesse205.activitylauncher.utils

import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import androidx.annotation.RequiresApi


@Suppress("DEPRECATION")
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
fun Window.setDecorFitsSystemWindowsCompat(fitsSystemWindows: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        return
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        setDecorFitsSystemWindows(fitsSystemWindows)
    } else {
        decorView.apply {
            systemUiVisibility = if (fitsSystemWindows) {
                systemUiVisibility and (
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        ).inv()
            } else {
                systemUiVisibility or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.M)
fun Window.setSystemBarsAppearance(isLightStatusBar: Boolean? = null, isLightNavigationBar: Boolean? = null) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        decorView.windowInsetsController?.apply {
            var mask = 0
            var appearance = 0
            if (isLightStatusBar != null) {
                mask = mask or WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                if (isLightStatusBar) {
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
        decorView.apply {
            var visibility = systemUiVisibility
            if (isLightStatusBar != null) {
                visibility = if (isLightStatusBar) {
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

object WindowCompat {
    const val VIEWS_TAG: String = "android:views"
    const val ACTION_BAR_TAG: String = "android:ActionBar"
}