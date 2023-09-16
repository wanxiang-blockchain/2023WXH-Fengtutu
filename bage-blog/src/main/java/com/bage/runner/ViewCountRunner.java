package com.bage.runner;
/*
* 在项目启动的时候执行runner，将浏览量存储到redis中
* */
import com.bage.domain.entity.Article;
import com.bage.mapper.ArticleMapper;
import com.bage.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component //方法注入到容器中才能被我们的spring容器调用到
public class ViewCountRunner implements CommandLineRunner {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private RedisCache redisCache;

    @Override
    public void run(String... args) throws Exception {
        //查询所有博客信息  id  viewCount
        List<Article> articles = articleMapper.selectList(null); //不需要条件，就传null，这样查的就是所有
        Map<String, Integer> viewCountMap = articles.stream()//使用stream流的方式
                .collect(Collectors.toMap(article -> article.getId().toString(), article -> {
                    return article.getViewCount().intValue();//
                }));
        //存储到redis中
        redisCache.setCacheMap("article:viewCount",viewCountMap);
    }
}
