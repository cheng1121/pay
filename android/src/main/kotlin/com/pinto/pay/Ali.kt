package com.pinto.pay

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import com.alipay.sdk.app.PayTask
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.lang.Exception

/**
 * author：cheng
 * time: 2020/5/9 8:30 PM
 * desc:
 *
 */

class Ali(private val activity: Activity, private val channel: MethodChannel) {


    @SuppressLint("PackageManagerGetSignatures")
    fun isAlipayInstalled(result: MethodChannel.Result) {
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


    fun aliPay(call: MethodCall, result: MethodChannel.Result) {
        val orderInfo = call.argument<String>("orderInfo") as String
        val isShowLoading = call.argument<Boolean>("isShowLoading") as Boolean
        Thread(
                Runnable {
                    val alipay = PayTask(activity)
                    val payResult = alipay.payV2(orderInfo, isShowLoading)
                            ?: mapOf<String, String>()
                    activity.runOnUiThread {
                        println("pay result =========${payResult}")
                        channel.invokeMethod("onAliPayResp", payResult)
                    }
                }
        ).start()

        result.success(null)

    }
}