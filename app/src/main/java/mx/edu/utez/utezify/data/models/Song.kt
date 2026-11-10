package mx.edu.utez.utezify.data.models

import com.google.gson.annotations.SerializedName
import mx.edu.utez.utezify.data.retrofit.RetrofitClient

/**
 * Modelo de Canción con campos de Auditoría (CRUD y Soft Delete)
 */
data class Song(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("title", alternate = ["titulo", "name", "nombre"])
    val title: String,

    @SerializedName("artist", alternate = ["artista", "author", "autor"])
    val artist: String,

    @SerializedName("duration", alternate = ["duracion", "time", "length"])
    val duration: String,

    @SerializedName("imageUrl", alternate = ["image_url", "image", "imagen", "cover", "cover_url", "img", "foto"])
    val imageUrl: String? = null,

    @SerializedName("audioUrl", alternate = ["audio_url", "audio", "archivo", "song_url", "file", "filename", "mp3"])
    val audioUrl: String? = null,

    // Campos de auditoría
    @SerializedName("createdBy", alternate = ["created_by", "usuario", "matricula"])
    val createdBy: String? = null,

    @SerializedName("createdAt", alternate = ["created_at"])
    val createdAt: String? = null,

    @SerializedName("updatedBy", alternate = ["updated_by"])
    val updatedBy: String? = null,

    @SerializedName("updatedAt", alternate = ["updated_at"])
    val updatedAt: String? = null,

    @SerializedName("deletedBy", alternate = ["deleted_by"])
    val deletedBy: String? = null,

    @SerializedName("deletedAt", alternate = ["deleted_at"])
    val deletedAt: String? = null,

    @SerializedName("isDeleted", alternate = ["is_deleted", "deleted"])
    val isDeleted: Boolean = false
) {
    /**
     * Retorna la URL completa de la imagen. Si es relativa o sólo el nombre de archivo,
     * la concatena con la raíz del servidor Flask obtenida dinámicamente de RetrofitClient.BASE_URL.
     */
    fun getFullImageUrl(serverUrl: String = RetrofitClient.BASE_URL): String? {
        val img = imageUrl ?: return null
        if (img.isBlank()) return null

        val rootUrl = extractRootUrl(serverUrl)
        val cleanImg = img.trim()

        val fullUrl = when {
            cleanImg.startsWith("http://") || cleanImg.startsWith("https://") -> cleanImg
            cleanImg.startsWith("/static/") -> "${rootUrl.removeSuffix("/")}$cleanImg"
            cleanImg.startsWith("static/") -> "${rootUrl.removeSuffix("/")}/$cleanImg"
            cleanImg.startsWith("/uploads/") -> "${rootUrl.removeSuffix("/")}$cleanImg"
            cleanImg.startsWith("uploads/") -> "${rootUrl.removeSuffix("/")}/$cleanImg"
            cleanImg.startsWith("/") -> "${rootUrl.removeSuffix("/")}$cleanImg"
            else -> "${rootUrl.removeSuffix("/")}/static/images/$cleanImg"
        }

        return fullUrl.replace(" ", "%20")
    }

    /**
     * Retorna la URL completa del archivo de audio MP3.
     */
    fun getFullAudioUrl(serverUrl: String = RetrofitClient.BASE_URL): String? {
        val audio = audioUrl ?: return null
        if (audio.isBlank()) return null

        val rootUrl = extractRootUrl(serverUrl)
        val cleanAudio = audio.trim()

        val fullUrl = when {
            cleanAudio.startsWith("http://") || cleanAudio.startsWith("https://") -> cleanAudio
            cleanAudio.startsWith("/static/") -> "${rootUrl.removeSuffix("/")}$cleanAudio"
            cleanAudio.startsWith("static/") -> "${rootUrl.removeSuffix("/")}/$cleanAudio"
            cleanAudio.startsWith("/uploads/") -> "${rootUrl.removeSuffix("/")}$cleanAudio"
            cleanAudio.startsWith("uploads/") -> "${rootUrl.removeSuffix("/")}/$cleanAudio"
            cleanAudio.startsWith("/") -> "${rootUrl.removeSuffix("/")}$cleanAudio"
            else -> "${rootUrl.removeSuffix("/")}/static/audio/$cleanAudio"
        }

        return fullUrl.replace(" ", "%20")
    }

    private fun extractRootUrl(serverUrl: String): String {
        return if (serverUrl.contains("/api")) {
            serverUrl.substringBefore("/api").removeSuffix("/") + "/"
        } else {
            serverUrl.removeSuffix("/") + "/"
        }
    }
}
