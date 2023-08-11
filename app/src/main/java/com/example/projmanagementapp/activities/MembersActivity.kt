package com.example.projmanagementapp.activities

import android.app.Activity
import android.app.Dialog
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projmanagementapp.R
import com.example.projmanagementapp.adapters.MemberListItemsAdapter
import com.example.projmanagementapp.databinding.ActivityMembersBinding
import com.example.projmanagementapp.databinding.DialogSearchMemberBinding
import com.example.projmanagementapp.firebase.FireStoreClass
import com.example.projmanagementapp.models.Board
import com.example.projmanagementapp.models.User
import com.example.projmanagementapp.utils.Constants
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL


class MembersActivity : BaseActivity() {
    private lateinit var binding: ActivityMembersBinding
    private lateinit var dialogBinding: DialogSearchMemberBinding
    private lateinit var currentBoard: Board
    private lateinit var assignedMembersList: ArrayList<User>
    private var anyChangesMade = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra(Constants.BOARD_DETAILS)) {
            currentBoard =
                if (SDK_INT >= 33) {
                    intent.extras!!.getParcelable(Constants.BOARD_DETAILS, Board::class.java)!!
                } else {
                    @Suppress("DEPRECATION")
                    intent.extras!!.getParcelable(Constants.BOARD_DETAILS)!!
                }
        }

        setupActionBar()
        showProgressDialog(resources.getString(R.string.please_wait), this)
        FireStoreClass().getAssignedMembersList(this, currentBoard.assignedTo)
    }

    fun setupMembersList(membersList: ArrayList<User>) {
        assignedMembersList = membersList
        hideProgressDialog()

        val adapter = MemberListItemsAdapter(this, membersList)
        binding.rvMembersList.layoutManager = LinearLayoutManager(this)
        binding.rvMembersList.setHasFixedSize(true)
        binding.rvMembersList.adapter = adapter
    }

    fun memberDetails(user: User) {
        currentBoard.assignedTo.add(user.id)
        FireStoreClass().assignMemberToBoard(this, currentBoard, user)
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarMembersActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_white)
        supportActionBar?.title = resources.getString(R.string.members)

        binding.toolbarMembersActivity.setNavigationOnClickListener {
            if (anyChangesMade) setResult(Activity.RESULT_OK)
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_member -> {
                dialogSearchMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun dialogSearchMember() {
        dialogBinding = DialogSearchMemberBinding.inflate(
            LayoutInflater.from(this),
            findViewById(R.id.cv_dialog_search_member),
            false
        )
        val dialog = Dialog(this)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.tvAdd.setOnClickListener {
            val email = dialogBinding.etEmailSearchMember.text.toString()
            if (email.isNotEmpty()) {
                dialog.dismiss()
                showProgressDialog(resources.getString(R.string.please_wait), this)
                FireStoreClass().getMemberDetails(this, email)
            } else {
                Toast.makeText(
                    this,
                    "Please enter members email address",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        dialogBinding.tvCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun memberAssignSuccess(user: User) {
        assignedMembersList.add(user)
        anyChangesMade = true
        setupMembersList(assignedMembersList)

        showProgressDialog(resources.getString(R.string.please_wait), this)
        lifecycleScope.launch { sendNotificationToUser(currentBoard.name, user.fcmToken) }

        hideProgressDialog()
    }

    private suspend fun sendNotificationToUser(boardName: String, token: String): String {
        var result = ""

        withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null

            result = try {
                val url = URL(Constants.FCM_BASE_URL)
                connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.doOutput = true
                connection.instanceFollowRedirects = false
                connection.useCaches = false
                connection.requestMethod = "POST"

                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")

                connection.setRequestProperty(
                    Constants.FCM_AUTHORIZATION,
                    "${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}"
                )

                val dataOutputStream = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()

                val dataObject = JSONObject()
                dataObject.put(
                    Constants.FCM_KEY_TITLE,
                    "Assigned to the board $boardName"
                )
                dataObject.put(
                    Constants.FCM_KEY_MESSAGE,
                    "You have been assigned to the Board by ${assignedMembersList[0].name}"
                )

                jsonRequest.put(Constants.FCM_KEY_DATA, dataObject)
                jsonRequest.put(Constants.FCM_KEY_TO, token)

                dataOutputStream.writeBytes(jsonRequest.toString())
                dataOutputStream.flush()
                dataOutputStream.close()

                val httpResultCode = connection.responseCode
                if (httpResultCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val br = BufferedReader(InputStreamReader(inputStream))
                    val sb = StringBuilder()
                    var line: String?

                    try {
                        while (br.readLine().also { line = it } != null) {
                            sb.append(line + "\n")
                        }

                    } catch (e: IOException) {
                        e.printStackTrace()

                    } finally {
                        try {
                            inputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }

                    sb.toString()
                } else connection.responseMessage

            } catch (e:SocketTimeoutException) {
                "Connection Timeout"
            } catch (e:Exception) {
                "Error : " + e.message
            } finally {
                connection?.disconnect()
            }
        }
        return result
    }
}