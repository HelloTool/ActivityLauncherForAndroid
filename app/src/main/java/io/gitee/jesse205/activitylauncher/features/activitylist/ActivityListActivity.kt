package io.gitee.jesse205.activitylauncher.features.activitylist

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.view.Window
import android.widget.AdapterView
import android.widget.EditText
import android.widget.GridView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import io.gitee.jesse205.activitylauncher.R
import io.gitee.jesse205.activitylauncher.core.BaseActivity
import io.gitee.jesse205.activitylauncher.model.LoadedActivityInfo
import io.gitee.jesse205.activitylauncher.utils.CollapseActionViewMenuItemPatch
import io.gitee.jesse205.activitylauncher.utils.IntentCompat
import io.gitee.jesse205.activitylauncher.utils.isMenuSearchBarSupported
import io.gitee.jesse205.activitylauncher.utils.isNavigationGestureSupported
import io.gitee.jesse205.activitylauncher.utils.isPermissionDenialException
import io.gitee.jesse205.activitylauncher.utils.temporarilyClearFocus

class ActivityListActivity : BaseActivity<ActivityListActivityState>(), AdapterView.OnItemClickListener,
    ActivityListActivityState.ActivityListActivityStateListener {
    override val stateClass = ActivityListActivityState::class.java
    private lateinit var adapter: ActivityListAdapter
    private val gridView: GridView by lazy { findViewById(R.id.grid) }
    private val loadingLayout: ViewGroup by lazy { findViewById(R.id.loading_layout) }
    private val emptyTipLayout: ViewGroup by lazy { findViewById(R.id.empty_tip_layout) }
    private val emptyLayout: ViewGroup by lazy { findViewById(android.R.id.empty) }
    private var freshMenuItem: MenuItem? = null
    private var searchView: SearchView? = null

    override val enableEdgeToEdge: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    override fun onCreateState(): ActivityListActivityState {
        return ActivityListActivityState(
            _packageName = intent.extras?.getString(IntentCompat.EXTRA_PACKAGE_NAME) ?: run {
                Log.e(TAG, "packageName is null")
                ""
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isNavigationGestureSupported) {
            requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY)
        }

        setContentView(R.layout.activity_grid)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setupActionBar()
        } else {
            setupSearchLayout()
        }
        adapter = ActivityListAdapter(this)

        if (state.packageName.isBlank()) {
            showAppNotInstalledToast()
            finish()
            return
        }
        runCatching {
            packageManager.getApplicationInfo(state.packageName, 0)
        }.onFailure {
            showAppNotInstalledToast()
            finish()
            return
        }.onSuccess {
            setTitle(it.loadLabel(packageManager))
        }

        gridView.apply {
            emptyView = emptyLayout
            adapter = this@ActivityListActivity.adapter
            onItemClickListener = this@ActivityListActivity
        }

        findViewById<TextView>(R.id.loading_text).apply {
            text = getString(R.string.label_getting_activities)
        }

        state.bind(this, this)
        onActivitiesUpdate(state.activities)
        onActivitiesLoadingUpdate(state.isActivitiesLoading)
        if (!state.isActivitiesLoadingOrLoaded) {
            loadActivities()
        }
    }

    private fun setupSearchLayout() {
        // HONEYCOMB 以上采用 ActionBar 中的 SearchView
        val searchLayout = findViewById<ViewStub>(R.id.search_layout).inflate()
        val searchInput = searchLayout.findViewById<EditText>(R.id.search_input)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                adapter.filter.filter(s)
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private fun setupSearchBar() {
        searchView?.apply {
            queryHint = getString(R.string.hint_search_activities)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter.filter.filter(newText)
                    return true
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private fun setupActionBar() {
        getActionBar()?.apply {
            setDisplayHomeAsUpEnabled(true)
            if (!isMenuSearchBarSupported) {
                setDisplayShowTitleEnabled(resources.configuration.screenWidthDp >= 600)
                setDisplayShowCustomEnabled(true)
                searchView = SearchView(this@ActivityListActivity).apply {
                    isIconifiedByDefault = false
                }
                customView = searchView
                setupSearchBar()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        getMenuInflater().inflate(R.menu.menu_activity_list, menu)
        freshMenuItem = menu.findItem(R.id.menu_refresh)
        freshMenuItem!!.isEnabled = !state.isActivitiesLoading
        if (isMenuSearchBarSupported) {
            menu.findItem(R.id.menu_search).apply {
                isVisible = true
                // 修复启用返回按钮时候工具栏多出边距
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    actionBar?.let {
                        setOnActionExpandListener(CollapseActionViewMenuItemPatch(it))
                    }
                }
            }.also {
                searchView = it.actionView as SearchView
                setupSearchBar()
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            R.id.menu_refresh -> {
                loadActivities()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // 当窗口获取焦点后，会自动弹出软键盘，临时清除焦点以避免弹出输入法
            searchView?.temporarilyClearFocus()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.destroy()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val activityInfo: LoadedActivityInfo? = adapter.getItem(position)
        if (activityInfo == null) {
            return
        }
        launchActivity(activityInfo.activityInfo)
    }

    private fun loadActivities() {
        state.loadActivities(application)
    }

    private fun showAppNotInstalledToast() {
        Toast.makeText(this, R.string.toast_app_not_installed, Toast.LENGTH_SHORT).show()
    }

    private fun showNoActivityFoundToast() {
        Toast.makeText(this, R.string.toast_no_activity_found, Toast.LENGTH_SHORT).show()
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
            Log.w(TAG, "launchActivity failed: ", it)
            when (it) {
                is ActivityNotFoundException -> showNoActivityFoundToast()
                is SecurityException -> {
                    if (isPermissionDenialException(it)) {
                        showPermissionDeniedToast()
                    }
                }
            }
        }
    }

    override fun onActivitiesUpdate(activities: List<LoadedActivityInfo>?) {
        adapter.setActivities(activities ?: listOf())
    }

    override fun onActivitiesLoadingUpdate(isActivitiesLoading: Boolean) {
        val visibleWhenLoading = if (isActivitiesLoading) View.VISIBLE else View.GONE
        val visibleWhenNotLoading = if (isActivitiesLoading) View.GONE else View.VISIBLE
        loadingLayout.visibility = visibleWhenLoading
        emptyTipLayout.visibility = visibleWhenNotLoading
        freshMenuItem?.isEnabled = !isActivitiesLoading
    }

    override fun onPackageNameUpdate(packageName: String) {}

    companion object {
        private const val TAG = "ActivityListActivity"
    }
}
