package com.example.projmanagementapp.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.projmanagementapp.activities.*
import com.example.projmanagementapp.models.Board
import com.example.projmanagementapp.models.User
import com.example.projmanagementapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FireStoreClass {
    private val fireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User) {
        fireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
            .addOnFailureListener {
                Log.e(activity.javaClass.simpleName, "Error")
            }
    }

    fun createBoard(activity: CreateBoardActivity, board:Board) {

        fireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Board created successfully.")
                Toast.makeText(activity, "Board created", Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully()
            }
            .addOnFailureListener { exception ->
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board.",
                    exception
                )
            }
    }

    fun loadUserData(activity: Activity, readBoardsList: Boolean = false) {
        fireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                val loggedUser = document.toObject(User::class.java)
                if (loggedUser != null) {
                    when (activity) {
                        is SignInActivity -> activity.signInSuccess(loggedUser)
                        is MainActivity -> activity.updateNavUserDetails(loggedUser, readBoardsList)
                        is MyProfileActivity -> activity.setUserDataInUI(loggedUser)
                    }
                }
            }
            .addOnFailureListener {
                when (activity) {
                    is SignInActivity -> activity.hideProgressDialog()
                    is MainActivity -> activity.hideProgressDialog()
                    is MyProfileActivity -> activity.hideProgressDialog()
                }
                Log.e(activity.javaClass.simpleName, "Error")
            }
    }

    fun getBoardsList(activity: MainActivity) {
        fireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val boardsList = ArrayList<Board>()

                document.documents.forEach { item ->
                    val board = item.toObject(Board::class.java)!!
                    board.documentId = item.id
                    boardsList.add(board)
                }

                activity.populateBoardsListToUI(boardsList)
            }
            .addOnFailureListener { errorMsg ->

                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board.",
                    errorMsg
                )
                Toast.makeText(
                    activity,
                    "Error when updating the profile.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun addOrUpdateTaskList(activity: Activity, board: Board) {
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        fireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "TaskList updated successfully.")
                if (activity is TaskListActivity) {
                    activity.addOrUpdateTaskListSuccess()
                } else if (activity is CardActivity) {
                    activity.addOrUpdateTaskListSuccess()
                }
            }
            .addOnFailureListener { errorMsg ->
                if (activity is TaskListActivity) {
                    activity.hideProgressDialog()
                } else if (activity is CardActivity) {
                    activity.hideProgressDialog()
                }

                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creation a board.",
                    errorMsg
                )
            }
    }

    fun updateUserProfileData(
        activity: Activity,
        userHashMap: HashMap<String, Any>
    ) {
        fireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i(
                    activity.javaClass.simpleName,
                    "Profile Data updated successfully."
                )

                Toast.makeText(
                    activity,
                    "Profile updated successfully.",
                    Toast.LENGTH_SHORT
                ).show()

                when (activity) {
                    is MainActivity -> {
                        activity.tokenUpdateSuccess()
                    }
                    is MyProfileActivity -> {
                        activity.profileUpdateSuccess()
                    }
                }
            }
            .addOnFailureListener { errorMsg ->

                when (activity) {
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(
                    activity.javaClass.simpleName,
                    "Error when updating the profile.",
                    errorMsg
                )

                Toast.makeText(
                    activity,
                    "Error when updating the profile.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid ?: ""
    }

    fun boardDetailsToActivity(activity: TaskListActivity, boardDocumentId: String) {
        fireStore.collection(Constants.BOARDS)
            .document(boardDocumentId)
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.toString())

                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id
                activity.setCurrentBoard(board)
            }
            .addOnFailureListener { errorMsg ->

                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting a board details.",
                    errorMsg
                )
                Toast.makeText(
                    activity,
                    "Error while getting a board details.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun getAssignedMembersList(
        activity: Activity,
        assignedTo: ArrayList<String>
    ) {
        fireStore.collection(Constants.USERS)
            .whereIn(Constants.ID, assignedTo)
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())

                val usersList = ArrayList<User>()
                document.documents.forEach {
                    val user = it.toObject(User::class.java)
                    user?.let { usersList.add(user) }
                }

                if (activity is MembersActivity) {
                    activity.setupMembersList(usersList)
                } else if (activity is TaskListActivity) {
                    activity.setBoardMembersList(usersList)
                }
            }
            .addOnFailureListener { errorMsg ->
                if (activity is MembersActivity) {
                    activity.hideProgressDialog()
                } else if (activity is TaskListActivity) {
                    activity.hideProgressDialog()
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while loading members details.",
                    errorMsg
                )
            }
    }

    fun getMemberDetails(activity: MembersActivity, email: String) {
        fireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener { document ->
                if (document.documents.size > 0) {
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                } else {
                    activity.hideProgressDialog()
                    activity.showSnackBar("No such member found.")
                }
            }
            .addOnFailureListener { errorMsg ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting user details.",
                    errorMsg
                )
            }
    }

    fun assignMemberToBoard(activity: MembersActivity, board: Board, user: User) {
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        fireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user)
            }
            .addOnFailureListener { errorMsg ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while updating a board",
                    errorMsg
                )
            }
    }
}