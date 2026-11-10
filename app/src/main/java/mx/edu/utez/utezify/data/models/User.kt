package mx.edu.utez.utezify.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de Usuario
 */
data class User(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("fullName", alternate = ["full_name", "nombre"])
    val fullName: String,
    @SerializedName("matricula")
    val matricula: String,
    @SerializedName("password")
    val password: String? = null
)

/**
 * Petición de Login
 */
data class LoginRequest(
    @SerializedName("matricula")
    val matricula: String,
    @SerializedName("password")
    val password: String
)

/**
 * Petición de Registro
 */
data class RegisterRequest(
    @SerializedName("fullName", alternate = ["full_name", "nombre"])
    val fullName: String,
    @SerializedName("matricula")
    val matricula: String,
    @SerializedName("password")
    val password: String
)

/**
 * Respuesta genérica o de Autenticación de la API Flask
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean = true,
    @SerializedName("message")
    val message: String = "",
    @SerializedName("data")
    val data: T? = null,
    @SerializedName("user")
    val user: User? = null
)
