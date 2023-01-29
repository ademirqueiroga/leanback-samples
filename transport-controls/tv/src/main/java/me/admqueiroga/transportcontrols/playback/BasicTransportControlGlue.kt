package me.admqueiroga.transportcontrols.playback

import android.content.Context
import android.net.Uri
import android.view.KeyEvent
import android.view.View
import androidx.leanback.media.PlaybackBaseControlGlue
import androidx.leanback.media.PlaybackGlue
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.*
import me.admqueiroga.transportcontrols.Movie

class BasicTransportControlGlue(
    context: Context,
    playerAdapter: BasicMediaPlayerAdapter,
) : PlaybackTransportControlGlue<BasicMediaPlayerAdapter>(context, playerAdapter) {

    // Primary actions
    private val forwardAction = PlaybackControlsRow.FastForwardAction(context)
    private val rewindAction = PlaybackControlsRow.RewindAction(context)
    private val nextAction = PlaybackControlsRow.SkipNextAction(context)
    private val previousAction = PlaybackControlsRow.SkipPreviousAction(context)

    init {
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
            forwardAction -> playerAdapter.fastForward()
            rewindAction -> playerAdapter.rewind()
            else -> super.onActionClicked(action)
        }
        onUpdateProgress()
    }
    val currentMovie: Movie
        get() = playerAdapter.playlist[playerAdapter.playlistPosition]
    // Event when ready state for play changes.
    override fun onPreparedStateChanged() {
        super.onPreparedStateChanged()
        playWhenPrepared()
        updateMovieInfo(currentMovie)
    }

    private fun updateMovieInfo(movie: Movie?) {
        title = movie?.title
        subtitle = movie?.description
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
        if (host.isControlsOverlayVisible || event.repeatCount > 0) {
            return super.onKey(v, keyCode, event)
        }
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_RIGHT -> if (event.action != KeyEvent.ACTION_DOWN) false else {
                onActionClicked(forwardAction)
                true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> if (event.action != KeyEvent.ACTION_DOWN) false else {
                onActionClicked(rewindAction)
                true
            }
            else -> super.onKey(v, keyCode, event)
        }
    }


    fun loadMovie(playlistPosition: Int) {
        playerAdapter.loadMovie(0)
    }

    fun setPlaylist(movies: List<Movie>) {
        playerAdapter.playlist.clear()
        playerAdapter.playlist.addAll(movies)
    }

}
