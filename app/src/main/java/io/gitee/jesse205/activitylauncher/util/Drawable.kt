package io.gitee.jesse205.activitylauncher.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

fun Drawable.scaleToFit(containerWidth: Int, containerHeight: Int): Drawable {
    val widthRatio = containerWidth.toFloat() / intrinsicWidth
    val heightRatio = containerHeight.toFloat() / intrinsicHeight
    val scale = minOf(widthRatio, heightRatio)

    val scaledWidth = (intrinsicWidth * scale).toInt()
    val scaledHeight = (intrinsicHeight * scale).toInt()

    val bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    setBounds(0, 0, scaledWidth, scaledHeight)
    draw(canvas)

    return BitmapDrawable(null, bitmap)
}