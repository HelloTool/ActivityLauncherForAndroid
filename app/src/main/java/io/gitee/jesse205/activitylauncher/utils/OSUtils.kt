package io.gitee.jesse205.activitylauncher.utils

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

val isEmui by lazy {
    runCatching { Class.forName("com.huawei.android.os.BuildEx") }.isSuccess
}

@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.HONEYCOMB)
val isMenuSearchBarSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB

// TODO: 判断MagicUI/MagicOS并启用Magic主题

@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
val isNavigationGestureSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q