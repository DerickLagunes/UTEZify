package mx.edu.utez.utezify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.edu.utez.utezify.data.models.UiState
import mx.edu.utez.utezify.ui.theme.SpotifyGreen
import mx.edu.utez.utezify.ui.theme.SpotifyLightGrey
import mx.edu.utez.utezify.ui.viewmodels.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var matricula by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState by authViewModel.authState.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            onNavigateToHome()
        }
    }

    LaunchedEffect(authState) {
        if (authState is UiState.Success) {
            authViewModel.resetState()
            onNavigateToHome()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo / Icono
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Logo UTEZify",
                tint = SpotifyGreen,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "UTEZify",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 32.sp
                )
            )

            Text(
                text = "Iniciar Sesión",
                style = MaterialTheme.typography.bodyLarge.copy(color = SpotifyLightGrey)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo de Matrícula (elimina automáticamente espacios en blanco)
            OutlinedTextField(
                value = matricula,
                onValueChange = { matricula = it.trim() },
                label = { Text("Matrícula de Usuario") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SpotifyGreen) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SpotifyGreen,
                    unfocusedBorderColor = SpotifyLightGrey
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SpotifyGreen) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SpotifyGreen,
                    unfocusedBorderColor = SpotifyLightGrey
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de Iniciar Sesión o Indicador de Carga
            if (authState is UiState.Loading) {
                CircularProgressIndicator(color = SpotifyGreen)
            } else {
                Button(
                    onClick = { authViewModel.login(matricula.trim(), password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
                ) {
                    Text(
                        text = "INICIAR SESIÓN",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            // Mensaje de Error
            if (authState is UiState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (authState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Ir a Registro
            TextButton(onClick = onNavigateToRegister) {
                Text(
                    text = "¿No tienes cuenta? Regístrate aquí",
                    color = SpotifyLightGrey
                )
            }
        }
    }
}
