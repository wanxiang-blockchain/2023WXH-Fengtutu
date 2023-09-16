package com.bage.controller;

import com.bage.domain.ResponseResult;
import com.bage.domain.dto.AddCommentDto;
import com.bage.domain.entity.CommunityComment;
import com.bage.service.CommunityCommentService;
import com.bage.utils.BeanCopyUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/co/comment")
@Api(tags = "社区文章评论",description = "文章评论相关接口") //swagger的注解
public class CommunityCommentController {
    @Autowired
    private CommunityCommentService communityCommentService;

    @GetMapping("/delCommunityComment/by/id/{id}")
    @ApiOperation(value = "删除评论",notes = "删除评论")
    public ResponseResult delCommunityComment(@PathVariable(value = "id") Long id){
        return communityCommentService.delCommunityComment(id);
    }

    @GetMapping("/getCoCommentList/by/articleId/{id}")
    @ApiOperation(value = "根据文章id获取评论列表",notes = "根据文章id获取评论列表")
    public ResponseResult getCoCommentListById(@PathVariable(value = "id") Long id){

        return communityCommentService.getCoCommentListById(id);
    }

    @PostMapping("/addCommunityComment")
    @ApiOperation(value = "添加评论",notes = "添加评论")
    public ResponseResult addCommunityComment(@RequestBody AddCommentDto addCommentDto){
        CommunityComment coComment = BeanCopyUtils.copyBean(addCommentDto, CommunityComment.class); //把AddCommentDto转换成CommunityComment再传给service
        return communityCommentService.addCommunityComment(coComment);
    }
}
