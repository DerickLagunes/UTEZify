package mx.edu.utez.utezify.data.models

import com.google.gson.annotations.SerializedName

/**
 * Registro individual de actualización desde la tabla `song_update_history`
 */
data class SongUpdateRecord(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("songId", alternate = ["song_id"])
    val songId: Int? = null,
    @SerializedName("updatedBy", alternate = ["updated_by", "usuario", "matricula"])
    val updatedBy: String? = null,
    @SerializedName("updatedAt", alternate = ["updated_at", "fecha"])
    val updatedAt: String? = null
)

/**
 * Registro individual de eliminación desde la tabla `song_deletion_history`
 */
data class SongDeletionRecord(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("songId", alternate = ["song_id"])
    val songId: Int? = null,
    @SerializedName("deletedBy", alternate = ["deleted_by", "usuario", "matricula"])
    val deletedBy: String? = null,
    @SerializedName("deletedAt", alternate = ["deleted_at", "fecha"])
    val deletedAt: String? = null
)

/**
 * Respuesta completa del historial de una canción
 */
data class SongHistoryResponse(
    @SerializedName("song")
    val song: Song? = null,
    @SerializedName("createdBy", alternate = ["created_by"])
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
    @SerializedName("isDeleted", alternate = ["is_deleted"])
    val isDeleted: Boolean = false,
    @SerializedName("updates", alternate = ["update_history", "updates_history", "historial_actualizaciones"])
    val updates: List<SongUpdateRecord> = emptyList(),
    @SerializedName("deletions", alternate = ["deletion_history", "deletions_history", "historial_eliminaciones"])
    val deletions: List<SongDeletionRecord> = emptyList()
)
