package mx.edu.utez.utezify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.edu.utez.utezify.data.models.Song
import mx.edu.utez.utezify.data.models.SongHistoryResponse
import mx.edu.utez.utezify.data.models.UiState
import mx.edu.utez.utezify.ui.theme.SpotifyGreen
import mx.edu.utez.utezify.ui.theme.SpotifyLightGrey
import mx.edu.utez.utezify.ui.theme.SpotifyMediumGrey

@Composable
fun SongHistoryDialog(
    song: Song,
    historyState: UiState<SongHistoryResponse>,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = SpotifyGreen,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Historial de Modificaciones",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Text(
                        text = "${song.title} - ${song.artist}",
                        fontSize = 12.sp,
                        color = SpotifyLightGrey
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                when (historyState) {
                    is UiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = SpotifyGreen)
                        }
                    }

                    is UiState.Error, is UiState.Empty, is UiState.Success -> {
                        val response = (historyState as? UiState.Success)?.data

                        val createdBy = response?.createdBy ?: song.createdBy ?: "No registrado"
                        val createdAt = response?.createdAt ?: song.createdAt ?: "N/A"
                        val updatedBy = response?.updatedBy ?: song.updatedBy
                        val updatedAt = response?.updatedAt ?: song.updatedAt
                        val deletedBy = response?.deletedBy ?: song.deletedBy
                        val deletedAt = response?.deletedAt ?: song.deletedAt
                        val isDeleted = response?.isDeleted ?: song.isDeleted

                        val updatesList = response?.updates ?: emptyList()
                        val deletionsList = response?.deletions ?: emptyList()

                        // 1. INFORMACIÓN DE CREACIÓN
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SpotifyMediumGrey),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = SpotifyGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Creación del Registro", fontWeight = FontWeight.Bold, color = SpotifyGreen, fontSize = 14.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Creado por: $createdBy", color = Color.White, fontSize = 13.sp)
                                Text("Fecha de creación: $createdAt", color = SpotifyLightGrey, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 2. ÚLTIMO ESTADO Y AUDITORÍA
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SpotifyMediumGrey),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Update, contentDescription = null, tint = SpotifyGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Resumen de Auditoría", fontWeight = FontWeight.Bold, color = SpotifyGreen, fontSize = 14.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Última mod. por: ${updatedBy ?: "Sin modificaciones"}", color = Color.White, fontSize = 13.sp)
                                if (updatedAt != null) {
                                    Text("Fecha mod.: $updatedAt", color = SpotifyLightGrey, fontSize = 12.sp)
                                }
                                Text("Estado: ${if (isDeleted) "Eliminado Lógicamente (Soft Delete)" else "Activo"}", color = if (isDeleted) MaterialTheme.colorScheme.error else SpotifyGreen, fontSize = 13.sp)
                                if (deletedBy != null) {
                                    Text("Borrado por: $deletedBy", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                                }
                                if (deletedAt != null) {
                                    Text("Fecha de borrado: $deletedAt", color = SpotifyLightGrey, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = SpotifyMediumGrey)
                        Spacer(modifier = Modifier.height(12.dp))

                        // 3. HISTORIAL DE ACTUALIZACIONES (song_update_history)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Update, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tabla song_update_history (${updatesList.size})", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        if (updatesList.isEmpty()) {
                            Text("No existen registros individuales de actualización.", color = SpotifyLightGrey, fontSize = 12.sp)
                        } else {
                            updatesList.forEachIndexed { index, record ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("#${index + 1} - Modificado por: ${record.updatedBy ?: "Desconocido"}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("Fecha: ${record.updatedAt ?: "N/A"}", color = SpotifyLightGrey, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = SpotifyMediumGrey)
                        Spacer(modifier = Modifier.height(12.dp))

                        // 4. HISTORIAL DE ELIMINACIONES (song_deletion_history)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tabla song_deletion_history (${deletionsList.size})", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        if (deletionsList.isEmpty()) {
                            Text("No existen registros de eliminación previa.", color = SpotifyLightGrey, fontSize = 12.sp)
                        } else {
                            deletionsList.forEachIndexed { index, record ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("#${index + 1} - Eliminado por: ${record.deletedBy ?: "Desconocido"}", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("Fecha: ${record.deletedAt ?: "N/A"}", color = SpotifyLightGrey, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = SpotifyGreen, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}
