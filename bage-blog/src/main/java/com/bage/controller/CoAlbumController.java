package com.bage.controller;

import com.bage.domain.ResponseResult;
import com.bage.domain.dto.CoAlbumDto;
import com.bage.domain.dto.QueryFromOpenpidDto;
import com.bage.domain.entity.CoAlbum;
import com.bage.service.CoAlbumService;
import com.bage.utils.BeanCopyUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/co/album") //他可以让我们的ArticleController当中的接口访问路径的前缀都带article
@Api(tags = "画册",description = "画册相关接口") //swagger的注解
public class CoAlbumController {
    @Autowired
    private CoAlbumService coAlbumService;
    /**
     * 用户添加画册
     * */
    @PostMapping("/addCoAlbum")
    @ApiOperation(value = "添加画册",notes = "添加画册")
    public ResponseResult creatNewCoAlbum(@RequestBody CoAlbumDto coAlbumDto){
        CoAlbum coAlbum = BeanCopyUtils.copyBean(coAlbumDto, CoAlbum.class);
        return coAlbumService.creatNewCoAlbum(coAlbum);
    }
    /**
     * 根据用户openpid获取画册
     * */
    @PostMapping("/getCoAlbumList")
    @ApiOperation(value = "根据用户openpid获取画册",notes = "根据用户openpid获取画册")
    public ResponseResult queryCoAlbumList(@RequestBody QueryFromOpenpidDto queryFromOpenpidDto){
        return coAlbumService.queryCoAlbumList(queryFromOpenpidDto);
    }
    /**
     * 用户更新画册
     * */
    @PostMapping("/updateCoAlbum")
    @ApiOperation(value = "更新画册",notes = "更新画册")
    public ResponseResult updateCoAlbum(@RequestBody CoAlbumDto coAlbumDto){
        CoAlbum coAlbum = BeanCopyUtils.copyBean(coAlbumDto, CoAlbum.class);
        return coAlbumService.updateCoAlbum(coAlbum);
    }
    /**
     * 删除单个画册（逻辑删除）
     * */
    @GetMapping("/deleteCoAlbum/by/id/{id}")
    @ApiOperation(value = "删除单个画册",notes = "删除单个画册")
    public ResponseResult deleteCoAlbum(@PathVariable(value = "id") Long id){

        return coAlbumService.deleteCoAlbum(id);
    }
}
