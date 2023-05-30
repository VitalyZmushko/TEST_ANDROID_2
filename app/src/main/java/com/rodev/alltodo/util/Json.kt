package com.rodev.alltodo.util

import com.google.gson.GsonBuilder

fun Any.toJson(): String {
    return GsonBuilder().create().toJson(this)
}

fun <T> String.fromJson(clazz: Class<T>): T {
    return GsonBuilder().create().fromJson(this, clazz)
}