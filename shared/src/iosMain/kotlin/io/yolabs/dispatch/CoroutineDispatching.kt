package io.yolabs.dispatch

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Delay
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.SupervisorJob
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.NSEC_PER_MSEC
import platform.darwin.dispatch_after
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_t
import platform.darwin.dispatch_time
import kotlin.coroutines.CoroutineContext

// NOTE: These two dispatchers will use the same underlying GCD queue until the advent of official
// multi-threaded dispatchers for iOS.

@OptIn(InternalCoroutinesApi::class)
actual val DefaultDispatcher: CoroutineDispatcher
    get() = MainQueueDispatcher

@OptIn(InternalCoroutinesApi::class)
actual val MainDispatcher: CoroutineDispatcher
    get() = MainQueueDispatcher

@OptIn(InternalCoroutinesApi::class)
@ThreadLocal
object MainQueueScope : CoroutineScope {
    override var coroutineContext: CoroutineContext = MainQueueDispatcher + SupervisorJob()
}

@OptIn(InternalCoroutinesApi::class)
internal object MainQueueDispatcher : GCDDispatcher(dispatch_get_main_queue())

@OptIn(InternalCoroutinesApi::class)
open class GCDDispatcher(private val queue: dispatch_queue_t) : CoroutineDispatcher(), Delay {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        dispatch_async(queue) { block.run() }
    }

    @ExperimentalUnsignedTypes
    override fun scheduleResumeAfterDelay(
        timeMillis: Long,
        continuation: CancellableContinuation<Unit>
    ) {
        val time = dispatch_time(DISPATCH_TIME_NOW, (timeMillis * NSEC_PER_MSEC.toLong()))
        dispatch_after(time, queue) {
            with(continuation) { resumeUndispatched(Unit) }
        }
    }
}
