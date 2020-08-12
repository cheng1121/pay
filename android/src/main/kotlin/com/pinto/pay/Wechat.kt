package com.pinto.pay

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.modelpay.PayResp
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.util.concurrent.atomic.AtomicBoolean

/**
 * author：cheng
 * time: 2020/5/9 8:11 PM
 * desc:
 *
 */

class Wechat(private val activity: Activity, private val channel: MethodChannel) : WechatReceiver(), IWXAPIEventHandler {
    private val ERROR_CODE = "errorCode"
    private val ERROR_MSG = "errorMsg"
    private val RETURN_KEY = "returnKey"
    private val WECHAT_INSTALLED = "wechatInstalled"
    private val ON_PAY_RESP = "onWechatPayResp"
    private val ARGUMENT_APPID ="wechatAppId"
    private val ARGUMENT_PARTNERID ="wechatPartnerId"
    private val ARGUMENT_PREPAYID ="wechatPrepayId"
    private val ARGUMENT_NONCESTR ="wechatNonceStr"
    private val ARGUMENT_TIMESTAMP ="wechatTimeStamp"
    private val ARGUMENT_PACKAGE ="wechatPackageValue"
    private val ARGUMENT_SIGN ="wechatSign"

    
    private lateinit var iwxapi: IWXAPI
    

    private val register = AtomicBoolean(false)

    init {
        if (register.compareAndSet(false, true)) {
            registerReceiver(activity, this)
        }
    }

    fun destroy(){
        if (register.compareAndSet(true,false)){
            unregisterReceiver(activity,this)
        }
    }

    //初始化微信sdk
    fun wechatInit(call: MethodCall, result: MethodChannel.Result) {
        val appId = call.argument<String>(ARGUMENT_APPID)
        iwxapi = WXAPIFactory.createWXAPI(activity, appId)
        iwxapi.registerApp(appId)
        result.success(null)
    }

    //判断是否安装微信
    fun wechatInstalled(call: MethodCall, result: MethodChannel.Result) {
        result.success(iwxapi.isWXAppInstalled)
    }

    //微信支付
    fun wechatPay(call: MethodCall, result: MethodChannel.Result) {
       val appId= call.argument<String>(ARGUMENT_APPID)
        val partnerId = call.argument<String>(ARGUMENT_PARTNERID)
        val prepayId = call.argument<String>(ARGUMENT_PREPAYID)
        val packageValue  = call.argument<String>(ARGUMENT_PACKAGE)
        val nonceStr = call.argument<String>(ARGUMENT_NONCESTR)
        val timeStamp = call.argument<String>(ARGUMENT_TIMESTAMP)
        val sign = call.argument<String>(ARGUMENT_SIGN)
        val req = PayReq()
        req.appId = appId
        req.partnerId = partnerId
        req.prepayId = prepayId
        req.packageValue = packageValue
        req.nonceStr = nonceStr
        req.timeStamp = timeStamp
        req.sign = sign
        iwxapi.sendReq(req)
        result.success(null)
    }


    override fun handleIntent(intent: Intent) {
        iwxapi.handleIntent(intent, this)
    }

    override fun onResp(resp: BaseResp?) {
        val map = HashMap<String, Any>()
        if (resp != null) {
            map[ERROR_CODE] = resp.errCode
            map[ERROR_MSG] = ""
            //支付回调
            if (resp is PayResp && resp.errCode == 0) {
                map[RETURN_KEY] = resp.returnKey


            }
        }
        channel.invokeMethod(ON_PAY_RESP, map)
    }

    override fun onReq(req: BaseReq?) {
        TODO("Not yet implemented")
    }
}