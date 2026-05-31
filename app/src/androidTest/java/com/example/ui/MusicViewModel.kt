package com.example.ui

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.MusicRepository
import com.example.data.Playlist
import com.example.data.Song
import com.example.player.MediaPlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    object Home : Screen()
    object Search : Screen()
    object Library : Screen()
    object PlaylistDetail : Screen()
}

class MusicViewModel(
    private val repository: MusicRepository,
    private val playerManager: MediaPlayerManager
) : ViewModel() {

    private val TAG = "MusicViewModel"

    // Backing database states
    val allSongs: StateFlow<List<Song>> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<Song>> = repository.favoriteSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlaylists: StateFlow<List<Playlist>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // App Navigation & Session States
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _isPlayerExpanded = MutableStateFlow(false)
    val isPlayerExpanded: StateFlow<Boolean> = _isPlayerExpanded.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    private val _playlistSongs = MutableStateFlow<List<Song>>(emptyList())
    val playlistSongs: StateFlow<List<Song>> = _playlistSongs.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filter outcomes
    val searchResults: StateFlow<List<Song>> = combine(allSongs, _searchQuery) { songs, query ->
        if (query.isBlank()) {
            songs
        } else {
            songs.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.artist.contains(query, ignoreCase = true) ||
                it.album.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Player Playback Syncs
    val currentSong = playerManager.currentSong
    val isPlaying = playerManager.isPlaying
    val playbackProgress = playerManager.playbackProgress
    val currentPositionMs = playerManager.currentPositionMs
    val isBuffering = playerManager.isBuffering

    // Playback Queue
    private var playbackQueue = listOf<Song>()
    private var currentQueueIndex = -1

    init {
        viewModelScope.launch {
            // Seed library with default songs if DB is empty
            repository.seedDefaultSongsIfNeeded()
        }

        // Setup Player completion automatically to skip to next track
        playerManager.setOnSongCompletedListener {
            skipNext()
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
        _isPlayerExpanded.value = false // collapse visual player when moving tabs
    }

    fun setPlayerExpanded(expanded: Boolean) {
        _isPlayerExpanded.value = expanded
    }

    fun selectPlaylist(playlist: Playlist) {
        _selectedPlaylist.value = playlist
        _currentScreen.value = Screen.PlaylistDetail
        viewModelScope.launch {
            repository.getSongsInPlaylist(playlist.id).collect {
                _playlistSongs.value = it
            }
        }
    }

    fun createPlaylist(name: String, description: String? = null) {
        viewModelScope.launch {
            repository.createPlaylist(name, description)
        }
    }

    fun deletePlaylist(playlistId: Int) {
        viewModelScope.launch {
            // Reset selected if it is the deleted one
            if (_selectedPlaylist.value?.id == playlistId) {
                _selectedPlaylist.value = null
                _currentScreen.value = Screen.Library
            }
            repository.deletePlaylist(playlistId)
        }
    }

    fun addSongToPlaylist(playlistId: Int, songId: Int) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
            // Trigger refresh
            _selectedPlaylist.value?.let { current ->
                if (current.id == playlistId) {
                    _playlistSongs.value = repository.getSongsInPlaylist(playlistId).first()
                }
            }
        }
    }

    fun removeSongFromPlaylist(playlistId: Int, songId: Int) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
            _selectedPlaylist.value?.let { current ->
                if (current.id == playlistId) {
                    _playlistSongs.value = repository.getSongsInPlaylist(playlistId).first()
                }
            }
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.updateFavoriteStatus(song.id, !song.isFavorite)
            // If the currently playing track favorite state changed, recreate Song instance to sync state flow
            if (currentSong.value?.id == song.id) {
                // Trigger state updates
            }
        }
    }

    fun addSongToLibrary(title: String, artist: String, album: String, url: String, coverUrl: String) {
        viewModelScope.launch {
            val finalCover = if (coverUrl.isBlank()) {
                "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400" // default modern festival cover
            } else {
                coverUrl
            }
            val newSong = Song(
                title = title,
                artist = artist,
                album = album,
                durationMs = 240000, // 4 mins default
                durationString = "4:00",
                audioUrl = url,
                albumArtUrl = finalCover,
                isLocal = !url.startsWith("http")
            )
            repository.insertSong(newSong)
        }
    }

    fun deleteSong(songId: Int) {
        viewModelScope.launch {
            repository.deleteSong(songId)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // --- PLAYBACK ---
    fun playSongInList(song: Song, list: List<Song>) {
        playbackQueue = list
        currentQueueIndex = list.indexOfFirst { it.id == song.id }
        if (currentQueueIndex == -1) {
            playbackQueue = listOf(song)
            currentQueueIndex = 0
        }
        playerManager.play(song)
    }

    fun togglePlayPause() {
        if (currentSong.value != null) {
            playerManager.togglePlayPause()
        } else {
            // Play first song in list
            val targetList = allSongs.value
            if (targetList.isNotEmpty()) {
                playSongInList(targetList.first(), targetList)
            }
        }
    }

    fun skipNext() {
        if (playbackQueue.isEmpty() && allSongs.value.isNotEmpty()) {
            playbackQueue = allSongs.value
        }
        if (playbackQueue.isNotEmpty()) {
            currentQueueIndex = (currentQueueIndex + 1) % playbackQueue.size
            playerManager.play(playbackQueue[currentQueueIndex])
        }
    }

    fun skipPrevious() {
        if (playbackQueue.isEmpty() && allSongs.value.isNotEmpty()) {
            playbackQueue = allSongs.value
        }
        if (playbackQueue.isNotEmpty()) {
            currentQueueIndex = if (currentQueueIndex - 1 < 0) {
                playbackQueue.size - 1
            } else {
                currentQueueIndex - 1
            }
            playerManager.play(playbackQueue[currentQueueIndex])
        }
    }

    fun seekTo(progress: Float) {
        playerManager.seekTo(progress)
    }

    // --- LOCAL PHYSICAL SOUND FILE INTEGRATION ---
    fun scanLocalAudioFiles(context: Context) {
        viewModelScope.launch {
            try {
                val projection = arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DURATION
                )
                val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
                val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

                val query = context.contentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    null,
                    sortOrder
                )

                query?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val title = cursor.getString(titleColumn) ?: "Unknown Track"
                        val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                        val album = cursor.getString(albumColumn) ?: "Unknown Album"
                        val durationMs = cursor.getLong(durationColumn)

                        val contentUri: Uri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        )

                        val minutes = (durationMs / 1000) / 60
                        val seconds = (durationMs / 1000) % 60
                        val durationString = String.format("%02d:%02d", minutes, seconds)

                        // Check if song already registered matching filePath
                        val existingSongs = allSongs.value
                        val alreadyScanned = existingSongs.any { it.filePath == contentUri.toString() }

                        if (!alreadyScanned) {
                            val scannedSong = Song(
                                title = title,
                                artist = artist,
                                album = album,
                                durationMs = durationMs,
                                durationString = durationString,
                                audioUrl = contentUri.toString(),
                                albumArtUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400", // placeholder
                                isFavorite = false,
                                filePath = contentUri.toString(),
                                isLocal = true
                            )
                            repository.insertSong(scannedSong)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error scanning local sound files", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}

class MusicViewModelFactory(
    private val repository: MusicRepository,
    private val playerManager: MediaPlayerManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MusicViewModel(repository, playerManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
