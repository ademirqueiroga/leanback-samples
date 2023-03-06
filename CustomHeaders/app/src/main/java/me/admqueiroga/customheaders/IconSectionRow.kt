package me.admqueiroga.customheaders

import androidx.annotation.DrawableRes
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.SectionRow

class IconSectionRow(
    @DrawableRes val icon: Int,
    headerItem: HeaderItem
) : SectionRow(headerItem)