package com.android.sdk.scanx.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by cbutani on 7/13/2020.
 * Copyright (c) 2020 ANGUS SYSTEMS
 **/

object CoroutineUseCase {

    fun main(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.Main).launch {
            work()
        }

    fun io(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.IO).launch {
            work()
        }

    fun default(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.Default).launch {
            work()
        }

    suspend fun withContextIo(work: (() -> Boolean)): Boolean {
        return withContext(Dispatchers.IO) {
            work()
        }
    }
}