package com.bage.controller;
/*
* 疯兔兔登录
* */

import com.bage.domain.ResponseResult;
import com.bage.domain.entity.AIUser;
import com.bage.domain.entity.CoUser;
import com.bage.enums.AppHttpCodeEnum;
import com.bage.exception.SystemException;
import com.bage.service.AIUserService;
import com.bage.service.CoUserService;
import com.bage.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/co/user") //他可以让我们的ArticleController当中的接口访问路径的前缀都带article
@Api(tags = "用户",description = "用户操作相关接口") //swagger的注解
public class CoUserController {
    @Autowired
    private CoUserService coUserService;
    /**
    * 用户默认标签设置
    * */
    @PostMapping("/defaultTagUpdate")
    @ApiOperation(value = "用户默认标签设置",notes = "设置用户正词和反词标签")
    public ResponseResult defaultTagUpdate(@RequestBody CoUser user){//使用User类去接收参数

        return coUserService.defaultTagUpdate(user);
    }
}
