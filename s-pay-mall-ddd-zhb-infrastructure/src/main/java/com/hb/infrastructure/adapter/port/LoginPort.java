package com.hb.infrastructure.adapter.port;

import cn.hutool.core.util.IdUtil;
import com.google.common.cache.Cache;
import com.hb.domain.auth.adapter.port.ILoginPort;
import com.hb.infrastructure.adapter.repository.OrderRepository;
import com.hb.infrastructure.gateway.IWeixinApiService;
import com.hb.infrastructure.gateway.dto.WeixinQrCodeRequestDTO;
import com.hb.infrastructure.gateway.dto.WeixinQrCodeResponseDTO;
import com.hb.infrastructure.gateway.dto.WeixinTemplateMessageDTO;
import com.hb.infrastructure.gateway.dto.WeixinTokenResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Call;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class LoginPort implements ILoginPort {

    @Value("${weixin.config.app-id}")
    private String appid;
    @Value("${weixin.config.app-secret}")
    private String appSecret;
    @Value("${weixin.config.template_id}")
    private String template_id;

    @Resource
    private IWeixinApiService weixinApiService;
    @Resource
    private Cache<String, String> weixinAccessToken;


    /**
     * 获取 ticket；
     * <a href="https://developers.weixin.qq.com/doc/offiaccount/Account_Management/Generating_a_Parametric_QR_Code.html">获取 ticket API</a>
     */
    @Override
    public String createQrCodeTicket() throws IOException {
        String sceneStr = IdUtil.getSnowflake().nextIdStr();
        return createQrCodeTicket(sceneStr);

    }


    @Override
    public String createQrCodeTicket(String sceneStr) throws IOException {
        // 1. 获取 accessToken
        String accessToken = weixinAccessToken.getIfPresent(appid);
        if (null == accessToken) {
            Call<WeixinTokenResponseDTO> call = weixinApiService.getToken("client_credential", appid, appSecret);
            WeixinTokenResponseDTO weixinTokenRes = call.execute().body();
            assert weixinTokenRes != null;
            accessToken = weixinTokenRes.getAccess_token();
            weixinAccessToken.put(appid, accessToken);
        }


        // 2. 生成ticket
        WeixinQrCodeRequestDTO weixinQrCodeReq = WeixinQrCodeRequestDTO.builder()
                .expire_seconds(2592000)
                .action_name(WeixinQrCodeRequestDTO.ActionNameTypeVO.QR_STR_SCENE.getCode())
                .action_info(WeixinQrCodeRequestDTO.ActionInfo.builder()
                        .scene(WeixinQrCodeRequestDTO.ActionInfo.Scene.builder()
                                .scene_str(sceneStr)
                                .build())
                        .build())
                .build();

        Call<WeixinQrCodeResponseDTO> call = weixinApiService.createQrCode(accessToken, weixinQrCodeReq);
        WeixinQrCodeResponseDTO weixinQrCodeRes = call.execute().body();
        assert null != weixinQrCodeRes;
        return weixinQrCodeRes.getTicket();
    }

    @Override
    public void sendLoginTemplate(String openid) throws IOException {
        // 1. 获取 accessToken 【实际业务场景，按需处理下异常】
        String accessToken = weixinAccessToken.getIfPresent(appid);
        if (null == accessToken) {
            Call<WeixinTokenResponseDTO> call = weixinApiService.getToken("client_credential", appid, appSecret);
            WeixinTokenResponseDTO weixinTokenRes = call.execute().body();
            assert weixinTokenRes != null;
            accessToken = weixinTokenRes.getAccess_token();
            weixinAccessToken.put(appid, accessToken);
        }

        // 2. 发送模板消息
        Map<String, Map<String, String>> data = new HashMap<>();


        WeixinTemplateMessageDTO.put(data, WeixinTemplateMessageDTO.TemplateKey.USER, openid);

        WeixinTemplateMessageDTO templateMessageDTO = new WeixinTemplateMessageDTO(openid, template_id);
        templateMessageDTO.setUrl("https://zhbblog.netlify.app/");
        templateMessageDTO.setData(data);


        Call<Void> call = weixinApiService.sendMessage(accessToken, templateMessageDTO);
        call.execute();
    }


}
