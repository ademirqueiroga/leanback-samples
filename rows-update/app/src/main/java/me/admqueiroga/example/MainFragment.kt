package me.admqueiroga.example

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Loads a grid of cards with movies to browse.
 */
class MainFragment : BrowseSupportFragment() {


    private val cardPresenter = CardPresenter()
    private var coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        adapter = ArrayObjectAdapter(MoviesListRowPresenter())
        setupUIElements()
        coroutineScope.launch {
            while (true) {
                loadRows()
                delay(5000)
            }
        }
    }

    private fun loadRows() {
        val listRows = mutableListOf<MovieListRow>()
        val list = MovieList.list
        for (i in 0 until NUM_ROWS) {
            val listRowAdapter = ArrayObjectAdapter(cardPresenter)
            for (j in 0 until Random.nextInt(5, NUM_COLS)) {
                listRowAdapter.add(list[j % 5])
            }
            val header = HeaderItem(i.toLong(), "Category($i) - ${System.currentTimeMillis()}")
            listRows.add(MovieListRow(header, listRowAdapter))
        }
        val rowsAdapter = adapter as ArrayObjectAdapter

        // TODO: Replace MovieListRowDiffCallback with null to check the adapter behavior.
        rowsAdapter.setItems(listRows, MovieListRowDiffCallback)
    }

    private fun setupUIElements() {
        title = getString(R.string.browse_title)
        brandColor = ContextCompat.getColor(activity!!, R.color.cyan_background)
    }

    companion object {
        private const val NUM_ROWS = 6
        private const val NUM_COLS = 15

    }
}