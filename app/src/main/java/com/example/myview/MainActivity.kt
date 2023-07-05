package com.example.myview

import android.content.Context
import android.view.View
import com.example.myview.base.BaseActivity
import com.example.myview.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun bindView(): View? {
        binding = ActivityMainBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun doBusiness(context: Context) {
        //do nothing
    }
}