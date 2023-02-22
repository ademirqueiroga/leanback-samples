package me.admqueiroga.channelsample.model

object CategoriesList {

    val list: List<Category> = buildList {
        add(Category(0L, "Action"))
        add(Category(1L, "Drama"))
        add(Category(2L, "Comedy"))
        add(Category(3L, "Romance"))
        add(Category(4L, "Horror"))
        add(Category(5L, "Thriller"))
    }

}