package com.example.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.example.data.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class MediaPlayerManager(private val context: Context) {

    private val TAG = "MediaPlayerManager"

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress // 0.0 to 1.0

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering

    private var onSongCompletedCallback: (() -> Unit)? = null

    init {
        initializePlayer()
    }

    private fun initializePlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setOnPreparedListener { mp ->
                    _isBuffering.value = false
                    mp.start()
                    _isPlaying.value = true
                    startProgressTracker()
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    _playbackProgress.value = 1.0f
                    _currentPositionMs.value = currentSong.value?.durationMs ?: 0L
                    stopProgressTracker()
                    onSongCompletedCallback?.invoke()
                }
                setOnErrorListener { mp, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    _isPlaying.value = false
                    _isBuffering.value = false
                    stopProgressTracker()
                    // Re-initialize if severe
                    resetPlayer()
                    true
                }
                setOnBufferingUpdateListener { _, percent ->
                    // Buffering update
                }
            }
        }
    }

    fun setOnSongCompletedListener(callback: () -> Unit) {
        onSongCompletedCallback = callback
    }

    fun play(song: Song) {
        initializePlayer()
        val prevSong = _currentSong.value
        _currentSong.value = song
        _isBuffering.value = true

        try {
            mediaPlayer?.apply {
                stopProgressTracker()
                reset()
                
                // Set audio attributes again after reset
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                if (song.filePath != null && song.isLocal) {
                    val uri = Uri.parse(song.filePath)
                    setDataSource(context, uri)
                } else {
                    setDataSource(song.audioUrl)
                }

                _playbackProgress.value = 0f
                _currentPositionMs.value = 0L
                prepareAsync()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error setting data source for song: ${song.title}", e)
            _isBuffering.value = false
            _isPlaying.value = false
        }
    }

    fun resume() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                _isPlaying.value = true
                startProgressTracker()
            }
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
                stopProgressTracker()
            }
        }
    }

    fun togglePlayPause() {
        if (isPlaying.value) {
            pause()
        } else {
            if (currentSong.value != null) {
                resume()
            }
        }
    }

    fun seekTo(progress: Float) {
        mediaPlayer?.let { mp ->
            val duration = mp.duration.toFloat()
            if (duration > 0) {
                val seekPosition = (progress * duration).toInt()
                mp.seekTo(seekPosition)
                _playbackProgress.value = progress
                _currentPositionMs.value = seekPosition.toLong()
            }
        }
    }

    fun release() {
        stopProgressTracker()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun resetPlayer() {
        try {
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing MediaPlayer", e)
        }
        mediaPlayer = null
        initializePlayer()
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isPlaying.value) {
                mediaPlayer?.let { mp ->
                    try {
                        if (mp.isPlaying && mp.duration > 0) {
                            val current = mp.currentPosition.toFloat()
                            val total = mp.duration.toFloat()
                            _playbackProgress.value = (current / total).coerceIn(0f, 1f)
                            _currentPositionMs.value = mp.currentPosition.toLong()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error calculating progress", e)
                    }
                }
                delay(250)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }
}
