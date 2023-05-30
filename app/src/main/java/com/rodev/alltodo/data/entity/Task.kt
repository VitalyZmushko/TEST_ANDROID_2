package com.rodev.alltodo.data.entity

import java.io.Serializable
import java.util.*

data class Task(
    val id: String,
    var imageUri: String? = null,
    var title: String? = null,
    var description: String? = null,
    var time: Long? = null,
    var createdDate: Date? = null,
): Serializable