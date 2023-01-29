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
import me.admqueiroga.transportcontrols.MainFragment.Companion.EXTRA_MOVIES
import me.admqueiroga.transportcontrols.Movie
import me.admqueiroga.transportcontrols.R

class PlaybackVideoFragment : VideoSupportFragment() {

    private lateinit var transportControlGlue: BasicTransportControlGlue
    private lateinit var fastForwardIndicatorView: View
    private lateinit var rewindIndicatorView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val movies = activity?.intent?.getParcelableArrayListExtra(EXTRA_MOVIES) ?: emptyList<Movie>()
        transportControlGlue = BasicTransportControlGlue(
            context = requireContext(),
            playerAdapter = BasicMediaPlayerAdapter(requireContext())
        )
        transportControlGlue.host = VideoSupportFragmentGlueHost(this)
        transportControlGlue.setPlaylist(movies)
        transportControlGlue.loadMovie(playlistPosition = 0)

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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState) as ViewGroup
        fastForwardIndicatorView = inflater.inflate(R.layout.view_playback_forward, view, false)
        view.addView(fastForwardIndicatorView)
        rewindIndicatorView = inflater.inflate(R.layout.view_playback_rewind, view, false)
        view.addView(rewindIndicatorView)
        return view
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