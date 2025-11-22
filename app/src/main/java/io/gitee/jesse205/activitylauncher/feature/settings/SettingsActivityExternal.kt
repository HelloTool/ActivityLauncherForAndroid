package io.gitee.jesse205.activitylauncher.feature.settings

import android.app.Activity
import android.os.Bundle

/**
 * 外部启动设置界面，为了避免直接启动 [SettingsActivity]
 *
 * - [Google Android架构Fragment注入本地安全绕过漏洞](https://www.cnvd.org.cn/flaw/show/CNVD-2013-15052)
 */
class SettingsActivityExternal : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SettingsActivity.launch(this)
        finish()
    }
}