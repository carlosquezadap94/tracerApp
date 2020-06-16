package io.bluetrace.opentrace.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.bluetrace.opentrace.R
import io.bluetrace.opentrace.ui.IntroItemModel
import io.bluetrace.opentrace.ui.viewholder.IntroItemViewHolder

class IntroItemAdapter(var items: ArrayList<IntroItemModel>, val context: Context) :
    RecyclerView.Adapter<IntroItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = IntroItemViewHolder(
        LayoutInflater.from(context)
            .inflate(R.layout.intro_item, parent, false)
    )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: IntroItemViewHolder, position: Int) {
        holder.tv_itemTitle.text = items.get(position).title
        holder.tv_itemSubTitle.text = items.get(position).subTitle
    }
}