package io.bluetrace.opentrace.ui.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.bluetrace.opentrace.R
import io.bluetrace.opentrace.ui.viewholder.HomeRequirementViewHolder

class HomeRequirementsAdapter(var items: ArrayList<String>, val context: Context) :
    RecyclerView.Adapter<HomeRequirementViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = HomeRequirementViewHolder(
        LayoutInflater.from(context)
            .inflate(R.layout.requirement_item, parent, false)
    )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: HomeRequirementViewHolder, position: Int) {
        holder.tv_requirement.text = items.get(position)
        holder.imageView_requirement.setImageDrawable(getImage(items.get(position)))

    }

    private fun getImage(requirement:String):Drawable{

        var drawable:Drawable

        if (requirement.equals(context.getString(R.string.bluetooth))){
            drawable = context.getDrawable(R.drawable.ic_bluetooth)!!
        }else if (requirement.equals(context.getString(R.string.notifications))){
            drawable = context.getDrawable(R.drawable.bell)!!
        }else if(requirement.equals(context.getString(R.string.gps))){
            drawable = context.getDrawable(R.drawable.pin)!!
            }else{
            drawable = context.getDrawable(R.drawable.profile)!!
        }
        return drawable
    }
}