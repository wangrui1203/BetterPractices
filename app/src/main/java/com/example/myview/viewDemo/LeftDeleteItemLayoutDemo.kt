package com.example.myview.viewDemo

import com.example.myview.base.BaseFragment
import com.example.myview.databinding.FragmentLeftDeleteBinding

class LeftDeleteItemLayoutDemo : BaseFragment(){

    private var _binding: FragmentLeftDeleteBinding? = null
    private val binding get() = _binding!!

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}