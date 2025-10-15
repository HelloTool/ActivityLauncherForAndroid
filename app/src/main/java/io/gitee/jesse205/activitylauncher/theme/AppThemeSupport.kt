package io.gitee.jesse205.activitylauncher.theme

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import io.gitee.jesse205.activitylauncher.preferences.AppPreferences
import io.gitee.jesse205.activitylauncher.utils.ActivityCompat
import io.gitee.jesse205.activitylauncher.utils.ActivityListener
import io.gitee.jesse205.activitylauncher.utils.AndroidInternalR
import io.gitee.jesse205.activitylauncher.utils.DialogCompat
import io.gitee.jesse205.activitylauncher.utils.ScopedActivityListenerManager
import io.gitee.jesse205.activitylauncher.utils.WindowCompat
import io.gitee.jesse205.activitylauncher.utils.copyFieldsTo
import io.gitee.jesse205.activitylauncher.utils.isHighPerformanceDeviceByOSVersion
import io.gitee.jesse205.activitylauncher.utils.recreateCompat

object AppThemeSupport : ScopedActivityListenerManager<AppThemeSupport.AppThemeSupportActivityListener>() {
    private const val TAG = "AppThemeSupport"
    private const val APP_THEME_SUPPORT_TAG = "appThemeSupport"
    private const val THEME_ID_TAG = "themeId"


    override fun createActivityScopeListener(activity: Activity) = AppThemeSupportActivityListener(activity)

    /**
     * 转换或者丢弃 ActionBar 的 State，用于解决不同主题之间的 SavedState 可能不兼容从而引发的 ClassCastException
     */
    fun Bundle.transformOrDropActionBarState(refWindowState: Bundle) {
        val windowState = getBundle(ActivityCompat.WINDOW_HIERARCHY_TAG) ?: return

        @Suppress("DEPRECATION")
        val toolbarStates = windowState
            .getSparseParcelableArray<Parcelable>(WindowCompat.ACTION_BAR_TAG) ?: return

        @Suppress("DEPRECATION")
        val refToolbarState = refWindowState
            .getSparseParcelableArray<Parcelable>(WindowCompat.ACTION_BAR_TAG) ?: return

        val actionBarState = toolbarStates.get(AndroidInternalR.id.ACTION_BAR) ?: return
        val refActionBarState = refToolbarState.get(AndroidInternalR.id.ACTION_BAR) ?: return
        if (actionBarState.javaClass != refActionBarState.javaClass) {
            runCatching {
                // TODO: 使用 LSPosed/AndroidHiddenApiBypass 访问隐藏 API
                actionBarState.copyFieldsTo(refActionBarState, arrayOf("expandedMenuItemId", "isOverflowOpen"))
                toolbarStates.set(AndroidInternalR.id.ACTION_BAR, refActionBarState)
            }.onFailure {
                Log.w(TAG, "onActivityPreCreate: Failed to transform action bar state", it)
                windowState.remove(WindowCompat.ACTION_BAR_TAG)
            }
        }
    }

    fun Bundle.dropButtonPanelState() {
        val dialogState = getBundle(ActivityCompat.SAVED_DIALOGS_TAG) ?: return
        dialogState.keySet().forEach {
            if (it.startsWith(ActivityCompat.SAVED_DIALOG_KEY_PREFIX)) {
                val windowState =
                    dialogState.getBundle(it)?.getBundle(DialogCompat.DIALOG_HIERARCHY_TAG) ?: return@forEach

                @Suppress("DEPRECATION")
                val savedStates =
                    windowState.getSparseParcelableArray<Parcelable>(WindowCompat.VIEWS_TAG) ?: return@forEach
                savedStates.remove(AndroidInternalR.id.BUTTON_PANEL)
            }
        }
    }

    val Activity.appTheme get() = getActivityScopeListener(this).appTheme

    class AppThemeSupportActivityListener(val activity: Activity) : ActivityListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
        val appTheme: AppTheme = ThemeManager.getCurrentTheme()
        var atLeastOnceResume = false

        val isThemeOutdated: Boolean get() = appTheme.id != AppPreferences.themeId

        fun recreateIfThemeOutdated() {
            if (isThemeOutdated) {
                activity.recreateCompat()
            }
        }

        fun isThemeChanging(savedInstanceState: Bundle): Boolean {
            return savedInstanceState.getBundle(APP_THEME_SUPPORT_TAG)?.getString(THEME_ID_TAG)?.let {
                it != appTheme.id
            } ?: false
        }

        override fun onActivityPreCreate(activity: Activity, savedInstanceState: Bundle?) {
            activity.apply {
                ThemeManager.applyTheme(this, appTheme)
            }
        }

        override fun onActivityCreate(activity: Activity, savedInstanceState: Bundle?) {
            if (isHighPerformanceDeviceByOSVersion) {
                AppPreferences.registerChangeListener(this)
            }
        }

        override fun onActivityResume(activity: Activity) {
            if (!isHighPerformanceDeviceByOSVersion) {
                AppPreferences.registerChangeListener(this)
            }
            recreateIfThemeOutdated()
            atLeastOnceResume = true
        }

        override fun onActivityPause(activity: Activity) {
            if (!isHighPerformanceDeviceByOSVersion) {
                AppPreferences.unregisterListener(this)
            }
        }

        override fun onActivityDestroy(activity: Activity) {
            if (isHighPerformanceDeviceByOSVersion) {
                AppPreferences.unregisterListener(this)
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            val appThemeSupportState = Bundle().apply {
                putString(THEME_ID_TAG, appTheme.id)
            }
            outState.putBundle(APP_THEME_SUPPORT_TAG, appThemeSupportState)
        }

        override fun onActivityPreRestoreInstanceState(activity: Activity, savedInstanceState: Bundle) {
            if (isThemeChanging(savedInstanceState)) {
                val referenceBundle = activity.window.saveHierarchyState()!!
                savedInstanceState.transformOrDropActionBarState(referenceBundle)
                savedInstanceState.dropButtonPanelState()
            }
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
            if (key == AppPreferences.PREFERENCE_KEY_THEME) {
                // 为解决平行视界下 recreate 时窗口不附加到窗口管理器的问题，仅在至少一次恢复时 recreate
                if (atLeastOnceResume) {
                    recreateIfThemeOutdated()
                }
            }
        }
    }
}