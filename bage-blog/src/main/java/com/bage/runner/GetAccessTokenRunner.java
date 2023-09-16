package com.bage.runner;
/*
* 项目启动时执行获取微信的accessToken任务
* */
import com.alibaba.fastjson.JSONObject;
import com.bage.utils.RedisCache;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Data
@ConfigurationProperties(prefix = "aivx")
@Component//因为这个类要交给spring容器管理，所以一定要加这个注解
public class GetAccessTokenRunner implements CommandLineRunner {
    private String appid;
    private String appsecret;

    @Autowired
    private RedisCache redisCache;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("开始获取微信的accessToken");
//        String appid = "wxae6fd03c53b99951";
//        String appsecret = "b6b20875aaf2ce7a20d0d41fb01ecd11";
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
            System.out.println("获取微信的accessToken成功："+accessToken);
        } else {
            // 请求失败，处理异常情况
            throw new RuntimeException("获取 access token失败，错误码为: " + response.getStatusCodeValue());
        }
    }
}
