package me.admqueiroga.example

import androidx.leanback.widget.FocusHighlight
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.Presenter
import me.admqueiroga.example.MovieListRowDiffCallback.CONTENT_CHANGE
import me.admqueiroga.example.MovieListRowDiffCallback.HEADER_CHANGE

class MoviesListRowPresenter : ListRowPresenter(FocusHighlight.ZOOM_FACTOR_MEDIUM, false) {

    init {
        selectEffectEnabled = false
    }

    override fun onBindViewHolder(viewHolder: Presenter.ViewHolder?, item: Any?, payloads: MutableList<Any>?) {
        val changes = payloads?.firstOrNull() as? ArrayList<String>
        val rowHolder = getRowViewHolder(viewHolder)
        if (changes?.remove(CONTENT_CHANGE) == true) {
            val currentAdapter = (rowHolder.row as MovieListRow).adapter
            val newMovies = (item as MovieListRow).adapter.unmodifiableList<Movie>()
            // TODO: Replace MovieDiffCallback with null to check the adapter behavior.
            currentAdapter.setItems(newMovies, MovieDiffCallback)
        }
        if (changes?.remove(HEADER_CHANGE) == true) {
            headerPresenter.onBindViewHolder(rowHolder.headerViewHolder, item)
        }
        if (changes == null || changes.isNotEmpty()) {
            super.onBindViewHolder(viewHolder, item, payloads)
        }
    }
}
