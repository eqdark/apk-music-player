package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.Playlist
import com.example.data.Song
import com.example.ui.MusicViewModel
import com.example.ui.Screen
import com.example.ui.components.PlaylistGridCard
import com.example.ui.components.TrackRow
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyGreenBright
import com.example.ui.theme.TextGrey
import com.example.ui.theme.TextLight
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    onAddTrackClick: () -> Unit,
    onScanAudioClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val songs by viewModel.allSongs.collectAsStateWithLifecycle()
    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 4..11 -> "Selamat Pagi 🌅"
            in 12..15 -> "Selamat Siang ☀️"
            in 16..18 -> "Selamat Sore 🌇"
            else -> "Selamat Malam 🌙"
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = greeting,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextLight,
                        fontSize = 28.sp
                    )
                },
                actions = {
                    // Quick Action: Scan local physical device directories
                    IconButton(
                        onClick = onScanAudioClick,
                        modifier = Modifier.testTag("scan_audios_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Scan device storage for tracks",
                            tint = SpotifyGreen
                        )
                    }
                    // Quick Action: Add standard track url manually
                    IconButton(
                        onClick = onAddTrackClick,
                        modifier = Modifier.testTag("add_track_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add remote or sample tracks",
                            tint = SpotifyGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        containerColor = Color.Black,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = 90.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // High highlight feature banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF2E6B47), Color(0xFF161616)),
                                    radius = 700f
                                )
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = SpotifyGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Halo, Nikmati Musik Anda!",
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Daftar putar lofi premium siap dimainkan secara offline kapan saja. Ketuk tombol folder di atas untuk memindai lagu di memori HP Anda.",
                                color = TextGrey,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Section title
            item {
                Text(
                    text = "Daftar Lagu Lofi & Offline",
                    color = TextLight,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (songs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = SpotifyGreen)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Memuat file audio...", color = TextGrey, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                items(songs) { song ->
                    TrackRow(
                        song = song,
                        isPlaying = isPlaying,
                        isCurrent = currentSong?.id == song.id,
                        onPlayClick = { viewModel.playSongInList(song, songs) },
                        onFavoriteToggle = { viewModel.toggleFavorite(song) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pencarian", fontWeight = FontWeight.Bold, color = TextLight) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Elegant styling search box
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextGrey
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search query",
                                tint = TextLight
                            )
                        }
                    }
                },
                placeholder = { Text("Cari Lagu, Artis, atau Album...", color = TextGrey) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = TextLight,
                    unfocusedTextColor = TextLight,
                    focusedLabelColor = SpotifyGreen,
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E),
                    cursorColor = SpotifyGreen
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_text_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = TextGrey,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Lagu tidak ditemukan",
                            color = TextLight,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Coba kata kunci lain atau tambahkan lagu baru di beranda.",
                            color = TextGrey,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 90.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(searchResults) { song ->
                        TrackRow(
                            song = song,
                            isPlaying = isPlaying,
                            isCurrent = currentSong?.id == song.id,
                            onPlayClick = { viewModel.playSongInList(song, searchResults) },
                            onFavoriteToggle = { viewModel.toggleFavorite(song) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: MusicViewModel,
    onAddPlaylistClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playlists by viewModel.allPlaylists.collectAsStateWithLifecycle()
    val favorites by viewModel.favoriteSongs.collectAsStateWithLifecycle()
    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val chunkedPlaylists = remember(playlists) { playlists.chunked(2) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Koleksi Musik", fontWeight = FontWeight.Bold, color = TextLight) },
                actions = {
                    IconButton(
                        onClick = onAddPlaylistClick,
                        modifier = Modifier.testTag("create_playlist_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = "Create new playlist",
                            tint = SpotifyGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = 90.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // First highlight: "Lagu yang Disukai" card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            // Quick play favorites
                            if (favorites.isNotEmpty()) {
                                viewModel.playSongInList(favorites.first(), favorites)
                            }
                        }
                        .testTag("liked_songs_card")
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF381460),
                                        Color(0xFF121212)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = SpotifyGreenBright,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Lagu yang Disukai",
                                color = TextLight,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${favorites.size} lagu favorit Anda",
                                color = TextGrey,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Playlists listing section
            item {
                Text(
                    text = "Daftar Putar Anda",
                    color = TextLight,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (playlists.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.QueueMusic,
                                contentDescription = null,
                                tint = TextGrey,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Belum ada Daftar Putar", color = TextGrey, fontSize = 14.sp)
                            TextButton(onClick = onAddPlaylistClick) {
                                Text("Buat Daftar Putar Baru", color = SpotifyGreen)
                            }
                        }
                    }
                }
            } else {
                items(chunkedPlaylists) { rowPlaylists ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowPlaylists.forEach { playlist ->
                            PlaylistGridCard(
                                playlist = playlist,
                                onTap = { viewModel.selectPlaylist(playlist) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowPlaylists.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    viewModel: MusicViewModel,
    onBackTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playlist by viewModel.selectedPlaylist.collectAsStateWithLifecycle()
    val pSongs by viewModel.playlistSongs.collectAsStateWithLifecycle()
    val allSongs by viewModel.allSongs.collectAsStateWithLifecycle()

    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()

    var showAddSongToPlaylistMenu by remember { mutableStateOf(false) }

    if (playlist == null) return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(playlist?.name ?: "", fontWeight = FontWeight.Bold, color = TextLight) },
                navigationIcon = {
                    IconButton(onClick = onBackTap) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextLight
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.deletePlaylist(playlist!!.id) },
                        modifier = Modifier.testTag("delete_playlist_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Hapus daftar putar ini",
                            tint = Color.Red
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSongToPlaylistMenu = true },
                containerColor = SpotifyGreen,
                modifier = Modifier.testTag("play_add_to_playlist_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlaylistAdd,
                    contentDescription = "Add track",
                    tint = Color.Black
                )
            }
        },
        containerColor = Color.Black,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = 90.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Description block
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF1E5235),
                                        Color(0xFF0F2015)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = null,
                            tint = SpotifyGreen,
                            modifier = Modifier.size(80.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = playlist?.name ?: "",
                        color = TextLight,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = playlist?.description ?: "Tanpa deskripsi",
                        color = TextGrey,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "${pSongs.size} Lagu",
                        color = SpotifyGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (pSongs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Daftar putar masih kosong", color = TextGrey, fontSize = 14.sp)
                            TextButton(onClick = { showAddSongToPlaylistMenu = true }) {
                                Text("Tambahkan Lagu", color = SpotifyGreen)
                            }
                        }
                    }
                }
            } else {
                items(pSongs) { song ->
                    TrackRow(
                        song = song,
                        isPlaying = isPlaying,
                        isCurrent = currentSong?.id == song.id,
                        onPlayClick = { viewModel.playSongInList(song, pSongs) },
                        onFavoriteToggle = { viewModel.toggleFavorite(song) },
                        onLongClick = {
                            // Remove song on long click
                            viewModel.removeSongFromPlaylist(playlist!!.id, song.id)
                        }
                    )
                }
            }
        }

        // Sub and select menu to append a song directly from local library database to this playlist
        if (showAddSongToPlaylistMenu) {
            AlertDialog(
                onDismissRequest = { showAddSongToPlaylistMenu = false },
                title = { Text("Tambahkan ke Daftar Putar", color = TextLight) },
                text = {
                    Box(modifier = Modifier.heightIn(max = 300.dp)) {
                        val songsNotAdded = allSongs.filter { s -> pSongs.none { it.id == s.id } }
                        if (songsNotAdded.isEmpty()) {
                            Text("Semua lagu koleksi Anda telah ditambahkan ke playlist ini.", color = TextGrey)
                        } else {
                            LazyColumn {
                                items(songsNotAdded) { song ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.addSongToPlaylist(playlist!!.id, song.id)
                                                showAddSongToPlaylistMenu = false
                                            }
                                            .padding(vertical = 10.dp)
                                    ) {
                                        AsyncImage(
                                            model = song.albumArtUrl,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color.Gray),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(song.title, color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text(song.artist, color = TextGrey, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAddSongToPlaylistMenu = false }) {
                        Text("Tutup", color = SpotifyGreen)
                    }
                },
                containerColor = Color(0xFF1E1E1E)
            )
        }
    }
}

@Composable
fun ExpandedPlayerScreen(
    viewModel: MusicViewModel,
    onCollapseTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val song by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val progress by viewModel.playbackProgress.collectAsStateWithLifecycle()
    val currentPositionMs by viewModel.currentPositionMs.collectAsStateWithLifecycle()
    val isBuffering by viewModel.isBuffering.collectAsStateWithLifecycle()

    if (song == null) return

    val currentProgSeconds = currentPositionMs / 1000
    val totalMs = song?.durationMs ?: 0L
    val totalSeconds = totalMs / 1000

    val elapsedString = String.format("%d:%02d", currentProgSeconds / 60, currentProgSeconds % 60)
    val totalString = String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF214E34),
                        Color(0xFF0F1B12),
                        Color(0xFF000000)
                    )
                )
            )
            .testTag("expanded_player")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Navigation control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCollapseTap) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse full player",
                        tint = TextLight,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "MEMUTAR SEKARANG",
                    color = TextLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                IconButton(onClick = { /* More operations */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = TextLight
                    )
                }
            }

            // Giant central album artwork
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                modifier = Modifier
                    .size(310.dp)
                    .padding(vertical = 12.dp)
            ) {
                AsyncImage(
                    model = song?.albumArtUrl,
                    contentDescription = "Album art cover image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Track details block (Title, subtitle author, Favorite icon)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song?.title ?: "",
                        color = TextLight,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = song?.artist ?: "",
                        color = TextGrey,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { song?.let { viewModel.toggleFavorite(it) } },
                    modifier = Modifier.testTag("player_favorite_toggle")
                ) {
                    Icon(
                        imageVector = if (song?.isFavorite == true) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Toggle favorite from player view",
                        tint = if (song?.isFavorite == true) SpotifyGreen else TextLight,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Timeline seekbar Slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = progress,
                    onValueChange = { viewModel.seekTo(it) },
                    colors = SliderDefaults.colors(
                        thumbColor = TextLight,
                        activeTrackColor = SpotifyGreen,
                        inactiveTrackColor = Color(0xFF444444)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("playback_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = elapsedString, color = TextGrey, fontSize = 12.sp)
                    Text(text = totalString, color = TextGrey, fontSize = 12.sp)
                }
            }

            // Central media play/skip buttons layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary actions: Shuffle
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = TextGrey,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Action: Previous
                IconButton(
                    onClick = { viewModel.skipPrevious() },
                    modifier = Modifier.testTag("player_skip_previous")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Skip to previous track",
                        tint = TextLight,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Master play/pause
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(SpotifyGreen)
                        .clickable(onClick = { viewModel.togglePlayPause() })
                        .testTag("player_play_pause_fab"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isBuffering) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play or Pause music",
                            tint = Color.Black,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Action: Next
                IconButton(
                    onClick = { viewModel.skipNext() },
                    modifier = Modifier.testTag("player_skip_next")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Skip to next track",
                        tint = TextLight,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Secondary actions: Repeat
                IconButton(onClick = { /* Toggle repeat mode */ }) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = TextGrey,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Bottom metadata info banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = null,
                    tint = SpotifyGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "VibeFlow Audio Engine Connected",
                    color = SpotifyGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
