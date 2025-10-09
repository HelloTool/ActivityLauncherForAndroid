package io.gitee.jesse205.activitylauncher.features.applist

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.FileUriExposedException
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.SearchView
import android.widget.TextView
import androidx.annotation.RequiresApi
import io.gitee.jesse205.activitylauncher.R
import io.gitee.jesse205.activitylauncher.app.BaseActivity
import io.gitee.jesse205.activitylauncher.features.activitylist.ActivityListActivity
import io.gitee.jesse205.activitylauncher.features.settings.SettingsActivity
import io.gitee.jesse205.activitylauncher.model.LoadedAppInfo
import io.gitee.jesse205.activitylauncher.utils.AppProvisionType
import io.gitee.jesse205.activitylauncher.utils.AppSortCategory
import io.gitee.jesse205.activitylauncher.utils.IntentCompat
import io.gitee.jesse205.activitylauncher.utils.copyText
import io.gitee.jesse205.activitylauncher.utils.getBoolean
import io.gitee.jesse205.activitylauncher.utils.isActionBarSupported
import io.gitee.jesse205.activitylauncher.utils.isMenuSearchBarSupported
import io.gitee.jesse205.activitylauncher.utils.isNavigationGestureSupported
import io.gitee.jesse205.activitylauncher.utils.parentsDoNotClipChildrenAndPadding
import io.gitee.jesse205.activitylauncher.utils.shouldApplyEdgeToEdge
import io.gitee.jesse205.activitylauncher.utils.showToast
import io.gitee.jesse205.activitylauncher.utils.tabs.TabControllerFactory
import io.gitee.jesse205.activitylauncher.utils.temporarilyClearFocus

class MainActivity : BaseActivity<MainActivityState>(), AdapterView.OnItemClickListener,
    MainActivityState.MainActivityStateListener {

    override val stateClass = MainActivityState::class.java
    private lateinit var adapter: AppListAdapter
    private val gridView: GridView by lazy { findViewById(R.id.grid) }
    private val loadingLayout: ViewGroup by lazy { findViewById(R.id.loading_layout) }
    private val emptyTipLayout: ViewGroup by lazy { findViewById(R.id.empty_tip_layout) }
    private val emptyLayout: ViewGroup by lazy { findViewById(android.R.id.empty) }
    private var searchView: SearchView? = null
    private var freshMenuItem: MenuItem? = null
    private var sortInstallTimeMenuItem: MenuItem? = null
    private var sortUpdateTimeMenuItem: MenuItem? = null
    private var sortNameMenuItem: MenuItem? = null

    private val preferences: MainActivityPreferences by lazy { MainActivityPreferences(this) }

    override fun onCreateState(): MainActivityState {
        return MainActivityState(
            _provisionType = preferences.provisionType ?: AppProvisionType.USER,
            _sortCategory = preferences.sortCategory ?: AppSortCategory.INSTALL_TIME
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isActionBarSupported) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
        }

        if (isNavigationGestureSupported) {
            requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY)
        }

        setContentView(R.layout.activity_grid)

        val isActionBarInitialized = if (isActionBarSupported) {
            setupActionBar()
        } else {
            false
        }
        setupTabs()
        if (!isActionBarSupported || !isActionBarInitialized) {
            setupSearchLayout()
        }

        adapter = AppListAdapter(this@MainActivity)
        val rootLayout = findViewById<ViewGroup>(R.id.root_layout)
        gridView.apply {
            emptyView = emptyLayout
            adapter = this@MainActivity.adapter
            onItemClickListener = this@MainActivity
            registerForContextMenu(this)
            if (shouldApplyEdgeToEdge) {
                parentsDoNotClipChildrenAndPadding(rootLayout)
            }
        }

        findViewById<TextView>(R.id.loading_text).apply {
            text = getString(R.string.label_getting_apps)
        }

        state.bind(this, this)
        onAppsUpdate(state.apps)
        onAppsLoadingUpdate(state.isAppsLoading)
        if (!state.isAppsLoadingOrLoaded) {
            loadApps()
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        if (v != gridView || menuInfo !is AdapterView.AdapterContextMenuInfo) {
            return
        }
        val appInfo = adapter.getItem(menuInfo.position)
        menu.setHeaderTitle(appInfo.loadLabel(packageManager))
        menuInflater.inflate(R.menu.menu_main_list_item, menu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            menu.setGroupDividerEnabled(true)
        }
    }


    override fun onContextItemSelected(item: MenuItem): Boolean {
        val menuInfo = item.menuInfo
        if (menuInfo !is AdapterView.AdapterContextMenuInfo) {
            return super.onContextItemSelected(item)
        }
        val appInfo = adapter.getItem(menuInfo.position)
        when (item.itemId) {
            R.id.menu_app_details -> openAppDetails(appInfo.packageName)
            R.id.menu_copy_app_name -> copyAppName(appInfo)
            R.id.menu_copy_package_name -> copyPackageName(appInfo)
            else -> return super.onContextItemSelected(item)
        }
        return true
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
        onAppSortCategoryUpdate(state.sortCategory)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        @Suppress("DEPRECATION")
        when (item.itemId) {
            R.id.menu_refresh -> loadApps()
            R.id.menu_about -> showDialog(DIALOG_ID_ABOUT)
            R.id.menu_sort_name -> changeAppSortCategory(AppSortCategory.NAME)
            R.id.menu_sort_install_time -> changeAppSortCategory(AppSortCategory.INSTALL_TIME)
            R.id.menu_sort_update_time -> changeAppSortCategory(AppSortCategory.UPDATE_TIME)
            R.id.menu_launch_uri -> showDialog(DIALOG_ID_LAUNCH_URI)
            R.id.menu_settings -> SettingsActivity.launch(this)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun copyPackageName(item: LoadedAppInfo) {
        copyText(getString(R.string.label_package_name), item.packageName)
    }

    private fun copyAppName(item: LoadedAppInfo) {
        copyText(getString(R.string.label_app_name), item.loadLabel(packageManager))
    }


    override fun onDestroy() {
        super.onDestroy()
        adapter.destroy()
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


    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION", "InflateParams")
    override fun onCreateDialog(id: Int, args: Bundle?): Dialog? {
        return when (id) {
            DIALOG_ID_ABOUT -> createAboutDialog()
            DIALOG_ID_LAUNCH_URI -> createLaunchUriDialog()
            else -> super.onCreateDialog(id, args)
        }
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onPrepareDialog(id: Int, dialog: Dialog, args: Bundle?) {
        super.onPrepareDialog(id, dialog, args)
        dialog.setOnDismissListener {
            @Suppress("DEPRECATION")
            removeDialog(id)
        }
    }

    private fun createAboutDialog(): AlertDialog {
        return AlertDialog.Builder(this)
            .setTitle(R.string.app_name)
            .setMessage(R.string.message_about)
            .setPositiveButton(android.R.string.ok, null)
            .create()
    }

    @SuppressLint("InflateParams")
    private fun createLaunchUriDialog(): AlertDialog {
        val content = layoutInflater.inflate(R.layout.dialog_launch_uri, null)
        val input = content.findViewById<EditText>(android.R.id.input)
        return AlertDialog.Builder(this)
            .setTitle(R.string.menu_title_launch_uri)
            .setView(content)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                launchUri(input.text.toString())
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create().apply {
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                var positiveButton: Button? = getButton(AlertDialog.BUTTON_POSITIVE)
                input.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {}

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        positiveButton?.isEnabled = !s.isNullOrBlank()
                    }
                })
                setOnShowListener {
                    input.requestFocus()
                    positiveButton = getButton(AlertDialog.BUTTON_POSITIVE).apply {
                        isEnabled = false
                    }
                }
            }
    }

    private fun setupSearchLayout() {
        // HONEYCOMB 以上采用 ActionBar 中的 SearchView
        val searchLayout = findViewById<ViewStub>(R.id.search_layout).inflate()
        val searchInput = searchLayout.findViewById<EditText>(R.id.search_input)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
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
            state.changeAppProvisionType(application, AppProvisionType.valueOf(it))
        }.apply {
            val isShowTabIcons = theme.getBoolean(R.attr.showTabIcons, false)
            setup()
            addTab(
                tabTag = AppProvisionType.USER.name,
                textId = R.string.tab_user_apps,
                tabIconId = if (isShowTabIcons) R.drawable.ic_tab_person else null
            )
            addTab(
                tabTag = AppProvisionType.SYSTEM.name,
                textId = R.string.tab_system_apps,
                tabIconId = if (isShowTabIcons) R.drawable.ic_tab_system else null
            )
            setCurrentTab(state.provisionType.name)
        }
    }

    private fun openActivityListForApp(packageName: String?) {
        val intent = Intent(this, ActivityListActivity::class.java)
        intent.putExtra(IntentCompat.EXTRA_PACKAGE_NAME, packageName)
        startActivity(intent)
    }

    private fun launchUri(uri: String) {
        runCatching {
            startActivity(Intent.parseUri(uri, Intent.URI_INTENT_SCHEME))
        }.onFailure {
            Log.w(TAG, "launchUri: launch URI failed", it)
            if (it is ActivityNotFoundException) {
                showToast(R.string.toast_no_activity_found)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && it is FileUriExposedException) {
                showToast(R.string.toast_file_uri_not_allowed)
            }
        }
    }

    private fun openAppDetails(packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.Builder()
                .scheme("package")
                .opaquePart(packageName)
                .build()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    override fun onAppSortCategoryUpdate(sortCategory: AppSortCategory) {
        preferences.sortCategory = sortCategory
        when (sortCategory) {
            AppSortCategory.NAME -> sortNameMenuItem?.isChecked = true
            AppSortCategory.INSTALL_TIME -> sortInstallTimeMenuItem?.isChecked = true
            AppSortCategory.UPDATE_TIME -> sortUpdateTimeMenuItem?.isChecked = true
        }
    }

    override fun onAppProvisionTypeUpdate(provisionType: AppProvisionType) {
        preferences.provisionType = provisionType
    }

    override fun onAppsLoadingUpdate(isAppsLoading: Boolean) {
        val visibleWhenLoading = if (isAppsLoading) View.VISIBLE else View.GONE
        val visibleWhenNotLoading = if (isAppsLoading) View.GONE else View.VISIBLE
        loadingLayout.visibility = visibleWhenLoading
        emptyTipLayout.visibility = visibleWhenNotLoading
        freshMenuItem?.isEnabled = !isAppsLoading
    }

    override fun onAppsUpdate(apps: List<LoadedAppInfo>?) {
        adapter.setApps(apps ?: listOf())
    }

    private fun loadApps() {
        state.loadApps(application)
    }

    private fun changeAppSortCategory(sortCategory: AppSortCategory) {
        state.changeAppSortCategory(application, sortCategory)
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val DIALOG_ID_ABOUT = 1
        private const val DIALOG_ID_LAUNCH_URI = 2
    }
}