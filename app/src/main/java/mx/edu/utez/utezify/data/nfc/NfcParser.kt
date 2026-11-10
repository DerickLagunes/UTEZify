package mx.edu.utez.utezify.data.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import java.nio.charset.Charset

object NfcParser {

    /**
     * Parsea un Tag NFC extrayendo el contenido NDEF (Matrícula)
     */
    fun parseTag(tag: Tag?): String? {
        if (tag == null) return null
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            try {
                ndef.connect()
                val ndefMessage = ndef.cachedNdefMessage ?: ndef.ndefMessage
                ndef.close()
                if (ndefMessage != null) {
                    return parseNdefMessage(ndefMessage)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return parseTagIdAsFallback(tag)
    }

    /**
     * Extrae texto de un NdefMessage
     */
    fun parseNdefMessage(ndefMessage: NdefMessage?): String? {
        if (ndefMessage == null) return null
        for (record in ndefMessage.records) {
            val text = parseRecord(record)
            if (!text.isNullOrBlank()) {
                return text.trim()
            }
        }
        return null
    }

    private fun parseRecord(record: NdefRecord): String? {
        val payload = record.payload ?: return null
        if (payload.isEmpty()) return null

        return try {
            // NDEF Text Record (RTD_TEXT)
            if (record.tnf == NdefRecord.TNF_WELL_KNOWN && record.type.contentEquals(NdefRecord.RTD_TEXT)) {
                val statusByte = payload[0].toInt()
                val isUtf16 = (statusByte and 128) != 0
                val textEncoding = if (isUtf16) Charset.forName("UTF-16") else Charset.forName("UTF-8")
                val languageCodeLength = statusByte and 63
                String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1, textEncoding)
            } else {
                // Formato MIME o Texto plano directo
                String(payload, Charset.forName("UTF-8")).trim()
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseTagIdAsFallback(tag: Tag): String? {
        return try {
            val idBytes = tag.id
            if (idBytes != null && idBytes.isNotEmpty()) {
                idBytes.joinToString("") { "%02X".format(it) }
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
