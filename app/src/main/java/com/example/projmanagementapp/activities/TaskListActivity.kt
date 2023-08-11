package com.example.projmanagementapp.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projmanagementapp.R
import com.example.projmanagementapp.adapters.TaskListItemsAdapter
import com.example.projmanagementapp.databinding.ActivityTaskListBinding
import com.example.projmanagementapp.firebase.FireStoreClass
import com.example.projmanagementapp.models.Board
import com.example.projmanagementapp.models.Card
import com.example.projmanagementapp.models.Task
import com.example.projmanagementapp.models.User
import com.example.projmanagementapp.utils.Constants

class TaskListActivity : BaseActivity() {
    private lateinit var binding: ActivityTaskListBinding
    private lateinit var currentBoard: Board
    private lateinit var boardDocumentId: String
    lateinit var assignedMembersList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityTaskListBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        boardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID) ?: ""

        showProgressDialog(resources.getString(R.string.please_wait), this)
        FireStoreClass().boardDetailsToActivity(this, boardDocumentId)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private val startMembersActivityForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode == Activity.RESULT_OK) {
            showProgressDialog(resources.getString(R.string.please_wait), this)
            FireStoreClass().boardDetailsToActivity(this, boardDocumentId)
        } else {
            Log.i("MembersActivity", "Canceled, return without changes")
        }
    }

    private val startCardActivityForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode == Activity.RESULT_OK) {
            showProgressDialog(resources.getString(R.string.please_wait), this)
            FireStoreClass().boardDetailsToActivity(this, boardDocumentId)
        } else {
            Log.i("CardActivity", "Canceled, return without changes")
        }
    }

    fun cardDetails(taskListPosition: Int, cardPosition: Int) {
        val intent = Intent(this, CardActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAILS, currentBoard)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION, cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST, assignedMembersList)
        startCardActivityForResult.launch(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_members -> {
                val intent = Intent(this, MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAILS, currentBoard)
                startMembersActivityForResult.launch(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupActionBar() {
        val toolbar = binding.toolbarTaskListActivity
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_white)
        supportActionBar?.title = currentBoard.name

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    fun setCurrentBoard(board: Board) {
        currentBoard = board

        hideProgressDialog()
        setupActionBar()

        showProgressDialog(resources.getString(R.string.please_wait), this)
        FireStoreClass().getAssignedMembersList(this, currentBoard.assignedTo)
    }

    fun addOrUpdateTaskListSuccess() {
        hideProgressDialog()
        FireStoreClass().boardDetailsToActivity(this, currentBoard.documentId)
    }

    fun createTaskList(taskListName: String) {
        val task = Task(taskListName, FireStoreClass().getCurrentUserId())

        currentBoard.taskList.removeAt(currentBoard.taskList.size - 1)
        currentBoard.taskList.add(task)

        showProgressDialog(resources.getString(R.string.please_wait), this)
        FireStoreClass().addOrUpdateTaskList(this, currentBoard)
    }

    fun updateTaskList(position: Int, listName: String, task: Task) {
        val resultTask = Task(listName, task.createdBy)

        currentBoard.taskList[position] = resultTask
        currentBoard.taskList.removeAt(currentBoard.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait), this)

        FireStoreClass().addOrUpdateTaskList(this, currentBoard)
    }

    fun deleteTasksList(position: Int) {
        currentBoard.taskList.removeAt(position)
        currentBoard.taskList.removeAt(currentBoard.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait), this)

        FireStoreClass().addOrUpdateTaskList(this, currentBoard)
    }

    fun addCardToTaskList(position: Int, cardName: String) {
        currentBoard.taskList.removeAt(currentBoard.taskList.size - 1)

        val cardAssignedUsersList = ArrayList<String>()
        cardAssignedUsersList.add(FireStoreClass().getCurrentUserId())

        val card = Card(cardName, FireStoreClass().getCurrentUserId(), cardAssignedUsersList)

        val cardsList = currentBoard.taskList[position].cards
        cardsList.add(card)

        val task = Task(
            currentBoard.taskList[position].title,
            currentBoard.createdBy,
            cardsList
        )
        currentBoard.taskList[position] = task

        showProgressDialog(resources.getString(R.string.please_wait), this)
        FireStoreClass().addOrUpdateTaskList(this, currentBoard)
    }

    fun updateCardsInTaskList(taskPosition: Int, cards: ArrayList<Card>) {
        currentBoard.taskList.removeLast()
        currentBoard.taskList[taskPosition].cards = cards

        showProgressDialog(resources.getString(R.string.please_wait), this)
        FireStoreClass().addOrUpdateTaskList(this, currentBoard)
    }

    fun setBoardMembersList(membersList: ArrayList<User>) {
        assignedMembersList = membersList
        hideProgressDialog()

        val newTask = Task(resources.getString(R.string.add_list))
        currentBoard.taskList.add(newTask)

        binding.rvTaskList.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false
        )
        binding.rvTaskList.setHasFixedSize(true)

        val adapter = TaskListItemsAdapter(this, currentBoard.taskList)
        binding.rvTaskList.adapter = adapter

    }
}