package com.pinto.pay

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import com.alipay.sdk.app.PayTask
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.*
import java.lang.Exception

/**
 * author：cheng
 * time: 2020/5/9 8:30 PM
 * desc:
 *
 */

class Ali(private val activity: Activity, private val channel: MethodChannel) {


    @SuppressLint("PackageManagerGetSignatures")
    fun isAlipayInstalled(call: MethodCall, result: MethodChannel.Result) {
        var isAlipayInstalled = false
        try {
            val packageManager = activity.packageManager
            val packageInfo = packageManager.getPackageInfo("com.eg.android.AlipayGphone", PackageManager.GET_SIGNATURES)
            isAlipayInstalled = packageInfo != null
            result.success(isAlipayInstalled)
            return
        } catch (e: Exception) {

        }
        result.success(isAlipayInstalled)

    }


    private suspend fun doPayTask(orderInfo: String, isShowLoading: Boolean): Map<String, String> = withContext(Dispatchers.IO) {
        val alipay = PayTask(activity)
        alipay.payV2(orderInfo, isShowLoading) ?: mapOf<String, String>()

    }

    fun aliPay(call: MethodCall, result: MethodChannel.Result) {
        ///创建一个协程
        GlobalScope.launch {
            val orderInfo = call.argument<String>("orderInfo") as String
            val isShowLoading = call.argument<Boolean>("isShowLoading") as Boolean
            val payResult = doPayTask(orderInfo, isShowLoading)


            withContext(Dispatchers.Main){
                println("pay result =========${payResult}")
                channel.invokeMethod("onAliPayResp", payResult)
            }
        }

        result.success(null)

    }
}