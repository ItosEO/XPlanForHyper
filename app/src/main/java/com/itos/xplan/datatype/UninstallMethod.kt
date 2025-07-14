package com.itos.xplan.datatype

enum class UninstallMethod(val value: Int, val displayName: String) {
    PM(0, "pm命令"),
    PM_ENHANCED(1, "pm命令（增强）"),
    SERVICE_CALL_S(2, "service call（Android 12）"),
    SERVICE_CALL_T(3, "service call（Android 13）"),
    SERVICE_CALL_U(4, "service call（Android 14）"),
    SERVICE_CALL_V(5, "service call（Android 15）");

    companion object {
        fun fromValue(value: Int): UninstallMethod {
            return entries.find { it.value == value } ?: PM_ENHANCED
        }
    }
} 