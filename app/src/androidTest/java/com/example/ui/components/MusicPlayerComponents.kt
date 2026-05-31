package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.Playlist
import com.example.data.Song
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyGreenBright
import com.example.ui.theme.TextGrey
import com.example.ui.theme.TextLight

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackRow(
    song: Song,
    isPlaying: Boolean,
    isCurrent: Boolean,
    onPlayClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) Color(0xFF1E2F23) else Color(0xFF1A1A1A)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = onPlayClick,
                onLongClick = onLongClick
            )
            .testTag("track_row_${song.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            // Album artwork with high quality Unsplash lofi load or fallback
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.DarkGray)
            ) {
                AsyncImage(
                    model = song.albumArtUrl,
                    contentDescription = "Album art for ${song.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (isCurrent && isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Small animated wave effect indicator
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 0.6f,
                            targetValue = 1.2f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "scale"
                        )
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Playing",
                            tint = SpotifyGreen,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text Metadata block
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(
                    text = song.title,
                    color = if (isCurrent) SpotifyGreenBright else TextLight,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.basicMarquee()
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (song.isLocal) {
                        Icon(
                            imageVector = Icons.Default.OfflinePin,
                            contentDescription = "Offline cached",
                            tint = SpotifyGreen,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 4.dp)
                        )
                    }
                    Text(
                        text = "${song.artist} • ${song.album}",
                        color = TextGrey,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Interactive Actions Bar
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier.testTag("favorite_button_${song.id}")
            ) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Toggle favorite status",
                    tint = if (song.isFavorite) SpotifyGreen else TextGrey,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun BottomMiniPlayer(
    song: Song?,
    isPlaying: Boolean,
    progress: Float,
    isBuffering: Boolean,
    onTogglePlay: () -> Unit,
    onSkipNext: () -> Unit,
    onPlayerTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (song == null) return

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF222222)
        ),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onPlayerTap)
            .testTag("bottom_mini_player")
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .fillMaxWidth()
            ) {
                AsyncImage(
                    model = song.albumArtUrl,
                    contentDescription = "Cover",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        color = TextLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.basicMarquee()
                    )
                    Text(
                        text = song.artist,
                        color = TextGrey,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (isBuffering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp).padding(4.dp),
                        color = SpotifyGreen,
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier.testTag("mini_play_pause")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play or Pause music",
                            tint = TextLight,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier.testTag("mini_skip_next")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Play next track",
                        tint = TextLight,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Sleek Spotify-like flat line progress bar underlay
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = SpotifyGreen,
                trackColor = Color(0xFF444444)
            )
        }
    }
}

@Composable
fun PlaylistGridCard(
    playlist: Playlist,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(115.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onTap)
            .testTag("playlist_card_${playlist.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Elegant background gradient design
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF2C3E50),
                                Color(0xFF000000)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Default.QueueMusic,
                    contentDescription = null,
                    tint = SpotifyGreen,
                    modifier = Modifier.size(32.dp)
                )

                Column {
                    Text(
                        text = playlist.name,
                        color = TextLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = playlist.description ?: "Custom playlist",
                        color = TextGrey,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun AddPlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var name by remember { mutableStateFlowOf("") }
    var description by remember { mutableStateFlowOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF262626)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Buat Daftar Putar Baru",
                    color = TextLight,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Playlist", color = TextGrey) },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight,
                        focusedLabelColor = SpotifyGreen,
                        unfocusedLabelColor = TextGrey,
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("playlist_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi (Opsional)", color = TextGrey) },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight,
                        focusedLabelColor = SpotifyGreen,
                        unfocusedLabelColor = TextGrey,
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("playlist_desc_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = TextGrey)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onCreate(name, description)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                        enabled = name.isNotBlank()
                    ) {
                        Text("Buat", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddSongDialog(
    onDismiss: () -> Unit,
    onSaveSong: (String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateFlowOf("") }
    var artist by remember { mutableStateFlowOf("") }
    var album by remember { mutableStateFlowOf("") }
    var audioUrl by remember { mutableStateFlowOf("") }
    var coverUrl by remember { mutableStateFlowOf("") }

    var useSampleStream by remember { mutableStateFlowOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF262626)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Tambah Lagu Baru",
                    color = TextLight,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Lagu", color = TextGrey) },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = TextLight,
                        focusedLabelColor = SpotifyGreen,
                        unfocusedTextColor = TextLight,
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("song_title_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artis / Band", color = TextGrey) },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = TextLight,
                        focusedLabelColor = SpotifyGreen,
                        unfocusedTextColor = TextLight,
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("song_artist_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = album,
                    onValueChange = { album = it },
                    label = { Text("Album", color = TextGrey) },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = TextLight,
                        focusedLabelColor = SpotifyGreen,
                        unfocusedTextColor = TextLight,
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("song_album_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { useSampleStream = !useSampleStream }
                ) {
                    Checkbox(
                        checked = useSampleStream,
                        onCheckedChange = { useSampleStream = it },
                        colors = CheckboxDefaults.colors(checkedColor = SpotifyGreen)
                    )
                    Text(
                        text = "Gunakan Sampel Aliran (Lofi)",
                        color = TextLight,
                        fontSize = 13.sp
                    )
                }

                if (!useSampleStream) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = audioUrl,
                        onValueChange = { audioUrl = it },
                        label = { Text("URL Link Audio (MP3/AAC)", color = TextGrey) },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = TextLight,
                            focusedLabelColor = SpotifyGreen,
                            unfocusedTextColor = TextLight,
                            focusedContainerColor = Color(0xFF1E1E1E),
                            unfocusedContainerColor = Color(0xFF1E1E1E)
                        ),
                        placeholder = { Text("https://example.com/audio.mp3", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("song_url_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = coverUrl,
                        onValueChange = { coverUrl = it },
                        label = { Text("URL Link Gambar Album (Opsional)", color = TextGrey) },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = TextLight,
                            focusedLabelColor = SpotifyGreen,
                            unfocusedTextColor = TextLight,
                            focusedContainerColor = Color(0xFF1E1E1E),
                            unfocusedContainerColor = Color(0xFF1E1E1E)
                        ),
                        placeholder = { Text("https://unsplash.com/some_photo.jpg", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("song_cover_input")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = TextGrey)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && artist.isNotBlank() && album.isNotBlank()) {
                                val finalUrl = if (useSampleStream) {
                                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3" // Seed sample fallback
                                } else {
                                    audioUrl
                                }
                                onSaveSong(title, artist, album, finalUrl, coverUrl)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                        enabled = title.isNotBlank() && artist.isNotBlank() && album.isNotBlank()
                    ) {
                        Text("Simpan", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T> mutableStateFlowOf(value: T): MutableState<T> = mutableStateOf(value)
