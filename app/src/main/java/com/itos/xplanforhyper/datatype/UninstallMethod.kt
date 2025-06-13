package com.itos.xplanforhyper.datatype

enum class UninstallMethod(val value: Int, val displayName: String) {
    PM(0, "pm命令"),
    PM_ENHANCED(1, "pm命令（增强）"),
    SERVICE_CALL_S(2, "service call（Android 12）"),
    SERVICE_CALL_T(3, "service call（Android 13）");

    companion object {
        fun fromValue(value: Int): UninstallMethod {
            return entries.find { it.value == value } ?: PM_ENHANCED
        }
    }
} 