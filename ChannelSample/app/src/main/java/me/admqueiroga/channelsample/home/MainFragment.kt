package me.admqueiroga.channelsample.home

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ContentUris
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.tvprovider.media.tv.Channel
import androidx.tvprovider.media.tv.PreviewChannelHelper
import androidx.tvprovider.media.tv.TvContractCompat
import me.admqueiroga.channelsample.R
import me.admqueiroga.channelsample.model.*
import me.admqueiroga.channelsample.playback.PlaybackActivity
import me.admqueiroga.channelsample.playback.PlaybackVideoFragment
import me.admqueiroga.channelsample.presenter.CardPresenter

/**
 * Loads a grid of cards with movies to browse.
 */
class MainFragment : BrowseSupportFragment() {

    private var pendingSelectionCategoryId = -1L

    fun handleIntent(intent: Intent) {
        val intentAction = intent.action
        if (intentAction == Intent.ACTION_VIEW) {
            val intentData = intent.data
            val pathSegments = intentData?.pathSegments ?: emptyList()
            when (pathSegments.firstOrNull()) {
                "movie" -> pathSegments.get(1)?.let { movieId ->
                    val movie = MovieList.list.firstOrNull { it.id.toString() == movieId }
                    if (movie != null) {
                        val movieIntent = Intent(context, PlaybackActivity::class.java)
                        movieIntent.putExtra(PlaybackVideoFragment.MOVIE, movie)
                        startActivity(movieIntent)
                    }
                }
                "category" -> pathSegments.getOrNull(1)?.let { categoryId ->
                    pendingSelectionCategoryId = categoryId
                }
                "discover", null -> {
                    // Do nothing, just open the app as the user
                    // can browse the content on the main screen.
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = requireActivity().intent
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title = getString(R.string.browse_title)
        onItemViewClickedListener = ItemViewClickedListener()
        loadRows()
        if (pendingSelectionCategoryId != -1L) {
            val categoryAdapterList = (adapter as ArrayObjectAdapter).unmodifiableList<ListRow>()
            val categoryIndex = categoryAdapterList.indexOfFirst { it.id == pendingSelectionCategoryId }
            setSelectedPosition(categoryIndex, false)
            pendingSelectionCategoryId = -1L
        }
    }

    private fun loadRows() {
        val categoryList = CategoriesList.list
        val movieList = MovieList.list

        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val cardPresenter = CardPresenter()

        for (category in categoryList) {
            val listRowAdapter = ArrayObjectAdapter(cardPresenter)
            for (j in 0 until NUM_COLS) {
                listRowAdapter.add(movieList[j % 5])
            }
            listRowAdapter.add(category)
            val header = HeaderItem(category.id, category.title)
            rowsAdapter.add(ListRow(header, listRowAdapter))
        }

        adapter = rowsAdapter
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
            itemViewHolder: Presenter.ViewHolder,
            item: Any,
            rowViewHolder: RowPresenter.ViewHolder,
            row: Row
        ) {
            when (item) {
                is Movie -> {
                    val intent = Intent(context, PlaybackActivity::class.java)
                    intent.putExtra(PlaybackVideoFragment.MOVIE, item)
                    startActivity(intent)
                }
                is Category -> {
                    val movies = ((row as ListRow).adapter as ArrayObjectAdapter).unmodifiableList<Any>()
                        .filterIsInstance<Movie>()
                    addOrRemoveCategoryChannel(item, movies)
                }
            }
        }
    }

    private fun queryChannels(): List<Channel> {
        val channels = ArrayList<Channel>()
        requireContext().contentResolver.query(
            /* uri = */ TvContractCompat.Channels.CONTENT_URI,
            /* projection = */ Channel.PROJECTION,
            /* selection = */ null,
            /* selectionArgs = */ null,
            /* sortOrder = */ null
        ).use { cursor ->
            while (cursor != null && cursor.moveToNext()) {
                channels.add(Channel.fromCursor(cursor))
            }
        }
        return channels
    }

    private fun addOrRemoveCategoryChannel(category: Category, movies: List<Movie>) {
        val existingChannel = queryChannels().firstOrNull {
            it.internalProviderId == category.id.toString()
        }
        val channelBuilder = when (existingChannel) {
            null -> Channel.Builder()
            else -> Channel.Builder(existingChannel)
        }
        val channel = channelBuilder
            .setType(TvContractCompat.Channels.TYPE_PREVIEW)
            .setDisplayName(category.title)
            .setInternalProviderId(category.id.toString())
            .setAppLinkIntentUri("content://channelsample.com/category/${category.id}".toUri())
            .build()
        val channelHelper = PreviewChannelHelper(context)
        when {
            existingChannel == null -> {
                // Publish channel
                val channelUri = requireContext().contentResolver.insert(
                    TvContractCompat.Channels.CONTENT_URI,
                    channel.toContentValues()
                )
                if (channelUri != null) {
                    val channelId = ContentUris.parseId(channelUri)
                    // Add programs to channel
                    movies.forEach { movie ->
                        channelHelper.publishPreviewProgram(movie.toPreviewProgram(channelId))
                    }
                    // Ask permission to display channel on home screen
                    try {
                        val intent = Intent(TvContractCompat.ACTION_REQUEST_CHANNEL_BROWSABLE)
                        intent.putExtra(TvContractCompat.EXTRA_CHANNEL_ID, channelId)
                        startActivityForResult(intent, REQUEST_CHANNEL_BROWSABLE)
                    } catch (exception: ActivityNotFoundException) {
                        // Handle exception
                    }
                }

            }
            !existingChannel.isBrowsable -> {
                // Ask permission to display channel on home screen
                val intent = Intent(TvContractCompat.ACTION_REQUEST_CHANNEL_BROWSABLE)
                intent.putExtra(TvContractCompat.EXTRA_CHANNEL_ID, existingChannel.id)
                startActivityForResult(intent, REQUEST_CHANNEL_BROWSABLE)
            }
            else -> {
                AlertDialog.Builder(context)
                    .setMessage("Do you really want to remove the ${existingChannel.displayName} channel?")
                    .setPositiveButton("Yes") { _, _ ->
                        val channelUri = TvContractCompat.buildChannelUri(existingChannel.id)
                        requireContext().contentResolver.delete(channelUri, null, null)
                    }
                    .setNegativeButton("No") { _, _ -> }
                    .show()
            }
        }
    }

    companion object {
        private const val NUM_COLS = 5
        private const val REQUEST_CHANNEL_BROWSABLE = 1
    }
}