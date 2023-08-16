package com.example.myview.moduleDemo

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.loglib.KLog
import com.example.myview.R
import com.example.myview.base.BaseFragment
import com.example.myview.base.BaseRecyclerAdapter
import com.example.myview.base.ViewHolder
import com.example.myview.databinding.FragmentModuleAidlBinding
import com.example.myview.moduleDemo.MyClient.ClientManager

/**
 * @author ray
 * @date 2023/8/14 17:51
 */
class AIDLEasyTestView : BaseFragment() {

    private var _binding: FragmentModuleAidlBinding? = null

    private val binding get() = _binding!!

    data class Item(val index: Int, val title: String)


    private val mData = arrayListOf<Item>().apply {
        add(Item(1,"基础使用"))
        add(Item(2,"transferCustomType"))
        add(Item(3,"transferParcelSerial"))
        add(Item(4,"transferBundle"))
        add(Item(5,"callOneway"))
        add(Item(6,"callRemote"))
        add(Item(7,"callBinderPool"))
        add(Item(8,"callPermission"))
        add(Item(9,"binderpool"))
    }

    //设置adapter
    private val adapter02 = object : BaseRecyclerAdapter<Item>(R.layout.item_button, mData){
        override fun convertView(viewHolder: ViewHolder?, item: Item, position: Int) {

            viewHolder?.setText(R.id.button_idx, item.title)
            viewHolder?.setOnClickListener(R.id.button_idx) {
                val index = mData.indexOf(item) + 1
                KLog.d(""," - index - $index")
                handleButtonClick(index)
            }
        }
    }

    private fun handleButtonClick(index: Int) {
        when(index) {
            1 -> handleBaseUse()
            2 -> handleTransferCustomType()
            3 -> handleTransferParcelSerial()
            4 -> handleTransferBundle()
            5 -> handleCallOneway()
            6 -> handleCallRemote()
            7 -> handleCallBinderPool()
            8 -> handleCallPermission()
            9 -> handleNine()
            else -> KLog.d("","null")
        }
    }

    //绑定布局
    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): View? {
        _binding = FragmentModuleAidlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        Log.d(""," - wwwww init - ")
        ClientManager.getInstance().bindToService(activity)
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

    private fun handleCallPermission() {
        KLog.d(TAG, " - callPermission - ")
        ClientManager.getInstance().callPermission()
    }


    private fun handleCallBinderPool() {
        KLog.d(TAG, " - callBinderPool - ")
        ClientManager.getInstance().callBinderPool()
    }

    private fun handleCallRemote() {
        KLog.d(TAG, " - callRemote - ")
        ClientManager.getInstance().callRemote()
    }

    private fun handleCallOneway() {
        KLog.d(TAG, " - callOneway - ")
        ClientManager.getInstance().callOneway()
    }

    private fun handleTransferBundle() {
        KLog.d(TAG, " - transferBundle - ")
        ClientManager.getInstance().transferBundle()
    }

    private fun handleTransferParcelSerial() {
        KLog.d(TAG, " - transferParcelSerial - ")
        ClientManager.getInstance().transferParcelSerial()
    }

    private fun handleTransferCustomType() {
        KLog.d(TAG,"- transferCustomType -")
        ClientManager.getInstance().transferCustomType()
    }

    private fun handleBaseUse() {
        KLog.d(TAG,"- handleBaseUse -")
        ClientManager.getInstance().baseCalculate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}