package net.mypapit.mobile.callsignview.data.csv

import net.mypapit.mobile.callsignview.data.local.CallsignEntity
import java.io.BufferedReader
import java.io.Reader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * RFC 4180 compliant streaming CSV parser for MYCallsign data.
 * Handles quoted commas, embedded newlines inside quotes, and escaped double quotes ("").
 */
object CallsignCsvParser {

    private val inputDateFormatter = DateTimeFormatter.ofPattern("d/M/yyyy", Locale.ROOT)
    private val isoDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val whitespaceCollapseRegex = Regex("\\s+")

    fun parse(reader: Reader): List<CallsignEntity> {
        val buffered = if (reader is BufferedReader) reader else BufferedReader(reader, 16 * 1024)
        val records = ArrayList<CallsignEntity>(11_000)

        var row = ArrayList<String>(6)
        val field = StringBuilder(64)
        var inQuotes = false
        var afterQuote = false
        var sawData = false
        var isFirstRow = true

        fun flushRow() {
            row.add(field.toString())
            field.setLength(0)
            if (isFirstRow) {
                isFirstRow = false
                if (isHeaderRow(row)) {
                    row = ArrayList(6)
                    return
                }
            }
            toEntity(row)?.let { records.add(it) }
            row = ArrayList(6)
        }

        while (true) {
            val chInt = buffered.read()
            if (chInt == -1) break
            val ch = chInt.toChar()
            sawData = true

            if (inQuotes) {
                if (ch == '"') {
                    // Look ahead for escaped quote ""
                    buffered.mark(1)
                    val nextInt = buffered.read()
                    if (nextInt != -1 && nextInt.toChar() == '"') {
                        field.append('"')
                    } else {
                        if (nextInt != -1) buffered.reset()
                        inQuotes = false
                        afterQuote = true
                    }
                } else {
                    field.append(ch)
                }
            } else {
                when (ch) {
                    '"' -> {
                        if (field.isEmpty() && !afterQuote) {
                            inQuotes = true
                        } else {
                            field.append(ch)
                        }
                    }
                    ',' -> {
                        row.add(field.toString())
                        field.setLength(0)
                        afterQuote = false
                    }
                    '\r' -> {
                        // Ignore CR; LF will flush the row
                        afterQuote = false
                    }
                    '\n' -> {
                        afterQuote = false
                        if (row.isNotEmpty() || field.isNotEmpty()) {
                            flushRow()
                        }
                    }
                    else -> {
                        field.append(ch)
                        afterQuote = false
                    }
                }
            }
        }

        if (sawData && (row.isNotEmpty() || field.isNotEmpty())) {
            flushRow()
        }

        return records
    }

    private fun isHeaderRow(row: List<String>): Boolean {
        if (row.size < 3) return false
        val first = row[0].trim().removePrefix("\uFEFF")
        val third = row[2].trim()
        return first.equals("No.", ignoreCase = true) || third.equals("Call Sign", ignoreCase = true)
    }

    private fun toEntity(row: List<String>): CallsignEntity? {
        if (row.size < 5) return null
        val recordNo = row[0].trim().removePrefix("\uFEFF").toIntOrNull() ?: 0
        val holderRaw = row[1].replace('\r', ' ').replace('\n', ' ')
        val holderName = whitespaceCollapseRegex.replace(holderRaw.trim(), " ")
        val callsign = row[2].trim().uppercase(Locale.ROOT)
        val assignmentNo = row[3].trim()
        val rawExpiry = row[4].trim()

        if (callsign.isBlank() || holderName.isBlank()) return null

        val (isoDate, displayDate) = normalizeDate(rawExpiry)

        return CallsignEntity(
            callsign = callsign,
            recordNo = recordNo,
            holderName = holderName,
            assignmentNo = assignmentNo,
            expiryDateIso = isoDate,
            expiryDateDisplay = displayDate,
            prefixGroup = computePrefixGroup(callsign)
        )
    }

    internal fun normalizeDate(raw: String): Pair<String, String> {
        if (raw.isBlank()) return "" to ""
        return try {
            val parsed = LocalDate.parse(raw, inputDateFormatter)
            parsed.format(isoDateFormatter) to raw
        } catch (_: Exception) {
            try {
                val parsedIso = LocalDate.parse(raw, isoDateFormatter)
                parsedIso.format(isoDateFormatter) to parsedIso.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ROOT))
            } catch (_: Exception) {
                raw to raw
            }
        }
    }

    internal fun computePrefixGroup(callsign: String): String {
        val upper = callsign.uppercase(Locale.ROOT)
        return when {
            upper.startsWith("9M2") -> "9M2"
            upper.startsWith("9M4") -> "9M4"
            upper.startsWith("9M6") -> "9M6"
            upper.startsWith("9M8") -> "9M8"
            upper.startsWith("9W2") -> "9W2"
            upper.startsWith("9W3") -> "9W3"
            upper.startsWith("9W4") -> "9W4"
            upper.startsWith("9W6") -> "9W6"
            upper.startsWith("9W8") -> "9W8"
            else -> "OTHER"
        }
    }
}
