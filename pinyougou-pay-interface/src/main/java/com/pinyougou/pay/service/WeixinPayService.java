package com.pinyougou.pay.service;

import java.util.Map;

public interface WeixinPayService {
    //生成微信支付二维码
    public Map createNative(String out_trade_no,String total_fee);
    //查询支付状态
    public Map queryPayStatus(String out_trade_no);
}
