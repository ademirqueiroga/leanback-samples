package me.admqueiroga.example

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.PageRow

/**
 * Loads a grid of cards with movies to browse.
 */
class MainFragment : BrowseSupportFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainFragmentRegistry.registerFragment(PageRow::class.java, PageFragmentsFactory)

        setupUIElements()

        val pagesAdapter = ArrayObjectAdapter(ListRowPresenter())
        pagesAdapter.add(PageRow(HeaderItem(MOVIES)))
        adapter = pagesAdapter
    }

    private fun setupUIElements() {
        title = getString(R.string.browse_title)
        brandColor = ContextCompat.getColor(activity!!, R.color.cyan_background)
    }

    private object PageFragmentsFactory : FragmentFactory<Fragment>() {
        override fun createFragment(row: Any?): Fragment {
            row as PageRow
            return when (row.headerItem.name) {
                MOVIES -> MovieRowsFragment()
                else -> throw Exception("Unable to instantiate fragment for PageRow named '${row.headerItem.name}'")
            }
        }
    }

    companion object {
        private const val MOVIES = "Movies"
    }

}