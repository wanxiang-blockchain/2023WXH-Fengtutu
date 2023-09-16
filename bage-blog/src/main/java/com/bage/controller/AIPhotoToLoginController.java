package com.bage.controller;
/*
* 疯兔兔登录
* */
import com.bage.domain.ResponseResult;
import com.bage.domain.dto.AddCommentDto;
import com.bage.domain.dto.AiConfigDto;
import com.bage.domain.entity.AiConfig;
import com.bage.domain.entity.Comment;
import com.bage.domain.entity.User;
import com.bage.enums.AppHttpCodeEnum;
import com.bage.exception.SystemException;
import com.bage.domain.entity.AIUser;
import com.bage.service.AIUserService;
import com.bage.service.ArticleService;
import com.bage.service.BlogLoginService;
import com.bage.utils.BeanCopyUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/aiphototo") //他可以让我们的ArticleController当中的接口访问路径的前缀都带article
@Api(tags = "用户",description = "用户操作相关接口") //swagger的注解
public class AIPhotoToLoginController {
    @Autowired//直接用我们的service
    private AIUserService aIUserService;

    @PostMapping("/login")
    @ApiOperation(value = "登录",notes = "登录接口")
    public ResponseResult login(@RequestBody AIUser user){//使用User类去接收参数
        //如果没有传入用户名
        if(!StringUtils.hasText(user.getUserName())){
            //提示 必须要传用户名
            throw new SystemException(AppHttpCodeEnum.REQUIRE_USERNAME);
        }
        return aIUserService.login(user);
    }
    /*
    * 获取用户广告数
    * */
    @PostMapping("/getAdCount")
    @ApiOperation(value = "获取用户广告数",notes = "获取用户观看广告数")
    public ResponseResult getAdCount(@RequestBody AIUser user){//使用User类去接收参数
        //如果没有传入用户名
        if(!StringUtils.hasText(user.getUserName())){
            //提示 必须要传用户名
            throw new SystemException(AppHttpCodeEnum.REQUIRE_USERNAME);
        }
        return aIUserService.getAdCount(user);
    }
    /*
     * 用户观看广告
     * */
    @PostMapping("/lookedAd")
    @ApiOperation(value = "用户观看广告",notes = "用户观看广告调用")
    public ResponseResult lookedAd(@RequestBody AIUser user){//使用User类去接收参数
        //如果没有传入用户名
        if(!StringUtils.hasText(user.getUserName())){
            //提示 必须要传用户名
            throw new SystemException(AppHttpCodeEnum.REQUIRE_USERNAME);
        }
        return aIUserService.lookedAd(user);
    }
    /*
    * 用户默认标签设置
    * */
    @PostMapping("/defaultTagUpdate")
    @ApiOperation(value = "用户默认标签设置",notes = "设置用户正词和反词标签")
    public ResponseResult defaultTagUpdate(@RequestBody AIUser user){//使用User类去接收参数
        //如果没有传入用户名
        if(!StringUtils.hasText(user.getUserName())){
            //提示 必须要传用户名
            throw new SystemException(AppHttpCodeEnum.REQUIRE_USERNAME);
        }
        return aIUserService.defaultTagUpdate(user);
    }
    /*
     * 用户获取奖励次数
     * */
    @PostMapping("/reward")
    @ApiOperation(value = "用户获取奖励次数",notes = "用户获取奖励次数")
    public ResponseResult reward(@RequestBody AiConfigDto aiConfigDto){
        return aIUserService.reward(aiConfigDto);
    }
}
