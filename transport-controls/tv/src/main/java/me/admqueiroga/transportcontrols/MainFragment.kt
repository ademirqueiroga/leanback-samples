package me.admqueiroga.transportcontrols

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import me.admqueiroga.transportcontrols.playback.PlaybackActivity
import java.util.*

/**
 * Loads a grid of cards with movies to browse.
 */
class MainFragment : BrowseSupportFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUIElements()

        loadRows()

        setupEventListeners()
    }

    private fun setupUIElements() {
        title = getString(R.string.browse_title)
        // over title
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        
        // set fastLane (or headers) background color
        brandColor = ContextCompat.getColor(requireActivity(), R.color.fastlane_background)
        // set search icon color
        searchAffordanceColor = ContextCompat.getColor(requireActivity(), R.color.search_opaque)
    }

    private fun loadRows() {
        val list = MovieList.list
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val cardPresenter = CardPresenter()
        for (i in 0 until NUM_ROWS) {
            if (i != 0) {
                Collections.shuffle(list)
            }
            val listRowAdapter = ArrayObjectAdapter(cardPresenter)
            for (j in 0 until NUM_COLS) {
                listRowAdapter.add(list[j % 5])
            }
            val header = HeaderItem(i.toLong(), MovieList.MOVIE_CATEGORY[i])
            rowsAdapter.add(ListRow(header, listRowAdapter))
        }
        adapter = rowsAdapter
    }

    private fun setupEventListeners() {
        setOnSearchClickedListener {
            Toast.makeText(requireActivity(), "Implement your own in-app search", Toast.LENGTH_LONG)
                .show()
        }

        onItemViewClickedListener = ItemViewClickedListener()
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
            itemViewHolder: Presenter.ViewHolder,
            item: Any,
            rowViewHolder: RowPresenter.ViewHolder,
            row: Row
        ) {
            if (item is Movie) {
                val intent = Intent(activity!!, PlaybackActivity::class.java)
                val movies = ((row as ListRow).adapter as ArrayObjectAdapter).unmodifiableList<Movie>()
                intent.putParcelableArrayListExtra(EXTRA_MOVIES, ArrayList(movies))
                val rowAdapter = row.adapter
                val playlistPosition = (rowAdapter as? ArrayObjectAdapter)?.indexOf(item) ?: 0
                intent.putExtra(EXTRA_PLAYLIST_POSITION, playlistPosition)
                startActivity(intent)
            }
        }
    }

    companion object {
        private const val NUM_ROWS = 6
        private const val NUM_COLS = 15

        const val EXTRA_MOVIES = "extra_movies"
        const val EXTRA_PLAYLIST_POSITION = "extra_playlist_position"
    }
}