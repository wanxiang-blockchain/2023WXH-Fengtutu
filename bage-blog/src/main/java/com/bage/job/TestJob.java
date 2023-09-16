package com.bage.job;
/**
 * 测试定时任务的文件
 **/

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TestJob {

//    @Scheduled(cron = "0/5 * * * * ?") //注解标识需要定时执行的代码 目前可以使用 0/5 * * * * ? 进行测试，代表从0秒开始，每隔5秒执行一次。
//    public void testJob(){
//        //要执行的代码
//        System.out.println("定时任务执行了");
//    }
}
