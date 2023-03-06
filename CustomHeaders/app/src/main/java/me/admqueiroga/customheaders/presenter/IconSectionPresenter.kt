package me.admqueiroga.customheaders.presenter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.RowHeaderPresenter
import me.admqueiroga.customheaders.IconSectionRow
import me.admqueiroga.customheaders.R

class IconSectionPresenter : RowHeaderPresenter() {

    override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
        val customView = LayoutInflater.from(parent.context)
            .inflate(R.layout.icon_section_row, parent, false)
        val superHolder = super.onCreateViewHolder(parent) as ViewHolder
        (customView as ViewGroup).addView(superHolder.view)
        return IconSectionViewHolder(customView)
    }

    override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any?) {
        super.onBindViewHolder(viewHolder as IconSectionViewHolder, item)
        viewHolder.icon.setImageResource((item as IconSectionRow).icon)
    }

    override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {
        super.onUnbindViewHolder(viewHolder as IconSectionViewHolder)
        viewHolder.icon.setImageDrawable(null)
    }

    override fun onSelectLevelChanged(holder: ViewHolder?) {
        // Do nothing as we don't want the row alpha to change.
    }

    class IconSectionViewHolder(view: View) : ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.row_header_icon)
    }

}