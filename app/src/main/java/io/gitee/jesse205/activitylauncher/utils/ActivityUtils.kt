package io.gitee.jesse205.activitylauncher.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import java.lang.reflect.Field

private const val TAG = "ActivityUtils"

object ActivityCompat {
    const val WINDOW_HIERARCHY_TAG: String = "android:viewHierarchyState"
    const val SAVED_DIALOG_IDS_KEY: String = "android:savedDialogIds"
    const val SAVED_DIALOGS_TAG: String = "android:savedDialogs"
    const val SAVED_DIALOG_KEY_PREFIX: String = "android:dialog_"
    const val SAVED_DIALOG_ARGS_KEY_PREFIX: String = "android:dialog_args_"
}

fun Activity.launchUri(uri: String) {
    startActivity(Intent.parseUri(uri, Intent.URI_INTENT_SCHEME))
}

@delegate:SuppressLint("DiscouragedPrivateApi")
val mTokenField: Field by lazy {
    Activity::class.java.getDeclaredField("mToken").apply {
        isAccessible = true
    }
}

@delegate:SuppressLint("DiscouragedPrivateApi")
val mMainThreadField: Field by lazy {
    Activity::class.java.getDeclaredField("mMainThread").apply {
        isAccessible = true
    }
}

fun Activity.reflectivelyGetActivityToken() = mTokenField.get(this) as? IBinder

fun Activity.reflectivelyGetActivityThread(): Any? = mMainThreadField.get(this)

fun Activity.recreateGingerbread() {
    val token = reflectivelyGetActivityToken() ?: return
    val activityThread = reflectivelyGetActivityThread() ?: return
    ActivityThreadReflector(activityThread).let {
        val activityClientRecord = ActivityThreadReflector.ActivityClientRecordReflector(it.mActivities[token]!!)
        ActivityThreadReflector.ApplicationThreadReflector(it.mAppThread).scheduleRelaunchActivity(
            token,
            activityClientRecord.pendingResults,
            activityClientRecord.pendingIntents,
            0,
            activityClientRecord.startsNotResumed,
            activityClientRecord.createdConfig
        )
    }
}

fun Activity.tryRecreateCompat() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        recreate()
        return
    }
    runCatching {
        recreateGingerbread()
    }.onFailure {
        Log.e(TAG, "recreateCompat: Failed to recreate activity", it)
    }
}