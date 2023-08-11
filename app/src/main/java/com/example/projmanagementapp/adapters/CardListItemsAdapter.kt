package com.example.projmanagementapp.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projmanagementapp.R
import com.example.projmanagementapp.activities.TaskListActivity
import com.example.projmanagementapp.models.Card
import com.example.projmanagementapp.models.SelectedMember

class CardListItemsAdapter(
    private val context: Context,
    private val cardsList: ArrayList<Card>
): RecyclerView.Adapter<CardListItemsAdapter.MyViewHolder>() {

    private var onClickListener: OnClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_card, parent, false))
    }

    override fun getItemCount() = cardsList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val cardPosition = holder.adapterPosition

        val cardItem = cardsList[cardPosition]

        holder.tvCardName.text = cardItem.name

        if (cardItem.labelColor.isNotEmpty()) {
            holder.viewLabelColor.setBackgroundColor(Color.parseColor(cardItem.labelColor))
            holder.viewLabelColor.visibility = View.VISIBLE
        } else {
            holder.viewLabelColor.visibility = View.GONE
        }

        if ((context as TaskListActivity).assignedMembersList.isNotEmpty()) {
            val selectedMembersList = ArrayList<SelectedMember>()

            for (i in context.assignedMembersList.indices) {
                for (j in cardItem.assignedTo) {
                    if (context.assignedMembersList[i].id == j) {
                        selectedMembersList.add(SelectedMember(
                            context.assignedMembersList[i].id,
                            context.assignedMembersList[i].image
                        ))
                    }
                }
            }

            if (selectedMembersList.isNotEmpty()) {
                if (selectedMembersList.size == 1
                    && selectedMembersList[0].id == cardItem.createdBy) {

                    holder.rvCardSelectedMembersList.visibility = View.GONE
                } else {
                    holder.rvCardSelectedMembersList.visibility = View.VISIBLE
                    holder.rvCardSelectedMembersList.layoutManager =
                        GridLayoutManager(context, 4)

                    val adapter =
                        CardMemberListItemsAdapter(context, selectedMembersList, false)

                    adapter.setOnClickListener(
                        object: CardMemberListItemsAdapter.OnClickListener {
                            override fun onClick() {
                                onClickListener?.onClick(cardPosition)
                            }
                        }
                    )

                    holder.rvCardSelectedMembersList.adapter = adapter
                }
            } else {
                holder.rvCardSelectedMembersList.visibility = View.GONE
            }
        }

        holder.itemView.setOnClickListener {
            onClickListener?.onClick(cardPosition)
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(cardPosition: Int)
    }

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val tvCardName: TextView = itemView.findViewById(R.id.tv_card_name)
        val viewLabelColor: View = itemView.findViewById(R.id.view_label_color)
        val rvCardSelectedMembersList: RecyclerView =
            itemView.findViewById(R.id.rv_card_selected_members_list)
    }
}