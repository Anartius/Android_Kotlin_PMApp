package com.example.projmanagementapp.activities

import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.projmanagementapp.R
import com.example.projmanagementapp.databinding.DialogProgressBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

open class BaseActivity : AppCompatActivity() {
    lateinit var progressDialog: Dialog
    private var doubleBackToExitPressedOnce = false

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_base)
//    }

    fun showProgressDialog(text: String, context: Context) {
        progressDialog = Dialog(context)
        val dialogBinding = DialogProgressBinding.inflate(
            LayoutInflater.from(this), findViewById(R.id.tv_progress_text),false)
        progressDialog.setContentView(dialogBinding.root)
        val progressDialogText = dialogBinding.tvProgressText
        progressDialogText.text = text

        progressDialog.show()
    }

    fun hideProgressDialog() {
        progressDialog.dismiss()
    }

    fun getCurrentUserID(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: "none"
    }

    fun showSnackBar(msg: String) {
        val snackBar = Snackbar.make(
            findViewById(android.R.id.content),
            msg,
            Snackbar.LENGTH_SHORT
        )

        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(
            ContextCompat.getColor(this, R.color.snackbar_error_color))

        snackBar.show()
    }

    fun doubleBackToExit() {
        if (doubleBackToExitPressedOnce) {
            finishAndRemoveTask()
            return
        }

        this.doubleBackToExitPressedOnce = true

        val snackBar = Snackbar.make(
            findViewById(android.R.id.content),
            getString(R.string.please_click_back_again_to_exit),
            Snackbar.LENGTH_SHORT
        )

        val snackBarTextView = snackBar.view.findViewById<TextView>(
            com.google.android.material.R.id.snackbar_text
        )
        snackBarTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER

        snackBar.show()

        Handler(Looper.getMainLooper()).postDelayed(
            { doubleBackToExitPressedOnce = false },
            2000
        )
    }
}