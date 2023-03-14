package me.admqueiroga.mediasession.data

import androidx.leanback.widget.DiffCallback

object MovieDiffCallback : DiffCallback<Movie>() {
    override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return newItem == oldItem
    }
}