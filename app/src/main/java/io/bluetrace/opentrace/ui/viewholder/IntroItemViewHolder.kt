package io.bluetrace.opentrace.ui.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.intro_item.view.*

class IntroItemViewHolder(view:View): RecyclerView.ViewHolder(view) {
        val tv_itemTitle  =   view.textView_introItemTitle
        val tv_itemSubTitle  =   view.textView_introItemsubTitle
        val imageView_introItem  =   view.imageView_introItem
}