package mx.edu.utez.utezify.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.edu.utez.utezify.data.models.Song
import mx.edu.utez.utezify.ui.theme.SpotifyLightGrey

@Composable
fun DeleteConfirmDialog(
    song: Song,
    onDismiss: () -> Unit,
    onConfirmDelete: (Song) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Eliminar Canción",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Text(
                text = "¿Está seguro de que desea eliminar '${song.title}' de ${song.artist}? Esta acción registrará un borrado lógico (Soft Delete).",
                color = SpotifyLightGrey
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirmDelete(song) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
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
