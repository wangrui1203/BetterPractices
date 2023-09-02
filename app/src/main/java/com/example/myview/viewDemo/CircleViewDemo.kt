package com.example.myview.viewDemo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.loglib.KLog
import com.example.myview.R
import com.example.myview.base.BaseFragment
import com.example.myview.databinding.FragmentCircleViewBinding
import com.example.myview.databinding.FragmentNeoViewBinding
import com.example.myview.view.CircleView

class CircleViewDemo:BaseFragment() {

    private var _binding: FragmentCircleViewBinding? = null
    private val binding get() = _binding!!
    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = FragmentCircleViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun doBusiness(context: Context?) {
        //
        //设置进度
        binding.setProgress.setOnClickListener {
            binding.progressView.setValue("100",100f)
        }
        KLog.d(TAG,"do business 100%")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}