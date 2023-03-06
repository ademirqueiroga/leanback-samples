package me.admqueiroga.customheaders.presenter

import android.view.View
import android.view.ViewGroup
import androidx.leanback.widget.Presenter

class SpaceDividerPresenter(private val height: Int) : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = View(parent.context)
        // Make sure to make it not focusable. Otherwise, when
        // HeadersSupportFragment tries to cast this ViewHolder to
        // RowHeaderPresenter.ViewHolder it will crash.
        view.isFocusable = false
        view.isFocusableInTouchMode = false
        // For the same reason, disable clicks so your app does not
        // crash if you click on this holder on an emulator.
        view.isClickable = false
        view.isImportantForAccessibility
        view.layoutParams = ViewGroup.LayoutParams(0, height)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder?, item: Any?) {

    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder?) {

    }

}