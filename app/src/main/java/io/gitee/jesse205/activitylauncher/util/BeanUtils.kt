package io.gitee.jesse205.activitylauncher.util

fun Any.copyFieldsTo(dest: Any, fields: Array<String>) {
    val fromClass = javaClass
    val destClass = dest.javaClass
    fields.forEach {
        val fromField = fromClass.getDeclaredField(it)
        val destField = destClass.getDeclaredField(it)
        fromField.isAccessible = true
        destField.isAccessible = true
        destField.set(dest, fromField.get(this))
    }
}