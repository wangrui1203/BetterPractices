package com.example.myview.viewDemo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myview.R
import com.example.myview.base.BaseFragment
import com.example.myview.base.BaseRecyclerAdapter
import com.example.myview.base.ViewHolder
import com.example.myview.databinding.FragmentLeftDeleteBinding
import com.example.myview.view.LeftDeleteItemLayout

class LeftDeleteItemLayoutDemo : BaseFragment(){

    private var _binding: FragmentLeftDeleteBinding? = null
    private val binding get() = _binding!!

    private val mData = arrayListOf(0, 1, 2, 3, 4, 5 ,6, 7, 8, 9, 1, 2, 3, 4, 5 ,6, 7, 8, 9)

    // 适配器
    private val mAdapter = object: BaseRecyclerAdapter<Int>(R.layout.item_left_delete, mData) {
        override fun convertView(viewHolder: ViewHolder?, item: Int, position: Int) {
            viewHolder?.apply {
                setText(R.id.order, position.toString())
                // setText(R.id.title, item.toString())
                (itemView as LeftDeleteItemLayout).mDeleteClickListener = View.OnClickListener {
                    val index = mData.indexOf(item)
                    // 删除数据并更新
                    mData.remove(item)
                    notifyItemRemoved(index)
                }
            }
        }
    }

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = FragmentLeftDeleteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun doBusiness(context: Context?) {
        // 设置列表
        binding.recycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.recycler.adapter = mAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}