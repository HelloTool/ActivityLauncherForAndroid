package io.gitee.jesse205.activitylauncher.util

import android.content.res.Configuration

val Configuration.screenLayoutSize get() = screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK

val Configuration.isNightModeCompat get() = (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
