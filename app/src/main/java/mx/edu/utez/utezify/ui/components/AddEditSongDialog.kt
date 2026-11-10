package mx.edu.utez.utezify.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import mx.edu.utez.utezify.data.models.Song
import mx.edu.utez.utezify.ui.theme.SpotifyGreen
import mx.edu.utez.utezify.ui.theme.SpotifyLightGrey

@Composable
fun AddEditSongDialog(
    songToEdit: Song? = null,
    onDismiss: () -> Unit,
    onConfirm: (title: String, artist: String, duration: String, audioUri: Uri?, imageUri: Uri?) -> Unit
) {
    var title by remember { mutableStateOf(songToEdit?.title ?: "") }
    var artist by remember { mutableStateOf(songToEdit?.artist ?: "") }
    var duration by remember { mutableStateOf(songToEdit?.duration ?: "") }

    var selectedAudioUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Picker para archivo de Audio (MP3)
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedAudioUri = uri
    }

    // Picker para Imagen de Portada
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (songToEdit == null) "Agregar Nueva Canción" else "Editar Canción",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título de la canción") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SpotifyGreen,
                        unfocusedBorderColor = SpotifyLightGrey
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artista") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SpotifyGreen,
                        unfocusedBorderColor = SpotifyLightGrey
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duración (ej. 3:45)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SpotifyGreen,
                        unfocusedBorderColor = SpotifyLightGrey
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Selección de Archivo MP3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { audioPickerLauncher.launch("audio/*") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.AudioFile, contentDescription = null, tint = SpotifyGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (selectedAudioUri != null) "MP3 Seleccionado" else "Seleccionar MP3",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Selección de Imagen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = SpotifyGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (selectedImageUri != null) "Imagen Seleccionada" else "Seleccionar Imagen",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || artist.isBlank()) {
                        errorMessage = "El título y el artista son obligatorios"
                    } else {
                        onConfirm(title, artist, duration, selectedAudioUri, selectedImageUri)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
            ) {
                Text("Guardar", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = SpotifyLightGrey)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}
