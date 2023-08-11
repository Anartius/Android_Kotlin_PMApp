package com.example.projmanagementapp.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.projmanagementapp.R

class LabelColorListItemsAdapter(
    private val context: Context,
    private val colorsList: ArrayList<String>,
    private val selectedColor: String
): RecyclerView.Adapter<LabelColorListItemsAdapter.MyViewHolder>() {

    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_label_color, parent, false)
        )
    }

    override fun getItemCount() = colorsList.size

    override fun onBindViewHolder(holder: MyViewHolder, colorPosition: Int) {
        val colorItem = colorsList[colorPosition]

        holder.viewMain.setBackgroundColor(Color.parseColor(colorItem))
        if (colorItem == selectedColor) {
            holder.ivSelectedColor.visibility = View.VISIBLE
        } else {
            holder.ivSelectedColor.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            onItemClickListener?.onClick(colorPosition, colorItem)
        }
    }

    interface OnItemClickListener {
        fun onClick(position: Int, color: String)
    }

    fun setOnClickListener(onClickListener: OnItemClickListener) {
        onItemClickListener = onClickListener
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewMain: View = itemView.findViewById(R.id.view_main)
        val ivSelectedColor: ImageView = itemView.findViewById(R.id.iv_selected_color)
    }
}