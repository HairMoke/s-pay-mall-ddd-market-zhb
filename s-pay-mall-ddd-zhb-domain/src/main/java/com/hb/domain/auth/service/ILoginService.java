package com.hb.domain.auth.service;

import java.io.IOException;

/**
 * 微信服务
 */
public interface ILoginService {

    String createQrCodeTicket() throws Exception;

    String checkLogin(String ticket);

    void saveLoginState(String ticket, String openid) throws IOException;

}
