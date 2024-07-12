package com.daisydev.daisy.util

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

val dateFormat: DateFormat = SimpleDateFormat.getDateInstance()

/**
 * Function that formats a Date object to a String in local format
 */
fun formatDate(date: Date): String {
    return dateFormat.format(date)
}