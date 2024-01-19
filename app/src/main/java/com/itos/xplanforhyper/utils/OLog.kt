package com.itos.xplanforhyper.utils

import android.util.Log
import com.itos.xplanforhyper.BuildConfig

object OLog {
    fun i(tag: String, msg: String) {
        if (BuildConfig.DEBUG) Log.i("[调试信息]$tag", msg)
    }
    fun e(tag:String, t: Throwable) = Log.e("[错误]$tag", t.stackTraceToString())
}