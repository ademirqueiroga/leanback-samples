package me.admqueiroga.channelsample.model

import android.content.Context
import androidx.core.net.toUri
import androidx.tvprovider.media.tv.Channel
import androidx.tvprovider.media.tv.PreviewChannelHelper
import androidx.tvprovider.media.tv.PreviewProgram
import androidx.tvprovider.media.tv.TvContractCompat
import java.io.Serializable
import kotlin.random.Random

/**
 * Movie class represents video entity with title, description, image thumbs and video url.
 */
data class Movie(
    var id: Long = 0,
    var title: String? = null,
    var description: String? = null,
    var backgroundImageUrl: String? = null,
    var cardImageUrl: String? = null,
    var videoUrl: String? = null,
    var studio: String? = null,
    var genre: String? = null,
) : Serializable {

    override fun toString(): String {
        return "Movie{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", backgroundImageUrl='" + backgroundImageUrl + '\'' +
                ", cardImageUrl='" + cardImageUrl + '\'' +
                '}'
    }

    companion object {
        internal const val serialVersionUID = 727566175075960653L
    }
    val channelId = 0L


    lateinit var context: Context
    fun getChannelPrograms(channelId: Long): List<PreviewProgram> {
        val programs = ArrayList<PreviewProgram>()
        context.contentResolver.query(
            /* uri = */ TvContractCompat.PreviewPrograms.CONTENT_URI,
            /* projection = */ PreviewProgram.PROJECTION,
            /* selection = */ null,
            /* selectionArgs = */ null,
            /* sortOrder = */ null
        ).use { cursor ->
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val program = PreviewProgram.fromCursor(cursor)
                    if (program.channelId == channelId) {
                        programs.add(program)
                    }
                }
            }
        }
        return programs
    }


    fun insertOrUpdateChannelPrograms(context: Context, channelId: Long) {
        val existingPrograms = getChannelPrograms(channelId)
        MovieList.list.forEach { movie ->
            val existingProgram = existingPrograms.find {
                it.internalProviderId == movie.id.toString()
            }
            val programBuilder = when (existingProgram) {
                null -> PreviewProgram.Builder()
                else -> PreviewProgram.Builder(existingProgram)
            }
            val program = programBuilder.setChannelId(channelId)
                .setType(TvContractCompat.PreviewPrograms.TYPE_MOVIE)
                .setTitle(title)
                .setPreviewVideoUri(videoUrl?.toUri())
                .setDurationMillis(Random.nextInt(0, 10) * 60 * 1000)
                .setLastPlaybackPositionMillis(0)
                .setGenre(genre)
                .setDescription(description)
                .setPosterArtUri(cardImageUrl?.toUri())
                .setIntentUri("content://channelsample.com/movie/$id".toUri())
                .setInternalProviderId(id.toString())
                .build()

            when (existingProgram) {
                null -> context.contentResolver.insert(
                    /* url = */ TvContractCompat.PreviewPrograms.CONTENT_URI,
                    /* values = */ program.toContentValues()
                )
                else -> context.contentResolver.update(
                    /* uri = */ TvContractCompat.PreviewPrograms.CONTENT_URI,
                    /* values = */ program.toContentValues(),
                    /* where = */ null,
                    /* selectionArgs = */ null
                )
            }
        }
    }

}

fun Movie.toPreviewProgram(channelId: Long): PreviewProgram {
    return PreviewProgram.Builder()
        .setChannelId(channelId)
        .setType(TvContractCompat.PreviewPrograms.TYPE_MOVIE)
        .setTitle(title)
        .setPreviewVideoUri(videoUrl?.toUri())
        .setDurationMillis(Random.nextInt(0, 10) * 60 * 1000)
        .setLastPlaybackPositionMillis(0)
        .setGenre(genre)
        .setDescription(description)
        .setPosterArtUri(cardImageUrl?.toUri())
        .setIntentUri("content://channelsample.com/movie/$id".toUri())
        .setInternalProviderId(id.toString())
        .build()
}