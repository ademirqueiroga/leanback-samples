package me.admqueiroga.customheaders

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import me.admqueiroga.customheaders.presenter.IconSectionPresenter
import me.admqueiroga.customheaders.presenter.SpaceDividerPresenter

/**
 * Loads a grid of cards with movies to browse.
 */

class MainFragment : BrowseSupportFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        title = "Explore Content!"
        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60f, resources.displayMetrics).toInt()
        val headerPresenterSelector = headersSupportFragment.presenterSelector as ClassPresenterSelector
        headerPresenterSelector
            .addClassPresenter(IconSectionRow::class.java, IconSectionPresenter())
            .addClassPresenter(DividerRow::class.java, SpaceDividerPresenter(height))
        loadRows()
    }

    override fun onStart() {
        super.onStart()
        // Uncomment to change header items alignment.
//        headersSupportFragment.setAlignment(400)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun loadRows() {
        val list = MovieList.list

        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val cardPresenter = CardPresenter()

        for (k in 0 until NUM_SECTIONS) {
            rowsAdapter.add(IconSectionRow(sectionIcons[k], HeaderItem("Section $k")))
            for (i in 0 until NUM_ROWS) {
                val listRowAdapter = ArrayObjectAdapter(cardPresenter)
                for (j in 0 until NUM_COLS) {
                    listRowAdapter.add(list[j % 5])
                }
                val header = HeaderItem(i.toLong(), MovieList.MOVIE_CATEGORY[i])
                header.description = "Category description $i"
                rowsAdapter.add(ListRow(header, listRowAdapter))
            }
            rowsAdapter.add(DividerRow())
        }
        adapter = rowsAdapter
    }

    companion object {
        val sectionIcons = arrayOf(
            R.drawable.icon_1,
            R.drawable.icon_2,
            R.drawable.icon_3,
        )
        private val NUM_SECTIONS = 3
        private val NUM_ROWS = 5
        private val NUM_COLS = 5
    }
}