package com.bage.controller;

import com.bage.domain.ResponseResult;
import com.bage.domain.dto.CreatCoArticleDto;
import com.bage.domain.dto.QueryFromOpenpidDto;
import com.bage.service.CoArticleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/co/article") //他可以让我们的ArticleController当中的接口访问路径的前缀都带article
@Api(tags = "发布内容",description = "发布内容相关接口") //swagger的注解
public class CoArticleController {
    @Autowired
    private CoArticleService coArticleService;
    /**
     * 获取发布列表
     * */
    @PostMapping("/getArticleList")
    @ApiOperation(value = "获取发布列表",notes = "获取发布列表")
    public ResponseResult getArticleList(@RequestBody QueryFromOpenpidDto queryFromOpenpidDto){

        return coArticleService.getArticleList(queryFromOpenpidDto);
    }
    /**
     * 用户发布个人作品
     * */
    @PostMapping("/addCoArticle")
    @ApiOperation(value = "发布作品",notes = "发布作品")
    public ResponseResult createNewCoArticle(@RequestBody CreatCoArticleDto coArticleDto){

        return coArticleService.createNewCoArticle(coArticleDto);
    }
    /**
     * 查询单个发布作品
     * */
    @GetMapping("/queryOneCoArticle")
    @ApiOperation(value = "查询单个发布作品",notes = "查询单个发布作品")
    public ResponseResult queryOneCoArticle(@PathVariable Long articleId){

        return coArticleService.queryOneCoArticle(articleId);
    }
    /**
     * 删除单个发布作品（逻辑删除）
     * */
    @GetMapping("/deleteCoArticle")
    @ApiOperation(value = "删除单个发布作品",notes = "删除单个发布作品")
    public ResponseResult deleteCoArticle(@PathVariable Long articleId){

        return coArticleService.deleteCoArticle(articleId);
    }
}
