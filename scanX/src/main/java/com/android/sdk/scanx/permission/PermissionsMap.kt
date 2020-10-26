package com.asgl.sdk.common.permission

import java.util.concurrent.atomic.AtomicInteger

internal object PermissionsMap {

    private val atomicInteger = AtomicInteger(100)

    private val map = mutableMapOf<Int, PermissionCallbacks>()

    fun put(callbacks: PermissionCallbacks): Int {
        return atomicInteger.getAndIncrement().also {
            map[it] = callbacks
        }
    }

    fun get(requestCode: Int): PermissionCallbacks? {
        return map[requestCode].also {
            map.remove(requestCode)
        }
    }
}