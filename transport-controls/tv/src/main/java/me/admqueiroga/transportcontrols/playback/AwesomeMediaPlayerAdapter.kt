package me.admqueiroga.transportcontrols.playback

import android.content.Context
import androidx.leanback.media.MediaPlayerAdapter
import androidx.leanback.media.PlaybackBaseControlGlue
import androidx.leanback.widget.PlaybackControlsRow.ShuffleAction

class AwesomeMediaPlayerAdapter(context: Context) : MediaPlayerAdapter(context) {

    var shuffleEnabled: Boolean = false
        private set
    var repeatMode = 0
        private set

    override fun fastForward() = seekTo(currentPosition + 10_000)

    override fun rewind() = seekTo(currentPosition - 10_000)

    override fun getSupportedActions(): Long {
        return (PlaybackBaseControlGlue.ACTION_SKIP_TO_PREVIOUS xor
                PlaybackBaseControlGlue.ACTION_REWIND xor
                PlaybackBaseControlGlue.ACTION_PLAY_PAUSE xor
                PlaybackBaseControlGlue.ACTION_FAST_FORWARD xor
                PlaybackBaseControlGlue.ACTION_SKIP_TO_NEXT xor
                PlaybackBaseControlGlue.ACTION_REPEAT xor
                PlaybackBaseControlGlue.ACTION_SHUFFLE).toLong()
    }

    override fun setRepeatAction(repeatActionIndex: Int) {
        repeatMode = repeatActionIndex
    }

    override fun setShuffleAction(shuffleActionIndex: Int) {
        shuffleEnabled = shuffleActionIndex == ShuffleAction.INDEX_ON
    }

}