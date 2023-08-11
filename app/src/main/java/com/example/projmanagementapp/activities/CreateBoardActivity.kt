package com.example.projmanagementapp.activities

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.projmanagementapp.R
import com.example.projmanagementapp.databinding.ActivityCreateBoardBinding
import com.example.projmanagementapp.firebase.FireStoreClass
import com.example.projmanagementapp.models.Board
import com.example.projmanagementapp.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import java.io.IOException

class CreateBoardActivity : BaseActivity() {
    private lateinit var binding: ActivityCreateBoardBinding
    private lateinit var userName: String
    private var selectedImageUri: Uri? = null
    private var boardImageUri = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()

        if (intent.hasExtra(Constants.NAME)) {
            userName = intent.getStringExtra(Constants.NAME)!!
        }

        binding.ivBoardImage.setOnClickListener {
            Constants.pickImageFromGallery(
                this,
                pickImageFromGalleryForResult,
                packageName
            )
        }

        binding.btnCreate.setOnClickListener {
            if (selectedImageUri != null) {
                uploadBoardImage()
            } else {
                createBoard()
            }
        }
    }

    private fun setupActionBar() {
        val toolbar = binding.toolbarCreateBoardActivity
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.create_board_title)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_white)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private val pickImageFromGalleryForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            try {
                Glide
                    .with(this)
                    .load(selectedImageUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(binding.ivBoardImage)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun createBoard() {
        val assignedUsers = ArrayList<String>()
        assignedUsers.add(getCurrentUserID())

        val board = Board("",
            binding.etBoardName.text.toString(),
            boardImageUri,
            userName,
            assignedUsers
        )

        FireStoreClass().createBoard(this, board)
    }

    private fun uploadBoardImage() {
        showProgressDialog(resources.getString(R.string.please_wait), this)

        val storageRef = FirebaseStorage.getInstance().reference.child(
            "BOARD_Image"
                    + System.currentTimeMillis()
                    + "."
                    + Constants.getFileExtension(this, selectedImageUri)
        )

        storageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                    taskSnapshot ->
                Log.i(
                    "Firebase Board Image URI",
                    taskSnapshot.metadata?.reference?.downloadUrl.toString())

                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener {
                        uri ->
                    Log.i("Downloadable Image URI", uri.toString())
                    boardImageUri = uri.toString()

                    createBoard()
                }
            }
            .addOnFailureListener { exception ->
                exception.message?.let { msg ->
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun boardCreatedSuccessfully() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
}