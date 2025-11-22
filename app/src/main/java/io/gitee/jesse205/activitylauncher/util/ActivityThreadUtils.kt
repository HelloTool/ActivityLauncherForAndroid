@file:SuppressLint("PrivateApi")

package io.gitee.jesse205.activitylauncher.util

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.IBinder
import java.lang.reflect.Field
import java.lang.reflect.Method

class ActivityThreadReflector(val instance: Any) {

    val mAppThread get() = mAppThreadField.get(instance)!!

    @Suppress("UNCHECKED_CAST")
    val mActivities get() = mActivitiesField.get(instance) as Map<IBinder, Any>

    companion object {
        val ActivityThread: Class<*> by lazy {
            Class.forName("android.app.ActivityThread")
        }
        val mAppThreadField: Field by lazy {
            ActivityThread.getDeclaredField("mAppThread").apply {
                isAccessible = true
            }
        }
        val mActivitiesField: Field by lazy {
            ActivityThread.getDeclaredField("mActivities").apply {
                isAccessible = true
            }
        }
    }

    class ActivityClientRecordReflector(val instance: Any) {
        val pendingResults get() = pendingResultsField.get(instance) as List<*>?

        @Suppress("UNCHECKED_CAST")
        val pendingIntents get() = pendingIntentsField.get(instance) as List<Intent>?
        val startsNotResumed get() = startsNotResumedField.get(instance) as Boolean
        val createdConfig get() = createdConfigField.get(instance) as Configuration?

        companion object {
            val ActivityClientRecord: Class<*> by lazy {
                Class.forName("android.app.ActivityThread\$ActivityClientRecord")
            }
            val pendingResultsField: Field by lazy {
                ActivityClientRecord.getDeclaredField("pendingResults").apply {
                    isAccessible = true
                }
            }

            val pendingIntentsField: Field by lazy {
                ActivityClientRecord.getDeclaredField("pendingIntents").apply {
                    isAccessible = true
                }
            }

            val startsNotResumedField: Field by lazy {
                ActivityClientRecord.getDeclaredField("startsNotResumed").apply {
                    isAccessible = true
                }
            }

            val createdConfigField: Field by lazy {
                ActivityClientRecord.getDeclaredField("createdConfig").apply {
                    isAccessible = true
                }
            }
        }
    }

    class ApplicationThreadReflector(val instance: Any) {

        fun scheduleRelaunchActivity(
            token: IBinder,
            pendingResults: List<*>?,
            pendingIntents: List<Intent>?,
            configChanges: Int,
            notResumed: Boolean,
            config: Configuration?
        ) {
            scheduleRelaunchActivity.invoke(
                instance,
                token,
                pendingResults,
                pendingIntents,
                configChanges,
                notResumed,
                config
            )
        }

        companion object {
            val ApplicationThread: Class<*> by lazy {
                Class.forName("android.app.ActivityThread\$ApplicationThread")
            }

            val scheduleRelaunchActivity: Method by lazy {
                ApplicationThread.getDeclaredMethod(
                    "scheduleRelaunchActivity",
                    IBinder::class.java,
                    List::class.java,  // List<ResultInfo>
                    List::class.java,  // List<Intent>
                    Int::class.javaPrimitiveType,
                    Boolean::class.javaPrimitiveType,
                    Configuration::class.java
                ).apply {
                    isAccessible = true
                }
            }
        }
    }
}