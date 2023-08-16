package com.example.myview.view

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myview.R
import com.example.myview.base.BaseFragment
import com.example.myview.base.BaseRecyclerAdapter
import com.example.myview.base.ViewHolder
import com.example.myview.databinding.FragmentMainBinding


/**
 * @author ray
 * @date 2023/8/14 12:48
 */
class ModuleMainFragment :BaseFragment() {

    private var _binding: FragmentMainBinding? = null

    private val binding get() = _binding!!

    data class Item(val index: Int, val title: String, val desc: String)

    private val mData = arrayListOf<Item>().apply {
        add(
            Item(1, "aidl easy use", "AIDL - easy use -best practice")
        )

        add(
            Item(2, "xxxxx", "xxxxxxxxxxxxxx")
        )

    }

    //设置adapter
    private val adapter = object : BaseRecyclerAdapter<Item>(R.layout.item_main, mData){
        override fun convertView(viewHolder: ViewHolder?, item: Item, position: Int) {
            viewHolder?.setText(R.id.title, item.title)
            viewHolder?.setText(R.id.desc, item.desc)
        }
    }


    //绑定布局
    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    //处理业务
    override fun doBusiness(context: Context?) {
        // 设置列表
        binding.recycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.recycler.adapter = adapter

        adapter.setOnItemClickListener(object : BaseRecyclerAdapter.ItemClickListener<Item> {
            override fun onItemClick(view: View?, itemObj: Item, position: Int) {

                view?.findNavController()?.navigate(when(itemObj.index) {
                    1 -> R.id.action_main_to_aidl_easytest
                    else -> 0
                })
            }
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}