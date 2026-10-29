package com.zaacoo.katalog.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val rupiahFormat = NumberFormat.getIntegerInstance(Locale("id", "ID"))
private val dateOutput = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale("id", "ID"))
private val dateInputNoZone = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US)

fun Int.toRupiah(): String = "Rp" + rupiahFormat.format(this)

fun String?.orDash(): String = if (this.isNullOrBlank()) "-" else this

fun formatDateTime(raw: String?): String {
    if (raw.isNullOrBlank()) return "-"

    val parsed = runCatching { OffsetDateTime.parse(raw).toLocalDateTime() }.getOrNull()
        ?: runCatching { LocalDateTime.parse(raw, dateInputNoZone) }.getOrNull()

    return parsed?.format(dateOutput) ?: raw
}

fun formatDate(raw: String?): String {
    if (raw.isNullOrBlank()) return "-"
    val parsed = runCatching { OffsetDateTime.parse(raw).toLocalDateTime() }.getOrNull()
        ?: runCatching { LocalDateTime.parse(raw, dateInputNoZone) }.getOrNull()
    return parsed?.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale("id", "ID"))) ?: raw
}

fun openUrl(context: Context, url: String?) {
    if (url.isNullOrBlank()) {
        Toast.makeText(context, "Tautan belum tersedia.", Toast.LENGTH_SHORT).show()
        return
    }

    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }.onFailure {
        Toast.makeText(context, "Tidak bisa membuka tautan.", Toast.LENGTH_SHORT).show()
    }
}

fun phoneDigits(phone: String): String = phone.filter { it.isDigit() }

fun isValidPhone(phone: String): Boolean = phoneDigits(phone).length >= 8
