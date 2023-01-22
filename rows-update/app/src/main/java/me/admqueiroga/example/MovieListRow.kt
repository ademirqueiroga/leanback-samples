package me.admqueiroga.example

import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow

class MovieListRow(
    headerItem: HeaderItem,
    val adapter: ArrayObjectAdapter
) : ListRow(headerItem, adapter) {

    val items: List<Movie>
        get() = adapter.unmodifiableList()

}