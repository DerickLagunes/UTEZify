package mx.edu.utez.utezify.data.repository

import kotlinx.coroutines.flow.Flow
import mx.edu.utez.utezify.data.UserPreferences
import mx.edu.utez.utezify.data.models.LoginRequest
import mx.edu.utez.utezify.data.models.RegisterRequest
import mx.edu.utez.utezify.data.models.User
import mx.edu.utez.utezify.data.retrofit.ApiService

class AuthRepository(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences
) {
    val isLoggedIn: Flow<Boolean> = userPreferences.isLoggedIn
    val userSession: Flow<User?> = userPreferences.userSession

    suspend fun login(matricula: String, password: String): Result<User> {
        val cleanMatricula = matricula.trim()
        return try {
            val request = LoginRequest(matricula = cleanMatricula, password = password)
            val response = apiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                val apiResp = response.body()!!
                val user = apiResp.user ?: return Result.failure(Exception("Respuesta de usuario nula"))

                val cleanUser = user.copy(matricula = user.matricula.trim(), fullName = user.fullName.trim())
                userPreferences.saveSession(cleanUser)
                Result.success(cleanUser)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Matrícula o contraseña incorrectas"
                    400 -> "Faltan credenciales"
                    else -> "Error en el servidor (${response.code()})"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.localizedMessage ?: "Verifica que Flask esté corriendo"}"))
        }
    }

    suspend fun register(fullName: String, matricula: String, password: String): Result<User> {
        val cleanFullName = fullName.trim()
        val cleanMatricula = matricula.trim()
        return try {
            val request = RegisterRequest(fullName = cleanFullName, matricula = cleanMatricula, password = password)
            val response = apiService.register(request)

            if (response.isSuccessful && response.body() != null) {
                val apiResp = response.body()!!
                val user = apiResp.user ?: return Result.failure(Exception("Respuesta de usuario nula"))

                val cleanUser = user.copy(matricula = user.matricula.trim(), fullName = user.fullName.trim())
                userPreferences.saveSession(cleanUser)
                Result.success(cleanUser)
            } else {
                val errorMsg = if (response.code() == 400) {
                    "La matrícula ya está registrada o los datos son inválidos"
                } else {
                    "Error al registrar usuario (${response.code()})"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.localizedMessage}"))
        }
    }

    suspend fun logout() {
        userPreferences.clearSession()
    }
}
