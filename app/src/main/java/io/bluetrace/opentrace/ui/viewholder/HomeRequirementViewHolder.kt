package io.bluetrace.opentrace.ui.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.requirement_item.view.*

class HomeRequirementViewHolder(view:View): RecyclerView.ViewHolder(view) {
        val tv_requirement  =   view.textView_requirement
        val imageView_requirement  =   view.iamgeView_requirement
        val switch_requirement  =   view.switch_requirement
}