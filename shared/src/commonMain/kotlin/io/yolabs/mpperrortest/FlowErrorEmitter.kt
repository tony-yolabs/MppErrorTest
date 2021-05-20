package io.yolabs.mpperrortest

import io.yolabs.dispatch.DefaultCoroutineScopeImpl
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class FlowErrorEmitterException: Exception {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}
class FlowErrorEmitter {
    private val handler = CoroutineExceptionHandler { context, exception ->
        println("FlowErrorCollector Emitter got $exception in context $context")
    }

    private val simpleFlow = flowOf(1, 2, 3)
    private val exceptionFlow = flowOf(4, 5, 6).onEach {
        if (it == 5) {
            throw FlowErrorEmitterException("Not number 5!")
        }
    }

    // good flow
    fun noThrowSimple()  {
        val scope = DefaultCoroutineScopeImpl()
        scope.launch {
            simpleFlow.collect {
                println("value: $it")
            }
        }
    }

    // flow that throws exception
    fun noThrowLaunch()  {
        val scope = DefaultCoroutineScopeImpl()
        scope.launch {
            exceptionFlow.collect {
                println("value: $it")
            }
        }
    }

    // flow accessed in suspend that throws exception
    @Throws(*[FlowErrorEmitterException::class, CancellationException::class])
    suspend fun throwLaunchSuspend()  {
        exceptionFlow.collect {
            println("value: $it")
        }
    }

    // rethrown before terminal
    fun noThrowLaunchRethrowCollect()  {
        val scope = DefaultCoroutineScopeImpl()
        scope.launch {
            exceptionFlow.catch { e ->
                throw(e)
            }.collect {
                println("value: $it")
            }
        }
    }

    // rethrown before terminal
    fun noThrowLaunchRethrowCollectWithHandler()  {
        val scope = DefaultCoroutineScopeImpl()
        scope.launch(handler) {
            exceptionFlow.catch { e ->
                throw(e)
            }.collect {
                println("value: $it")
            }
        }
    }

    // rethrown after terminal
    fun noThrowLaunchRethrowOnEach()  {
        val scope = DefaultCoroutineScopeImpl()
        scope.launch {
            exceptionFlow.onEach {
                println("value: $it")
            }.catch { e ->
                println("Inline caught $e")
                throw e
            }.collect()
        }
    }

    // rethrown after terminal
    fun noThrowLaunchRethrowOnEachWithHandler()  {
        val scope = DefaultCoroutineScopeImpl()
        scope.launch(handler) {
            exceptionFlow.onEach {
                println("value: $it")
            }.catch { e ->
                println("Inline caught $e")
                throw e
            }.collect()
        }
    }

    @Throws(*[FlowErrorEmitterException::class, FlowErrorCollectorException::class, CancellationException::class])
    suspend fun throwSuspendRethrowOnEachWithHandler()  {
        exceptionFlow.onEach {
            println("value: $it")
        }.catch { e ->
            println("Inline caught $e")
            throw e
        }.collect()
    }
}


// https://elizarov.medium.com/exceptions-in-kotlin-flows-b59643c940fb
// Every flow implementation has to ensure exception transparency — a downstream exception must always be propagated to the collector⁵.
