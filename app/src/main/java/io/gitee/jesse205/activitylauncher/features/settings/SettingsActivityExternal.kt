package io.gitee.jesse205.activitylauncher.features.settings

import android.app.Activity
import android.os.Bundle

/**
 * 外部启动设置界面，为了避免直接启动 [SettingsActivity]
 *
 * @see <a href="http://securityintelligence.com/new-vulnerability-android-framework-fragment-injection">http://securityintelligence.com/new-vulnerability-android-framework-fragment-injection</a>
 */
class SettingsActivityExternal : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SettingsActivity.launch(this)
        finish()
    }
}