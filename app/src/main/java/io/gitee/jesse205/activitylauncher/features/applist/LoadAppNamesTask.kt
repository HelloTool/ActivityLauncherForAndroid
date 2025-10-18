@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package io.gitee.jesse205.activitylauncher.features.applist

import android.app.Application
import android.content.pm.PackageManager
import android.os.AsyncTask

class LoadAppNamesTask(
    application: Application,
    private val apps: List<AppModel>,
    private val onBeforeLoad: () -> Unit,
    private val onLoad: () -> Unit,
    private val onCancel: () -> Unit,
) : AsyncTask<Unit, Unit, Unit>() {
    private val packageManager: PackageManager = application.packageManager
    private var isTaskIgnored = false
    override fun doInBackground(vararg params: Unit) {
        apps.forEach {
            it.getOrLoadLabel(packageManager)
        }
    }


    override fun onPreExecute() {
        if (!isTaskIgnored) {
            onBeforeLoad()
        }
    }

    override fun onPostExecute(result: Unit) {
        if (!isTaskIgnored) {
            onLoad()
        }
    }

    override fun onCancelled() {
        if (!isTaskIgnored) {
            onCancel()
        }
    }

    fun ignore() {
        isTaskIgnored = true
    }
}