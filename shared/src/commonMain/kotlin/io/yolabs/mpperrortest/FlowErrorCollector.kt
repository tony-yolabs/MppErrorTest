package io.yolabs.mpperrortest

import io.yolabs.dispatch.DefaultCoroutineScopeImpl
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class FlowErrorCollectorException: Exception {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}

class FlowErrorCollector {
    private val simpleFlow = flowOf(1, 2, 3)
    private val exceptionFlow = flowOf(1, 2, 3).onEach {
        if (it == 2) {
            throw FlowErrorEmitterException("Not number 2!")
        }
    }

    private val handler = CoroutineExceptionHandler { context, exception ->
        println("FlowErrorCollector Collector got $exception in context $context")
    }

    @Throws(*[FlowErrorEmitterException::class, FlowErrorCollectorException::class, CancellationException::class])
    fun throwExceptionBeforeLaunch()  {
        val scope = DefaultCoroutineScopeImpl()
        throw FlowErrorCollectorException("noThrowExceptionBeforeLaunch")
        scope.launch(handler) {
            simpleFlow.collect {
                println("value: $it")
            }
        }
    }

    fun noThrowExceptionBeforeCollect()  {
        val scope = DefaultCoroutineScopeImpl()
        scope.launch(handler) {
            throw FlowErrorCollectorException("noThrowExceptionBeforeCollect")
            simpleFlow.collect {
                println("value: $it")
            }
        }
    }

    fun noThrowExceptionDuringCollect()  {
        val scope = DefaultCoroutineScopeImpl()
        scope.launch {
            simpleFlow.collect {
                println("value: $it")
                throw FlowErrorCollectorException("noThrowExceptionDuringCollect")
            }
        }
    }

    fun noThrowExceptionAfterCollect()  {
        val scope = DefaultCoroutineScopeImpl()
        scope.launch {
            simpleFlow.collect {
                println("value: $it")
            }
            throw FlowErrorCollectorException("noThrowExceptionAfterCollect")
        }
    }

    @Throws(*[FlowErrorEmitterException::class, FlowErrorCollectorException::class, CancellationException::class])
    suspend fun throwExceptionBeforeCollectSuspend()  {
        throw FlowErrorCollectorException("throwExceptionBeforeCollectSuspend")
        simpleFlow.collect {
            println("value: $it")
        }
    }

    @Throws(*[FlowErrorEmitterException::class, FlowErrorCollectorException::class, CancellationException::class])
    suspend fun throwExceptionDuringCollectSuspend()  {
        simpleFlow.collect {
            println("value: $it")
            throw FlowErrorCollectorException("throwExceptionDuringCollectSuspend")
        }
    }

    @Throws(*[FlowErrorEmitterException::class, FlowErrorCollectorException::class, CancellationException::class])
    suspend fun throwExceptionAfterCollectSuspend()  {
        simpleFlow.collect {
            println("value: $it")
            throw FlowErrorCollectorException("throwExceptionFromFlowDuringCollectSuspend")
        }
    }

    @Throws(*[FlowErrorEmitterException::class, FlowErrorCollectorException::class, CancellationException::class])
    suspend fun throwExceptionFromFlowDuringCollectSuspend()  {
        exceptionFlow.collect {
            println("value: $it")
        }
        throw FlowErrorCollectorException("throwExceptionAfterCollectSuspend")
    }
}
