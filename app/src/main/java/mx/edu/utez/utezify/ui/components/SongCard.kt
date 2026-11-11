package mx.edu.utez.utezify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import mx.edu.utez.utezify.data.models.Song
import mx.edu.utez.utezify.ui.theme.SpotifyGreen
import mx.edu.utez.utezify.ui.theme.SpotifyLightGrey

@Composable
fun SongCard(
    song: Song,
    onPlayClick: (Song) -> Unit,
    onEditClick: (Song) -> Unit,
    onHistoryClick: (Song) -> Unit,
    onDeleteClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDeleted = song.isDeleted

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .graphicsLayer {
                // Opacidad reducida si la canción está borrada lógicamente
                alpha = if (isDeleted) 0.55f else 1f
            }
            .clickable(enabled = !isDeleted) {
                onPlayClick(song)
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isDeleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDeleted) 1.dp else 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Portada de la canción (Coil AsyncImage)
            AsyncImage(
                model = song.getFullImageUrl(),
                contentDescription = "Portada de ${song.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Información de la Canción
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textDecoration = if (isDeleted) TextDecoration.LineThrough else TextDecoration.None,
                            color = if (isDeleted) SpotifyLightGrey else Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isDeleted) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "[BORRADA]",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${song.artist} • ${song.duration}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SpotifyLightGrey,
                        fontSize = 13.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Acciones: Reproducir (Deshabilitado si borrada), Historial (SIEMPRE HABILITADO), Editar (Deshabilitado si borrada), Eliminar (Deshabilitado si borrada)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                // Reproducir
                IconButton(
                    onClick = { onPlayClick(song) },
                    enabled = !isDeleted
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Reproducir",
                        tint = if (isDeleted) SpotifyLightGrey.copy(alpha = 0.3f) else SpotifyGreen
                    )
                }

                // HISTORIAL: SIEMPRE HABILITADO (Permite consultar auditoría incluso si está borrada)
                IconButton(
                    onClick = { onHistoryClick(song) },
                    enabled = true
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Ver Historial de Auditoría",
                        tint = SpotifyGreen
                    )
                }

                // Editar
                IconButton(
                    onClick = { onEditClick(song) },
                    enabled = !isDeleted
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = if (isDeleted) SpotifyLightGrey.copy(alpha = 0.3f) else SpotifyLightGrey
                    )
                }

                // Eliminar
                IconButton(
                    onClick = { onDeleteClick(song) },
                    enabled = !isDeleted
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar (Soft Delete)",
                        tint = if (isDeleted) SpotifyLightGrey.copy(alpha = 0.3f) else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
