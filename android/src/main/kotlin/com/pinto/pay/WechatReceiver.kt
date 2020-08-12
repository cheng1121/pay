package com.pinto.pay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.text.TextUtils



/**
 * author：cheng
 * time: 2020/5/9 7:12 PM
 * desc:
 *
 */

abstract class WechatReceiver: BroadcastReceiver() {

    ///内部为静态方法和静态常量
    companion object {

        private  val ACTION_WECHAT_RESP ="${WechatReceiver::class.java.`package`}.action.WECHAT_RESP"
        private val KEY_WECHAT_RESP = "wechat_resp"


        @JvmStatic
        fun registerReceiver(context: Context,receiver: WechatReceiver){
            val filter = IntentFilter()
            filter.addAction(ACTION_WECHAT_RESP)
            context.registerReceiver(receiver,filter)
        }

        @JvmStatic
        fun unregisterReceiver(context: Context,receiver: WechatReceiver){
            context.unregisterReceiver(receiver)
        }

        fun sendWechatResp(context: Context,resp:Intent){
            val intent = Intent()
            intent.action = ACTION_WECHAT_RESP
            intent.putExtra(KEY_WECHAT_RESP,resp)
            intent.setPackage(context.packageName)
            context.sendBroadcast(intent)
        }

    }

    override fun onReceive(context: Context?, intent: Intent?) {
      if(TextUtils.equals(ACTION_WECHAT_RESP, intent?.action)){
         val resp = intent!!.getParcelableExtra<Intent>(KEY_WECHAT_RESP)
          handleIntent(resp)
      }
    }

   abstract fun handleIntent(intent: Intent)


}