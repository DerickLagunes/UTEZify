package mx.edu.utez.utezify.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import mx.edu.utez.utezify.data.models.Song
import mx.edu.utez.utezify.services.PlayerController

class PlayerViewModel : ViewModel() {

    val currentSong: StateFlow<Song?> = PlayerController.currentSong
    val isPlaying: StateFlow<Boolean> = PlayerController.isPlaying
    val currentPosition: StateFlow<Long> = PlayerController.currentPosition
    val duration: StateFlow<Long> = PlayerController.duration

    fun initPlayer(context: Context) {
        PlayerController.initController(context)
    }

    fun playSong(song: Song) {
        PlayerController.playSong(song)
    }

    fun togglePlayPause() {
        PlayerController.togglePlayPause()
    }

    fun seekRewind5s() {
        PlayerController.seekRewind5s()
    }

    fun seekForward5s() {
        PlayerController.seekForward5s()
    }

    fun stop() {
        PlayerController.stop()
    }
}
