package io.yolabs.mpperrortest

class SimpleCustomException: Exception {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}

class SimpleInnerException: Exception {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}

class SimpleError {
    fun noThrow() = true

    @Throws(IllegalStateException::class)
    fun baseThrow(): Boolean {
        throw IllegalStateException("SimpleError: baseThrow")
        return false
    }

    @Throws(SimpleCustomException::class)
    fun customThrow(): Boolean {
        throw SimpleCustomException("SimpleError: customThrow")
        return false
    }

    @Throws(SimpleCustomException::class)
    fun customThrowNested(): Boolean {
        try {
            throw SimpleInnerException("Inner: customThrowNested")
        } catch (e: Exception) {
            throw SimpleCustomException("Inner: customThrowNested", e)
        }

        return false
    }
}
