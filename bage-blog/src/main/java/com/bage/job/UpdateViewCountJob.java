package com.bage.job;
/**
 * 定时更新文章浏览量的定时任务 将rides中的浏览量定时更新到数据库中
 * */
import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.common.utils.DateUtil;
import com.bage.domain.entity.AIUser;
import com.bage.domain.entity.AiTranslate;
import com.bage.domain.entity.Article;
import com.bage.mapper.AIUserMapper;
import com.bage.mapper.AiTranslateMapper;
import com.bage.service.AIUserService;
import com.bage.service.ArticleService;
import com.bage.utils.RedisCache;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
//@Data
//@ConfigurationProperties(prefix = "aivx")
public class UpdateViewCountJob {

//    private String appid;
//    private String appsecret;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private AIUserMapper aIUserMapper;

    @Autowired
    private AiTranslateMapper aiTranslateMapper;

    @Scheduled(cron = "0/55 * * * * ?")
    public void updateViewCount(){
        //获取redis中的浏览量
//        Map<String, Integer> viewCountMap = redisCache.getCacheMap("article:viewCount");
//
//        List<Article> articles = viewCountMap.entrySet()
//                .stream()
//                .map(entry -> new Article(Long.valueOf(entry.getKey()), entry.getValue().longValue()))
//                .collect(Collectors.toList());
//        //更新到数据库中
////        articleService.updateBatchById(articles);//使用这个方法会报错，因为是判空的问题，它会检验参数是否有属性是null，如果有直接异常。在视频评论区有答案https://www.bilibili.com/video/BV1hq4y1F7zk/?vd_source=0ddd95fdacbb1316d3e4eeab7c4eecab#reply171854396512
//        //更新到数据库中
//        for (Article article : articles) {
//            LambdaUpdateWrapper<Article> updateWrapper = new LambdaUpdateWrapper<>();
//            updateWrapper.eq(Article :: getId, article.getId());
//            updateWrapper.set(Article :: getViewCount, article.getViewCount());
//            articleService.update(updateWrapper);
//        }
    }
    /**
     * 每天凌晨0点执行定时任务，重置用户可用次数和观看广告次数
     */
//    @Scheduled(cron = "0 0 0 * * ?")
//    @Scheduled(cron = "0 12 10 * * ?")// 每天16:16执行
//    public void resetUsableCount() {
//        System.out.println("树奇的定时任务在0点执行了");
//        List<AIUser> aiUserList = aIUserMapper.selectList(null);
//        aiUserList.forEach(user -> {
//            user.setUsableCount("5");
//            user.setAdViewNumber(0);
//            aIUserMapper.updateById(user);
//        });
//    }
    /**
     * 每月最后一天0点执行定时任务，重置用户翻译状态
     */
    @Scheduled(cron = "0 0 0 L * ?") // 每月最后一天的午夜执行任务
    public void runMonthlyTask() {
        // 在这里编写要执行的任务代码
        System.out.println("树奇的定时任务在每月最后一天0点执行了");
        List<AiTranslate> aiTranslateList = aiTranslateMapper.selectList(null);
        aiTranslateList.forEach(aiTranslate -> {
            aiTranslate.setStaus("1");
            aiTranslate.setTranslationuselength("0");
            aiTranslateMapper.updateById(aiTranslate);
        });
    }

    /**
     * 每2小时执行一次执行定时任务，获取用户AccessToken
     */
//    @Scheduled(cron = "0 0 */2 * * ?") // 每2小时执行一次
//    public void getAccessToken() {
//        String appid = "wxae6fd03c53b99951";
//        String appsecret = "b6b20875aaf2ce7a20d0d41fb01ecd11";
//        //把用户信息存入redis
//        String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appid + "&secret=" + appsecret;
//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<String> response = restTemplate.exchange(ACCESS_TOKEN_URL, HttpMethod.GET, null, String.class);
//        if (response.getStatusCode().is2xxSuccessful()) {
//            String responseBody = response.getBody();
//            String str = responseBody;
//            JSONObject jsonObject = JSONObject.parseObject(str);
//            String accessToken = jsonObject.getString("access_token");
//            redisCache.setCacheObject("accessToken" , accessToken);//key为accessToken 要存的对象为accessToken
//            System.out.println("树奇的定时任务在每2小时获取了一次微信的AccessToken"+accessToken);
//        } else {
//            // 请求失败，处理异常情况
//            throw new RuntimeException("获取 access token失败，错误码为: " + response.getStatusCodeValue());
//        }
//    }


}
