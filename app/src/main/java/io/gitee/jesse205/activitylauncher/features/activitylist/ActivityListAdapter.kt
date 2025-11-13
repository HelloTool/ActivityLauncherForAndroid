package io.gitee.jesse205.activitylauncher.features.activitylist

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import io.gitee.jesse205.activitylauncher.R
import io.gitee.jesse205.activitylauncher.utils.getDimensionPixelSize
import io.gitee.jesse205.activitylauncher.utils.scaleToFit
import io.gitee.jesse205.activitylauncher.utils.setTextOrGone
import io.gitee.jesse205.activitylauncher.utils.submitWithCheckAndCallback
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class ActivityListAdapter(context: Context) : BaseAdapter(), Filterable {
    private val handler = Handler(Looper.getMainLooper())
    private val packageManager: PackageManager = context.packageManager
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var originalActivities: List<AppActivityItem> = listOf()
    private var filteredActivities: List<AppActivityItem> = originalActivities

    private val iconSize = context.theme.getDimensionPixelSize(R.attr.listIconLarge)
    private val activityFilter by lazy { ActivityFilter() }
    private var lastFilterConstraint: CharSequence? = null

    private val executor = ThreadPoolExecutor(
        8,
        16,
        60L, TimeUnit.SECONDS,
        LinkedBlockingQueue()
    )
    private var iconCache: LruCache<String, Drawable>? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            LruCache(4 * 1024 * 1024)
        } else {
            null
        }

    var View.holder
        get() = tag as ActivityListViewHolder?
        set(value) {
            tag = value
        }

    override fun getCount() = filteredActivities.count()

    override fun getItem(position: Int) = filteredActivities[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: inflater.inflate(R.layout.item_activity, parent, false).apply {
            holder = ActivityListViewHolder(this)
        }
        view.holder!!.bind(getItem(position))
        return view
    }

    fun setActivities(activities: List<AppActivityItem>) {
        originalActivities = activities
        filteredActivities = if (lastFilterConstraint.isNullOrBlank()) activities else listOf()
        notifyDataSetChanged()
        activityFilter.filter(lastFilterConstraint)
    }

    override fun getFilter(): Filter = activityFilter

    fun destroy() {
        executor.shutdownNow()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            iconCache?.evictAll()
            iconCache = null
        }
    }

    inner class ActivityListViewHolder(root: View) {
        private val icon: ImageView = root.findViewById(android.R.id.icon)
        private val title: TextView = root.findViewById(android.R.id.title)
        private val summary: TextView = root.findViewById(android.R.id.summary)
        private var labelFuture: Future<*>? = null
        private var iconFuture: Future<*>? = null
        private var boundActivityInfo: AppActivityItem? = null


        fun bind(activityInfo: AppActivityItem?) {
            if (boundActivityInfo == activityInfo) {
                return
            }
            boundActivityInfo = activityInfo
            icon.setImageDrawable(null)
            title.setTextOrGone(null)
            summary.text = null
            summary.paint.isStrikeThruText = false
            labelFuture?.cancel(true)
            iconFuture?.cancel(true)
            if (activityInfo != null) {
                summary.text = activityInfo.name
                summary.paint.isStrikeThruText = !activityInfo.exported
                loadLabel()
                loadIcon()
            }
        }

        fun loadLabel() {
            val info = boundActivityInfo ?: return
            if (info.isLabelLoaded) {
                title.setTextOrGone(info.label)
                return
            }
            labelFuture = executor.submitWithCheckAndCallback(
                handler = handler,
                check = { boundActivityInfo == info },
                task = { info.getOrLoadLabel(packageManager) },
                callback = { title.setTextOrGone(it) },
            )
        }

        fun loadIcon() {
            val info = boundActivityInfo ?: return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                iconCache?.get(info.name)?.let {
                    icon.setImageDrawable(it)
                    return
                }
            }

            iconFuture = executor.submitWithCheckAndCallback(
                handler = handler,
                check = { boundActivityInfo == info },
                task = { info.loadIcon(packageManager)?.scaleToFit(iconSize, iconSize) },
                callback = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                        iconCache?.put(info.name, it)
                    }
                    icon.setImageDrawable(it)
                },
            )
        }
    }

    inner class ActivityFilter : Filter() {
        protected override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()

            val filteredList: List<AppActivityItem> = if (constraint.isNullOrEmpty()) {
                originalActivities
            } else {
                originalActivities.filter {
                    it.getOrLoadLabel(packageManager).contains(constraint, true) ||
                            it.name.contains(constraint, true)
                }
            }

            results.values = filteredList
            results.count = filteredList.size
            return results
        }

        protected override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            @Suppress("UNCHECKED_CAST")
            filteredActivities = results.values as List<AppActivityItem>
            lastFilterConstraint = constraint
            notifyDataSetChanged()
        }
    }
}
