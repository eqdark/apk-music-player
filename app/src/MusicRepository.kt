package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class MusicRepository(private val musicDao: MusicDao) {

    val allSongs: Flow<List<Song>> = musicDao.getAllSongs()
    val favoriteSongs: Flow<List<Song>> = musicDao.getFavoriteSongs()
    val allPlaylists: Flow<List<Playlist>> = musicDao.getAllPlaylists()

    suspend fun getSongById(id: Int): Song? = musicDao.getSongById(id)

    suspend fun insertSong(song: Song): Long = musicDao.insertSong(song)

    suspend fun updateFavoriteStatus(songId: Int, isFavorite: Boolean) {
        musicDao.updateFavoriteStatus(songId, isFavorite)
    }

    suspend fun deleteSong(songId: Int) = musicDao.deleteSong(songId)

    suspend fun createPlaylist(name: String, description: String? = null): Long {
        return musicDao.insertPlaylist(Playlist(name = name, description = description))
    }

    suspend fun deletePlaylist(playlistId: Int) {
        musicDao.deletePlaylistWithSongs(playlistId)
    }

    suspend fun addSongToPlaylist(playlistId: Int, songId: Int) {
        musicDao.insertPlaylistSongCrossRef(PlaylistSongCrossRef(playlistId, songId))
    }

    suspend fun removeSongFromPlaylist(playlistId: Int, songId: Int) {
        musicDao.removeSongFromPlaylist(playlistId, songId)
    }

    fun getSongsInPlaylist(playlistId: Int): Flow<List<Song>> = musicDao.getSongsInPlaylist(playlistId)

    suspend fun seedDefaultSongsIfNeeded() {
        val currentSongs = allSongs.first()
        if (currentSongs.isEmpty()) {
            val defaultTracks = listOf(
                Song(
                    title = "Midnight Chill",
                    artist = "Lofi Sunset",
                    album = "Chillhop Dreams",
                    durationMs = 302000,
                    durationString = "5:02",
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                    albumArtUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=400"
                ),
                Song(
                    title = "Retro Synthesis",
                    artist = "Neon Skyline",
                    album = "Cyber Drive",
                    durationMs = 425000,
                    durationString = "7:05",
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                    albumArtUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=400"
                ),
                Song(
                    title = "Cloud Floating",
                    artist = "Aesthetic Beats",
                    album = "Fluffy Clouds",
                    durationMs = 302000,
                    durationString = "5:02",
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                    albumArtUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400"
                ),
                Song(
                    title = "Cozy Espresso",
                    artist = "Coffeehouse Session",
                    album = "Daily Grind Cafe",
                    durationMs = 318000,
                    durationString = "5:18",
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                    albumArtUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=400"
                ),
                Song(
                    title = "Autumn Leaves",
                    artist = "Rainy Street",
                    album = "Acoustic Whispers",
                    durationMs = 280000,
                    durationString = "4:40",
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3",
                    albumArtUrl = "https://images.unsplash.com/photo-1487180142328-0c4e37023af5?w=400"
                )
            )
            musicDao.insertSongs(defaultTracks)
        }
    }
}
