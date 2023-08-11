package com.example.projmanagementapp.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projmanagementapp.R
import com.example.projmanagementapp.activities.TaskListActivity
import com.example.projmanagementapp.models.Task
import java.util.Collections

open class TaskListItemsAdapter(
    private val context: Context,
    private val tasksList: ArrayList<Task>
): RecyclerView.Adapter<TaskListItemsAdapter.MyViewHolder>() {

    private var positionDraggedFrom = -1
    private var positionDraggedTo = - 1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val view = LayoutInflater
            .from(context)
            .inflate(R.layout.item_task, parent,false)

        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(15.toDp().toPx(), 0, 40.toDp().toPx(), 0)

        view.layoutParams = layoutParams

        return MyViewHolder(view)
    }

    override fun getItemCount() = tasksList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val taskPosition = holder.adapterPosition
        val taskItem = tasksList[taskPosition]

        if (taskPosition == tasksList.size - 1) {
            holder.tvAddTaskList.visibility = View.VISIBLE
            holder.llTaskItem.visibility = View.GONE
        } else {
            holder.tvAddTaskList.visibility = View.GONE
            holder.llTaskItem.visibility = View.VISIBLE
        }

        holder.tvTaskListTitle.text = taskItem.title
        holder.tvAddTaskList.setOnClickListener {
            holder.tvAddTaskList.visibility = View.GONE
            holder.cvAddTaskListName.visibility = View.VISIBLE
        }

        holder.ibCloseListName.setOnClickListener {
            holder.tvAddTaskList.visibility = View.VISIBLE
            holder.cvAddTaskListName.visibility = View.GONE
        }

        holder.ibDoneListName.setOnClickListener {
            val listName = holder.etTaskListName.text.toString()

            if (listName.isNotEmpty()) {
                if (context is TaskListActivity) context.createTaskList(listName)
            } else {
                Toast.makeText(
                    context,
                    "Please enter a list name.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        holder.ibEditListName.setOnClickListener {
            holder.etEditTaskListName.setText(taskItem.title)
            holder.llTitleView.visibility = View.GONE
            holder.cvEditTaskListName.visibility = View.VISIBLE
        }

        holder.ibCloseEditableView.setOnClickListener {
            holder.llTitleView.visibility = View.VISIBLE
            holder.cvEditTaskListName.visibility = View.GONE
        }

        holder.ibDoneEditListName.setOnClickListener {
            val listName = holder.etEditTaskListName.text.toString()

            if (listName.isNotEmpty()) {
                if (context is TaskListActivity) {
                    context.updateTaskList(taskPosition, listName, taskItem)
                }
            } else {
                Toast.makeText(
                    context,
                    "Please enter a list name.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        holder.ibDeleteList.setOnClickListener {
            alertDialogForDeleteTasksList(taskPosition, taskItem.title)
        }

        holder.tvAddCard.setOnClickListener {
            holder.tvAddCard.visibility = View.GONE
            holder.cvAddCard.visibility = View.VISIBLE
        }

        holder.ibCloseCardName.setOnClickListener {
            holder.tvAddCard.visibility = View.VISIBLE
            holder.cvAddCard.visibility = View.GONE
        }

        holder.ibDoneCardName.setOnClickListener {
            val cardName = holder.etCardName.text.toString()

            if (cardName.isNotEmpty()) {
                if (context is TaskListActivity) {
                    context.addCardToTaskList(taskPosition, cardName)
                }
            } else {
                Toast.makeText(
                    context,
                    "Please enter a card name.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val adapter = CardListItemsAdapter(context, taskItem.cards)

        holder.rvCardList.layoutManager = LinearLayoutManager(context)
        holder.rvCardList.setHasFixedSize(true)
        holder.rvCardList.adapter = adapter

        adapter.setOnClickListener(
            object: CardListItemsAdapter.OnClickListener {
                override fun onClick(cardPosition: Int) {
                    if (context is TaskListActivity) {
                        context.cardDetails(taskPosition, cardPosition)
                    }
                }
            }
        )

        val dividerItemDecoration = DividerItemDecoration(
            context, DividerItemDecoration.VERTICAL)
        holder.rvCardList.addItemDecoration(dividerItemDecoration)

        val helper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val startPosition = viewHolder.adapterPosition
                    val targetPosition = target.adapterPosition

                    if (positionDraggedFrom == -1) positionDraggedFrom = startPosition
                    positionDraggedTo = targetPosition

                    Collections.swap(tasksList[taskPosition].cards, startPosition, targetPosition)

                    adapter.notifyItemMoved(startPosition, targetPosition)

                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    super.clearView(recyclerView, viewHolder)

                    if (positionDraggedFrom != -1 &&
                        positionDraggedTo != -1 &&
                        positionDraggedFrom != positionDraggedTo
                    ) {
                        (context as TaskListActivity).updateCardsInTaskList(
                            taskPosition,
                            tasksList[taskPosition].cards
                        )
                    }

                    positionDraggedFrom = -1
                    positionDraggedTo = -1
                }

            }
        )

        helper.attachToRecyclerView(holder.rvCardList)
    }

    private fun Int.toDp() = (this / Resources.getSystem().displayMetrics.density).toInt()

    private fun Int.toPx() = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun alertDialogForDeleteTasksList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.alert))
        builder.setMessage(context.getString(R.string.ask_for_delete, title))
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton(context.getString(R.string.btn_yes)) { dialogInterface, _ ->
            dialogInterface.dismiss()
            if (context is TaskListActivity) {
                context.deleteTasksList(position)
            }
        }
        builder.setNegativeButton(context.getString(R.string.btn_no)) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val tvAddTaskList: TextView = itemView.findViewById(R.id.tv_add_task_list)
        val tvTaskListTitle: TextView = itemView.findViewById(R.id.tv_task_list_title)
        val tvAddCard: TextView = itemView.findViewById(R.id.tv_add_card)

        val llTaskItem: LinearLayout = itemView.findViewById(R.id.ll_task_item)
        val llTitleView: LinearLayout = itemView.findViewById(R.id.ll_title_view)

        val rvCardList: RecyclerView = itemView.findViewById(R.id.rv_card_list)

        val cvAddTaskListName: CardView = itemView.findViewById(R.id.cv_add_task_list_name)
        val cvEditTaskListName: CardView = itemView.findViewById(R.id.cv_edit_task_list_name)
        val cvAddCard: CardView = itemView.findViewById(R.id.cv_add_card)

        val ibCloseListName: ImageButton = itemView.findViewById(R.id.ib_close_list_name)
        val ibCloseEditableView: ImageButton = itemView.findViewById(R.id.ib_close_editable_view)
        val ibDoneListName: ImageButton = itemView.findViewById(R.id.ib_done_list_name)
        val ibDoneEditListName: ImageButton = itemView.findViewById(R.id.ib_done_edit_list_name)
        val ibEditListName: ImageButton = itemView.findViewById(R.id.ib_edit_list_name)
        val ibDeleteList: ImageButton = itemView.findViewById(R.id.ib_delete_list)
        val ibCloseCardName: ImageButton = itemView.findViewById(R.id.ib_close_card_name)
        val ibDoneCardName: ImageButton = itemView.findViewById(R.id.ib_done_card_name)

        val etTaskListName: EditText = itemView.findViewById(R.id.et_task_list_name)
        val etEditTaskListName: EditText = itemView.findViewById(R.id.et_edit_task_list_name)
        val etCardName: EditText = itemView.findViewById(R.id.et_card_name)
    }
}