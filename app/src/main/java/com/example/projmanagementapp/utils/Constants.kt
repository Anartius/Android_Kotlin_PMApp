package com.example.projmanagementapp.utils

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResultLauncher
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

object Constants {

    const val USERS = "users"
    const val BOARDS = "boards"

    const val PM_PREFERENCES = "pm_preferences"
    const val FCM_TOKEN_UPDATED = "fcm_token_updated"
    const val FCM_BASE_URL = "https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION = "authorization"
    const val FCM_KEY = "key"
    const val FCM_SERVER_KEY = "AAAA2qJ2GG4:APA91bHm_hi3iIOla2ucEyCSsb61DibFvCYo" +
            "UF3nPWzHimbNMCG5hA4oWtf6ibWGLUkmCgZxCrzc5ZDw1nmgOB7o77Z88wdcVahBR1O" +
            "d9Jdt64GogVYdxKsiE0l7OGmRdqz-ftMElZEE"
    const val FCM_KEY_TITLE = "title"
    const val FCM_KEY_MESSAGE = "message"
    const val FCM_KEY_DATA = "data"
    const val FCM_KEY_TO = "to"

    const val ID = "id"
    const val FCM_TOKEN = "fcmToken"
    const val NAME = "name"
    const val EMAIL = "email"
    const val IMAGE = "image"
    const val MOBILE = "mobile"
    const val ASSIGNED_TO = "assignedTo"
    const val DOCUMENT_ID = "documentId"
    const val TASK_LIST = "taskList"

    const val BOARD_DETAILS = "board_details"
    const val TASK_LIST_ITEM_POSITION = "task_list_item_position"
    const val CARD_LIST_ITEM_POSITION = "card_list_item_position"
    const val BOARD_MEMBERS_LIST = "board_members_list"
    const val SELECT = "Select"
    const val UNSELECT = "Unselect"

    private val permissions = if (Build.VERSION.SDK_INT >= 33) {
        listOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_VIDEO,
            android.Manifest.permission.READ_MEDIA_AUDIO
        )
    } else {
        listOf(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
        )
    }

    fun pickImageFromGallery(
        context: Context,
        pickImageFromGalleryForResult: ActivityResultLauncher<Intent>,
        packageName: String
    ) {
        Dexter.withContext(context)
            .withPermissions(permissions)
            .withListener(object: MultiplePermissionsListener {

                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {

                        val pickIntent = Intent(Intent.ACTION_PICK)
                        pickIntent.setDataAndType(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            "image/*"
                        )
                        pickImageFromGalleryForResult.launch(pickIntent)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>,
                    token: PermissionToken
                ) {
                    showRationaleDialogForPermissions(context, packageName)
                }
            }).onSameThread().check()
    }

    private fun showRationaleDialogForPermissions(context: Context, packageName: String) {
        AlertDialog.Builder(context)
            .setMessage("It looks like you have turned off permission required for" +
                    " this feature. It can be enabled in the apps settings.")
            .setPositiveButton("GO TO SETTINGS") {
                    _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("CANCEL") {
                    dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    fun getFileExtension(context: Context, uri: Uri?): String? {
        return MimeTypeMap
            .getSingleton()
            .getExtensionFromMimeType(uri?.let { context.contentResolver.getType(it) })
    }
}