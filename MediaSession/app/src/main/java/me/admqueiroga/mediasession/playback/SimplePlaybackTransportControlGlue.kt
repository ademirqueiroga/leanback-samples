package me.admqueiroga.mediasession.playback

import android.content.Context
import android.net.Uri
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.leanback.media.MediaPlayerAdapter
import androidx.leanback.media.PlaybackControlGlue
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.Action
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.PlaybackControlsRow
import me.admqueiroga.mediasession.data.MovieList

class SimplePlaybackTransportControlGlue(
    context: Context,
    playerAdapter: MediaPlayerAdapter,
) : PlaybackTransportControlGlue<MediaPlayerAdapter>(context, playerAdapter) {

    private val mediaSession = MediaSessionCompat(context, "VideoPlayback")

    private var playbackState: Int = -1
        set(value) {
            if (field != value) {
                field = value
                invalidatePlaybackState()
            }
        }

    // Used for debugging only
    private var supportActionsNames: String = ""

    private var canSkipToPrevious = false
    private var canSkipToNext = false

    // Primary actions
    private val forwardAction = PlaybackControlsRow.FastForwardAction(context)
    private val rewindAction = PlaybackControlsRow.RewindAction(context)
    private val nextAction = PlaybackControlsRow.SkipNextAction(context)
    private val previousAction = PlaybackControlsRow.SkipPreviousAction(context)

    var currentVideoIndex = 0
        private set

    init {
        Log.i(TAG, "Creating new instance...")
        mediaSession.setCallback(SimpleMediaSessionCallback())
        playbackState = PlaybackStateCompat.STATE_NONE
        isSeekEnabled = true
    }

    override fun onCreatePrimaryActions(primaryActionsAdapter: ArrayObjectAdapter?) {
        primaryActionsAdapter ?: return
        primaryActionsAdapter.add(previousAction)
        primaryActionsAdapter.add(rewindAction)
        super.onCreatePrimaryActions(primaryActionsAdapter)
        primaryActionsAdapter.add(forwardAction)
        primaryActionsAdapter.add(nextAction)
    }

    override fun onActionClicked(action: Action?) {
        when (action) {
            forwardAction -> fastForward()
            rewindAction -> rewind()
            else -> super.onActionClicked(action)
        }
        onUpdateProgress()
    }

    override fun onHostStart() {
        super.onHostStart()
        Log.i(TAG, "Activating MediaSession...")
        mediaSession.isActive = true
    }

    override fun onHostPause() {
        super.onHostPause()
        Log.i(TAG, "Deactivating MediaSession...")
        mediaSession.isActive = false
    }

    override fun onDetachedFromHost() {
        super.onDetachedFromHost()
        Log.i(TAG, "Releasing MediaSession...")
        mediaSession.release()
    }

    override fun onUpdateBufferedProgress() {
        super.onUpdateBufferedProgress()
        invalidatePlaybackState()
    }

    override fun onPreparedStateChanged() {
        super.onPreparedStateChanged()
        Log.i(TAG, "onPreparedStateChanged()")
        invalidatePlaybackState()
    }

    override fun onPlayStateChanged() {
        super.onPlayStateChanged()
        Log.i(TAG, "onPlayStateChanged()")
        playbackState = when {
            isPlaying -> PlaybackStateCompat.STATE_PLAYING
            else -> PlaybackStateCompat.STATE_PAUSED
        }
    }

    override fun onPlayCompleted() {
        super.onPlayCompleted()
        Log.i(TAG, "onPlayCompleted()")
        playbackState = PlaybackStateCompat.STATE_NONE
    }

    override fun getSupportedActions(): Long {
        var actions = ACTION_REWIND xor ACTION_PLAY_PAUSE xor ACTION_FAST_FORWARD
        if (canSkipToNext) {
            actions = actions xor ACTION_SKIP_TO_NEXT
        }
        if (canSkipToPrevious) {
            actions = actions xor ACTION_SKIP_TO_PREVIOUS
        }
        return actions.toLong()
    }

    override fun next() {
        Log.i(TAG, "next()")
        if (canSkipToNext) {
            loadMovie(++currentVideoIndex)
        }
    }

    override fun previous() {
        Log.i(TAG, "previous()")
        if (canSkipToPrevious) {
            loadMovie(--currentVideoIndex)
        }
    }

    private fun mediaSessionSupportedActions(): Long {
        val supportedActions = supportedActions
        val supportedActionsNames = ArrayList<String>()
        var mediaSessionSupportedActions = 0L
        if ((supportedActions and PlaybackControlGlue.ACTION_PLAY_PAUSE.toLong()) != 0L) {
            mediaSessionSupportedActions = PlaybackStateCompat.ACTION_PAUSE xor
                    PlaybackStateCompat.ACTION_PLAY xor
                    PlaybackStateCompat.ACTION_PLAY_PAUSE
            supportedActionsNames.add("Play/Pause")
        }
        if ((supportedActions and PlaybackControlGlue.ACTION_REWIND.toLong()) != 0L) {
            mediaSessionSupportedActions = mediaSessionSupportedActions xor PlaybackStateCompat.ACTION_REWIND
            supportedActionsNames.add("Rewind")
        }
        if ((supportedActions and PlaybackControlGlue.ACTION_FAST_FORWARD.toLong()) != 0L) {
            mediaSessionSupportedActions = mediaSessionSupportedActions xor PlaybackStateCompat.ACTION_FAST_FORWARD
            supportedActionsNames.add("FastForward")
        }
        if ((supportedActions and PlaybackControlGlue.ACTION_SKIP_TO_NEXT.toLong()) != 0L) {
            mediaSessionSupportedActions = mediaSessionSupportedActions xor PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            supportedActionsNames.add("SkipNext")
        }
        if ((supportedActions and PlaybackControlGlue.ACTION_SKIP_TO_PREVIOUS.toLong()) != 0L) {
            mediaSessionSupportedActions = mediaSessionSupportedActions xor PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            supportedActionsNames.add("SkipPrevious")
        }
        val newSupportedActionsNames = supportedActionsNames.joinToString()
        if (supportActionsNames != newSupportedActionsNames) {
            supportActionsNames = newSupportedActionsNames
            Log.i(TAG, "mediaSessionSupportedActions: $newSupportedActionsNames")
        }
        return mediaSessionSupportedActions
    }

    private fun invalidatePlaybackState() {
        val playbackStateBuilder = PlaybackStateCompat.Builder()
            .setState(playbackState, currentPosition, 1.0F)
            .setActions(mediaSessionSupportedActions())
            .setBufferedPosition(bufferedPosition)
        mediaSession.setPlaybackState(playbackStateBuilder.build())
    }

    fun loadMovie(index: Int) {
        Log.i(TAG, "loadMovie(index:$index)")
        playerAdapter.setDataSource(Uri.parse(MovieList.list[index].videoUrl))
        playWhenPrepared()
        currentVideoIndex = index
        canSkipToPrevious = index > 0
        canSkipToNext = index < MovieList.list.lastIndex
    }

    fun rewind() {
        Log.i(TAG, "rewind()")
        playbackState = PlaybackStateCompat.STATE_REWINDING
        seekTo(currentPosition - 10_000)
        onUpdateProgress()
    }

    fun fastForward() {
        Log.i(TAG, "fastForward()")
        playbackState = PlaybackStateCompat.STATE_FAST_FORWARDING
        seekTo(currentPosition + 10_000)
        onUpdateProgress()
    }

    fun onStartBuffering() {
        Log.i(TAG, "onStartBuffering()")
        playbackState = PlaybackStateCompat.STATE_BUFFERING
    }

    fun onFinishedBuffering() {
        Log.i(TAG, "onFinishedBuffering()")
        playbackState = when (isPlaying) {
            true -> PlaybackStateCompat.STATE_PLAYING
            else -> PlaybackStateCompat.STATE_PAUSED
        }
    }

    fun onError(errorCode: Int, errorMessage: CharSequence?) {
        Log.i(TAG, "onError($errorCode, $errorMessage)")
        val playbackStateBuilder = PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_ERROR, currentPosition, 1.0F)
            .setActions(mediaSessionSupportedActions())
            .setBufferedPosition(bufferedPosition)
            .setErrorMessage(errorCode, errorMessage)
        mediaSession.setPlaybackState(playbackStateBuilder.build())
    }

    private inner class SimpleMediaSessionCallback : MediaSessionCompat.Callback() {

        private val TAG = "SimpleMediaSessionCallback"

        override fun onPlay() {
            Log.i(TAG, "onPlay()")
            play()
        }

        override fun onPause() {
            Log.i(TAG, "onPause()")
            pause()
        }

        override fun onSkipToNext() {
            Log.i(TAG, "onSkipToNext()")
            next()
            playWhenPrepared()
        }

        override fun onSkipToPrevious() {
            Log.i(TAG, "onSkipToPrevious()")
            previous()
            playWhenPrepared()
        }

        override fun onRewind() {
            Log.i(TAG, "onRewind()")
            rewind()
        }

        override fun onFastForward() {
            Log.i(TAG, "onFastForward()")
            fastForward()
        }

        override fun onSeekTo(pos: Long) {
            Log.i(TAG, "onSeekTo($pos)")
            seekTo(pos)
        }

    }

    companion object {
        const val TAG = "SimplePlaybackTransportControlGlue"
    }

}