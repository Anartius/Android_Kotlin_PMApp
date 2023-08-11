package com.example.projmanagementapp.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projmanagementapp.adapters.MemberListItemsAdapter
import com.example.projmanagementapp.databinding.DialogListBinding
import com.example.projmanagementapp.models.User

abstract class MembersListDialog(
    context: Context,
    private val title: String = "",
    private var usersList: ArrayList<User>
): Dialog(context) {

    private lateinit var binding: DialogListBinding
    private var adapter: MemberListItemsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.tvTitle.text = title

        if (usersList.size > 0) {
            binding.rvList.layoutManager = LinearLayoutManager(context)

            adapter = MemberListItemsAdapter(context, usersList)
            binding.rvList.adapter = adapter

            adapter!!.setOnClickListener(
                object: MemberListItemsAdapter.OnClickListener {
                    override fun onClick(position: Int, user: User, action: String) {
                        dismiss()
                        onItemSelected(user, action)
                    }
                }
            )
        }
    }

    protected abstract fun onItemSelected(user: User, action: String)
}