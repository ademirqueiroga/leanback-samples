package me.admqueiroga.mediasession.playback

import android.os.Bundle
import android.view.View
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.MediaPlayerAdapter
import androidx.leanback.media.PlaybackGlue
import androidx.leanback.widget.*
import me.admqueiroga.mediasession.data.Movie
import me.admqueiroga.mediasession.data.MovieDiffCallback
import me.admqueiroga.mediasession.data.MovieList
import me.admqueiroga.mediasession.home.CardPresenter

/** Handles video playback with media controls. */
class PlaybackVideoFragment : VideoSupportFragment() {

    private lateinit var transportControlGlue: SimplePlaybackTransportControlGlue

    private var movieIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        movieIndex = activity?.intent?.getIntExtra(EXTRA_MOVIE_INDEX, 0) ?: 0
        val movie = MovieList.list[movieIndex]

        transportControlGlue = SimplePlaybackTransportControlGlue(requireContext(), MediaPlayerAdapter(context))
        transportControlGlue.host = VideoSupportFragmentGlueHost(this@PlaybackVideoFragment)
        transportControlGlue.title = movie.title
        transportControlGlue.subtitle = movie.description
        transportControlGlue.loadMovie(movieIndex)

        setOnItemViewClickedListener { _, item, _, _ ->
            if (item is Movie) {
                val index = MovieList.list.indexOf(item)
                transportControlGlue.loadMovie(index)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val upNextAdapter = ArrayObjectAdapter(CardPresenter())
        upNextAdapter.addAll(0, MovieList.list.subList(1, MovieList.list.size))
        val upNextRow = ListRow(HeaderItem("Up Next"), upNextAdapter)
        (adapter.presenterSelector as ClassPresenterSelector)
            .addClassPresenter(ListRow::class.java, ListRowPresenter())
        (adapter as ArrayObjectAdapter).add(upNextRow)

        transportControlGlue.addPlayerCallback(object : PlaybackGlue.PlayerCallback() {
            override fun onPreparedStateChanged(glue: PlaybackGlue?) {
                super.onPreparedStateChanged(glue)
                // Update up next row
                upNextAdapter.setItems(
                    MovieList.list.subList(
                        transportControlGlue.currentVideoIndex + 1,
                        MovieList.list.size
                    ),
                    MovieDiffCallback
                )
            }
        })
    }

    override fun onBufferingStateChanged(start: Boolean) {
        super.onBufferingStateChanged(start)
        if (start) {
            transportControlGlue.onStartBuffering()
        } else {
            transportControlGlue.onFinishedBuffering()
        }
    }

    override fun onError(errorCode: Int, errorMessage: CharSequence?) {
        super.onError(errorCode, errorMessage)
        transportControlGlue.onError(errorCode, errorMessage)
    }

    companion object {
        const val EXTRA_MOVIE_INDEX = "extra_movie_index"
    }

}