package com.hb.api;

import com.hb.api.response.Response;

public interface IAuthService {

    Response<String> weixinQrCodeTicket();

    Response<String> checkLogin(String ticket);
}
