package mx.edu.utez.utezify.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Forward5
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay5
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import mx.edu.utez.utezify.data.models.Song
import mx.edu.utez.utezify.ui.theme.SpotifyGreen
import mx.edu.utez.utezify.ui.theme.SpotifyLightGrey
import mx.edu.utez.utezify.ui.theme.SpotifyMediumGrey

@Composable
fun BottomPlayerBar(
    currentSong: Song?,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onTogglePlayPause: () -> Unit,
    onRewind5s: () -> Unit,
    onForward5s: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentSong == null) return

    val progress = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = SpotifyMediumGrey
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column {
            // Barra de progreso de la canción en tiempo real
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = SpotifyGreen,
                trackColor = Color.DarkGray
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Portada
                AsyncImage(
                    model = currentSong.getFullImageUrl(),
                    contentDescription = currentSong.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Título y Artista
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = currentSong.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentSong.artist,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SpotifyLightGrey,
                            fontSize = 12.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Controles de Reproducción (Retroceder 5s, Play/Pause, Adelantar 5s)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Retroceder 5 segundos
                    IconButton(onClick = onRewind5s) {
                        Icon(
                            imageVector = Icons.Default.Replay5,
                            contentDescription = "Retroceder 5 segundos",
                            tint = SpotifyLightGrey
                        )
                    }

                    // Play / Pause
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .size(40.dp)
                            .background(SpotifyGreen, shape = RoundedCornerShape(20.dp))
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                            tint = Color.Black
                        )
                    }

                    // Adelantar 5 segundos
                    IconButton(onClick = onForward5s) {
                        Icon(
                            imageVector = Icons.Default.Forward5,
                            contentDescription = "Adelantar 5 segundos",
                            tint = SpotifyLightGrey
                        )
                    }
                }
            }
        }
    }
}
