package net.pantasystem.milktea.common_android.ui.listview

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.*

fun getFlexBoxLayoutManager(context: Context) : FlexboxLayoutManager{
    val flexBoxLayoutManager = FlexboxLayoutManager(context)
    flexBoxLayoutManager.flexDirection = FlexDirection.ROW
    flexBoxLayoutManager.flexWrap = FlexWrap.WRAP
    flexBoxLayoutManager.justifyContent = JustifyContent.FLEX_START
    flexBoxLayoutManager.alignItems = AlignItems.STRETCH
    return flexBoxLayoutManager
}

fun RecyclerView.applyFlexBoxLayout(context: Context) {
    this.layoutManager = getFlexBoxLayoutManager(context)
}