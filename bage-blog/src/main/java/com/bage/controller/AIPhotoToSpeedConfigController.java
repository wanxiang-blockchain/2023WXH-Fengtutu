package com.bage.controller;
/*
* 疯兔兔信息提醒
* */

import com.bage.domain.ResponseResult;
import com.bage.enums.AppHttpCodeEnum;
import com.bage.exception.SystemException;
import com.bage.service.AiModelService;
import com.bage.service.AiSpeedConfigService;
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
@Api(tags = "提示词",description = "提示词相关接口") //swagger的注解
public class AIPhotoToSpeedConfigController {
    @Autowired//直接用我们的service
    private AiSpeedConfigService aiSpeedConfigService;

    @PostMapping("/getSpeedConf")
    @ApiOperation(value = "获取提示词列表",notes = "获取提示词列表接口")
    public ResponseResult getSpeedConf(@RequestBody String token){//使用AiModel类去接收参数
        //如果没有传入用户名
        if(!StringUtils.hasText(token)){
            //提示 必须要传token
            throw new SystemException(AppHttpCodeEnum.NEED_LOGIN);
        }
        return aiSpeedConfigService.getSpeedConf(token);
    }
}
