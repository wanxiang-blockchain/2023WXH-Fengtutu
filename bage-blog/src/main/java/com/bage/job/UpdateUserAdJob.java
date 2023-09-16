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

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
//@Data
//@ConfigurationProperties(prefix = "aivx")
public class UpdateUserAdJob {

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private AIUserMapper aIUserMapper;

    @Autowired
    private AiTranslateMapper aiTranslateMapper;
//    /**
//     * 每天凌晨0点执行定时任务，重置用户可用次数和观看广告次数
//     */
//    @Scheduled(cron = "0 0 0 * * ?")
//    public void resetUsableCount() {
//        System.out.println("树奇的定时任务在0点执行了");
//        // 查询和修改 AdViewNumber 不等于0或 UsableCount 不等于5的数据
//        LambdaUpdateWrapper<AIUser> updateWrapper = new LambdaUpdateWrapper<>();
//        updateWrapper.ne(AIUser::getAdViewNumber, 0).or().ne(AIUser::getUsableCount, "5");
//        List<AIUser> aiUserList = aIUserMapper.selectList(updateWrapper);
//        aiUserList.forEach(user -> {
//            user.setUsableCount("5");
//            user.setAdViewNumber(0);
//            aIUserMapper.updateById(user);
//        });
//    }

    @Scheduled(cron = "0 30 1 * * ?")
    @Transactional
    public void resetUsableCount() {
        try {
            System.out.println("树奇的定时任务在0点执行了");

            LambdaUpdateWrapper<AIUser> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.ne(AIUser::getAdViewNumber, 0).or().ne(AIUser::getUsableCount, "5");

            List<AIUser> aiUserList = aIUserMapper.selectList(updateWrapper);

            for (AIUser user : aiUserList) {
                user.setUsableCount("999");
                user.setAdViewNumber(0);
                aIUserMapper.updateById(user);
            }

            System.out.println("定时任务执行完毕");
        } catch (Exception e) {
            // 处理异常，确保不会中断定时任务的正常执行
            e.printStackTrace();
        }
    }
}
