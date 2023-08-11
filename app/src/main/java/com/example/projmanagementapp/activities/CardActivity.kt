package com.example.projmanagementapp.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.projmanagementapp.R
import com.example.projmanagementapp.adapters.CardMemberListItemsAdapter
import com.example.projmanagementapp.databinding.ActivityCardBinding
import com.example.projmanagementapp.dialogs.LabelColorListDialog
import com.example.projmanagementapp.dialogs.MembersListDialog
import com.example.projmanagementapp.firebase.FireStoreClass
import com.example.projmanagementapp.models.Board
import com.example.projmanagementapp.models.Card
import com.example.projmanagementapp.models.SelectedMember
import com.example.projmanagementapp.models.User
import com.example.projmanagementapp.utils.Constants
import java.util.Date
import java.util.Locale

class CardActivity : BaseActivity() {
    private lateinit var binding: ActivityCardBinding
    private lateinit var board: Board
    private var taskListItemPosition = -1
    private var cardPosition = -1
    private var selectedColor = ""
    private lateinit var boardMembersList: ArrayList<User>
    private var dueDateInMilliSeconds: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCardBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        getIntentData()
        setupActionBar()

        val etName = binding.etNameCardDetails
        etName.setText(board.taskList[taskListItemPosition].cards[cardPosition].name)
        etName.setSelection(etName.text.toString().length)

        selectedColor = board.taskList[taskListItemPosition].cards[cardPosition].labelColor
        if (selectedColor.isNotEmpty()) setColor()

        setupSelectedMembersList()

        dueDateInMilliSeconds = board.taskList[taskListItemPosition].cards[cardPosition].dueDate
        if (dueDateInMilliSeconds > 0) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val selectedDate = sdf.format(Date(dueDateInMilliSeconds))
            binding.tvSelectDueDate.text = selectedDate
        }

        binding.btnUpdateCardDetails.setOnClickListener {
            if (etName.text.toString().isNotEmpty()) {
                updateCardDetails()
            } else {
                Toast.makeText(
                    this@CardActivity,
                    "Please enter a card name",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.tvSelectLabelColor.setOnClickListener {
            labelColorsListDialog()
        }

        binding.tvSelectMembers.setOnClickListener {
            membersListDialog()
        }

        binding.tvSelectDueDate.setOnClickListener {
            showDatePicker()
        }

    }

    private fun setupActionBar() {
        val toolbar = binding.toolbarCardActivity
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_white)
        supportActionBar?.title =
            board.taskList[taskListItemPosition].cards[cardPosition].name

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_delete_card -> {
                alertDialogForDeleteCard(
                    board.taskList[taskListItemPosition].cards[cardPosition].name
                )
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getIntentData() {
        if (intent.hasExtra(Constants.BOARD_DETAILS)) {
            board =
                if (Build.VERSION.SDK_INT >= 33) {
                    intent.extras!!.getParcelable(Constants.BOARD_DETAILS, Board::class.java)!!
                } else {
                    @Suppress("DEPRECATION")
                    intent.extras!!.getParcelable(Constants.BOARD_DETAILS)!!
                }
        }

        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)) {
            taskListItemPosition =
                if (Build.VERSION.SDK_INT >= 33) {
                    intent.extras!!.getInt(Constants.TASK_LIST_ITEM_POSITION)
                } else {
                    intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1)
                }
        }

        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)) {
            cardPosition =
                if (Build.VERSION.SDK_INT >= 33) {
                    intent.extras!!.getInt(Constants.CARD_LIST_ITEM_POSITION)
                } else {
                    intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, -1)
                }
        }

        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST)) {
            boardMembersList =
                if (Build.VERSION.SDK_INT >= 33) {
                    intent.extras!!.getParcelableArrayList(
                        Constants.BOARD_MEMBERS_LIST, User::class.java)!!
                } else {
                    @Suppress("DEPRECATION")
                    intent.extras!!.getParcelableArrayList(Constants.BOARD_MEMBERS_LIST)!!
                }
        }
    }

    fun addOrUpdateTaskListSuccess() {
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun membersListDialog() {
        val cardAssignedMembersList =
            board.taskList[taskListItemPosition].cards[cardPosition].assignedTo

        if (cardAssignedMembersList.isNotEmpty()) {
            for (i in boardMembersList.indices) {
                for (j in cardAssignedMembersList) {
                    if (boardMembersList[i].id == j) {
                        boardMembersList[i].selected = true
                    }
                }
            }
        } else boardMembersList.forEach { it.selected = false }

        val listDialog = object: MembersListDialog(
            this,
            resources.getString(R.string.select_members),
            boardMembersList
        ) {
            override fun onItemSelected(user: User, action: String) {
                val assignedList = board.taskList[taskListItemPosition]
                    .cards[cardPosition].assignedTo

                if (action == Constants.SELECT) {
                    if (!assignedList.contains(user.id)) {
                        assignedList.add(user.id)
                    }
                } else {
                    assignedList.remove(user.id)

                    boardMembersList.forEach {
                        if (it.id == user.id) it.selected = false
                    }
                }

                setupSelectedMembersList()
            }
        }

        listDialog.show()
    }
    private fun updateCardDetails() {
        val card = Card(
            binding.etNameCardDetails.text.toString(),
            board.taskList[taskListItemPosition].cards[cardPosition].createdBy,
            board.taskList[taskListItemPosition].cards[cardPosition].assignedTo,
            selectedColor,
            dueDateInMilliSeconds
        )

        val taskList = board.taskList
        taskList.removeLast()
        
        board.taskList[taskListItemPosition].cards[cardPosition] = card

        showProgressDialog(resources.getString(R.string.please_wait), this)
        FireStoreClass().addOrUpdateTaskList(this@CardActivity, board)
    }

    private fun deleteCard() {
        val cardList = board.taskList[taskListItemPosition].cards
        cardList.removeAt(cardPosition)

        val taskList = board.taskList
        taskList.removeAt(taskList.size - 1)

        taskList[taskListItemPosition].cards = cardList

        showProgressDialog(resources.getString(R.string.please_wait), this)
        FireStoreClass().addOrUpdateTaskList(this@CardActivity, board)
    }

    private fun getColorsList(): ArrayList<String> {
        val colorsList = ArrayList<String>()

        colorsList.add("#43C86F")
        colorsList.add("#0C90F1")
        colorsList.add("#F72400")
        colorsList.add("#7A8089")
        colorsList.add("#D57C1D")
        colorsList.add("#770000")
        colorsList.add("#0022F8")

        return colorsList
    }

    private fun setColor() {
        binding.tvSelectLabelColor.text = ""
        binding.tvSelectLabelColor.setBackgroundColor(Color.parseColor(selectedColor))
    }

    private fun labelColorsListDialog() {
        val colorsList = getColorsList()

        val colorsListDialog = object: LabelColorListDialog(
            this,
            colorsList,
            resources.getString(R.string.select_label_color),
            selectedColor
        ) {
            override fun onItemSelected(color: String) {
                selectedColor = color
                setColor()
            }

        }

        colorsListDialog.show()
    }

    private fun alertDialogForDeleteCard(cardName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.alert))
        builder.setMessage(resources.getString(R.string.ask_for_delete, cardName))
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton(resources.getString(R.string.btn_yes)) {
            dialogInterface, _ ->

            dialogInterface.dismiss()
            deleteCard()
        }
        builder.setNegativeButton(resources.getString(R.string.btn_no)) {
            dialogInterface, _ ->

            dialogInterface.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun setupSelectedMembersList() {
        val cardAssignedMembersList =
            board.taskList[taskListItemPosition].cards[cardPosition].assignedTo

        val selectedMembersList: ArrayList<SelectedMember> = ArrayList()

        for (i in boardMembersList.indices) {
            for (j in cardAssignedMembersList) {
                if (boardMembersList[i].id == j) {
                    val selectedMember = SelectedMember(
                        boardMembersList[i].id,
                        boardMembersList[i].image
                    )
                    selectedMembersList.add(selectedMember)
                }
            }
        }

        if (selectedMembersList.isNotEmpty()) {
            selectedMembersList.add(SelectedMember("", ""))
            binding.tvSelectMembers.visibility = View.GONE
            binding.rvSelectedMembersList.visibility = View.VISIBLE

            binding.rvSelectedMembersList.layoutManager =
                GridLayoutManager(this, 6)

            val adapter =
                CardMemberListItemsAdapter(this, selectedMembersList, true)

            adapter.setOnClickListener(
                object: CardMemberListItemsAdapter.OnClickListener {
                    override fun onClick() {
                        membersListDialog()
                    }
                }
            )

            binding.rvSelectedMembersList.adapter = adapter

        } else {
            binding.tvSelectMembers.visibility = View.VISIBLE
            binding.rvSelectedMembersList.visibility = View.GONE
        }
    }

    private fun showDatePicker() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val dayOfMonth = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,

            { _, dYear, dMonth, dDay ->
                val selectedDay = if (dDay < 10) "0$dDay" else "$dDay"
                val selectedMonth = if ((dMonth + 1) < 10) "0$dMonth" else "$dMonth"

                val selectedDate = "$selectedDay/$selectedMonth/$dYear"
                binding.tvSelectDueDate.text = selectedDate

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val theDate = sdf.parse(selectedDate)

                dueDateInMilliSeconds = theDate!!.time
            },

            year,
            month,
            dayOfMonth
        )

        datePickerDialog.show()
    }
}