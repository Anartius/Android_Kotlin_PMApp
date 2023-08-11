package com.example.projmanagementapp.activities

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.projmanagementapp.R
import com.example.projmanagementapp.databinding.ActivityMyProfileBinding
import com.example.projmanagementapp.firebase.FireStoreClass
import com.example.projmanagementapp.models.User
import com.example.projmanagementapp.utils.Constants
import com.google.firebase.storage.FirebaseStorage

import java.io.IOException

class MyProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityMyProfileBinding
    private lateinit var userDetails: User
    private var selectedUserImageUri: Uri? = null
    private var userProfileImageUri = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()

        FireStoreClass().loadUserData(this)

        binding.ivProfileUserImage.setOnClickListener {
            Constants.pickImageFromGallery(
                this,
                pickImageFromGalleryForResult,
                packageName
            )
        }

        binding.btnUpdate.setOnClickListener {
            if (selectedUserImageUri != null) {
                uploadUserImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait), this)
                updateUserProfileData()
            }
        }
    }

    private val pickImageFromGalleryForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode == Activity.RESULT_OK) {
            selectedUserImageUri = result.data?.data
            try {
                Glide
                    .with(this)
                    .load(selectedUserImageUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(binding.ivProfileUserImage)

            } catch (e:IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarMyProfileActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_white)
        supportActionBar?.title = resources.getString(R.string.my_profile)

        binding.toolbarMyProfileActivity.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    fun setUserDataInUI(user: User) {
        userDetails = user

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(binding.ivProfileUserImage)

        binding.etName.setText(user.name)
        binding.etEmail.setText(user.email)

        if (user.mobile != 0L) binding.etMobile.setText(user.mobile.toString())
    }

    private fun uploadUserImage() {
        showProgressDialog(resources.getString(R.string.please_wait), this)
        selectedUserImageUri?.let {
            val storageRef = FirebaseStorage.getInstance().reference.child(
                "USER_Image"
                        + System.currentTimeMillis()
                        + "."
                        + Constants.getFileExtension(this, selectedUserImageUri)
            )

            storageRef.putFile(selectedUserImageUri!!)
                .addOnSuccessListener {
                        taskSnapshot ->
                    Log.i(
                        "Firebase Image URI",
                        taskSnapshot.metadata?.reference?.downloadUrl.toString())

                    taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener {
                            uri ->
                        Log.i("Downloadable Image URI", uri.toString())
                        userProfileImageUri = uri.toString()

                        updateUserProfileData()
                    }
                }
                .addOnFailureListener { exception ->
                    exception.message?.let { msg ->
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>()

        if (
            userProfileImageUri.isNotEmpty() &&
            userProfileImageUri != userDetails.image
        ) {
            userHashMap[Constants.IMAGE] = userProfileImageUri
        }
        if (binding.etName.text.toString() != userDetails.name) {
            userHashMap[Constants.NAME] = binding.etName.text.toString()
        }
        if (binding.etMobile.text.toString() != userDetails.mobile.toString() &&
            binding.etMobile.text.toString().isNotEmpty()
        ) {
            userHashMap[Constants.MOBILE] = binding.etMobile.text.toString().toLong()
        }

        FireStoreClass().updateUserProfileData(this, userHashMap)
    }

    fun profileUpdateSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
}