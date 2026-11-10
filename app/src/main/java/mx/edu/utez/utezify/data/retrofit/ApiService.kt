package mx.edu.utez.utezify.data.retrofit

import mx.edu.utez.utezify.data.models.ApiResponse
import mx.edu.utez.utezify.data.models.LoginRequest
import mx.edu.utez.utezify.data.models.RegisterRequest
import mx.edu.utez.utezify.data.models.Song
import mx.edu.utez.utezify.data.models.SongHistoryResponse
import mx.edu.utez.utezify.data.models.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    // ==========================================
    // AUTENTICACIÓN
    // ==========================================

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<User>>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<User>>

    // ==========================================
    // CANCIONES & NFC
    // ==========================================

    @GET("songs")
    suspend fun getSongs(): Response<List<Song>>

    @GET("songs/user/{matricula}")
    suspend fun getSongsByUser(@Path("matricula") matricula: String): Response<Song>

    @GET("songs/{id}/history")
    suspend fun getSongHistory(@Path("id") songId: Int): Response<ApiResponse<SongHistoryResponse>>

    @GET("songs/{id}/history")
    suspend fun getSongHistoryDirect(@Path("id") songId: Int): Response<SongHistoryResponse>

    @Multipart
    @POST("songs")
    suspend fun uploadSong(
        @Part("title") title: RequestBody,
        @Part("artist") artist: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part("createdBy") createdBy: RequestBody,
        @Part audio: MultipartBody.Part,
        @Part image: MultipartBody.Part
    ): Response<ApiResponse<Song>>

    @Multipart
    @PUT("songs/{id}")
    suspend fun updateSong(
        @Path("id") id: Int,
        @Part("title") title: RequestBody,
        @Part("artist") artist: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part("updatedBy") updatedBy: RequestBody,
        @Part audio: MultipartBody.Part?,
        @Part image: MultipartBody.Part?
    ): Response<ApiResponse<Song>>

    @HTTP(method = "DELETE", path = "songs/{id}", hasBody = true)
    suspend fun softDeleteSong(
        @Path("id") id: Int,
        @Body body: DeleteSongRequest
    ): Response<ApiResponse<Unit>>
}

// Data class para enviar en el body del Delete
data class DeleteSongRequest(
    val deletedBy: String
)
