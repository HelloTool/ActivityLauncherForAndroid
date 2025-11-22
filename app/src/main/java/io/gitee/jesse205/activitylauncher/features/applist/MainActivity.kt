package io.gitee.jesse205.activitylauncher.features.applist

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import io.gitee.jesse205.activitylauncher.theme.ThemeManager
import io.gitee.jesse205.activitylauncher.theme.ThemeSupport.appTheme
import io.gitee.jesse205.activitylauncher.utils.AppProvisionType
import io.gitee.jesse205.activitylauncher.utils.AppSortCategory
import io.gitee.jesse205.activitylauncher.utils.copyText
import io.gitee.jesse205.activitylauncher.utils.getBoolean
import io.gitee.jesse205.activitylauncher.utils.getUserFriendlyMessage
import io.gitee.jesse205.activitylauncher.utils.isActionBarSupported
import io.gitee.jesse205.activitylauncher.utils.isMenuSearchBarSupported
import io.gitee.jesse205.activitylauncher.utils.isNavigationGestureSupported
import io.gitee.jesse205.activitylauncher.utils.isOpenable
import io.gitee.jesse205.activitylauncher.utils.isSupportEdgeToEdge
import io.gitee.jesse205.activitylauncher.utils.launchUri
import io.gitee.jesse205.activitylauncher.utils.openApp
import io.gitee.jesse205.activitylauncher.utils.parentsDoNotClipChildrenAndPadding
import io.gitee.jesse205.activitylauncher.utils.showToast
import io.gitee.jesse205.activitylauncher.utils.tabs.TabControllerFactory
import io.gitee.jesse205.activitylauncher.utils.temporarilyClearFocus
import io.gitee.jesse205.activitylauncher.utils.toViewVisibility

class MainActivity : BaseActivity<MainActivityState>(), AdapterView.OnItemClickListener,
    MainActivityState.MainActivityStateListener {

    override val stateClass = MainActivityState::class.java
    private val adapter: AppListAdapter by lazy { AppListAdapter(this@MainActivity) }

    private val rootLayout: ViewGroup by lazy { findViewById(R.id.root_layout) }
    private val gridContainer: ViewGroup by lazy { rootLayout.findViewById(R.id.grid_container) }
    private val gridView: GridView by lazy { gridContainer.findViewById(R.id.grid) }
    private val emptyLayout: ViewGroup by lazy { gridContainer.findViewById(android.R.id.empty) }
    private val emptyText: TextView by lazy { emptyLayout.findViewById(R.id.empty_text) }
    private val progressLayout: ViewGroup by lazy { rootLayout.findViewById(R.id.progress_layout) }
    private val progressText: TextView by lazy { progressLayout.findViewById(R.id.progress_text) }

    private var searchView: SearchView? = null
    private var freshMenuItem: MenuItem? = null
    private var sortInstallTimeMenuItem: MenuItem? = null
    private var sortUpdateTimeMenuItem: MenuItem? = null
    private var sortNameMenuItem: MenuItem? = null

    private val preferences: MainActivityPreferences by lazy { MainActivityPreferences(this) }

    private var isUsingSearchLayout = false

    override fun onCreateState(): MainActivityState {
        return MainActivityState(
            _provisionType = preferences.provisionType ?: AppProvisionType.USER,
            _sortCategory = preferences.sortCategory ?: AppSortCategory.UPDATE_TIME
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (appTheme.id == ThemeManager.THEME_GINGERBREAD) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grid)

        val isActionBarInitialized = if (isActionBarSupported) {
            setupActionBar()
        } else {
            false
        }

        setupTabs()

        if (!isActionBarSupported || !isActionBarInitialized) {
            isUsingSearchLayout = setupSearchLayout()
        }

        gridView.apply {
            emptyView = emptyLayout
            adapter = this@MainActivity.adapter
            onItemClickListener = this@MainActivity
            registerForContextMenu(this)
            if (isNavigationGestureSupported && theme.isSupportEdgeToEdge) {
                parentsDoNotClipChildrenAndPadding(rootLayout)
            }
        }

        progressText.apply {
            text = getString(R.string.label_getting_apps)
        }

        emptyText.apply {
            text = getString(R.string.label_empty_apps)
        }

        state.bind(this, this)
        onSortedAppsUpdate(state.sortedApps)
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
        menu.setHeaderTitle(appInfo.getOrLoadLabel(packageManager))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            menu.setGroupDividerEnabled(true)
        }
        menuInflater.inflate(R.menu.menu_main_list_item, menu)

        menu.findItem(R.id.menu_open_app).apply {
            isVisible = appInfo.applicationInfo.isOpenable(this@MainActivity)
        }
    }


    override fun onContextItemSelected(item: MenuItem): Boolean {
        val menuInfo = item.menuInfo
        if (menuInfo !is AdapterView.AdapterContextMenuInfo) {
            return super.onContextItemSelected(item)
        }
        val appInfo = adapter.getItem(menuInfo.position)
        when (item.itemId) {
            R.id.menu_open_app -> openAppOrShowException(appInfo.packageName)
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
        if (isMenuSearchBarSupported && !isUsingSearchLayout) {
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

    private fun copyPackageName(item: AppItem) {
        copyText(getString(R.string.label_package_name), item.packageName)
    }

    private fun copyAppName(item: AppItem) {
        copyText(getString(R.string.label_app_name), item.getOrLoadLabel(packageManager))
    }


    override fun onDestroy() {
        super.onDestroy()
        adapter.destroy()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val app = adapter.getItem(position)
        ActivityListActivity.launch(this, app.packageName)
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

    private fun createLaunchUriDialog(): AlertDialog {
        val content = layoutInflater.inflate(R.layout.dialog_launch_uri, null)
        val input = content.findViewById<EditText>(android.R.id.input)
        var positiveButton: Button? = null
        return AlertDialog.Builder(this)
            .setTitle(R.string.menu_launch_uri)
            .setView(content)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create().apply {
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
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
                        isEnabled = !input.text.isNullOrBlank() && input.error.isNullOrBlank()
                        setOnClickListener {
                            runCatching {
                                launchUri(input.text.toString())
                            }.onSuccess {
                                dismiss()
                            }.onFailure {
                                isEnabled = false
                                input.error = it.getUserFriendlyMessage(this@MainActivity)
                            }
                        }
                    }
                }
            }
    }

    private fun setupSearchLayout(): Boolean {
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
        return true
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

    private fun openAppOrShowException(packageName: String) {
        runCatching {
            openApp(packageName)
        }.onFailure {
            Log.w(TAG, "openAppOrShowToast: failed to open app $packageName", it)
            showToast(it.getUserFriendlyMessage(this))
        }
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
        updateProgressBar(isAppsLoading = isAppsLoading)
        gridContainer.visibility = (!isAppsLoading).toViewVisibility()
        freshMenuItem?.isEnabled = !isAppsLoading
    }

    override fun onAppNamesLoadingUpdate(isAppNamesLoading: Boolean) {
        updateProgressBar(isAppNamesLoading = isAppNamesLoading)
    }

    override fun onSortedAppsUpdate(apps: List<AppItem>?) {
        adapter.setApps(apps ?: listOf())
    }

    private fun loadApps() {
        state.loadApps(application)
    }

    private fun updateProgressBar(
        isAppsLoading: Boolean = state.isAppsLoading,
        isAppNamesLoading: Boolean = state.isAppNamesLoading
    ) {
        progressLayout.visibility = (isAppsLoading || isAppNamesLoading).toViewVisibility()
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