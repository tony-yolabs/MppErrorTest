package io.yolabs.mpperrortest
import io.yolabs.dispatch.DefaultCoroutineScopeImpl
import kotlinx.coroutines.*

class AsyncErrorCustomException: Exception {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}

class AsyncErrorCustomException2: Exception {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}

class AsyncError {
    private val handler = CoroutineExceptionHandler { context, exception ->
        println("AsyncError Handler got $exception in context $context")
    }

    // region BASE
    // a basic completion
    suspend fun noThrow(): Boolean  {
        val scope = DefaultCoroutineScopeImpl()
        val defer = scope.async {
            true
        }
        return defer.await()
    }

    @Throws(*[AsyncErrorCustomException::class, CancellationException::class])
    suspend fun throwAsync(): Boolean  {
        val scope = DefaultCoroutineScopeImpl()
        val defer = scope.async {
            throw AsyncErrorCustomException("throwAsync")
            //true
        }
        defer.await()
    }

    // note - throw happens in await ... not async
    //   so in this examples AsyncErrorCustomException2 is exposed as it is thrown first in the execution path
    @Throws(*[AsyncErrorCustomException::class, AsyncErrorCustomException2::class, CancellationException::class])
    suspend fun throwAsync2(): Boolean  {
        val scope = DefaultCoroutineScopeImpl()
        val deferred = scope.async {
            throw AsyncErrorCustomException("throwAsync")
            //true
        }
        throw AsyncErrorCustomException2("throwAsync2")
        deferred.await()
    }

    // note - throw happens in await ... not async
    //   so in this examples AsyncErrorCustomException2 is exposed as it is thrown first in the execution path
    //   This is unaffected by the  presence of a CoroutineExceptionHandler
    @Throws(*[AsyncErrorCustomException::class, AsyncErrorCustomException2::class, CancellationException::class])
    suspend fun throwAsync2WithHandler(): Boolean  {
        val scope = DefaultCoroutineScopeImpl()
        val deferred = scope.async(handler) {
            throw AsyncErrorCustomException("throwAsync")
            //true
        }
        throw AsyncErrorCustomException2("throwAsync2")
        deferred.await()
    }
    // endregion
}
