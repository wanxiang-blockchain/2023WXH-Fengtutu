package com.bage.controller;
/*
* 用户相关操作
* */

import com.bage.annotation.SystemLog;
import com.bage.domain.ResponseResult;
import com.bage.domain.entity.User;
import com.bage.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    //获取用户信息
    @GetMapping("/userInfo")
    public ResponseResult userInfo(){
        return userService.userInfo();
    }

    //修改用户信息
    @PutMapping("/userInfo")
    @SystemLog(businessName = "更新用户信息") //使用自己的自定义注解AOP去打印该接口的日志
    public ResponseResult updateUserInfo(@RequestBody User user){
        return userService.updateUserInfo(user);
    }

    //注册
    @PostMapping("/register")
    public ResponseResult register(@RequestBody User user){//请求体中要加@RequestBody注解
        return userService.register(user);
    }
}