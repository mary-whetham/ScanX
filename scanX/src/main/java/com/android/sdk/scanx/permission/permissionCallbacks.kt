package com.asgl.sdk.common.permission

interface PermissionCallbacks {

    /**
     * Will be invoked if all permissions are granted.
     */
    fun onGranted()

    /**
     * Will be invoked if rationale message should be shown.
     *
     * @param permissionRequest use this to retry permission request
     */
    fun onShowRationale(permissionRequest: PermissionRequest)

    /**
     * Will be invoked if any permission is denied.
     *
     * @param permissions list of permissions user denied
     */
    fun onDenied(permissions: List<String>)
}

/**
 * DSL implementation for [PermissionCallbacks].
 */
class PermissionCallbacksDSL : PermissionCallbacks {

    private var onGranted: () -> Unit = {}
    private var onDenied: (permissions: List<String>) -> Unit = {}
    private var onShowRational: (permissionRequest: PermissionRequest) -> Unit = {}

    fun onGranted(func: () -> Unit) {
        onGranted = func
    }

    fun onDenied(func: (permissions: List<String>) -> Unit) {
        onDenied = func
    }

    fun onShowRational(func: (permissionRequest: PermissionRequest) -> Unit) {
        onShowRational = func
    }

    override fun onGranted() {
        onGranted.invoke()
    }

    override fun onShowRationale(permissionRequest: PermissionRequest) {
        onShowRational.invoke(permissionRequest)
    }

    override fun onDenied(permissions: List<String>) {
        onDenied.invoke(permissions)
    }
}