package me.admqueiroga.transportcontrols.playback

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.view.isVisible
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.PlaybackGlue
import androidx.leanback.media.PlaybackGlue.PlayerCallback
import androidx.leanback.widget.*
import me.admqueiroga.transportcontrols.*
import me.admqueiroga.transportcontrols.MainFragment.Companion.EXTRA_MOVIES
import me.admqueiroga.transportcontrols.R

class PlaybackVideoFragment : VideoSupportFragment() {

    private lateinit var transportControlGlue: AwesomeTransportControlGlue
    private lateinit var fastForwardIndicatorView: View
    private lateinit var rewindIndicatorView: View

    private val upNextAdapter = ArrayObjectAdapter(CardPresenter())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = requireActivity().intent
        val movies = intent.getParcelableArrayListExtra(EXTRA_MOVIES) ?: emptyList<Movie>()

        transportControlGlue = AwesomeTransportControlGlue(
            context = requireContext(),
            playerAdapter = AwesomeMediaPlayerAdapter(requireContext()),
        )
        transportControlGlue.host = VideoSupportFragmentGlueHost(this)
        transportControlGlue.setPlaylist(movies)
        val playlistPosition = intent.getIntExtra(MainFragment.EXTRA_PLAYLIST_POSITION, 0)
        transportControlGlue.loadMovie(playlistPosition = playlistPosition)

        setOnKeyInterceptListener { view, keyCode, event ->
            if (isControlsOverlayVisible || event.repeatCount > 0) {
                isShowOrHideControlsOverlayOnUserInteraction = true
            } else when (keyCode) {
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    isShowOrHideControlsOverlayOnUserInteraction = event.action != KeyEvent.ACTION_DOWN
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        animateIndicator(fastForwardIndicatorView)
                    }
                }
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    isShowOrHideControlsOverlayOnUserInteraction = event.action != KeyEvent.ACTION_DOWN
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        animateIndicator(rewindIndicatorView)
                    }
                }
            }
            transportControlGlue.onKey(view, keyCode, event)
        }
        setOnItemViewClickedListener { _, item, _, row ->
            if (row is ListRow && row.adapter == upNextAdapter) {
                val movie = item as Movie
                transportControlGlue.loadMovie(transportControlGlue.getPlaylist().indexOf(movie))
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState) as ViewGroup
        fastForwardIndicatorView = inflater.inflate(R.layout.view_playback_forward, view, false)
        view.addView(fastForwardIndicatorView)
        rewindIndicatorView = inflater.inflate(R.layout.view_playback_rewind, view, false)
        view.addView(rewindIndicatorView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            (adapter.presenterSelector as ClassPresenterSelector)
                .addClassPresenter(ListRow::class.java, ListRowPresenter())
            val upNextRow = ListRow(1L, HeaderItem("Up Next"), upNextAdapter)
            (adapter as ArrayObjectAdapter).add(upNextRow)
        }

        val playlist = transportControlGlue.getPlaylist()
        val firstPlaylistPosition = (transportControlGlue.playlistPosition + 1).coerceAtMost(playlist.size)
        upNextAdapter.setItems(playlist.subList(firstPlaylistPosition, playlist.size), null)
        transportControlGlue.addPlayerCallback(object : PlayerCallback() {
            override fun onPreparedStateChanged(glue: PlaybackGlue?) {
                val newPlaylist = playlist.subList(
                    fromIndex = (transportControlGlue.playlistPosition + 1).coerceAtMost(playlist.size),
                    toIndex = playlist.size
                )
                upNextAdapter.setItems(newPlaylist, MovieDiffCallback)
            }
        })
    }

    override fun onPause() {
        super.onPause()
        transportControlGlue.pause()
    }

    private fun animateIndicator(indicatorView: View) {
        indicatorView.animate()
            .withEndAction {
                indicatorView.isVisible = false
                indicatorView.alpha = 1F
                indicatorView.scaleX = 1F
                indicatorView.scaleY = 1F
            }
            .withStartAction {
                indicatorView.isVisible = true
            }
            .alpha(0.2F)
            .scaleX(2f)
            .scaleY(2f)
            .setDuration(400)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

}