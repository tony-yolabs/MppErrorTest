package io.yolabs.mpperrortest

class Greeting {
    fun greeting(): String {
        return "Hello, ${Platform().platform}!"
    }
}