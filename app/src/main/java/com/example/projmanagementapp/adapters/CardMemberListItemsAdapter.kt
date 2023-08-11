package com.example.projmanagementapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projmanagementapp.R
import com.example.projmanagementapp.models.SelectedMember

open class CardMemberListItemsAdapter(
    private val context: Context,
    private val membersList: ArrayList<SelectedMember>,
    private val assignMembers: Boolean
) : RecyclerView.Adapter<CardMemberListItemsAdapter.MyViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater
                .from(context)
                .inflate(R.layout.item_card_selected_member, parent, false)
        )
    }

    override fun getItemCount() = membersList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val memberItem = membersList[position]

        if (position == membersList.size - 1 && assignMembers) {
            holder.ivAddMember.visibility = View.VISIBLE
            holder.ivSelectedMemberImage.visibility = View.GONE
        } else {
            holder.ivAddMember.visibility = View.GONE
            holder.ivSelectedMemberImage.visibility = View.VISIBLE

            Glide
                .with(context)
                .load(memberItem.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(holder.ivSelectedMemberImage)
        }

        holder.itemView.setOnClickListener {
            onClickListener?.onClick()
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick()
    }

    inner class MyViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivSelectedMemberImage: ImageView = itemView.findViewById(R.id.iv_selected_member_image)
        val ivAddMember: ImageView = itemView.findViewById(R.id.iv_add_member)
    }
}