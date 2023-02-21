package me.admqueiroga.example

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ObjectAdapter
import androidx.recyclerview.widget.DiffUtil
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
        adapter = MoviesListRowAdapter()
        setupUIElements()
        coroutineScope.launch {
            while (true) {
                loadRows()
                delay(5000)
            }
        }
    }

    class MoviesListRowAdapter : ObjectAdapter(MoviesListRowPresenter()) {

        val items = ArrayList<MovieListRow>()

        override fun size(): Int = items.size

        override fun get(position: Int) = items[position]

        fun setItems(items: List<MovieListRow>) {
            this.items.clear()
            this.items.addAll(items)
        }
    }

    private fun loadRows() {
        val newListRows = mutableListOf<MovieListRow>()
        val list = MovieList.list
        for (i in 0 until NUM_ROWS) {
            val listRowAdapter = ArrayObjectAdapter(cardPresenter)
            for (j in 0 until Random.nextInt(1, NUM_COLS)) {
                listRowAdapter.add(list[Random.nextInt(0, list.size - 1) % 5])
            }
            val header = HeaderItem(i.toLong(), "Category($i) - ${System.currentTimeMillis()}")
            newListRows.add(MovieListRow(header, listRowAdapter))
        }
        newListRows.shuffle()

        val rowsAdapter = adapter as MoviesListRowAdapter

        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = rowsAdapter.size()

            override fun getNewListSize(): Int = newListRows.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = rowsAdapter.items[oldItemPosition]
                val newItem = newListRows[newItemPosition]
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return false
            }

            override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
                val oldItem = rowsAdapter.items[oldItemPosition]
                val newItem = newListRows[newItemPosition]
                val changes = ArrayList<String>()
                if (oldItem.items != newItem.items) {
                    changes.add(MovieListRowDiffCallback.CONTENT_CHANGE)
                }
                if (oldItem.headerItem.name != newItem.headerItem.name) {
                    changes.add(MovieListRowDiffCallback.HEADER_CHANGE)
                }
                return changes
            }
        })

        try {
            rowsAdapter.setItems(newListRows)
            diffResult.dispatchUpdatesTo(rowsSupportFragment.bridgeAdapter)
            diffResult.dispatchUpdatesTo(headersSupportFragment.bridgeAdapter)
        } catch (e: Exception) {
            rowsAdapter.setItems(newListRows)
            rowsAdapter.notifyItemRangeChanged(0, newListRows.size)
        }
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