package com.example.projmanagementapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projmanagementapp.R
import com.example.projmanagementapp.models.Board

open class BoardItemsAdapter(
    private val context: Context,
    private val list: ArrayList<Board>
): RecyclerView.Adapter<BoardItemsAdapter.MyViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_board, parent, false))
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int) {
        val item = list[position]

        viewHolder.itemView.let {
            Glide
                .with(context)
                .load(item.image)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(viewHolder.ivBoardCircular)

            viewHolder.tvName.text = item.name
            viewHolder.tvCreatedBy.text = "Created by: ${item.createdBy}"

            viewHolder.itemView.setOnClickListener {
                onClickListener?.onClick(position, item)
            }
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: Board)
    }

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_item_board_name)
        val ivBoardCircular: ImageView = itemView.findViewById(R.id.iv_item_board_image)
        val tvCreatedBy: TextView = itemView.findViewById(R.id.tv_item_board_created_by)
    }
}