package com.hb.domain.auth.service;

import java.io.IOException;

/**
 * 微信服务
 */
public interface ILoginService {

    String createQrCodeTicket() throws Exception;

    String createQrCodeTicket(String sceneStr) throws Exception;

    String checkLogin(String ticket);

    String checkLogin(String ticket, String sceneStr);

    void saveLoginState(String ticket, String openid) throws IOException;

}
