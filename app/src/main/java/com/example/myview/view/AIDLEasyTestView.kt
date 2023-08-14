package com.example.myview.view

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myview.R
import com.example.myview.base.BaseFragment
import com.example.myview.base.BaseRecyclerAdapter
import com.example.myview.base.ViewHolder
import com.example.myview.databinding.FragmentModuleMainBinding

/**
 * @author ray
 * @date 2023/8/14 17:51
 */
class AIDLEasyTestView : BaseFragment() {

    private var _binding: FragmentModuleMainBinding? = null

    private val binding get() = _binding!!

    data class Item(val index: Int, val title: String)


    private val mData = arrayListOf<Item>().apply {
        add(Item(1,"基础add"))
        add(Item(2,"基础delete"))
        add(Item(3,"基础multi"))
        add(Item(4,"基础devide"))
        add(Item(5,"parcel"))
        add(Item(6,"binder"))
        add(Item(7,"file"))
        add(Item(8,"one way"))
        add(Item(9,"binderpool"))
    }

    //设置adapter
    private val adapter02 = object : BaseRecyclerAdapter<Item>(R.layout.item_button, mData){
        override fun convertView(viewHolder: ViewHolder?, item: Item, position: Int) {
            viewHolder?.setText(R.id.button_idx, item.title)
            viewHolder?.setOnClickListener(R.id.button_idx) {
                val index = mData.indexOf(item) + 1
                Log.d("","wwwww $index")
                handleButtonClick(index)
            }
        }
    }

    private fun handleButtonClick(index: Int) {
        when(index) {
            1 -> handleOne()
            2 -> handleTwo()
            3 -> handleThree()
            4 -> handleFour()
            5 -> handleFive()
            6 -> handleSix()
            7 -> handleSeven()
            8 -> handleEight()
            9 -> handleNine()
            else -> Log.d("","null")
        }
    }

    //绑定布局
    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): View? {
        _binding = FragmentModuleMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    //处理业务
    override fun doBusiness(context: Context?) {
        // 设置列表
        binding.recycler02.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.recycler02.adapter = adapter02

        adapter02.setOnItemClickListener(object : BaseRecyclerAdapter.ItemClickListener<Item> {
            override fun onItemClick(view: View?, itemObj: Item, position: Int) {
//                showToast("title: ${itemObj.title}")
            }
        })

    }


    private fun handleNine() {
    }

    private fun handleEight() {
    }

    private fun handleSeven() {
    }

    private fun handleSix() {
    }

    private fun handleFive() {
    }

    private fun handleFour() {
    }

    private fun handleThree() {
    }

    private fun handleTwo() {

    }

    private fun handleOne() {
        Log.d("","wwwww handleOne")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}