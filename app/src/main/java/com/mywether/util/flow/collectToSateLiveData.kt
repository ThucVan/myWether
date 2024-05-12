package com.mywether.util.flow

import android.util.Log
import com.mywether.data.liveData.MutableStateLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart

suspend fun <T> Flow<T>.collectAsSateLiveData(liveData: MutableStateLiveData<T>) {
    this.flowOn(Dispatchers.IO)
        .catch {
            Log.e("thucvan_error", "catch: ${Thread.currentThread().name}")
            Log.e("thucvan_error", "catch: ${it.message}")
            liveData.postError(it.message)
        }
        .onStart {
            liveData.postLoading()
        }.collect {
            liveData.postSuccess(it)
        }
}
