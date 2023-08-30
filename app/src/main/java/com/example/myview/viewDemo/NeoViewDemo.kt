package com.example.myview.viewDemo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.loglib.KLog
import com.example.myview.base.BaseFragment
import com.example.myview.databinding.FragmentLeftDeleteBinding
import com.example.myview.databinding.FragmentNeoViewBinding

/**
 * @author ray
 * @date 2023/8/29 16:14
 */
class NeoViewDemo: BaseFragment() {

    private var _binding: FragmentNeoViewBinding? = null
    private val binding get() = _binding!!

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = FragmentNeoViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun doBusiness(context: Context?) {
        //
        KLog.d(TAG,"do business")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}