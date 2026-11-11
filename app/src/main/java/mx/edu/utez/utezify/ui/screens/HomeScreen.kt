package mx.edu.utez.utezify.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import mx.edu.utez.utezify.data.models.Song
import mx.edu.utez.utezify.data.models.UiState
import mx.edu.utez.utezify.ui.components.AddEditSongDialog
import mx.edu.utez.utezify.ui.components.BottomPlayerBar
import mx.edu.utez.utezify.ui.components.DeleteConfirmDialog
import mx.edu.utez.utezify.ui.components.SongCard
import mx.edu.utez.utezify.ui.components.SongHistoryDialog
import mx.edu.utez.utezify.ui.theme.SpotifyGreen
import mx.edu.utez.utezify.ui.theme.SpotifyLightGrey
import mx.edu.utez.utezify.ui.viewmodels.AuthViewModel
import mx.edu.utez.utezify.ui.viewmodels.PlayerViewModel
import mx.edu.utez.utezify.ui.viewmodels.SongViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    songViewModel: SongViewModel,
    playerViewModel: PlayerViewModel,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current

    val songsState by songViewModel.songsState.collectAsState()
    val operationState by songViewModel.operationState.collectAsState()
    val historyState by songViewModel.historyState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    val currentSong by playerViewModel.currentSong.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val currentPosition by playerViewModel.currentPosition.collectAsState()
    val duration by playerViewModel.duration.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var songToEdit by remember { mutableStateOf<Song?>(null) }
    var songToDelete by remember { mutableStateOf<Song?>(null) }
    var songForHistory by remember { mutableStateOf<Song?>(null) }

    val userMatricula = currentUser?.matricula ?: "Anonimo"

    // Inicializar Player Service
    LaunchedEffect(Unit) {
        playerViewModel.initPlayer(context)
    }

    // Notificaciones inmediatas de errores en operaciones Retrofit (Subir, Editar, Borrar)
    LaunchedEffect(operationState) {
        when (operationState) {
            is UiState.Error -> {
                val errorMsg = (operationState as UiState.Error).message
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                songViewModel.clearOperationState()
            }
            is UiState.Success<*> -> {
                val msg = (operationState as UiState.Success<*>).data.toString()
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                songViewModel.clearOperationState()
            }
            else -> {}
        }
    }

    // Permisos de notificación para Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {}

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Player continúa en segundo plano salvo logout explícito
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "UTEZify",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Nfc,
                                contentDescription = "NFC Activo",
                                tint = SpotifyGreen
                            )
                        }
                        Text(
                            text = "Matrícula: $userMatricula",
                            fontSize = 12.sp,
                            color = SpotifyLightGrey
                        )
                    }
                },
                actions = {
                    // Botón de Recargar Canciones desde la API REST
                    IconButton(
                        onClick = { songViewModel.fetchSongs() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Recargar Canciones del Servidor",
                            tint = SpotifyGreen
                        )
                    }

                    // Botón explícito de Cerrar Sesión
                    IconButton(
                        onClick = {
                            playerViewModel.stop()
                            authViewModel.logout {
                                onNavigateToLogin()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar Sesión",
                            tint = SpotifyGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = SpotifyGreen,
                contentColor = Color.Black
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar Canción")
            }
        },
        bottomBar = {
            BottomPlayerBar(
                currentSong = currentSong,
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                duration = duration,
                onTogglePlayPause = { playerViewModel.togglePlayPause() },
                onRewind5s = { playerViewModel.seekRewind5s() },
                onForward5s = { playerViewModel.seekForward5s() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (songsState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(
                        color = SpotifyGreen,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is UiState.Empty -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No hay canciones disponibles.\nAproxima un Tag NFC o presiona (+).",
                            color = SpotifyLightGrey,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { songViewModel.fetchSongs() },
                            colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Recargar", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                is UiState.Error -> {
                    val errorMessage = (songsState as UiState.Error).message
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Error de Conexión Retrofit",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = errorMessage,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { songViewModel.fetchSongs() },
                            colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reintentar Conexión", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                is UiState.Success -> {
                    val songList = (songsState as UiState.Success<List<Song>>).data
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(songList, key = { it.id ?: it.title }) { song ->
                            SongCard(
                                song = song,
                                onPlayClick = { selectedSong ->
                                    playerViewModel.playSong(selectedSong)
                                },
                                onEditClick = { selectedSong ->
                                    songToEdit = selectedSong
                                },
                                onHistoryClick = { selectedSong ->
                                    songForHistory = selectedSong
                                    selectedSong.id?.let { songId ->
                                        songViewModel.fetchSongHistory(songId)
                                    }
                                },
                                onDeleteClick = { selectedSong ->
                                    songToDelete = selectedSong
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog para Ver Historial de Canción (Auditoría)
    if (songForHistory != null) {
        SongHistoryDialog(
            song = songForHistory!!,
            historyState = historyState,
            onDismiss = {
                songForHistory = null
                songViewModel.clearHistoryState()
            }
        )
    }

    // Modal Dialog para Agregar Canción
    if (showAddDialog) {
        AddEditSongDialog(
            songToEdit = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, artist, durationVal, audioUri, imageUri ->
                if (audioUri != null && imageUri != null) {
                    songViewModel.uploadSong(
                        context = context,
                        title = title,
                        artist = artist,
                        duration = durationVal,
                        audioUri = audioUri,
                        imageUri = imageUri,
                        createdBy = userMatricula
                    )
                    showAddDialog = false
                }
            }
        )
    }

    // Modal Dialog para Editar Canción
    if (songToEdit != null) {
        AddEditSongDialog(
            songToEdit = songToEdit,
            onDismiss = { songToEdit = null },
            onConfirm = { title, artist, durationVal, audioUri, imageUri ->
                songToEdit?.id?.let { songId ->
                    songViewModel.updateSong(
                        context = context,
                        songId = songId,
                        title = title,
                        artist = artist,
                        duration = durationVal,
                        audioUri = audioUri,
                        imageUri = imageUri,
                        updatedBy = userMatricula
                    )
                }
                songToEdit = null
            }
        )
    }

    // Modal Dialog para Confirmar Soft Delete
    if (songToDelete != null) {
        DeleteConfirmDialog(
            song = songToDelete!!,
            onDismiss = { songToDelete = null },
            onConfirmDelete = { targetSong ->
                targetSong.id?.let { songId ->
                    songViewModel.softDeleteSong(
                        songId = songId,
                        deletedBy = userMatricula
                    )
                }
                songToDelete = null
            }
        )
    }
}
