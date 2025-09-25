package io.gitee.jesse205.activitylauncher.utils

val isEmui by lazy {
    runCatching { Class.forName("com.huawei.android.os.BuildEx") }.isSuccess
}

//TODO: 判断MagicUI/MagicOS并启用Magic主题