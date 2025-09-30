package io.gitee.jesse205.activitylauncher.features.applist

import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.FileUriExposedException
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ListView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import io.gitee.jesse205.activitylauncher.R
import io.gitee.jesse205.activitylauncher.core.BaseActivity
import io.gitee.jesse205.activitylauncher.features.activitylist.ActivityListActivity
import io.gitee.jesse205.activitylauncher.model.LoadedAppInfo
import io.gitee.jesse205.activitylauncher.utils.AppProvisionType
import io.gitee.jesse205.activitylauncher.utils.AppSortCategory
import io.gitee.jesse205.activitylauncher.utils.IntentCompat
import io.gitee.jesse205.activitylauncher.utils.isActionBarSupported
import io.gitee.jesse205.activitylauncher.utils.isMenuSearchBarSupported
import io.gitee.jesse205.activitylauncher.utils.isNavigationGestureSupported
import io.gitee.jesse205.activitylauncher.utils.tabs.TabControllerFactory
import io.gitee.jesse205.activitylauncher.utils.temporarilyClearFocus

class MainActivity : BaseActivity<MainActivityState>(), AdapterView.OnItemClickListener,
    MainActivityState.MainActivityStateListener {

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

    override val enableEdgeToEdge: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    private val preferences: SharedPreferences by lazy { getPreferences(MODE_PRIVATE) }
    private var SharedPreferences.provisionType: AppProvisionType?
        get() = getString(PREFERENCE_KEY_PROVISION_TYPE, null)
            ?.let {
                runCatching {
                    AppProvisionType.valueOf(it)
                }.getOrNull()
            }
        set(value) {
            edit()
                .putString(PREFERENCE_KEY_PROVISION_TYPE, value?.name)
                .apply()
        }

    private var SharedPreferences.sortCategory: AppSortCategory?
        get() = getString(PREFERENCE_KEY_SORT_CATEGORY, null)
            ?.let {
                runCatching {
                    AppSortCategory.valueOf(it)
                }.getOrNull()
            }
        set(value) {
            edit()
                .putString(PREFERENCE_KEY_SORT_CATEGORY, value?.name)
                .apply()
        }

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

        setContentView(R.layout.activity_list)

        if (isActionBarSupported) {
            setupActionBar()
        }
        setupTabs()
        if (!isActionBarSupported) {
            setupSearchLayout()
        }

        adapter = AppListAdapter(this@MainActivity)

        listView.apply {
            emptyView = emptyLayout
            adapter = this@MainActivity.adapter
            onItemClickListener = this@MainActivity
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
        return when (item.itemId) {
            R.id.menu_refresh -> {
                true
            }

            R.id.menu_about -> {
                @Suppress("DEPRECATION")
                showDialog(DIALOG_ID_ABOUT)
                true
            }

            R.id.menu_sort_name -> {
                state.sortCategory = AppSortCategory.NAME
                loadApps()
                true
            }

            R.id.menu_sort_install_time -> {
                state.sortCategory = AppSortCategory.INSTALL_TIME
                loadApps()
                true
            }

            R.id.menu_sort_update_time -> {
                state.sortCategory = AppSortCategory.UPDATE_TIME
                loadApps()
                true
            }

            R.id.menu_launch_uri -> {
                @Suppress("DEPRECATION")
                showDialog(DIALOG_ID_LAUNCH_URI)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
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
            DIALOG_ID_ABOUT -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.app_name)
                    .setIcon(android.R.drawable.sym_def_app_icon)
                    .setMessage(R.string.message_about)
                    .setPositiveButton(android.R.string.ok, null)
                    .create().apply {
                        setOnDismissListener {
                            removeDialog(id)
                        }
                    }
            }

            DIALOG_ID_LAUNCH_URI -> {
                val dialogContent = layoutInflater.inflate(R.layout.dialog_launch_uri, null)
                AlertDialog.Builder(this)
                    .setTitle(R.string.menu_title_launch_uri)
                    .setView(dialogContent)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        dialogContent.findViewById<EditText>(android.R.id.input).let {
                            launchUri(it.text.toString())
                        }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .create().apply {
                        val input = dialogContent.findViewById<EditText>(android.R.id.input)
                        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        setOnShowListener {
                            input.requestFocus()
                            inputMethodManager.showSoftInput(input, 0)

                        }
                        setOnDismissListener {
                            removeDialog(id)
                            inputMethodManager.hideSoftInputFromWindow(input.windowToken, 0)
                        }
                    }
            }

            else -> super.onCreateDialog(id, args)
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
            state.provisionType = when (it) {
                TAG_USER_APPS -> AppProvisionType.USER
                TAG_SYSTEM_APPS -> AppProvisionType.SYSTEM
                else -> throw IllegalArgumentException("Invalid tab tag")
            }
            loadApps()
        }.apply {
            setup()
            addTab(
                tabTag = TAG_USER_APPS,
                textId = R.string.tab_user_apps,
                tabIconId = when {
                    isActionBarSupported -> null
                    else -> R.drawable.ic_tab_person
                }
            )
            addTab(
                tabTag = TAG_SYSTEM_APPS,
                textId = R.string.tab_system_apps,
                tabIconId = when {
                    isActionBarSupported -> null
                    else -> R.drawable.ic_tab_system
                }
            )
            setCurrentTab(
                when (state.provisionType) {
                    AppProvisionType.USER -> TAG_USER_APPS
                    AppProvisionType.SYSTEM -> TAG_SYSTEM_APPS
                }
            )
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
                showNoActivityFoundToast()
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && it is FileUriExposedException) {
                showFileUriNotAllowedToast()
            }
        }
    }

    private fun showNoActivityFoundToast() {
        Toast.makeText(this, R.string.toast_no_activity_found, Toast.LENGTH_SHORT).show()
    }

    private fun showFileUriNotAllowedToast() {
        Toast.makeText(this, R.string.toast_file_uri_not_allowed, Toast.LENGTH_SHORT).show()
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

    fun loadApps() {
        state.loadApps(application)
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val DIALOG_ID_ABOUT = 1
        private const val DIALOG_ID_LAUNCH_URI = 2
        private const val TAG_USER_APPS = "user_apps"
        private const val TAG_SYSTEM_APPS = "system_apps"
        private const val PREFERENCE_KEY_PROVISION_TYPE = "provision_type"
        private const val PREFERENCE_KEY_SORT_CATEGORY = "sort_category"
    }
}