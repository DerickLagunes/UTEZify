package mx.edu.utez.utezify.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import mx.edu.utez.utezify.data.models.Song
import mx.edu.utez.utezify.data.models.SongHistoryResponse
import mx.edu.utez.utezify.data.retrofit.ApiService
import mx.edu.utez.utezify.data.retrofit.DeleteSongRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class SongRepository(
    private val apiService: ApiService
) {
    companion object {
        private const val TAG = "SongRepository"
    }

    /**
     * Obtiene la lista de canciones desde la API Flask (incluyendo canciones borradas lógicamente para auditoría).
     */
    suspend fun getSongs(): Result<List<Song>> {
        return try {
            val response = apiService.getSongs()
            if (response.isSuccessful && response.body() != null) {
                val songs = response.body()!!
                Log.d(TAG, "getSongs: Éxito (${songs.size} canciones recibidas)")
                Result.success(songs)
            } else {
                val errorMsg = "Error del servidor HTTP ${response.code()}: ${response.message()}"
                Log.e(TAG, "getSongs fallo: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Error de red al consultar canciones: ${e.localizedMessage ?: "Imposible conectar con el servidor"}"
            Log.e(TAG, "getSongs excepción:", e)
            Result.failure(Exception(errorMsg, e))
        }
    }

    /**
     * Obtiene el historial de modificaciones (`song_update_history` y `song_deletion_history`) de una canción.
     */
    suspend fun getSongHistory(songId: Int): Result<SongHistoryResponse> {
        return try {
            val response = try {
                apiService.getSongHistory(songId)
            } catch (e: Exception) {
                Log.w(TAG, "getSongHistory API con wrapper falló, intentando respuesta directa...", e)
                null
            }

            if (response != null && response.isSuccessful && response.body() != null) {
                val apiResp = response.body()!!
                val history = apiResp.data ?: SongHistoryResponse()
                Log.d(TAG, "getSongHistory: Éxito para canción ID $songId")
                Result.success(history)
            } else {
                val directResp = apiService.getSongHistoryDirect(songId)
                if (directResp.isSuccessful && directResp.body() != null) {
                    Log.d(TAG, "getSongHistoryDirect: Éxito para canción ID $songId")
                    Result.success(directResp.body()!!)
                } else {
                    val errorMsg = "Error al obtener historial (HTTP ${directResp.code()})"
                    Log.e(TAG, "getSongHistory fallo: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            val errorMsg = "Error de red al consultar historial: ${e.localizedMessage ?: "Servidor inaccesible"}"
            Log.e(TAG, "getSongHistory excepción:", e)
            Result.failure(Exception(errorMsg, e))
        }
    }

    /**
     * Obtiene la primera canción de un alumno por su matrícula (utilizado en la lectura NFC).
     */
    suspend fun getFirstSongByUser(matricula: String): Result<Song> {
        return try {
            val response = apiService.getSongsByUser(matricula)
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "getFirstSongByUser: Éxito para matrícula $matricula")
                Result.success(response.body()!!)
            } else {
                val errorMsg = "No se encontraron canciones para la matrícula: $matricula (HTTP ${response.code()})"
                Log.e(TAG, "getFirstSongByUser fallo: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Error de conexión al consultar NFC: ${e.localizedMessage ?: "Fallo de comunicación con servidor"}"
            Log.e(TAG, "getFirstSongByUser excepción:", e)
            Result.failure(Exception(errorMsg, e))
        }
    }

    /**
     * Sube una nueva canción (audio MP3 + imagen + metadatos).
     */
    suspend fun uploadSong(
        context: Context,
        title: String,
        artist: String,
        duration: String,
        audioUri: Uri,
        imageUri: Uri,
        createdBy: String
    ): Result<Boolean> {
        return try {
            val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val artistPart = artist.toRequestBody("text/plain".toMediaTypeOrNull())
            val durationPart = duration.toRequestBody("text/plain".toMediaTypeOrNull())
            val createdByPart = createdBy.toRequestBody("text/plain".toMediaTypeOrNull())

            val audioPart = uriToMultipart(context, audioUri, "audio", "audio/mpeg")
                ?: return Result.failure(Exception("No se pudo procesar el archivo de audio local"))
            val imagePart = uriToMultipart(context, imageUri, "image", "image/*")
                ?: return Result.failure(Exception("No se pudo procesar la imagen de portada local"))

            val response = apiService.uploadSong(
                titlePart,
                artistPart,
                durationPart,
                createdByPart,
                audioPart,
                imagePart
            )

            if (response.isSuccessful) {
                Log.d(TAG, "uploadSong: Éxito al registrar '$title'")
                Result.success(true)
            } else {
                val errorMsg = "Error al subir la canción (HTTP ${response.code()}): ${response.message()}"
                Log.e(TAG, "uploadSong fallo: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Error al procesar subida de la canción: ${e.localizedMessage ?: "Error desconocido en el envío"}"
            Log.e(TAG, "uploadSong excepción:", e)
            Result.failure(Exception(errorMsg, e))
        }
    }

    /**
     * Actualiza metadatos y/o archivos de una canción existente.
     */
    suspend fun updateSong(
        context: Context,
        songId: Int,
        title: String,
        artist: String,
        duration: String,
        audioUri: Uri?,
        imageUri: Uri?,
        updatedBy: String
    ): Result<Boolean> {
        return try {
            val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val artistPart = artist.toRequestBody("text/plain".toMediaTypeOrNull())
            val durationPart = duration.toRequestBody("text/plain".toMediaTypeOrNull())
            val updatedByPart = updatedBy.toRequestBody("text/plain".toMediaTypeOrNull())

            val audioPart = audioUri?.let { uriToMultipart(context, it, "audio", "audio/mpeg") }
            val imagePart = imageUri?.let { uriToMultipart(context, it, "image", "image/*") }

            val response = apiService.updateSong(
                songId,
                titlePart,
                artistPart,
                durationPart,
                updatedByPart,
                audioPart,
                imagePart
            )

            if (response.isSuccessful) {
                Log.d(TAG, "updateSong: Éxito al actualizar canción ID $songId")
                Result.success(true)
            } else {
                val errorMsg = "Error al actualizar la canción (HTTP ${response.code()}): ${response.message()}"
                Log.e(TAG, "updateSong fallo: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Error en actualización de la canción: ${e.localizedMessage ?: "Servidor no responde"}"
            Log.e(TAG, "updateSong excepción:", e)
            Result.failure(Exception(errorMsg, e))
        }
    }

    /**
     * Elimina lógicamente (Soft Delete) una canción enviando la matrícula del auditor.
     */
    suspend fun softDeleteSong(songId: Int, deletedBy: String): Result<Boolean> {
        return try {
            val request = DeleteSongRequest(deletedBy = deletedBy)
            val response = apiService.softDeleteSong(songId, request)
            if (response.isSuccessful) {
                Log.d(TAG, "softDeleteSong: Éxito al eliminar canción ID $songId por $deletedBy")
                Result.success(true)
            } else {
                val errorMsg = "Error al eliminar la canción (HTTP ${response.code()}): ${response.message()}"
                Log.e(TAG, "softDeleteSong fallo: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Error de conexión al eliminar la canción: ${e.localizedMessage ?: "Fallo de red"}"
            Log.e(TAG, "softDeleteSong excepción:", e)
            Result.failure(Exception(errorMsg, e))
        }
    }

    /**
     * Convierte una Uri del selector de archivos de Android a MultipartBody.Part para Retrofit.
     * Preserva la extensión original (.mp3, .png, .jpg) para cumplir con la validación de Flask.
     */
    private fun uriToMultipart(
        context: Context,
        uri: Uri,
        paramName: String,
        defaultMimeType: String
    ): MultipartBody.Part? {
        return try {
            val contentResolver = context.contentResolver

            var fileName = getFileNameFromUri(context, uri)

            val mimeType = contentResolver.getType(uri) ?: defaultMimeType
            val extension = getExtensionFromMime(mimeType, paramName)

            if (fileName.isNullOrBlank()) {
                fileName = "${paramName}_${System.currentTimeMillis()}.$extension"
            } else if (!hasValidExtension(fileName, paramName)) {
                fileName = "$fileName.$extension"
            }

            val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}_$fileName")
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
            MultipartBody.Part.createFormData(paramName, fileName, requestFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error en uriToMultipart para el parámetro $paramName:", e)
            null
        }
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (index != -1) {
                            name = cursor.getString(index)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error consultando OpenableColumns:", e)
            }
        }
        if (name == null) {
            name = uri.path
            val cut = name?.lastIndexOf('/') ?: -1
            if (cut != -1 && name != null) {
                name = name.substring(cut + 1)
            }
        }
        return name
    }

    private fun getExtensionFromMime(mimeType: String, paramName: String): String {
        val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        if (!ext.isNullOrBlank()) return ext
        return if (paramName == "audio") "mp3" else "jpg"
    }

    private fun hasValidExtension(fileName: String, paramName: String): Boolean {
        val dotIndex = fileName.lastIndexOf('.')
        if (dotIndex == -1 || dotIndex == fileName.length - 1) return false
        val ext = fileName.substring(dotIndex + 1).lowercase()
        return if (paramName == "audio") {
            listOf("mp3", "wav", "ogg", "m4a", "flac").contains(ext)
        } else {
            listOf("png", "jpg", "jpeg", "webp", "gif").contains(ext)
        }
    }
}
