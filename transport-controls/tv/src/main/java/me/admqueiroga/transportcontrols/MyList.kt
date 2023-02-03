package me.admqueiroga.transportcontrols

object MyList {

    private val movies = HashSet<Movie>()

    fun add(movie: Movie) = movies.add(movie)

    fun remove(movie: Movie) = movies.remove(movie)

    fun contains(movie: Movie) = movies.contains(movie)

    fun toggle(movie: Movie) {
        if (contains(movie)) remove(movie) else add(movie)
    }

}