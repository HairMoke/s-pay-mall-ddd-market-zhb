package com.hb.api;

import com.hb.api.response.Response;

public interface IAuthService {

    Response<String> weixinQrCodeTicket();

    Response<String> weixinQrCodeTicket(String sceneStr);

    Response<String> checkLogin(String ticket);

    Response<String> checkLogin(String ticket, String sceneStr);
}
