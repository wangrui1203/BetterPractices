package com.example.myview

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myview.base.BaseFragment
import com.example.myview.base.BaseRecyclerAdapter
import com.example.myview.base.ViewHolder
import com.example.myview.databinding.FragmentMainBinding

class MainFragment: BaseFragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    //数据
    private val mData = arrayListOf<Item>().apply {
        add(Item(1, "custom view",
            "自定义view练习"))

        add(Item(2, "custom game",
            "自定义view做的小游戏"))

        add(Item(3, "custom widget",
            "自定义widget实用工具"))

        add(Item(4, "custom module",
            "常用封装小module"))
    }

    //适配器
    private val adapter = object: BaseRecyclerAdapter<Item>(R.layout.item_main, mData){
        override fun convertView(viewHolder: ViewHolder?, item: Item, position: Int) {
            viewHolder?.setText(R.id.title, item.title)
            viewHolder?.setText(R.id.desc, item.desc)
        }
    }

    //绑定视图
    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 业务
    override fun doBusiness(context: Context?) {
        // 设置列表
        binding.recycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.recycler.adapter = adapter
        adapter.setOnItemClickListener(object : BaseRecyclerAdapter.ItemClickListener<Item> {
            override fun onItemClick(view: View?, itemObj: Item, position: Int) {
                // navigation跳转
                view?.findNavController()?.navigate(when(itemObj.index) {
                    1 -> R.id.action_main_to_view
//                    2 -> R.id.action_main_to_game
//                    3 -> R.id.action_main_to_widget
                    4 -> R.id.action_main_to_module
                    else -> 0
                })
                // showToast("title: ${itemObj.title}")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    data class Item(val index: Int, val title:String, val desc:String)
}