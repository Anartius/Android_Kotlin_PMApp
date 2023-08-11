package com.example.projmanagementapp.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.projmanagementapp.R
import com.example.projmanagementapp.adapters.BoardItemsAdapter
import com.example.projmanagementapp.databinding.ActivityMainBinding
import com.example.projmanagementapp.databinding.MainContentBinding
import com.example.projmanagementapp.databinding.NavHeaderMainBinding
import com.example.projmanagementapp.firebase.FireStoreClass
import com.example.projmanagementapp.models.Board
import com.example.projmanagementapp.models.User
import com.example.projmanagementapp.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navHeaderMainBinding: NavHeaderMainBinding
    private lateinit var mainContentBinding: MainContentBinding
    private lateinit var userName: String
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        val currentUserId = FireStoreClass().getCurrentUserId()
        if (currentUserId.isEmpty()) {
            val intent = Intent(this@MainActivity, IntroActivity::class.java)
            startActivity(intent)
            finish()
        }

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navHeaderView = binding.navView.getHeaderView(0)
        navHeaderMainBinding = NavHeaderMainBinding.bind(navHeaderView)

        mainContentBinding = MainContentBinding.bind(binding.appBarMain.mainContent.root)

        setupActionBar()

        binding.navView.setNavigationItemSelectedListener(this)

        sharedPreferences =
            this.getSharedPreferences(Constants.PM_PREFERENCES, Context.MODE_PRIVATE)

        val tokenUpdated = sharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)

        if (tokenUpdated) {
            showProgressDialog(resources.getString(R.string.please_wait), this)
            FireStoreClass().loadUserData(this, true)
        } else {
            FirebaseMessaging.getInstance().token.addOnSuccessListener {
                updateFcmToken(it)
            }
        }

        FireStoreClass().loadUserData(this, true)

        binding.appBarMain.fabCreateBoard.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, userName)
            startCreateBoardActivityForResult.launch(intent)
        }
    }

    private fun setupActionBar() {
        val toolbar = binding.appBarMain.toolbarMainActivity
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_menu)
        toolbar.setNavigationOnClickListener {
            toggleDrawer()
        }
        onBackPressedDispatcher.addCallback(object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    doubleBackToExit()
                }
            }
        })
    }

    fun populateBoardsListToUI(boardsList: ArrayList<Board>) {
        val rvBoardsList = mainContentBinding.rvBoardsList
        val tvNoBoards = mainContentBinding.tvNoBoards

        hideProgressDialog()

        if (boardsList.isNotEmpty()) {
            rvBoardsList.visibility = View.VISIBLE
            tvNoBoards.visibility = View.GONE

            rvBoardsList.layoutManager = LinearLayoutManager(this)
            rvBoardsList.setHasFixedSize(true)

            val adapter = BoardItemsAdapter(this, boardsList)
            rvBoardsList.adapter = adapter

            adapter.setOnClickListener(object: BoardItemsAdapter.OnClickListener {
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }
            })
        } else {
            rvBoardsList.visibility = View.GONE
            tvNoBoards.visibility = View.VISIBLE
        }
    }

    private fun toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private val startCreateBoardActivityForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode == Activity.RESULT_OK) {
            FireStoreClass().getBoardsList(this)
        } else {
            Log.i("CreateBoardActivity", "Finish without a board creating.")
        }
    }

    private val startMyProfileActivityForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode == Activity.RESULT_OK) {
            FireStoreClass().loadUserData(this)
        } else {
            Log.i("MyProfileActivity", "Finish without changes.")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile -> {
                val intent = Intent(this, MyProfileActivity::class.java)
                startMyProfileActivityForResult.launch(intent)
            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                sharedPreferences.edit().clear().apply()

                val intent = Intent(this, IntroActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

                startActivity(intent)
                finish()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun updateNavUserDetails(user: User, readBoardsList: Boolean) {
        hideProgressDialog()

        userName = user.name

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(navHeaderMainBinding.navUserImage)

        navHeaderMainBinding.tvUsername.text = user.name

        if (readBoardsList) {
            showProgressDialog(resources.getString(R.string.please_wait), this)
            FireStoreClass().getBoardsList(this)
        }
    }

    fun tokenUpdateSuccess() {
        hideProgressDialog()

        val editor = sharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()

        showProgressDialog(resources.getString(R.string.please_wait), this)

        FireStoreClass().loadUserData(this, true)
    }

    private fun updateFcmToken(token: String) {
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token

        showProgressDialog(resources.getString(R.string.please_wait), this)

        FireStoreClass().updateUserProfileData(this, userHashMap)
    }
}