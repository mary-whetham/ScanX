package com.android.sdk.scanx.bus

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

/**
 * Created by cbutani on 1/22/2020.
 * Copyright (c) 2020 ANGUS SYSTEMS
 **/
class EventLiveData(@LiveDataBus.Channel channelType: Int) : LiveData<Any?>() {
    private val channel: Int = channelType

    fun update(data: Any?) {
        postValue(data)
    }

    override fun removeObserver(observer: Observer<in Any?>) {
        super.removeObserver(observer)
        if (!hasObservers()) {
            LiveDataBus.unregister(channel)
        }
    }
}