package com.bage.controller;
/*
* 疯兔兔信息提醒
* */

import com.bage.domain.ResponseResult;
import com.bage.domain.entity.AIUser;
import com.bage.domain.entity.AiMessage;
import com.bage.enums.AppHttpCodeEnum;
import com.bage.exception.SystemException;
import com.bage.service.AIUserService;
import com.bage.service.AiMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/aiphototo") //他可以让我们的ArticleController当中的接口访问路径的前缀都带article
@Api(tags = "获取提示信息",description = "获取系统提示信息相关接口") //swagger的注解
public class AIPhotoToMessageController {
    @Autowired//直接用我们的service
    private AiMessageService aiMessageService;

    @PostMapping("/getMessage")
    @ApiOperation(value = "获取系统提示信息",notes = "获取系统提示信息接口")
    public ResponseResult getMessage(@RequestBody AiMessage message){//使用AiMessage类去接收参数
        return aiMessageService.getMessage(message);
    }
}
