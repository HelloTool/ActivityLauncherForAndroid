package io.gitee.jesse205.activitylauncher.features.applist

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.view.Window
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ListView
import android.widget.SearchView
import android.widget.TextView
import androidx.annotation.RequiresApi
import io.gitee.jesse205.activitylauncher.R
import io.gitee.jesse205.activitylauncher.core.BaseActivity
import io.gitee.jesse205.activitylauncher.features.activitylist.ActivityListActivity
import io.gitee.jesse205.activitylauncher.utils.AppProvisionType
import io.gitee.jesse205.activitylauncher.utils.AppSortCategory
import io.gitee.jesse205.activitylauncher.utils.CollapseActionViewMenuItemPatch
import io.gitee.jesse205.activitylauncher.utils.IntentCompat
import io.gitee.jesse205.activitylauncher.utils.isMenuSearchBarSupported
import io.gitee.jesse205.activitylauncher.utils.isNavigationGestureSupported
import io.gitee.jesse205.activitylauncher.utils.setDecorFitsSystemWindowsCompat
import io.gitee.jesse205.activitylauncher.utils.tabs.TabControllerFactory
import io.gitee.jesse205.activitylauncher.utils.temporarilyClearFocus

class MainActivity : BaseActivity<MainActivityState>(), AdapterView.OnItemClickListener {

    override val stateClass = MainActivityState::class.java
    private lateinit var adapter: AppListAdapter
    private val listView: ListView by lazy { findViewById(android.R.id.list) }
    private val loadingLayout: ViewGroup by lazy { findViewById(R.id.loading_layout) }
    private val emptyTipLayout: ViewGroup by lazy { findViewById(R.id.empty_tip_layout) }
    private val emptyLayout: ViewGroup by lazy { findViewById(android.R.id.empty) }
    private var searchView: SearchView? = null
    private var freshMenuItem: MenuItem? = null
    private var sortInstallTimeMenuItem: MenuItem? = null
    private var sortUpdateTimeMenuItem: MenuItem? = null
    private var sortNameMenuItem: MenuItem? = null

    override fun onCreateState(): MainActivityState {
        return MainActivityState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
        }

        if (isNavigationGestureSupported) {
            requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY)
        }

        setContentView(R.layout.activity_list)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setupActionBar()
        }
        setupTabs()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            setupSearchLayout()
        }
        state.loadAppsTask?.attachActivity(this)
        adapter = AppListAdapter(this@MainActivity)

        listView.apply {
            emptyView = emptyLayout
            adapter = this@MainActivity.adapter
            onItemClickListener = this@MainActivity
        }
        findViewById<TextView>(R.id.loading_text).apply {
            text = getString(R.string.label_getting_apps)
        }

        window.apply {
            if (isNavigationGestureSupported) {
                // 安卓 10 才引入手势导航，之前的版本没必要启用
                setDecorFitsSystemWindowsCompat(false)
            }
        }

        refreshApps()
        refreshAppsLoading()
        if (!state.isAppsLoadingOrLoaded) {
            loadApps()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        getMenuInflater().inflate(R.menu.menu_main, menu)
        freshMenuItem = menu.findItem(R.id.menu_refresh)
        freshMenuItem!!.isEnabled = !state.isAppsLoading
        if (isMenuSearchBarSupported) {
            menu.findItem(R.id.menu_search).apply {
                isVisible = true
            }.also {
                searchView = it.actionView as SearchView
                setupSearchBar()
            }
        }
        sortNameMenuItem = menu.findItem(R.id.menu_sort_name)
        sortInstallTimeMenuItem = menu.findItem(R.id.menu_sort_install_time)
        sortUpdateTimeMenuItem = menu.findItem(R.id.menu_sort_update_time)
        refreshAppSortCategory()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_refresh -> {
                loadApps()
                true
            }

            R.id.menu_about -> {
                @Suppress("DEPRECATION")
                showDialog(DIALOG_ID_ABOUT)
                true
            }

            R.id.menu_sort_name -> {
                state.sortCategory = AppSortCategory.NAME
                refreshAppSortCategory()
                loadApps()
                true
            }

            R.id.menu_sort_install_time -> {
                state.sortCategory = AppSortCategory.INSTALL_TIME
                refreshAppSortCategory()
                loadApps()
                true
            }

            R.id.menu_sort_update_time -> {
                state.sortCategory = AppSortCategory.UPDATE_TIME
                refreshAppSortCategory()
                loadApps()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        adapter.destroy()
        @Suppress("DEPRECATION")
        state.loadAppsTask?.cancel(true)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val app = adapter.getItem(position)
        openActivityListForApp(app.packageName)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // 当窗口获取焦点后，会自动弹出软键盘，临时清除焦点以避免弹出输入法
            searchView?.temporarilyClearFocus()
        }
    }


    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onCreateDialog(id: Int, args: Bundle?): Dialog? {
        return when (id) {
            DIALOG_ID_ABOUT -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.app_name)
                    .setIcon(android.R.drawable.sym_def_app_icon)
                    .setMessage(R.string.message_about)
                    .setPositiveButton(android.R.string.ok, null)
                    .create()
            }

            else -> super.onCreateDialog(id, args)
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


    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private fun setupActionBar(): Boolean {
        val actionBar = getActionBar()?.apply {
            if (!isMenuSearchBarSupported) {
                setDisplayShowTitleEnabled(resources.configuration.screenWidthDp >= 600)
                setDisplayShowCustomEnabled(true)
                searchView = SearchView(this@MainActivity).apply {
                    isIconifiedByDefault = false
                }
                customView = searchView
                setupSearchBar()
            }
        }
        return actionBar != null
    }


    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private fun setupSearchBar() {
        searchView?.apply {
            queryHint = getString(R.string.hint_search_apps)
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

    private fun setupTabs() {
        TabControllerFactory.create(activity = this, rootView = findViewById(R.id.root_layout)) {
            when (it) {
                TAG_USER_APPS -> {
                    state.provisionType = AppProvisionType.USER
                }

                TAG_SYSTEM_APPS -> {
                    state.provisionType = AppProvisionType.SYSTEM
                }
            }
            loadApps()
        }.apply {
            setup()
            addTab(
                tabTag = TAG_USER_APPS,
                textId = R.string.tab_user_apps,
                tabIconId = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB -> null
                    else -> R.drawable.ic_tab_person
                }
            )
            addTab(
                tabTag = TAG_SYSTEM_APPS,
                textId = R.string.tab_system_apps,
                tabIconId = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB -> null
                    else -> R.drawable.ic_tab_system
                }
            )
            setCurrentTab(state.provisionType.name)
        }
    }

    private fun openActivityListForApp(packageName: String?) {
        val intent = Intent(this, ActivityListActivity::class.java)
        intent.putExtra(IntentCompat.EXTRA_PACKAGE_NAME, packageName)
        startActivity(intent)
    }

    fun refreshAppsLoading() {
        val visibleWhenLoading = if (state.isAppsLoading) View.VISIBLE else View.GONE
        val visibleWhenNotLoading = if (state.isAppsLoading) View.GONE else View.VISIBLE
        loadingLayout.visibility = visibleWhenLoading
        emptyTipLayout.visibility = visibleWhenNotLoading
        freshMenuItem?.isEnabled = !state.isAppsLoading
    }

    fun refreshApps() {
        adapter.setApps(state.apps ?: listOf())
    }

    fun refreshAppSortCategory() {
        when (state.sortCategory) {
            AppSortCategory.NAME -> sortNameMenuItem?.isChecked = true
            AppSortCategory.INSTALL_TIME -> sortInstallTimeMenuItem?.isChecked = true
            AppSortCategory.UPDATE_TIME -> sortUpdateTimeMenuItem?.isChecked = true
        }
    }

    @Suppress("DEPRECATION")
    private fun loadApps() {
        state.loadAppsTask?.cancel(true)
        LoadAppsTask(application = application, activity = this, state = state).execute()
    }

    companion object {
        private const val DIALOG_ID_ABOUT = 1
        private const val TAG_USER_APPS = "user_apps"
        private const val TAG_SYSTEM_APPS = "system_apps"
        private const val TAG = "MainActivity"
    }
}