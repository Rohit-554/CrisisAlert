package io.jadu.crisisprotect.feature.common

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import java.net.URI
import java.text.DateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

fun Instant.toLocalizedDateTime(): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault())
        .format(Date.from(this))

fun Context.openSafeWebUrl(url: String): Boolean {
    if (!isSafeWebUrl(url)) return false
    val uri = url.toUri()
    val intent = Intent(Intent.ACTION_VIEW, uri)
    if (intent.resolveActivity(packageManager) == null) return false
    return runCatching { startActivity(intent) }.isSuccess
}

fun isSafeWebUrl(url: String): Boolean =
    runCatching { URI(url).scheme?.lowercase(Locale.ROOT) in setOf("http", "https") }.getOrDefault(false)
