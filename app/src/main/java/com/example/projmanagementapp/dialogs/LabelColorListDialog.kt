package com.example.projmanagementapp.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projmanagementapp.adapters.LabelColorListItemsAdapter
import com.example.projmanagementapp.databinding.DialogListBinding

abstract class LabelColorListDialog(
    context: Context,
    private var colorList: ArrayList<String>,
    private val title: String = "",
    private var selectedColor: String = ""
): Dialog(context) {

    private lateinit var binding: DialogListBinding
    private var adapter: LabelColorListItemsAdapter? = null

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
        binding.rvList.layoutManager = LinearLayoutManager(context)

        adapter = LabelColorListItemsAdapter(context, colorList, selectedColor)
        binding.rvList.adapter = adapter

        adapter?.onItemClickListener = object: LabelColorListItemsAdapter.OnItemClickListener {
            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)
            }
        }
    }

    protected abstract fun onItemSelected(color :String)
}