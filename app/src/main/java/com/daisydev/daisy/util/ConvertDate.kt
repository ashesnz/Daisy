package com.daisydev.daisy.util

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date

/**
 * Converts a date in ISO 8601 format to a Date object
 * @param dateString Date in ISO 8601 format
 * @return Date object
 */
fun convertStringToDate(dateString: String): Date {
    val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    val offsetDateTime = OffsetDateTime.parse(dateString, formatter)
    val utcDateTime = offsetDateTime.withOffsetSameInstant(ZoneOffset.UTC)

    val cmdxZoneId = ZoneId.of("Pacific/Auckland")
    val cdmxDateTime = LocalDateTime.ofInstant(utcDateTime.toInstant(), cmdxZoneId)
    val cdmxOffsetDateTime =
        OffsetDateTime.of(cdmxDateTime, cmdxZoneId.rules.getOffset(cdmxDateTime))

    return Date.from(cdmxOffsetDateTime.toInstant())
}