package me.admqueiroga.mediasession.home

import android.content.Intent
import android.os.Bundle
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import me.admqueiroga.mediasession.data.Movie
import me.admqueiroga.mediasession.data.MovieList
import me.admqueiroga.mediasession.playback.PlaybackActivity
import me.admqueiroga.mediasession.playback.PlaybackVideoFragment.Companion.EXTRA_MOVIE_INDEX
import java.util.*

/**
 * Loads a grid of cards with movies to browse.
 */
class MainFragment : BrowseSupportFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loadRows()
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            if (item is Movie) {
                val intent = Intent(requireContext(), PlaybackActivity::class.java)
                intent.putExtra(EXTRA_MOVIE_INDEX, MovieList.list.indexOf(item))
                startActivity(intent)
            }
        }
    }


    private fun loadRows() {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val cardPresenter = CardPresenter()
        for (i in 0 until NUM_ROWS) {
            val listRowAdapter = ArrayObjectAdapter(cardPresenter)
            listRowAdapter.addAll(0, MovieList.list)
            val header = HeaderItem(i.toLong(), MovieList.MOVIE_CATEGORY[i])
            rowsAdapter.add(ListRow(header, listRowAdapter))
        }
        adapter = rowsAdapter
    }

    companion object {
        private const val NUM_ROWS = 3
    }
}