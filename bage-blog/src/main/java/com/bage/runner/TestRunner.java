package com.bage.runner;
/*
* 测试 CommandLineRunner 实现项目启动时预处理功能
* */
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component//因为这个类要交给spring容器管理，所以一定要加这个注解
public class TestRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        System.out.println("程序初始化");
    }
}
