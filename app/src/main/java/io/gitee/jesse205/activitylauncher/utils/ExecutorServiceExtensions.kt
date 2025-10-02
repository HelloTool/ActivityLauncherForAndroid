package io.gitee.jesse205.activitylauncher.utils

import android.os.Handler
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

fun <T> ExecutorService.submitWithCheckAndCallback(
    handler: Handler,
    check: () -> Boolean,
    task: () -> T,
    callback: (result:T) -> Unit
): Future<*>? {
    return submit {
        if (!check()) {
            return@submit
        }
        val result = task()
        if (!check()) {
            return@submit
        }
        handler.post{
            if (!check()) {
                return@post
            }
            callback(result)
        }
    }
}