package com.ray.standardapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.loglib.KLog


class MainActivity : AppCompatActivity() {
    lateinit var mAudioDataLoader:AudioDataLoader
    var mProcName = "My StandardApp"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        mProcName = getProcessNameByPid(android.os.Process.myPid())
        KLog.init(true, mProcName)
        // 在MVVM架构下应该放在ViewModel中调用
        mAudioDataLoader = AudioDataLoader()
        mAudioDataLoader.play()
        mAudioDataLoader.getDuration().observe(this, object: Observer<Long> {
            override fun onChanged(value: Long) {
                KLog.i("MainActivity", "onChanged: $value")
            }

        })

    }

//    private fun getProcessNameByPid(myPid: Int): String {
//        val activityManager = applicationContext.getSystemService(Context.ACTIVITY_SERVICE)
//        if (activityManager != null) {
//            for (processInfo in activityManager.runningAppProcesses) {
//                if (processInfo.pid == myPid) {
//                    return processInfo.processName
//                }
//            }
//        }
//        return ""
//    }

}