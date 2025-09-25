package io.gitee.jesse205.activitylauncher.features.activitylist

import android.app.ActionBar
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import io.gitee.jesse205.activitylauncher.R
import io.gitee.jesse205.activitylauncher.core.BaseActivity
import io.gitee.jesse205.activitylauncher.model.LoadedActivityInfo
import io.gitee.jesse205.activitylauncher.utils.IntentCompat
import io.gitee.jesse205.activitylauncher.utils.isPermissionDenialException

class ActivityListActivity : BaseActivity<ActivityListActivityState>(), AdapterView.OnItemClickListener {
    override val stateClass = ActivityListActivityState::class.java
    private val runningLoadActivitiesTask: LoadActivitiesTask? = null

    private lateinit var adapter: ActivityListAdapter
    private val listView: android.widget.ListView by lazy { findViewById(android.R.id.list) }
    private val loadingLayout: ViewGroup by lazy { findViewById(R.id.loading_layout) }
    private val emptyTipLayout: ViewGroup by lazy { findViewById(R.id.empty_tip_layout) }
    private val emptyLayout: ViewGroup by lazy { findViewById(android.R.id.empty) }

    override fun onCreateState(): ActivityListActivityState {
        return ActivityListActivityState().apply {
            packageName = intent.extras?.getString(IntentCompat.EXTRA_PACKAGE_NAME)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setupActionBar()
        }

        adapter = ActivityListAdapter(this)

        if (state.packageName == null) {
            showAppNotInstalledToast()
            finish()
            return
        }
        runCatching {
            packageManager.getApplicationInfo(state.packageName!!, 0);
        }.onFailure {
            showAppNotInstalledToast()
            finish()
            return
        }

        state.loadActivitiesTask?.attachActivity(this)

        listView.apply {
            emptyView = emptyLayout
            adapter = this@ActivityListActivity.adapter
            onItemClickListener = this@ActivityListActivity
        }

        findViewById<TextView>(R.id.loading_text).apply {
            text = getString(R.string.label_getting_activities)
        }

        setActivities(state.activities)
        setLoadingActivities(state.isLoadingActivities)

        if (state.activities == null && !state.isLoadingActivitiesTaskRunning) {
            loadActivities()
        }
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private fun setupActionBar(): ActionBar? {
        return getActionBar()?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu_activity_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.getItemId() == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val freshItem = menu.findItem(R.id.menu_refresh)
        freshItem.isEnabled = !state.isLoadingActivitiesTaskRunning
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val activityInfo: LoadedActivityInfo? = adapter.getItem(position)
        if (activityInfo == null) {
            return
        }
        launchActivity(activityInfo.activityInfo)
    }

    private fun loadActivities() {
        LoadActivitiesTask(
            application = application,
            activity = this,
            state = state
        ).execute()
    }

    private fun showAppNotInstalledToast() {
        Toast.makeText(this, R.string.toast_app_not_installed, Toast.LENGTH_SHORT).show()
    }

    private fun showActivityNotFoundToast() {
        Toast.makeText(this, R.string.toast_activity_not_found, Toast.LENGTH_SHORT).show()
    }

    private fun showPermissionDeniedToast() {
        Toast.makeText(this, R.string.toast_permission_denied, Toast.LENGTH_SHORT).show()
    }

    private fun launchActivity(activityInfo: ActivityInfo) {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.setClassName(activityInfo.packageName, activityInfo.name)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        runCatching {
            startActivity(intent)
        }.onFailure {
            Log.w(TAG, "launchActivity: ", it)
            when (it) {
                is ActivityNotFoundException -> showActivityNotFoundToast()
                is SecurityException -> {
                    if (isPermissionDenialException(it)) {
                        showPermissionDeniedToast()
                    }
                }
            }
        }
    }

    fun setLoadingActivities(loading: Boolean) {
        val visibleWhenLoading = if (loading) View.VISIBLE else View.GONE
        val visibleWhenNotLoading = if (loading) View.GONE else View.VISIBLE
        loadingLayout.visibility = visibleWhenLoading
        emptyTipLayout.visibility = visibleWhenNotLoading
    }

    fun setActivities(apps: List<LoadedActivityInfo>?) {
        adapter.setActivities(apps ?: listOf())
    }

    companion object {
        private const val TAG = "ActivityListActivity"
    }
}
