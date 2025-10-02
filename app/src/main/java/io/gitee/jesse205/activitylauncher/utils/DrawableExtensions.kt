package io.gitee.jesse205.activitylauncher.utils

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

fun Drawable.scale(resources: Resources, destWidth: Int, destHeight: Int): Drawable {
    return if (this is BitmapDrawable) {
        Bitmap.createScaledBitmap(this.bitmap, destWidth, destHeight, true)
    } else {
        Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888).also {
            val canvas = Canvas(it)
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
        }
    }.let { BitmapDrawable(resources, it) }
}