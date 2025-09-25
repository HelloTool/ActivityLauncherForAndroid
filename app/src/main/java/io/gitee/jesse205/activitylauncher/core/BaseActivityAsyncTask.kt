@file:Suppress("DEPRECATION")

package io.gitee.jesse205.activitylauncher.core

import android.app.Activity
import android.os.AsyncTask
import java.lang.ref.WeakReference

abstract class BaseActivityAsyncTask<A : Activity, S, Params, Progress, Result>(
    activity: A,
    protected val state: S
) : AsyncTask<Params, Progress, Result>() {
    private var activityReference: WeakReference<A?> = WeakReference(activity)
    protected val weakActivity: A?
        get() = activityReference.get()
    fun attachActivity(activity: A) {
        activityReference = WeakReference(activity)
    }
}