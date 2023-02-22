package me.admqueiroga.channelsample

import android.content.BroadcastReceiver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import androidx.tvprovider.media.tv.Channel
import androidx.tvprovider.media.tv.PreviewChannelHelper
import androidx.tvprovider.media.tv.TvContractCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import me.admqueiroga.channelsample.model.MovieList
import me.admqueiroga.channelsample.model.toPreviewProgram


class InitChannelsBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val channelHelper = PreviewChannelHelper(context)
        val channel = Channel.Builder()
            .setType(TvContractCompat.Channels.TYPE_PREVIEW)
            .setInternalProviderId("default_channel")
            .setDisplayName("ChannelSample: Hand picked recommendations!")
            .setAppLinkIntentUri("content://channelsample.com/discover".toUri())
            .build()
        val channelUri = context.contentResolver.insert(
            TvContractCompat.Channels.CONTENT_URI,
            channel.toContentValues()
        )
        if (channelUri != null) {
            val channelId = ContentUris.parseId(channelUri)
            val myChannels = channelHelper.allChannels.filter {
                it.packageName == context.packageName
            }
            // If there are no browsable channels, i.e., channels visible on the TV Home screen, we can
            // make the first channel browsable without asking the user permission.
            if (myChannels.none { it.isBrowsable }) {
                TvContractCompat.requestChannelBrowsable(context, channelId)
            }
            MovieList.list.forEach { movie ->
                val program = movie.toPreviewProgram(channelId)
                channelHelper.publishPreviewProgram(program)
            }
        }
    }
}