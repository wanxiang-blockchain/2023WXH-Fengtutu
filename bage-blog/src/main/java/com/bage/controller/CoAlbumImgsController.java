package com.bage.controller;

import com.bage.domain.ResponseResult;
import com.bage.domain.dto.RemoveCoAlbumImgsDto;
import com.bage.domain.entity.CoAlbumImgs;
import com.bage.service.CoAlbumImgsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/co/album/imgs") //他可以让我们的ArticleController当中的接口访问路径的前缀都带article
@Api(tags = "画册图片",description = "画册图片相关接口") //swagger的注解
public class CoAlbumImgsController {
    @Autowired
    private CoAlbumImgsService coAlbumImgsService;
    /**
     * 用户添加画册图片（批量）
     * */
    @PostMapping("/addCoAlbumImgs")
    @ApiOperation(value = "添加画册图片（批量）",notes = "添加画册图片")
    public ResponseResult addCoAlbumImgs(@RequestBody List<CoAlbumImgs> coAlbumImgs){
        return coAlbumImgsService.addCoAlbumImgs(coAlbumImgs);
    }
    /**
     * 批量删除图片（逻辑删除）
     * */
    @GetMapping("/delete/by/id/{id}")
    @ApiOperation(value = "批量删除图片",notes = "批量删除图片")
    public ResponseResult deleteCoAlbumImgs(@PathVariable(value = "id") List<Integer> ids){

        return coAlbumImgsService.deleteCoAlbumImgs(ids);
    }
    /**
     * 图片移动（批量）
     * */
    @PostMapping("/removeCoAlbumImgs")
    @ApiOperation(value = "移动画册图片（批量）",notes = "移动画册图片")
    public ResponseResult removeCoAlbumImgs(@RequestBody RemoveCoAlbumImgsDto removeCoAlbumImgsDto){
        return coAlbumImgsService.removeCoAlbumImgs(removeCoAlbumImgsDto);
    }
}
