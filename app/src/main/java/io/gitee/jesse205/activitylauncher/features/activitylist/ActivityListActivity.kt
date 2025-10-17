package io.gitee.jesse205.activitylauncher.features.activitylist

import android.content.Context
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
import android.widget.AdapterView
import android.widget.EditText
import android.widget.GridView
import android.widget.SearchView
import android.widget.TextView
import androidx.annotation.RequiresApi
import io.gitee.jesse205.activitylauncher.R
import io.gitee.jesse205.activitylauncher.app.BaseActivity
import io.gitee.jesse205.activitylauncher.model.LoadedActivityInfo
import io.gitee.jesse205.activitylauncher.utils.IntentCompat
import io.gitee.jesse205.activitylauncher.utils.errorMessageResId
import io.gitee.jesse205.activitylauncher.utils.isActionBarSupported
import io.gitee.jesse205.activitylauncher.utils.isMenuSearchBarSupported
import io.gitee.jesse205.activitylauncher.utils.isNavigationGestureSupported
import io.gitee.jesse205.activitylauncher.utils.isSupportEdgeToEdge
import io.gitee.jesse205.activitylauncher.utils.parentsDoNotClipChildrenAndPadding
import io.gitee.jesse205.activitylauncher.utils.patches.CollapseActionViewMenuItemPatch
import io.gitee.jesse205.activitylauncher.utils.showToast
import io.gitee.jesse205.activitylauncher.utils.temporarilyClearFocus

class ActivityListActivity : BaseActivity<ActivityListActivityState>(), AdapterView.OnItemClickListener,
    ActivityListActivityState.ActivityListActivityStateListener {
    override val stateClass = ActivityListActivityState::class.java
    private val adapter: ActivityListAdapter by lazy { ActivityListAdapter(this) }

    private val rootLayout: ViewGroup by lazy { findViewById(R.id.root_layout) }
    private val gridContainer: ViewGroup by lazy { rootLayout.findViewById(R.id.grid_container) }
    private val gridView: GridView by lazy { gridContainer.findViewById(R.id.grid) }
    private val emptyLayout: ViewGroup by lazy { gridContainer.findViewById(android.R.id.empty) }
    private val emptyText: TextView by lazy { emptyLayout.findViewById(R.id.empty_text) }
    private val loadingLayout: ViewGroup by lazy { rootLayout.findViewById(R.id.loading_layout) }
    private val loadingText: TextView by lazy { loadingLayout.findViewById(R.id.loading_text) }

    private var freshMenuItem: MenuItem? = null
    private var searchView: SearchView? = null

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

        setContentView(R.layout.activity_grid)

        val isActionBarInitialized = if (isActionBarSupported) {
            setupActionBar()
        } else {
            false
        }
        if (!isActionBarInitialized) {
            setupSearchLayout()
        }

        if (state.packageName.isBlank()) {
            showToast(R.string.toast_app_not_installed)
            finish()
            return
        }

        runCatching {
            packageManager.getApplicationInfo(state.packageName, 0)
        }.onFailure {
            showToast(R.string.toast_app_not_installed)
            finish()
            return
        }.onSuccess {
            setTitle(it.loadLabel(packageManager))
        }

        gridView.apply {
            emptyView = emptyLayout
            adapter = this@ActivityListActivity.adapter
            onItemClickListener = this@ActivityListActivity
            if (isNavigationGestureSupported && theme.isSupportEdgeToEdge) {
                parentsDoNotClipChildrenAndPadding(rootLayout)
            }
        }

        loadingText.apply {
            text = getString(R.string.label_getting_activities)
        }

        emptyText.apply {
            text = getString(R.string.label_empty_activities)
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
    private fun setupActionBar(): Boolean {
        val actionBar = getActionBar()?.apply {
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
        return actionBar != null
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

    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.menu_refresh -> loadActivities()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
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
        val activityInfo: LoadedActivityInfo = adapter.getItem(position)
        launchActivity(activityInfo.activityInfo)
    }

    private fun loadActivities() {
        state.loadActivities(application)
    }

    private fun launchActivity(activityInfo: ActivityInfo) {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.setClassName(activityInfo.packageName, activityInfo.name)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching {
            startActivity(intent)
        }.onFailure {
            Log.w(TAG, "launchActivity failed: ", it)
            showToast(it.errorMessageResId ?: R.string.error_unknown)
        }
    }

    override fun onActivitiesUpdate(activities: List<LoadedActivityInfo>?) {
        adapter.setActivities(activities ?: listOf())
    }

    override fun onActivitiesLoadingUpdate(isActivitiesLoading: Boolean) {
        val visibleWhenLoading = if (isActivitiesLoading) View.VISIBLE else View.GONE
        val visibleWhenNotLoading = if (isActivitiesLoading) View.GONE else View.VISIBLE
        loadingLayout.visibility = visibleWhenLoading
        gridContainer.visibility = visibleWhenNotLoading
        freshMenuItem?.isEnabled = !isActivitiesLoading
    }

    override fun onPackageNameUpdate(packageName: String) {}

    companion object {
        private const val TAG = "ActivityListActivity"
        fun launch(context: Context, packageName: String) {
            val intent = Intent(context, ActivityListActivity::class.java)
            intent.putExtra(IntentCompat.EXTRA_PACKAGE_NAME, packageName)
            context.startActivity(intent)
        }
    }
}
