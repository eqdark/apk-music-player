package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.MusicDatabase
import com.example.data.MusicRepository
import com.example.player.MediaPlayerManager
import com.example.ui.MusicViewModel
import com.example.ui.MusicViewModelFactory
import com.example.ui.Screen
import com.example.ui.components.AddPlaylistDialog
import com.example.ui.components.AddSongDialog
import com.example.ui.components.BottomMiniPlayer
import com.example.ui.screens.ExpandedPlayerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PlaylistDetailScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SpotifyGreen

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MusicViewModel

    // Audio scanning permission launcher (handling legacy + modern API rules)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.scanLocalAudioFiles(this)
            Toast.makeText(this, "Permisi diberikan! Memindai penyimpanan...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permisi ditolak. File lokal tidak dapat dipindai.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Core Orchestration Architecture
        val database = MusicDatabase.getDatabase(this)
        val repository = MusicRepository(database.musicDao())
        val playerManager = MediaPlayerManager(this)

        val factory = MusicViewModelFactory(repository, playerManager)
        viewModel = ViewModelProvider(this, factory)[MusicViewModel::class.java]

        setContent {
            MyApplicationTheme {
                var showAddSongDialog by remember { mutableStateOf(false) }
                var showAddPlaylistDialog by remember { mutableStateOf(false) }

                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                val isPlayerExpanded by viewModel.isPlayerExpanded.collectAsStateWithLifecycle()

                val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
                val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
                val progress by viewModel.playbackProgress.collectAsStateWithLifecycle()
                val isBuffering by viewModel.isBuffering.collectAsStateWithLifecycle()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Black,
                        bottomBar = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding() // Protect notch overlap
                            ) {
                                // 1. Dynamic slide player bar
                                if (currentSong != null && !isPlayerExpanded) {
                                    BottomMiniPlayer(
                                        song = currentSong,
                                        isPlaying = isPlaying,
                                        progress = progress,
                                        isBuffering = isBuffering,
                                        onTogglePlay = { viewModel.togglePlayPause() },
                                        onSkipNext = { viewModel.skipNext() },
                                        onPlayerTap = { viewModel.setPlayerExpanded(true) },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                // 2. Material 3 Bottom Navigation bar matching standard Spotify designs
                                NavigationBar(
                                    containerColor = Color(0xFF0F0F0F),
                                    tonalElevation = 0.dp,
                                    modifier = Modifier.height(60.dp)
                                ) {
                                    NavigationBarItem(
                                        selected = currentScreen is Screen.Home,
                                        onClick = { viewModel.navigateTo(Screen.Home) },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentScreen is Screen.Home) Icons.Default.Home else Icons.Outlined.Home,
                                                contentDescription = "Home"
                                            )
                                        },
                                        label = { Text("Mulai", color = Color.White) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = SpotifyGreen,
                                            unselectedIconColor = Color.Gray,
                                            indicatorColor = Color(0xFF242424)
                                        ),
                                        modifier = Modifier.testTag("nav_home")
                                    )

                                    NavigationBarItem(
                                        selected = currentScreen is Screen.Search,
                                        onClick = { viewModel.navigateTo(Screen.Search) },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentScreen is Screen.Search) Icons.Default.Search else Icons.Outlined.Search,
                                                contentDescription = "Search"
                                            )
                                        },
                                        label = { Text("Cari", color = Color.White) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = SpotifyGreen,
                                            unselectedIconColor = Color.Gray,
                                            indicatorColor = Color(0xFF242424)
                                        ),
                                        modifier = Modifier.testTag("nav_search")
                                    )

                                    NavigationBarItem(
                                        selected = currentScreen is Screen.Library,
                                        onClick = { viewModel.navigateTo(Screen.Library) },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentScreen is Screen.Library) Icons.Default.LibraryMusic else Icons.Outlined.LibraryMusic,
                                                contentDescription = "Your Library"
                                            )
                                        },
                                        label = { Text("Koleksi", color = Color.White) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = SpotifyGreen,
                                            unselectedIconColor = Color.Gray,
                                            indicatorColor = Color(0xFF242424)
                                        ),
                                        modifier = Modifier.testTag("nav_library")
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        
                        // Switch active displaying workspace screen based on enum routing
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = innerPadding.calculateBottomPadding())
                        ) {
                            when (currentScreen) {
                                is Screen.Home -> {
                                    HomeScreen(
                                        viewModel = viewModel,
                                        onAddTrackClick = { showAddSongDialog = true },
                                        onScanAudioClick = { triggerStoragePermissions() }
                                    )
                                }
                                is Screen.Search -> {
                                    SearchScreen(viewModel = viewModel)
                                }
                                is Screen.Library -> {
                                    LibraryScreen(
                                        viewModel = viewModel,
                                        onAddPlaylistClick = { showAddPlaylistDialog = true }
                                    )
                                }
                                is Screen.PlaylistDetail -> {
                                    PlaylistDetailScreen(
                                        viewModel = viewModel,
                                        onBackTap = { viewModel.navigateTo(Screen.Library) }
                                    )
                                }
                            }
                        }
                    }

                    // 1. Sliding Deck Overlay Player
                    AnimatedVisibility(
                        visible = isPlayerExpanded,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
                        ) + fadeIn(),
                        exit = slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) + fadeOut()
                    ) {
                        ExpandedPlayerScreen(
                            viewModel = viewModel,
                            onCollapseTap = { viewModel.setPlayerExpanded(false) }
                        )
                    }

                    // 2. Custom Creation Dialog overlays
                    if (showAddSongDialog) {
                        AddSongDialog(
                            onDismiss = { showAddSongDialog = false },
                            onSaveSong = { title, artist, album, url, cover ->
                                viewModel.addSongToLibrary(title, artist, album, url, cover)
                                showAddSongDialog = false
                            }
                        )
                    }

                    if (showAddPlaylistDialog) {
                        AddPlaylistDialog(
                            onDismiss = { showAddPlaylistDialog = false },
                            onCreate = { name, description ->
                                viewModel.createPlaylist(name, description)
                                showAddPlaylistDialog = false
                            }
                        )
                    }
                }
            }
        }
    }

    private fun triggerStoragePermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.scanLocalAudioFiles(this)
                Toast.makeText(this, "Memindai file musik lokal...", Toast.LENGTH_SHORT).show()
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }
}
