package mx.edu.utez.utezify.services

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import mx.edu.utez.utezify.data.models.Song

object PlayerController {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    fun initController(context: Context) {
        if (mediaController != null || controllerFuture != null) return

        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                setupControllerListener()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    private fun setupControllerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    startProgressUpdate()
                } else {
                    stopProgressUpdate()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _duration.value = mediaController?.duration?.coerceAtLeast(0L) ?: 0L
                } else if (playbackState == Player.STATE_ENDED) {
                    _isPlaying.value = false
                    stopProgressUpdate()
                }
            }
        })
    }

    fun playSong(song: Song) {
        _currentSong.value = song
        val audioUrl = song.getFullAudioUrl() ?: return
        val mediaUri = Uri.parse(audioUrl)

        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setArtworkUri(song.getFullImageUrl()?.let { Uri.parse(it) })
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(mediaUri)
            .setMediaMetadata(metadata)
            .build()

        mediaController?.let { controller ->
            controller.setMediaItem(mediaItem)
            controller.prepare()
            controller.play()
            _isPlaying.value = true
            startProgressUpdate()
        }
    }

    fun togglePlayPause() {
        mediaController?.let { controller ->
            if (controller.isPlaying) {
                controller.pause()
                _isPlaying.value = false
                stopProgressUpdate()
            } else {
                controller.play()
                _isPlaying.value = true
                startProgressUpdate()
            }
        }
    }

    fun seekRewind5s() {
        mediaController?.let { controller ->
            val newPosition = (controller.currentPosition - 5000L).coerceAtLeast(0L)
            controller.seekTo(newPosition)
            _currentPosition.value = newPosition
        }
    }

    fun seekForward5s() {
        mediaController?.let { controller ->
            val maxDur = controller.duration.coerceAtLeast(0L)
            val newPosition = (controller.currentPosition + 5000L).coerceAtMost(maxDur)
            controller.seekTo(newPosition)
            _currentPosition.value = newPosition
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.let { controller ->
            controller.seekTo(positionMs)
            _currentPosition.value = positionMs
        }
    }

    fun stop() {
        mediaController?.stop()
        _isPlaying.value = false
        _currentSong.value = null
        stopProgressUpdate()
    }

    private fun startProgressUpdate() {
        stopProgressUpdate()
        progressJob = scope.launch {
            while (isActive) {
                mediaController?.let { controller ->
                    _currentPosition.value = controller.currentPosition.coerceAtLeast(0L)
                    _duration.value = controller.duration.coerceAtLeast(0L)
                }
                delay(500L)
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stopProgressUpdate()
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        controllerFuture = null
        mediaController = null
    }
}
