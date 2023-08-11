package com.example.projmanagementapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projmanagementapp.R
import com.example.projmanagementapp.models.User
import com.example.projmanagementapp.utils.Constants

class MemberListItemsAdapter(
    private val context: Context,
    private val membersList: ArrayList<User>,
    private var onClickListener: OnClickListener? = null

): RecyclerView.Adapter<MemberListItemsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_member, parent, false))
    }

    override fun getItemCount() = membersList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val memberItem = membersList[position]

        Glide
            .with(context)
            .load(memberItem.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(holder.ivMemberImage)

        holder.tvMemberName.text = memberItem.name
        holder.tvMemberEmail.text = memberItem.email

        if (memberItem.selected) {
            holder.ivSelectedMember.visibility = View.VISIBLE
        } else {
            holder.ivSelectedMember.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if (onClickListener != null) {
                if (memberItem.selected) {
                    onClickListener!!.onClick(position, memberItem, Constants.UNSELECT)
                } else {
                    onClickListener!!.onClick(position, memberItem, Constants.SELECT)
                }

            }
        }
    }

    interface OnClickListener {
        fun onClick(position: Int, user: User, action: String)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivMemberImage: ImageView = itemView.findViewById(R.id.iv_member_image)
        val tvMemberName: TextView = itemView.findViewById(R.id.tv_item_member_name)
        val tvMemberEmail: TextView = itemView.findViewById(R.id.tv_member_email)
        val ivSelectedMember: ImageView = itemView.findViewById(R.id.iv_selected_member)
    }
}