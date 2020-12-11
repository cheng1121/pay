import 'dart:async';

import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';

class Pay {
  Pay() {
    _channel.setMethodCallHandler(_handlerMethod);
  }

  final MethodChannel _channel = const MethodChannel('com.pinto.plugin/pay');

  final StreamController<PayResp> _payRespStreamController =
      StreamController<PayResp>.broadcast();
  ValueSetter<PayResp> _onPayResp;

  ///判断是否已安装支付宝
  Future<bool> isAlipayInstalled() async {
    return await _channel.invokeMethod<bool>('isAlipayInstalled');
  }

  ///微信是否安装
  Future<bool> isWechatInstalled() async {
    return await _channel.invokeMethod<bool>('wechatInstalled');
  }

  ///初始化微信sdk
  Future<void> wechatInit({@required String wechatAppId}) async {
    assert(wechatAppId != null && wechatAppId.isNotEmpty);
    return await _channel.invokeMethod(
        'wechatInit', <String, dynamic>{'wechatAppId': wechatAppId});
  }

  Stream<PayResp> payResp() => _payRespStreamController.stream;

  void setOnPayResp(ValueSetter<PayResp> resp) {
    _onPayResp = resp;
  }

  /// 参数说明：https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_12&index=2
  Future<void> wechatPay({
    @required String appId,
    @required String partnerId,
    @required String prepayId,
    @required String package,
    @required String nonceStr,
    @required String timeStamp,
    @required String sign,
  }) {
    assert(appId != null && appId.isNotEmpty);
    assert(partnerId != null && partnerId.isNotEmpty);
    assert(prepayId != null && prepayId.isNotEmpty);
    assert(package != null && package.isNotEmpty);
    assert(nonceStr != null && nonceStr.isNotEmpty);
    assert(timeStamp != null && timeStamp.isNotEmpty);
    assert(sign != null && sign.isNotEmpty);
    return _channel.invokeMethod('wechatPay', <String, dynamic>{
      'wechatAppId': appId,
      'wechatPartnerId': partnerId,
      'wechatPrepayId': prepayId,
      'wechatNonceStr': nonceStr,
      'wechatTimeStamp': timeStamp,
      'wechatPackageValue': package,
      'wechatSign': sign,
    });
  }

  ///支付宝
  Future<void> aliPay({@required String orderInfo, bool isShowLoading = true}) {
//    _sendPayResp(PayResp.fromAli({
//      'resultStatus': 10,
//      'result': '=======',
//      'memo': '',
//    }));

    final Map<String, dynamic> map = <String, dynamic>{
      'orderInfo': orderInfo,
      'isShowLoading': isShowLoading
    };
    return _channel.invokeMethod('aliPay', map);
  }

  ///支付结果
  Future<dynamic> _handlerMethod(MethodCall call) async {
    switch (call.method) {
      case 'onAliPayResp':
        final String resultStatus = call.arguments['resultStatus'] as String;
        final String result = call.arguments['result'] as String;
        final String memo = call.arguments['memo'] as String;

        final PayResp resp = PayResp.formAlipay(
            resultStatus: int.parse(resultStatus), result: result, memo: memo);
        _sendPayResp(resp);
        break;
      case 'onWechatPayResp':
        final int errorCode = call.arguments['errorCode'] as int;
        final String errorMsg = call.arguments['errorMsg'] as String;
        final String returnKey = call.arguments['returnKey'] as String;
        _sendPayResp(PayResp.fromWechat(
            resultStatus: errorCode, result: errorMsg, memo: returnKey));
        break;
    }
  }

  void _sendPayResp(PayResp resp) {
    _payRespStreamController.add(resp);
    if (_onPayResp != null) {
      _onPayResp(resp);
    }
  }
}

class PayResp {
  PayResp({@required this.resultStatus, this.result, this.memo});

  PayResp.formAlipay(
      {this.resultStatus, this.result, this.memo, this.type = 2});

  PayResp.fromWechat(
      {this.resultStatus, this.result, this.memo, this.type = 1});

  ///微信
  /// 0:成功
  /// -1：错误------可能的原因：签名错误、未注册APPID、项目设置APPID不正确、注册的APPID与设置的不匹配、其他异常等。
  /// -2：用户取消

  ///支付宝
  /// 支付状态，参考支付宝的文档https://docs.open.alipay.com/204/105695/
  /// 返回码，标识支付状态，含义如下：
  /// 9000——订单支付成功         下面的result有值
  /// 8000——正在处理中
  /// 4000——订单支付失败
  /// 5000——重复请求
  /// 6001——用户中途取消
  /// 6002——网络连接出错
  int resultStatus;
  String result;

  ///支付宝--memo
  ///微信--returnKey
  String memo;

  ///1：微信 2：支付宝
  int type;

  // PayResp.fromWechat(Map<String, dynamic> map, {this.type = 1}) {
  //   this.resultStatus = map['errorCode'];
  //   this.result = map['errorMsg'];
  //   this.memo = map['returnKey'];
  // }

  Map<String, String> toMap() {
    return <String, String>{
      'resultStatus': resultStatus.toString(),
      'result': result,
      'memo': memo,
      'type': type.toString(),
    };
  }
}
