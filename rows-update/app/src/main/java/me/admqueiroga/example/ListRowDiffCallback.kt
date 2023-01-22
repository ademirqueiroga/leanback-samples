package me.admqueiroga.example

import androidx.leanback.widget.DiffCallback


object MovieListRowDiffCallback : DiffCallback<MovieListRow>() {

    const val CONTENT_CHANGE = "content_change"
    const val HEADER_CHANGE = "header_change"

    // Detect if the items at the same position in the current and new list are the same.
    override fun areItemsTheSame(oldItem: MovieListRow, newItem: MovieListRow): Boolean =
        oldItem.id == newItem.id

    // Detect if the content of these items changed. Here we are forcing to always return false.
    override fun areContentsTheSame(oldItem: MovieListRow, newItem: MovieListRow): Boolean =
        false // Returning false will cause the getChangePayload to be called

    // Detect what exactly changed between the old and new row
    override fun getChangePayload(oldItem: MovieListRow, newItem: MovieListRow): Any {
        val changes = ArrayList<String>()
        if (oldItem.items != newItem.items) {
            changes.add(CONTENT_CHANGE)
        }
        if (oldItem.headerItem.name != newItem.headerItem.name) {
            changes.add(HEADER_CHANGE)
        }
        return changes
    }


}
