package com.bage.controller;
/*
* 疯兔兔奖励领取
* */
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/aiphototo") //他可以让我们的ArticleController当中的接口访问路径的前缀都带article
@Api(tags = "奖励",description = "奖励相关接口") //swagger的注解
public class AiConfigController {


}
