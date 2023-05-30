package com.rodev.alltodo.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.fragment.app.Fragment
import com.rodev.alltodo.R
import com.rodev.alltodo.data.DataAccess
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


fun Date?.formatToString(): String {
    if (this == null) {
        return ""
    }

    return SimpleDateFormat
        .getDateInstance(DateFormat.MEDIUM, Locale.forLanguageTag("ru"))
        .format(this)
}

fun Context.showEmptyFieldsMessage() {
    Toast.makeText(this, R.string.fill_fields, LENGTH_LONG).show()
}

fun Context.replaceArg(msgId: Int, arg: String): String {
    return getString(msgId).replace("%s", arg)
}

fun Context.getImageFromUri(uri: Uri?): Bitmap? {
    if (uri == null) return null

    return ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireNotNull(contentResolver), uri))
}

fun Fragment.getImageFromUri(uri: Uri?): Bitmap? {
    return requireContext().getImageFromUri(uri)
}

fun Fragment.replaceArg(msgId: Int, arg: String): String {
    return requireContext().replaceArg(msgId, arg)
}