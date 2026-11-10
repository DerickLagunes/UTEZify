package mx.edu.utez.utezify

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import mx.edu.utez.utezify.data.UserPreferences
import mx.edu.utez.utezify.data.nfc.NfcManager
import mx.edu.utez.utezify.data.repository.AuthRepository
import mx.edu.utez.utezify.data.repository.SongRepository
import mx.edu.utez.utezify.data.retrofit.RetrofitClient
import mx.edu.utez.utezify.ui.navigation.UTEZifyNavGraph
import mx.edu.utez.utezify.ui.theme.UTEZifyTheme
import mx.edu.utez.utezify.ui.viewmodels.AuthViewModel
import mx.edu.utez.utezify.ui.viewmodels.PlayerViewModel
import mx.edu.utez.utezify.ui.viewmodels.SongViewModel

class MainActivity : ComponentActivity() {

    private lateinit var nfcManager: NfcManager

    private var authViewModelRef: AuthViewModel? = null
    private var songViewModelRef: SongViewModel? = null
    private var playerViewModelRef: PlayerViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        nfcManager = NfcManager(this)

        // Inicializar repositorios y servicios
        val userPreferences = UserPreferences(applicationContext)
        val apiService = RetrofitClient.apiService
        val authRepository = AuthRepository(apiService, userPreferences)
        val songRepository = SongRepository(apiService)

        setContent {
            UTEZifyTheme {
                val authViewModel: AuthViewModel = viewModel { AuthViewModel(authRepository) }
                val songViewModel: SongViewModel = viewModel { SongViewModel(songRepository) }
                val playerViewModel: PlayerViewModel = viewModel { PlayerViewModel() }

                authViewModelRef = authViewModel
                songViewModelRef = songViewModel
                playerViewModelRef = playerViewModel

                UTEZifyNavGraph(
                    authViewModel = authViewModel,
                    songViewModel = songViewModel,
                    playerViewModel = playerViewModel
                )
            }
        }

        // Suscribirse a eventos de lectura NFC
        lifecycleScope.launch {
            nfcManager.nfcEvents.collectLatest { matricula ->
                onNfcMatriculaScanned(matricula)
            }
        }

        // Manejo de Intent inicial si la app se abrió mediante NFC
        handleNfcIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        nfcManager.enableReaderMode()
    }

    override fun onPause() {
        super.onPause()
        nfcManager.disableReaderMode()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNfcIntent(intent)
    }

    private fun handleNfcIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == action ||
            NfcAdapter.ACTION_TAG_DISCOVERED == action) {
            val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }
            nfcManager.handleTag(tag)
        }
    }

    private fun onNfcMatriculaScanned(matricula: String) {
        Toast.makeText(this, "NFC Detectado - Matrícula: $matricula", Toast.LENGTH_SHORT).show()

        val authVM = authViewModelRef
        val songVM = songViewModelRef
        val playerVM = playerViewModelRef ?: return

        if (authVM != null && !authVM.isLoggedIn.value) {
            authVM.login(matricula, "123456")
        }

        songVM?.processNfcScan(
            matricula = matricula,
            onPlaySong = { songToPlay ->
                playerVM.playSong(songToPlay)
                Toast.makeText(this, "Reproduciendo automáticamente: ${songToPlay.title}", Toast.LENGTH_LONG).show()
            },
            onNoSongsFound = { message ->
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        )
    }
}
