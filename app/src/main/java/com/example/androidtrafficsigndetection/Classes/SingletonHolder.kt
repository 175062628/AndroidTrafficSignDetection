package com.example.androidtrafficsigndetection.Classes

open class SingletonHolder<out T : Any, in A>(creator: (A) -> T) {
    @Volatile private var instance: T? = null
    private val creator = creator

    fun getInstance(arg: A): T {
        return instance ?: synchronized(this) {
            instance ?: creator(arg).also { instance = it }
        }
    }
}