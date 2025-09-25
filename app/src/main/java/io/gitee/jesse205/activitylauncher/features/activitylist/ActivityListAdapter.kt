package io.gitee.jesse205.activitylauncher.features.activitylist

import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import io.gitee.jesse205.activitylauncher.R
import io.gitee.jesse205.activitylauncher.model.LoadedActivityInfo
import io.gitee.jesse205.activitylauncher.utils.setTextOrGone
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class ActivityListAdapter(context: Context) : BaseAdapter() {
    private val handler = Handler(Looper.getMainLooper())
    private val packageManager: PackageManager = context.packageManager
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var originalActivities: List<LoadedActivityInfo> = listOf()
    private var filteredApps: List<LoadedActivityInfo> = originalActivities
    private val executor = ThreadPoolExecutor(
        8,
        16,
        60L, TimeUnit.SECONDS,
        LinkedBlockingQueue()
    )

    var View.holder
        get() = tag as ActivityListViewHolder?
        set(value) {
            tag = value
        }

    override fun getCount() = filteredApps.count()

    override fun getItem(position: Int) = filteredApps[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: inflater.inflate(R.layout.item_activity, parent, false).apply {
            holder = ActivityListViewHolder(this)
        }
        view.holder!!.bind(getItem(position))
        return view
    }

    fun setActivities(activities: List<LoadedActivityInfo>) {
        originalActivities = activities
        filteredApps = activities
        notifyDataSetChanged()
    }


    inner class ActivityListViewHolder(root: View) {
        private val icon: ImageView = root.findViewById(android.R.id.icon)
        private val title: TextView = root.findViewById(android.R.id.title)
        private val summary: TextView = root.findViewById(android.R.id.summary)
        private var labelFuture: Future<*>? = null
        private var iconFuture: Future<*>? = null
        private var boundActivityName: String? = null


        fun bind(activityInfo: LoadedActivityInfo?) {
            if (boundActivityName == activityInfo?.name) {
                return
            }
            boundActivityName = activityInfo?.name
            icon.setImageDrawable(null)
            title.text = null
            title.visibility = View.GONE
            summary.text = null
            summary.paint.isStrikeThruText = false
            if (activityInfo != null) {
                summary.text = activityInfo.name
                summary.paint.isStrikeThruText = !activityInfo.activityInfo.exported
                if (activityInfo.label != null) {
                    title.setTextOrGone(activityInfo.label)
                } else {
                    labelFuture?.cancel(true)
                    labelFuture = executor.submit {
                        val label = activityInfo.loadLabel(packageManager)
                        if (Thread.currentThread().isInterrupted) {
                            return@submit
                        }
                        handler.post {
                            if (boundActivityName != activityInfo.name) {
                                return@post
                            }
                            title.setTextOrGone(label)
                        }
                    }
                }

                iconFuture?.cancel(true)
                iconFuture = executor.submit {
                    val iconDrawable = activityInfo.loadIcon(packageManager)
                    if (Thread.currentThread().isInterrupted) {
                        return@submit
                    }
                    handler.post {
                        if (boundActivityName != activityInfo.name) {
                            return@post
                        }
                        icon.setImageDrawable(iconDrawable)
                    }
                }
            }
        }
    }
}
