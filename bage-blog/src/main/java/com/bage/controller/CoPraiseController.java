package com.bage.controller;

import com.bage.domain.ResponseResult;
import com.bage.service.CoArticleService;
import com.bage.service.CoPraiseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/co/praise")
@Api(tags = "点赞",description = "点赞相关接口") //swagger的注解
public class CoPraiseController {
    @Autowired
    private CoPraiseService coPraiseService;

    @GetMapping("/delCoPraise/by/id/{id}")
    @ApiOperation(value = "删除点赞",notes = "删除点赞")
    public ResponseResult delCoPraise(@PathVariable(value = "id") Long id){
        return coPraiseService.delCoPraise(id);
    }

    @GetMapping("/getCoPraiseList/by/articleId/{articleId}")
    @ApiOperation(value = "根据文章id获取评点赞列表",notes = "根据文章id获取点赞列表")
    public ResponseResult getPraiseListByArticleId(@PathVariable(value = "articleId") Long articleId){

        return coPraiseService.getPraiseListByArticleId(articleId);
    }

    @GetMapping("/getCoPraiseList/by/createByUserId/{createByUserId}")
    @ApiOperation(value = "根据用户id获取评点赞列表（自己赞过的）",notes = "根据文章id获取点赞列表（自己赞过的）")
    public ResponseResult getPraiseListByCreateByUserId(@PathVariable(value = "createByUserId") Long createByUserId){

        return coPraiseService.getPraiseListByCreateByUserId(createByUserId);
    }

    @GetMapping("/getCoPraiseList/by/toPraiseUserId/{toPraiseUserId}")
    @ApiOperation(value = "根据用户id获取评点赞列表（自己获得的赞）",notes = "根据文章id获取点赞列表（自己获得的赞）")
    public ResponseResult getPraiseListByToPraiseUserId(@PathVariable(value = "toPraiseUserId") Long toPraiseUserId){

        return coPraiseService.getPraiseListByToPraiseUserId(toPraiseUserId);
    }
}
