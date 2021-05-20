package io.yolabs.mpperrortest
import io.yolabs.dispatch.DefaultCoroutineScopeImpl
import kotlinx.coroutines.*

class LaunchErrorCustomException: Exception {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}

// Examples using Coroutine.launch()
// Demonstrates that:
// - *none* of these examples expose an exception to the UI client
// - uncaught exceptions with no top level coroutine exception handler will crash the app
// - behavior of exceptions in some coroutine trees (e.g sibling coroutines) are not intuitive
//   (at least not to me) ... see the sibling examples
class LaunchError {
    private val handler = CoroutineExceptionHandler { context, exception ->
        println("LaunchError Handler got $exception in context $context")
    }

    private val handler2 = CoroutineExceptionHandler { context, exception ->
        println("LaunchError Handler2 got $exception in context $context")
    }

    private val handler3 = CoroutineExceptionHandler { context, exception ->
        println("LaunchError Handler3 got $exception in context $context")
    }

    // region BASE
    // a basic completion
    fun noThrow(): Boolean  {
        val scope = DefaultCoroutineScopeImpl()
        scope.launch {
            return@launch
        }
        return true
    }

    // no top level coroutine handler - so this would crash the app
    @Throws(IllegalStateException::class)
    fun baseThrowNoHandlerUncaught(): Boolean  {
        val scope = DefaultCoroutineScopeImpl()
        scope.launch {
            throw IllegalStateException("LaunchError: baseThrowNoHandlerUncaught")
        }
        return true
    }

    // proper level coroutine handler - so this would *not* crash the app
    // but=, alas, still does not expose the (standard) exception to UI
    @Throws(IllegalStateException::class)
    fun baseThrowWithHandler(): Boolean  {
        val scope = DefaultCoroutineScopeImpl()
        scope.launch(handler) {
            throw IllegalStateException("LaunchError: baseThrowWithHandler")
        }
        return true
    }
    // endregion

    // region INTRA
    // caught inside launch
    fun intraCaught(): Boolean  {
        val scope = DefaultCoroutineScopeImpl()
        scope.launch {
            try {
                throw IllegalStateException("LaunchError: intraCaught")
            } catch (e: Exception) {
                print("Caught properly: $e")
            }
        }
        return true
    }

    // caught inside launch ... rethrown
    fun intraUncaughtRethrow(): Boolean  {
        val scope = DefaultCoroutineScopeImpl()
        scope.launch {
            try {
                throw IllegalStateException("LaunchError: intraUncaughtRethrow")
            } catch (e: Exception) {
                print("Caught properly: $e")
                throw e
            }
        }
        return true
    }

    // caught inside launch ... rethrown with handler
    fun intraUncaughtRethrowHandler(): Boolean  {
        val scope = DefaultCoroutineScopeImpl()
        scope.launch(handler) {
            try {
                throw IllegalStateException("LaunchError: intraUncaughtRethrowHandler")
            } catch (e: Exception) {
                print("Caught properly: $e")
                throw e
            }
        }
        return true
    }

    // throw in an inner coroutine ... not caught
    fun intraNestingErrorUncaught(): Boolean  {
        val scope = DefaultCoroutineScopeImpl()
        scope.launch {
            try {
                launch {
                    throw IllegalStateException("LaunchError: intraNestingErrorUncaught")
                }
            } catch (e: Exception) {
                print("*NOT* Caught properly: $e")
            }
        }
        return true
    }
    // endregion

    // region NESTED
    // proper level coroutine handler - so this would *not* crash the app
    // but does not expose the (custom) exception to UI
    @Throws(LaunchErrorCustomException::class)
    fun nestedThrowNoHandlerUncaught(): Boolean  {
        val scope = DefaultCoroutineScopeImpl()
        scope.launch {
            launch {
                throw LaunchErrorCustomException("LaunchError: nestedThrowNoHandlerUncaught")
            }
        }
        return true
    }

    // outer coroutine has handler, but inner coroutine does not ... so this would crash the app
    @Throws(LaunchErrorCustomException::class)
    fun nestedThrowWithTopHandlerCaught(): Boolean  {
        val scope = DefaultCoroutineScopeImpl()
        scope.launch(handler) {
            launch {
                throw LaunchErrorCustomException("LaunchError: nestedThrowWithTopHandlerCaught")
            }
        }
        return true
    }


    @Throws(LaunchErrorCustomException::class)
    fun nestedMultpleWithHandlersCaught(): Boolean  {
        val scope = DefaultCoroutineScopeImpl()
        scope.launch(handler) {
            launch(handler2) {
                launch(handler3) {
                    try {
                        println("before throw")
                        throw LaunchErrorCustomException("LaunchError: nestedMultpleWithHandlersCaught")
                        println("after throw")
                    } catch (e: Exception) {
                        println(e)
                    }
                }
            }
        }
        return true
    }

    // outer coroutine has handler, *no* inner coroutine (it's actually a corouetine in a nested scope)
    // ... so this would crash the app as we have unhandled exception in a top level coroutine
    @Throws(LaunchErrorCustomException::class)
    fun nestedThrowWithTopHandlerUncaught(): Boolean  {
        val scope = DefaultCoroutineScopeImpl()
        scope.launch(handler) {
            // caution ... this creates a separate scope (rather than a nested coroutine)
            scope.launch {
                throw LaunchErrorCustomException("LaunchError: nestedThrowWithTopHandlerUncaught")
            }
        }
        return true
    }

    // outer and inners coroutines have handlers ... so this would *not* crash the app
    @Throws(LaunchErrorCustomException::class)
    fun nestedThrowWithHandlers(): Boolean  {
        val scope = DefaultCoroutineScopeImpl()
        scope.launch(handler) {
            launch(handler) {
                throw LaunchErrorCustomException("LaunchError: nestedThrowWithHandlers")
            }
        }
        return true
    }

    // outer coroutine has no handler, inner coroutine does ... so this will crash the app
    @Throws(LaunchErrorCustomException::class)
    fun nestedThrowWithInnerHandler(): Boolean  {
        val scope = DefaultCoroutineScopeImpl()
        scope.launch() {
            launch(handler) {
                throw LaunchErrorCustomException("LaunchError: nestedThrowWithInnerHandler")
            }
        }
        return true
    }
    // endregion

    // region TREE
    // Handling of exceptions in a sibling coroutine

    // no exception
    suspend fun treeCoroutinesNoHandlers(): Int {
        val scope = DefaultCoroutineScopeImpl()
        var acc = 0
        val jOuter = scope.launch {
            val j1 = launch {
                acc += 1
            }
            val j2 = launch {
                acc += 1
            }
            j1.join()
            j2.join()
        }
        jOuter.join()
        return acc
    }

    // - The inner exception is properly thrown but not handled ...
    // - ... and the function still completes and returns 1
    @Throws(*[LaunchErrorCustomException::class, CancellationException::class])
    suspend fun treeCoroutinesThrowsNoHandlerBeforeUncaught(): Int {
        val scope = DefaultCoroutineScopeImpl()
        var acc = 0
        val jOuter = scope.launch() {
            val j1 = launch() {
                acc += 1
            }
            val j2 = launch() {
                throw LaunchErrorCustomException("LaunchError: treeCoroutinesThrowsNoHandlerBeforeUncaught")
                acc += 1
            }
            j1.join()
            j2.join()
        }
        jOuter.join()
        return acc
    }

    // - The inner exception is properly thrown but and handled ...
    // - ... and the function still completes and returns 1
    @Throws(*[LaunchErrorCustomException::class, CancellationException::class])
    suspend fun treeCoroutinesThrowsWithTopHandlerBeforeCaught(): Int {
        val scope = DefaultCoroutineScopeImpl()
        var acc = 0
        val jOuter = scope.launch(handler) {
            val j1 = launch {
                acc += 1
            }
            val j2 = launch {
                throw LaunchErrorCustomException("LaunchError: treeCoroutinesThrowsWithTopHandlerBeforeCaught")
                acc += 1
            }
            j1.join()
            j2.join()
        }
        jOuter.join()
        return acc
    }

    // - The inner exception is properly thrown and handled ...
    // - ... and the function still completes and returns 1
    @Throws(*[LaunchErrorCustomException::class, CancellationException::class])
    suspend fun treeCoroutinesThrowsWithBothHandlersBefore(): Int {
        val scope = DefaultCoroutineScopeImpl()
        var acc = 0
        val jOuter = scope.launch(handler) {
            val j1 = launch {
                acc += 1
            }
            val j2 = scope.launch(handler) {
                throw LaunchErrorCustomException("LaunchError: treeCoroutinesThrowsWithBothHandlersBefore")
                acc += 1
            }
            j1.join()
            j2.join()
        }
        jOuter.join()
        return acc
    }

    // - The inner exception is properly thrown and handled ...
    // - ... and the function still completes and returns 2
    @Throws(*[LaunchErrorCustomException::class, CancellationException::class])
    suspend fun treeCoroutinesThrowsWithBothHandlersAfter(): Int {
        val scope = DefaultCoroutineScopeImpl()
        var acc = 0
        val jOuter = scope.launch(handler) {
            val j1 = launch() {
                acc += 1
            }
            val j2 = launch(handler) {
                acc += 1
                throw LaunchErrorCustomException("LaunchError: treeCoroutinesThrowsWithBothHandlersAfter")
            }
            j1.join()
            j2.join()
        }
        jOuter.join()

        return acc
    }

    // - The inner exception is properly thrown / bubbled up / handled ...
    // - ... and the function still completes and returns 0
    @Throws(*[LaunchErrorCustomException::class, CancellationException::class, IllegalStateException::class])
    suspend fun treeCoroutinesThrowsWithBothHandlersBeforeCancellation(): Int {
        val scope = DefaultCoroutineScopeImpl()
        var acc = 0
        val jOuter = scope.launch(handler) {
            val j1 = launch(handler) {
                delay(Long.MAX_VALUE)
                acc += 1
            }
            val j2 = launch(handler) {
                throw LaunchErrorCustomException("LaunchError: treeCoroutinesThrowsWithBothHandlersBeforeCancellation")
                acc += 1
            }
        }
        jOuter.join()
        return acc
    }

    // - The inner exception is properly thrown / bubbled up / handled ...
    // - ... and the function still completes and returns 1
    @Throws(*[LaunchErrorCustomException::class, CancellationException::class, IllegalStateException::class])
    suspend fun treeCoroutinesThrowsWithBothHandlersAfterCancellation(): Int {
        val scope = DefaultCoroutineScopeImpl()
        var acc = 0
        val jOuter = scope.launch(handler) {
            val j1 = launch(handler) {
                delay(Long.MAX_VALUE)
                acc += 1
            }
            val j2 = launch(handler) {
                acc += 1
                throw LaunchErrorCustomException("LaunchError: treeCoroutinesThrowsWithBothHandlersAfterCancellation")
            }
        }
        jOuter.join()
        return acc
    }

    // - Explicit job cancellation with downstream propagation
    @Throws(*[LaunchErrorCustomException::class, CancellationException::class, IllegalStateException::class])
    suspend fun treeCoroutinesExplicitCancellation(): Int {
        val scope = DefaultCoroutineScopeImpl()
        var acc = 0
        val jOuter = scope.launch(handler) {
            launch {
                yield()
                delay(Long.MAX_VALUE)
                acc += 1
            }
            launch {
                yield()
                delay(Long.MAX_VALUE)
                acc += 1
            }
        }
        jOuter.cancelAndJoin()
        return acc
    }
    // endregion

    // region SIBLINGS
    // Handling of exceptions in a sibling coroutine

    // no exception
    suspend fun siblingCoroutinesNoHandlers(): Int {
        val scope = DefaultCoroutineScopeImpl()
        var acc = 0

        val j1 = scope.launch {
            launch {
                acc += 1
            }
            launch {
                acc += 1
            }
        }
        j1.join()
        return acc
    }

    // - The inner exception is properly thrown but not handled ...
    // - ... and the function still completes and returns 1
    @Throws(*[LaunchErrorCustomException::class, CancellationException::class])
    suspend fun siblingCoroutinesThrowsNoHandlerBeforeUncaught(): Int {
        val scope = DefaultCoroutineScopeImpl()
        var acc = 0
        val j1 = scope.launch {
            launch {
                acc += 1
            }
            launch {
                throw LaunchErrorCustomException("LaunchError: siblingCoroutinesThrowsNoHandlerBeforeUncaught")
                acc += 1
            }
        }

        j1.join()
        return acc
    }

    // - The inner exception is properly thrown but not handled ...
    // - ... note the function still completes and returns 2
    @Throws(*[LaunchErrorCustomException::class, CancellationException::class])
    suspend fun siblingCoroutinesThrowsNoHandlerAfterUncaught(): Int {
        val scope = DefaultCoroutineScopeImpl()
        var acc = 0

        val j1 = scope.launch {
            launch {
                acc += 1
            }
            launch {
                acc += 1
                throw LaunchErrorCustomException("LaunchError: siblingCoroutinesThrowsNoHandlerBeforeUncaught")
            }
        }

        j1.join()
        return acc
    }

    // - The inner exception is properly thrown and handled ...
    // - ... and the function still completes and returns 1
    @Throws(*[LaunchErrorCustomException::class, CancellationException::class])
    suspend fun siblingCoroutinesThrowsWithHandlerBefore(): Int {
        val scope = DefaultCoroutineScopeImpl()
        var acc = 0
        val j1 = scope.launch(handler) {
            launch {
                acc += 1
            }
            launch {
                throw LaunchErrorCustomException("LaunchError: siblingCoroutinesThrowsNoHandlerBeforeUncaught")
                acc += 1
            }
        }

        j1.join()
        return acc
    }

    // - The inner exception is properly thrown and handled ...
    // - ... now the function still completes and returns 2
    @Throws(*[LaunchErrorCustomException::class, CancellationException::class])
    suspend fun siblingCoroutinesThrowsWithHandlerAfter(): Int {
        val scope = DefaultCoroutineScopeImpl()
        var acc = 0
        val j1 = scope.launch(handler) {
            launch {
                acc += 1
            }
            launch {
                acc += 1
                throw LaunchErrorCustomException("LaunchError: siblingCoroutinesThrowsNoHandlerBeforeUncaught")
            }
        }

        j1.join()
        return acc
    }
    // endregion
}
