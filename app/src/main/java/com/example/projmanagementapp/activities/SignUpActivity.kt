package com.example.projmanagementapp.activities

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.projmanagementapp.R
import com.example.projmanagementapp.databinding.ActivitySignUpBinding
import com.example.projmanagementapp.firebase.FireStoreClass
import com.example.projmanagementapp.models.User
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpActivity : BaseActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        FirebaseApp.initializeApp(this)
        auth = Firebase.auth

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarSignUpActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_black)

        binding.toolbarSignUpActivity.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSignUp.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val name = binding.etName.text.toString().trim { it <= ' ' }
        val email = binding.etEmail.text.toString().trim { it <= ' ' }
        val password = binding.etPassword.text.toString().trim { it <= ' ' }

        if (validateForm(name, email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait), this)
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        val firebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user = User(firebaseUser.uid, name, registeredEmail)

                        FireStoreClass().registerUser(this, user)

                    } else {
                        Toast.makeText(
                            this,
                            task.exception!!.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    fun userRegisteredSuccess() {
        Toast.makeText(
            this,
            "You have successfully registered.",
            Toast.LENGTH_SHORT
        ).show()

        hideProgressDialog()

        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun validateForm(name: String, email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showSnackBar("Please enter a name.")
                false
            }
            TextUtils.isEmpty(email) -> {
                showSnackBar("Please enter an email.")
                false
            }
            TextUtils.isEmpty(password) -> {
                showSnackBar("Please enter a password.")
                false
            }
            else -> true
        }
    }
}