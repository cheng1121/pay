package com.pinto.pay

import android.app.Activity
import android.content.Intent
import android.os.Bundle

/**
 * author：cheng
 * time: 2020/5/9 7:10 PM
 * desc:
 *
 */

class WechatCallbackActivity: Activity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        finish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent != null) {
            handleIntent(intent)
        }
        finish()

    }

    private fun handleIntent(intent: Intent){
        WechatReceiver.sendWechatResp(this,intent)
    }
}