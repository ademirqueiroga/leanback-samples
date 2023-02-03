package me.admqueiroga.transportcontrols.playback

import android.content.Context
import androidx.leanback.widget.PlaybackControlsRow.MultiAction
import me.admqueiroga.transportcontrols.R

class MyListAction(context: Context) : MultiAction(R.id.tv_my_list_action) {

    init {
        val drawables = arrayOf(
            context.getDrawable(R.drawable.playback_controls_my_list_add),
            context.getDrawable(R.drawable.playback_controls_my_list_remove),
        )
        setDrawables(drawables)
        val labels = arrayOf(
            context.getString(R.string.playback_controls_my_list_add),
            context.getString(R.string.playback_controls_my_list_remove),
        )
        setLabels(labels)
    }

    companion object {
        const val INDEX_ADD = 0
        const val INDEX_REMOVE = 1
    }

}