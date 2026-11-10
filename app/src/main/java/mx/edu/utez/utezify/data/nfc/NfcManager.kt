package mx.edu.utez.utezify.data.nfc

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class NfcManager(private val activity: Activity) {

    private val nfcAdapter: NfcAdapter? by lazy {
        NfcAdapter.getDefaultAdapter(activity)
    }

    private val _nfcEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val nfcEvents: SharedFlow<String> = _nfcEvents.asSharedFlow()

    fun isNfcAvailable(): Boolean = nfcAdapter != null
    fun isNfcEnabled(): Boolean = nfcAdapter?.isEnabled == true

    fun enableReaderMode() {
        val adapter = nfcAdapter ?: return
        if (!adapter.isEnabled) return

        adapter.enableReaderMode(
            activity,
            { tag ->
                val matricula = NfcParser.parseTag(tag)
                if (!matricula.isNullOrBlank()) {
                    activity.runOnUiThread {
                        _nfcEvents.tryEmit(matricula.trim())
                    }
                }
            },
            NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_F or
                    NfcAdapter.FLAG_READER_NFC_V or
                    NfcAdapter.FLAG_READER_NFC_BARCODE or
                    NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
            null
        )
    }

    fun disableReaderMode() {
        nfcAdapter?.disableReaderMode(activity)
    }

    fun handleTag(tag: Tag?) {
        val matricula = NfcParser.parseTag(tag)
        if (!matricula.isNullOrBlank()) {
            _nfcEvents.tryEmit(matricula.trim())
        }
    }
}
