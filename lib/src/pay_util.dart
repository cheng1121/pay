import 'dart:io';

import 'package:flutter/material.dart';
import 'package:in_app_purchase/in_app_purchase.dart';
import 'package:pay/pay_kit.dart';

enum PayChannel {
  wechat,
  ali,
  ios,
  google,
}

///使用顶级函数（第一次使用时才初始化）
PayUtil payUtil = PayUtil.init();

typedef OnIosPaySuccess = void Function(
    String orderId, String verificationData);
typedef OnIosPayError = void Function(String code, String error);

class PayUtil {
  InAppPurchaseConnection _connection;
  Pay _pay;

  ///应用内购监听
  Stream<List<PurchaseDetails>> _purchaseUpdates;
  List<ProductDetails> _productDetails;
  bool _isAvailable = false;
  bool _isWechatInit = false;
  PayUtil.init() {
    if (Platform.isIOS) {
      _connection = InAppPurchaseConnection.instance;
      _init();
    } else {
      _pay = Pay();
    }
  }

  ///产品列表
  List<ProductDetails> get productDetails => _productDetails;

  ///应用内支付的回调
  Stream<List<PurchaseDetails>> get purchaseUpdates => _purchaseUpdates;

  ///第三方支付的回调(微信和支付宝)
  Stream<PayResp> get payRespStream => _pay.payResp();

  Future<bool> isWechatInstalled() async {
    return await _pay.isWechatInstalled();
  }

  Future<bool> isAlipayInstalled() async {
    return await _pay.isAlipayInstalled();
  }

  void onPayResult({
    VoidCallback onAndroidResult,
    OnIosPaySuccess onIosSuccess,
    OnIosPayError onIosError,
  }) {
    if (Platform.isAndroid) {
      payUtil.payRespStream.listen((event) async {
        // showLoading(context, false);
        if (event.type == 2 && event.resultStatus == 9000) {
          ///支付宝
          if (onAndroidResult != null) {
            onAndroidResult();
          }
        } else if (event.type == 1 && event.resultStatus == 0) {
          ///微信
          if (onAndroidResult != null) {
            onAndroidResult();
          }
        }
      });
    } else if (Platform.isIOS) {
      payUtil.purchaseUpdates.listen((event) {
        event.forEach((PurchaseDetails details) async {
          if (details.status == PurchaseStatus.pending) {
            ///支付处理中
          } else if (details.status == PurchaseStatus.purchased) {
            ///通知完成交易
            payUtil.completePurchase(details);

            ///支付完成，向服务器中发送请求，进行验证是否已完成
            if (onIosSuccess != null) {
              onIosSuccess(details.purchaseID,
                  details.verificationData.localVerificationData);
            }
          } else if (details.status == PurchaseStatus.error) {
            if (onIosError != null) {
              onIosError(details.error.code, details.error.message);
            }

            ///通知完成交易
            payUtil.completePurchase(details);
            // showLoading(context, false);
          }
        });
      });
    }
  }

  void _init() async {
    _purchaseUpdates = _connection.purchaseUpdatedStream;
    _isAvailable = await _connection.isAvailable();
  }

  void pay({
    @required PayChannel channel,
    @required PayParams params,
    VoidCallback payStart,
    VoidCallback payEnd,
    ValueSetter<String> payError,
  }) async {
    switch (channel) {
      case PayChannel.wechat:
        await _wechatPay(wechatParams: params);
        break;
      case PayChannel.ali:
        await _aliPay(aliParams: params);
        break;
      case PayChannel.ios:
        await _iosPay(
            params: params,
            payStart: payStart,
            payEnd: payEnd,
            payError: payError);
        break;
      case PayChannel.google:
        break;
    }
  }

  Future<void> _iosPay({
    @required PayParams params,
    VoidCallback payStart,
    VoidCallback payEnd,
    ValueSetter<String> payError,
  }) async {
    void _payError(String error) {
      if (payError != null) {
        payError(error);
      }
    }

    if (_productDetails == null || _productDetails.isEmpty) {
      _payError('ios支付失败，未找到产品列表');
      return;
    }

    if (payStart != null) {
      payStart();
    }
    String productId = params.productId;
    ProductDetails details = _productDetails.firstWhere(
      (element) => element.id == productId,
      orElse: () {
        _payError('ios支付失败，未找到该产品productId = $productId');
        return null;
      },
    );
    if (productDetails == null) {
      return;
    }
    await _executePurchase(details);
    if (payEnd != null) {
      payEnd();
    }
  }

  Future<void> _aliPay({@required PayParams aliParams}) async {
    return await _pay.aliPay(orderInfo: aliParams.orderInfo);
  }

  Future<void> _wechatPay({@required PayParams wechatParams}) async {
    if (!_isWechatInit) {
      print('请先调用wechatInit方法');
      return;
    }
    return await _pay.wechatPay(
        appId: wechatParams.appId,
        partnerId: wechatParams.partnerId,
        prepayId: wechatParams.prepayId,
        package: wechatParams.packageValue,
        nonceStr: wechatParams.nonceStr,
        timeStamp: wechatParams.timeStamp,
        sign: wechatParams.sign);
  }

  ///执行购买产品
  Future<bool> _executePurchase(ProductDetails details,
      {bool isConsumable = false}) async {
    final PurchaseParam param = PurchaseParam(productDetails: details);
    if (isConsumable) {
      return await _connection.buyConsumable(purchaseParam: param);
    } else {
      return await _connection.buyNonConsumable(purchaseParam: param);
    }
  }

  Future<void> wechatInit(String appId) async {
    _isWechatInit = true;
    return await _pay.wechatInit(wechatAppId: appId);
  }

  ///获取未完成的交易
  Future<List<PurchaseDetails>> getPastPurchase({String appUserName}) async {
    if (!_isAvailable) {
      _isAvailable = await _connection.isAvailable();
    }
    final purchaseDetailsResp =
        await _connection.queryPastPurchases(applicationUserName: appUserName);
    return purchaseDetailsResp.pastPurchases;
  }

  Future<void> completePurchase(PurchaseDetails purchaseDetails) async {
    if (Platform.isIOS) {
      await _connection.completePurchase(purchaseDetails);
    } else if (Platform.isAndroid) {
      await _connection.consumePurchase(purchaseDetails);
    }
  }

  ///获取产品列表
  ///[productIds] 产品id列表
  Future<List<ProductDetails>> getProductDetail(List<String> productIds,
      {ValueSetter<List<String>> notFoundIds}) async {
    assert(productIds != null, '产品Id列表不能为null');
    assert(_isAvailable, '支付平台未准备好');
    final productDetailsResponse =
        await _connection.queryProductDetails(productIds.toSet());
    if (productDetailsResponse.error != null) {
      ///查询出错
      print('获取产品列表出错===${productDetailsResponse.error.message}');
      return null;
    }

    ///未发现对应产品的id列表
    notFoundIds(productDetailsResponse.notFoundIDs);
    _productDetails = productDetailsResponse.productDetails;

    ///返回产品列表
    return _productDetails;
  }
}

class PayParams {
  ///支付宝
  String orderInfo;

  ///微信
  String appId;
  String partnerId;
  String prepayId;
  String packageValue;
  String nonceStr;
  String timeStamp;
  String sign;

  ///ios
  String productId;

  PayParams.wechat({
    @required this.appId,
    @required this.partnerId,
    @required this.prepayId,
    @required this.packageValue,
    @required this.nonceStr,
    @required this.timeStamp,
    @required this.sign,
  });

  PayParams.ali({@required this.orderInfo});

  PayParams.ios({@required this.productId});
}
