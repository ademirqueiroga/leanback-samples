package me.admqueiroga.transportcontrols.playback

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.KeyEvent
import android.view.View
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.Action
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.PlaybackControlsRow
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import me.admqueiroga.transportcontrols.LikedMovies
import me.admqueiroga.transportcontrols.Movie
import me.admqueiroga.transportcontrols.MyList
import java.util.*

class AwesomeTransportControlGlue(
    context: Context,
    playerAdapter: AwesomeMediaPlayerAdapter,
) : PlaybackTransportControlGlue<AwesomeMediaPlayerAdapter>(context, playerAdapter) {

    private val playlist = ArrayList<Movie>()
    private val shuffledPositions = ArrayList<Int>()
    var playlistPosition = 0
        private set

    // Primary actions
    private val forwardAction = PlaybackControlsRow.FastForwardAction(context)
    private val rewindAction = PlaybackControlsRow.RewindAction(context)
    private val nextAction = PlaybackControlsRow.SkipNextAction(context)
    private val previousAction = PlaybackControlsRow.SkipPreviousAction(context)

    // Secondary actions
    private val thumbsUpAction = PlaybackControlsRow.ThumbsUpAction(context)
    private val shuffleAction = PlaybackControlsRow.ShuffleAction(context)
    private val repeatAction = PlaybackControlsRow.RepeatAction(context)
    private val myListAction = MyListAction(context)

    private val currentMovie: Movie
        get() = getMovie(playlistPosition)

    init {
        isSeekEnabled = true
    }

    override fun onCreatePrimaryActions(primaryActionsAdapter: ArrayObjectAdapter) {
        primaryActionsAdapter.add(previousAction)
        primaryActionsAdapter.add(rewindAction)
        super.onCreatePrimaryActions(primaryActionsAdapter)
        primaryActionsAdapter.add(forwardAction)
        primaryActionsAdapter.add(nextAction)
    }

    override fun onCreateSecondaryActions(secondaryActionsAdapter: ArrayObjectAdapter) {
        thumbsUpAction.index = PlaybackControlsRow.ThumbsUpAction.INDEX_OUTLINE
        secondaryActionsAdapter.add(thumbsUpAction)
        secondaryActionsAdapter.add(shuffleAction)
        secondaryActionsAdapter.add(repeatAction)
        secondaryActionsAdapter.add(myListAction)
    }

    override fun onActionClicked(action: Action?) {
        when (action) {
            thumbsUpAction,
            shuffleAction,
            repeatAction,
            myListAction -> onSecondaryActionPressed(action)
            forwardAction -> {
                playerAdapter.fastForward()
                onUpdateProgress()
            }
            rewindAction -> {
                playerAdapter.rewind()
                onUpdateProgress()
            }
            else -> super.onActionClicked(action)
        }
    }

    override fun onPreparedStateChanged() {
        super.onPreparedStateChanged()
        currentMovie.let(::updateMovieInfo)
    }

    override fun next() {
        playlistPosition = when {
            // If Repeat mode is enabled, behaves like a circular list.
            playerAdapter.repeatMode != PlaybackControlsRow.RepeatAction.INDEX_NONE -> {
                (playlistPosition + 1).mod(playlist.size)
            }
            playlistPosition == playlist.lastIndex -> {
                return
            }
            else -> {
                (playlistPosition + 1).coerceAtMost(playlist.lastIndex)
            }
        }
        when (playerAdapter.shuffleEnabled) {
            false -> loadMovie(playlistPosition)
            else -> when (shuffledPositions[playlistPosition]) {
                playlistPosition -> {
                    playlistPosition = (playlistPosition + 1).mod(playlist.size)
                    loadMovie(shuffledPositions[playlistPosition])
                }
                else -> loadMovie(shuffledPositions[playlistPosition])
            }
        }
    }

    override fun previous() {
        if (currentPosition > 10_000) seekTo(0) else {
            if (playlistPosition > 0) {
                playlistPosition -= 1
                when (playerAdapter.shuffleEnabled) {
                    true -> loadMovie(playlistPosition)
                    else -> loadMovie(shuffledPositions[playlistPosition])
                }
            }
        }
    }

    private fun onSecondaryActionPressed(action: Action) {
        val adapter = controlsRow.secondaryActionsAdapter as? ArrayObjectAdapter ?: return
        if (action is PlaybackControlsRow.MultiAction) {
            action.nextIndex()
            notifyItemChanged(adapter, action)
        }
        when (action) {
            shuffleAction -> {
                playerAdapter.setShuffleAction(shuffleAction.index)
                if (shuffleAction.index == PlaybackControlsRow.ShuffleAction.INDEX_ON) {
                    shuffledPositions.shuffle()
                }
            }
            repeatAction -> playerAdapter.setRepeatAction(repeatAction.index)
            thumbsUpAction -> currentMovie.let(LikedMovies::toggle)
            myListAction -> currentMovie.let(MyList::toggle)
        }
    }

    private fun updateMovieInfo(movie: Movie?) {
        if (movie != null) {
            title = movie.title
            subtitle = movie.description
            Glide.with(context).asBitmap().load(movie.cardImageUrl).into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    controlsRow.setImageBitmap(context, resource)
                    host.notifyPlaybackRowChanged()
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    controlsRow.setImageBitmap(context, null)
                    host.notifyPlaybackRowChanged()
                }
            })
            val secondaryActionsAdapter = controlsRow.secondaryActionsAdapter as ArrayObjectAdapter
            myListAction.index = when (MyList.contains(movie)) {
                true -> MyListAction.INDEX_REMOVE
                else -> MyListAction.INDEX_ADD
            }
            secondaryActionsAdapter.notifyItemRangeChanged(secondaryActionsAdapter.indexOf(myListAction), 1)
            thumbsUpAction.index = when (LikedMovies.contains(movie)) {
                true -> PlaybackControlsRow.ThumbsUpAction.INDEX_SOLID
                else -> PlaybackControlsRow.ThumbsUpAction.INDEX_OUTLINE
            }
            secondaryActionsAdapter.notifyItemRangeChanged(secondaryActionsAdapter.indexOf(thumbsUpAction), 1)
        } else {
            title = null
            subtitle = null
        }
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

    override fun onPlayCompleted() {
        super.onPlayCompleted()
        when (repeatAction.index) {
            PlaybackControlsRow.RepeatAction.INDEX_ONE -> {
                seekTo(0)
                playerAdapter.play()
            }
            PlaybackControlsRow.RepeatAction.INDEX_ALL -> {
                playerAdapter.next()
            }
            else -> {
                seekTo(0)
                onUpdateProgress()
            }
        }
    }

    private fun getMovie(position: Int): Movie = when (playerAdapter.shuffleEnabled) {
        true -> playlist[position]
        else -> playlist[shuffledPositions[position]]
    }

    fun loadMovie(playlistPosition: Int) {
        val movie = getMovie(playlistPosition)
        this.playlistPosition = playlistPosition
        playerAdapter.setDataSource(Uri.parse(movie.videoUrl))
        updateMovieInfo(movie)
        playWhenPrepared()
    }

    fun setPlaylist(movies: List<Movie>) {
        playlist.clear()
        playlist.addAll(movies)
        shuffledPositions.clear()
        for (i in playlist.indices) {
            shuffledPositions.add(i)
        }
    }

    fun getPlaylist(): List<Movie> {
        return when (playerAdapter.shuffleEnabled) {
            true -> Collections.unmodifiableList(playlist)
            else -> {
                val shuffledPlaylist = ArrayList<Movie>(playlist.size)
                for (i in playlist.indices) {
                    shuffledPlaylist.add(playlist[shuffledPositions[i]])
                }
                Collections.unmodifiableList(shuffledPlaylist)
            }
        }
    }

}
