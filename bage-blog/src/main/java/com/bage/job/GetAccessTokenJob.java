package com.bage.job;
/**
 * 每2小时执行一次执行定时任务，获取用户AccessToken
 * */
import com.alibaba.fastjson.JSONObject;
import com.bage.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

//@Data
//@ConfigurationProperties(prefix = "aivx")
@Component
public class GetAccessTokenJob {

    @Autowired
    private RedisCache redisCache;
    /**
     * 每1小时50分执行一次执行定时任务，获取用户AccessToken
     */
    @Scheduled(cron = "0 50 */1 * * ?") // 每1小时50分执行一次
    public void getAccessToken() {
        String appid = "wxae6fd03c53b99951";
        String appsecret = "b6b20875aaf2ce7a20d0d41fb01ecd11";
        //把用户信息存入redis
        String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appid + "&secret=" + appsecret;
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(ACCESS_TOKEN_URL, HttpMethod.GET, null, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            String responseBody = response.getBody();
            String str = responseBody;
            JSONObject jsonObject = JSONObject.parseObject(str);
            String accessToken = jsonObject.getString("access_token");
            redisCache.setCacheObject("accessToken" , accessToken);//key为accessToken 要存的对象为accessToken
            System.out.println("树奇的定时任务在每2小时获取了一次微信的AccessToken"+accessToken);
        } else {
            // 请求失败，处理异常情况
            throw new RuntimeException("获取 access token失败，错误码为: " + response.getStatusCodeValue());
        }
    }
}
