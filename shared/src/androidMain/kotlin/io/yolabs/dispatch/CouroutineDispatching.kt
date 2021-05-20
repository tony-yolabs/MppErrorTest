package io.yolabs.dispatch

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual val DefaultDispatcher: CoroutineDispatcher
    get() = Dispatchers.Default

actual val MainDispatcher: CoroutineDispatcher
    get() = Dispatchers.Main
