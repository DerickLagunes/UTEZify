package mx.edu.utez.utezify.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import mx.edu.utez.utezify.R
import mx.edu.utez.utezify.data.UserPreferences
import mx.edu.utez.utezify.data.models.Song
import mx.edu.utez.utezify.data.retrofit.RetrofitClient
import java.text.SimpleDateFormat
import java.util.Locale

class NewSongCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "NewSongCheckWorker"
        const val CHANNEL_ID = "new_songs_channel"
        const val WORK_NAME = "new_song_check_periodic_work"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Ejecutando tarea en segundo plano para verificar nuevas canciones...")
        return try {
            val userPreferences = UserPreferences(context)
            val lastOpenTime = userPreferences.getLastOpenTimestampSync()

            val apiService = RetrofitClient.apiService
            val response = try {
                apiService.getSongs()
            } catch (e: Exception) {
                null
            }

            if (response != null && response.isSuccessful && response.body() != null) {
                val songs = response.body()!!.filter { !it.isDeleted }
                Log.d(TAG, "Consulta exitosa: ${songs.size} canciones en total. Última apertura: $lastOpenTime")

                val newSongs = songs.filter { song ->
                    isSongNewerThan(song, lastOpenTime)
                }

                if (newSongs.isNotEmpty()) {
                    Log.d(TAG, "Se encontraron ${newSongs.size} nuevas canciones registradas desde la última apertura")
                    newSongs.forEachIndexed { index, song ->
                        sendNewSongNotification(song, index + 100)
                    }
                } else {
                    Log.d(TAG, "No hay canciones nuevas registradas desde la última vez.")
                }
            } else {
                Log.w(TAG, "No se pudo obtener la lista de canciones desde la API en segundo plano")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error durante la ejecución del worker:", e)
            Result.retry()
        }
    }

    private fun isSongNewerThan(song: Song, lastOpenTimestamp: Long): Boolean {
        if (lastOpenTimestamp <= 0L) return false
        val createdAtStr = song.createdAt ?: return false
        val songTime = parseTimestamp(createdAtStr) ?: return false
        return songTime > lastOpenTimestamp
    }

    private fun parseTimestamp(dateStr: String): Long? {
        return try {
            if (dateStr.matches(Regex("^\\d+$"))) {
                dateStr.toLong()
            } else {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                sdf.parse(dateStr)?.time
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun sendNewSongNotification(song: Song, notificationId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Nuevas Canciones",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones cuando se publica una nueva canción"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val matricula = song.createdBy ?: "Anonimo"
        val nombreCancion = song.title

        val notificationText = "matricula: $matricula ha publicado una nueva canción: $nombreCancion"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("¡Nueva canción publicada!")
            .setContentText(notificationText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(notificationId, builder.build())
    }
}
