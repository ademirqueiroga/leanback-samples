package me.admqueiroga.example

import android.os.Bundle
import android.view.View
import androidx.leanback.app.RowsSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random

class MovieRowsFragment : RowsSupportFragment() {

    private val cardPresenter = CardPresenter()
    private var coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ArrayObjectAdapter(MoviesListRowPresenter())
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
        for (i in 0 until Random.nextInt(3, NUM_ROWS)) {
            val listRowAdapter = ArrayObjectAdapter(cardPresenter)
            for (j in 0 until Random.nextInt(5, NUM_COLS)) {
                listRowAdapter.add(list.shuffled()[j % 5])
            }
            val header = HeaderItem(i.toLong(), "Category - ${Random.nextInt(0, 100)}")
            listRows.add(MovieListRow(header, listRowAdapter))
        }
        val rowsAdapter = adapter as ArrayObjectAdapter

        // TODO: Replace MovieListRowDiffCallback with null to check the adapter behavior.
        rowsAdapter.setItems(listRows, MovieListRowDiffCallback)
    }

    companion object {
        private const val NUM_ROWS = 6
        private const val NUM_COLS = 15
    }

}