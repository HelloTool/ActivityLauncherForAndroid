package io.gitee.jesse205.activitylauncher.features.applist

import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import io.gitee.jesse205.activitylauncher.R
import io.gitee.jesse205.activitylauncher.model.LoadedAppInfo
import io.gitee.jesse205.activitylauncher.utils.setTextOrGone
import java.util.Locale
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


class AppListAdapter(context: Context) :
    BaseAdapter(), Filterable {
    private val handler = Handler(Looper.getMainLooper())
    private val packageManager: PackageManager = context.packageManager
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var originalApps: List<LoadedAppInfo> = listOf()
    private var filteredApps: List<LoadedAppInfo> = originalApps

    val appFilter by lazy { AppFilter() }
    var lastFilterConstraint: CharSequence? = null

    // 创建一个单例的线程池执行器
    private val executor = ThreadPoolExecutor(
        8,
        16,
        60L, TimeUnit.SECONDS,
        LinkedBlockingQueue()
    )

    var View.holder
        get() = tag as AppListViewHolder?
        set(value) {
            tag = value
        }

    override fun getCount() = filteredApps.count()

    override fun getItem(position: Int) = filteredApps[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: inflater.inflate(R.layout.item_app, parent, false).apply {
            holder = AppListViewHolder(this)
        }
        view.holder!!.bind(getItem(position))
        return view
    }

    fun setApps(apps: List<LoadedAppInfo>) {
        originalApps = apps
        filteredApps = apps
        notifyDataSetChanged()
        appFilter.filter(lastFilterConstraint)
    }


    override fun getFilter(): AppFilter = appFilter


    inner class AppListViewHolder(private val root: View) {
        private val icon: ImageView = root.findViewById(android.R.id.icon)
        private val title: TextView = root.findViewById(android.R.id.title)
        private val summary: TextView = root.findViewById(android.R.id.summary)
        private var boundPackageName: String? = null
        private var labelFuture: Future<*>? = null
        private var iconFuture: Future<*>? = null

        fun bind(app: LoadedAppInfo?) {
            if (boundPackageName == app?.packageName) {
                return
            }
            boundPackageName = app?.packageName
            icon.setImageDrawable(null)
            title.text = null
            summary.text = null
            if (app != null) {
                title.setTextOrGone(app.label)
                summary.text = app.packageName
                labelFuture?.cancel(true)
                labelFuture = executor.submit {
                    val label = app.loadLabel(packageManager)

                    if (Thread.currentThread().isInterrupted) {
                        return@submit
                    }
                    handler.post {
                        if (boundPackageName != app.packageName) {
                            return@post
                        }
                        title.setTextOrGone(label)
                    }
                }

                iconFuture?.cancel(true)
                iconFuture = executor.submit {
                    val iconDrawable = app.loadIcon(packageManager)

                    if (Thread.currentThread().isInterrupted) {
                        return@submit
                    }
                    handler.post {
                        if (boundPackageName != app.packageName) {
                            return@post
                        }
                        icon.setImageDrawable(iconDrawable)
                    }
                }
            }
        }
    }

    fun destroy() {
        executor.shutdownNow()
    }


    inner class AppFilter : Filter() {
        protected override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()

            val filteredList: List<LoadedAppInfo?> = if (constraint.isNullOrEmpty()) {
                originalApps
            } else {
                mutableListOf<LoadedAppInfo>().apply {
                    val filterPattern = constraint.toString().lowercase(Locale.getDefault()).trim()
                    for (app in originalApps) {
                        val appName = app.label.toString().lowercase(Locale.getDefault())
                        val pkgName: String = app.packageName.lowercase(Locale.getDefault())
                        if (appName.contains(filterPattern) || pkgName.contains(filterPattern)) {
                            add(app)
                        }
                    }
                }
            }

            results.values = filteredList
            results.count = filteredList.size
            return results
        }

        protected override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            @Suppress("UNCHECKED_CAST")
            filteredApps = results.values as List<LoadedAppInfo>
            lastFilterConstraint = constraint
            notifyDataSetChanged()
        }
    }
    companion object{
        private const val TAG = "AppListAdapter"
    }
}