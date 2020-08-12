package com.pinto.pay

import android.app.Activity
import android.content.Context
import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

/** PayPlugin */
public class PayPlugin : FlutterPlugin, MethodCallHandler,ActivityAware {


    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var activity: Activity
    private lateinit var wechat: Wechat
    private lateinit var ali:Ali

   fun init() {
        wechat = Wechat(activity, channel)
        ali = Ali(activity,channel)
    }


    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "com.pinto.plugin/pay")
        channel.setMethodCallHandler(this)

    }
    

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "com.pinto.plugin/pay")
            val payPlugin = PayPlugin()
            payPlugin.activity = registrar.activity()
            payPlugin.channel = channel
            payPlugin.init()
            channel.setMethodCallHandler(payPlugin)
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "isAlipayInstalled" -> ali.isAlipayInstalled(call,result)
            "aliPay" -> ali.aliPay(call, result)
            "wechatPay" -> wechat.wechatPay(call, result)
            "wechatInit" -> wechat.wechatInit(call,result)
            "wechatInstalled" ->wechat.wechatInstalled(call,result)
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        wechat.destroy()
        channel.setMethodCallHandler(null)
    }

    override fun onDetachedFromActivity() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
      activity = binding.activity
        init()
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }
}


