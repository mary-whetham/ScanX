package com.android.sdk.scanx.bus

import android.util.SparseArray
import androidx.annotation.IntDef
import androidx.annotation.NonNull
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer


/**
 * Created by cbutani on 1/22/2020.
 * Copyright (c) 2020 ANGUS SYSTEMS
 **/
object LiveDataBus {
    private val sSubjectMap = SparseArray<EventLiveData>()
    /**Channel 1 is dedicated for listening network state in fragments*/
    const val CHANNEL_ONE = 1
    /**Channel 2 is dedicated for listening network state in Activities*/
    const val CHANNEL_TWO = 2
    /**Channel 3 is dedicated for listening auto-logout*/
    const val CHANNEL_THREE = 3
    /**Channel 4 is dedicated for listening sales demo flag on sign in screen multi-tap*/
    const val CHANNEL_FOUR = 4
    /**Channel 5 is dedicated for listening bluetooth message response*/
    const val CHANNEL_FIVE = 5
    /**Channel 6 is dedicated for listening bluetooth connection intent*/
    const val CHANNEL_SIX = 6
    /**Channel 7 is dedicated for listening permissions from requester object change*/
    const val CHANNEL_SEVEN = 7
    /**Channel 8 is dedicated for listening bluetooth broadcast receiver*/
    const val CHANNEL_EIGHT = 8
    /**Channel 9 is dedicated for listening QR code result*/
    const val CHANNEL_NINE = 9
    /**Channel 10 is dedicated for listening bluetooth device MAC address*/
    const val CHANNEL_TEN = 10
    /**Channel 11 is dedicated for listening play store update*/
    const val CHANNEL_ELEVEN = 11
  
    /**
     * Get the live data or create it if it's not already in memory.
     */
    @NonNull
    private fun getLiveData(@Channel channelType: Int): EventLiveData {
        var liveData = sSubjectMap[channelType]
        if (liveData == null) {
            liveData = EventLiveData(channelType)
            sSubjectMap.put(channelType, liveData)
        }
        return liveData
    }

    /**
     * Subscribe to the specified subject and listen for updates on that subject.
     */
    fun subscribe(@Channel channel: Int, @NonNull lifecycle: LifecycleOwner?, @NonNull action: Observer<Any?>) {
        getLiveData(channel).observe(lifecycle!!, action)
    }

    /**
     * Subscribe to the specified subject and listen for updates on that subject just once.
     */
    fun subscribeOnce(@Channel channel: Int, @NonNull lifecycle: LifecycleOwner?, @NonNull action: Observer<Any?>) {
        getLiveData(channel).observeOnce(lifecycle!!, action)
    }

    //An extension method to the live data observer where we can set the value to be observed once
    fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }

    /**
     * Subscribe to the specified channel value.
     */
    fun subscribeChannelValue(@Channel channel: Int) : Any? {
        return getLiveData(channel).value
    }

    /**
     * Removes this subject when it has no observers.
     */
    fun unregister(@Channel channel: Int) {
        sSubjectMap.remove(channel)
    }

    /**
     * Publish an object to the specified subject for all subscribers of that subject.
     */
    fun publish(@Channel channel: Int, @NonNull message: Any?) {
        getLiveData(channel).update(message)
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(CHANNEL_ONE, CHANNEL_TWO, CHANNEL_THREE, CHANNEL_FOUR, CHANNEL_FIVE, CHANNEL_SIX, CHANNEL_SEVEN, CHANNEL_EIGHT, CHANNEL_NINE, CHANNEL_TEN, CHANNEL_ELEVEN)
    internal annotation class Channel
}