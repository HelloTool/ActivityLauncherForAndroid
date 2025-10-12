@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package io.gitee.jesse205.activitylauncher.features.settings

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.view.MenuItem
import androidx.annotation.RequiresApi
import io.gitee.jesse205.activitylauncher.R
import io.gitee.jesse205.activitylauncher.app.BasePreferenceActivity
import io.gitee.jesse205.activitylauncher.preferences.AppPreferences
import io.gitee.jesse205.activitylauncher.theme.ThemeManager
import io.gitee.jesse205.activitylauncher.utils.findPreferenceCompat

class SettingsActivity : BasePreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setupActionBar()
        }
        addPreferencesFromResource(R.xml.settings)
        setupThemePreference()
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private fun setupActionBar() {
        getActionBar()?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 老版本中默认不调用 onBackPressed
        when (item.itemId) {
            item.itemId -> onBackPressed()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun setupThemePreference() {
        findPreferenceCompat<ListPreference>(AppPreferences.PREFERENCE_KEY_THEME)?.apply {

            entries = ThemeManager.themes.map { it.getDisplayName(this@SettingsActivity) }.toTypedArray()
            entryValues = ThemeManager.themes.map { it.id }.toTypedArray()
            val currentTheme = ThemeManager.getCurrentTheme()
            value = currentTheme.id
            summary = currentTheme.getDisplayName(this@SettingsActivity)
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                newValue as String
                summary == ThemeManager.getThemeById(newValue)?.getDisplayName(this@SettingsActivity)
                ThemeManager.setTheme(newValue)
                true
            }
        }

    }

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java)
            context.startActivity(intent)
        }
    }
}