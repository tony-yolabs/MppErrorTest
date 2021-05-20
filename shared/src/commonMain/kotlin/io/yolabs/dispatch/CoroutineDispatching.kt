package io.yolabs.dispatch

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

/** Dispatcher used by default. */
internal expect val DefaultDispatcher: CoroutineDispatcher

/** Dispatcher used for UI operations. */
internal expect val MainDispatcher: CoroutineDispatcher

/** A [CoroutineScope] which we know uses the default dispatcher. */
interface DefaultCoroutineScope : CoroutineScope

/** A [CoroutineScope] which we know uses the main dispatcher. */
interface MainCoroutineScope : CoroutineScope

/** A [CoroutineScope] which we know uses the default dispatcher and a SupervisorJob(). */
interface SupervisorCoroutineScope : CoroutineScope

/** @see DefaultCoroutineScope */
class DefaultCoroutineScopeImpl(private val dispatcher: CoroutineDispatcher = DefaultDispatcher) :
    DefaultCoroutineScope {

    constructor() : this(DefaultDispatcher)

    override val coroutineContext: CoroutineContext
        get() = Job() + dispatcher
}

/** @see MainCoroutineScope */
class MainCoroutineScopeImpl(private val dispatcher: CoroutineDispatcher = MainDispatcher) :
    MainCoroutineScope {

    constructor() : this(MainDispatcher)

    override val coroutineContext: CoroutineContext
        get() = Job() + dispatcher
}

/** @see SupervisorCoroutineScope */
class SupervisorCoroutineScopeImpl(private val dispatcher: CoroutineDispatcher = DefaultDispatcher) :
    SupervisorCoroutineScope {

    constructor() : this(DefaultDispatcher)

    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + dispatcher
}

/** A test [DefaultCoroutineScope]. The test dispatcher context should be provided. */
class TestDefaultCoroutineScopeImpl(override val coroutineContext: CoroutineContext) :
    DefaultCoroutineScope

/** A test [MainCoroutineScope]. The test dispatcher context should be provided. */
class TestMainCoroutineScopeImpl(override val coroutineContext: CoroutineContext) :
    MainCoroutineScope

/** A test [SupervisorCoroutineScope]. The test dispatcher context should be provided. */
class TestSupervisorCoroutineScopeImpl(override val coroutineContext: CoroutineContext) :
    SupervisorCoroutineScope
