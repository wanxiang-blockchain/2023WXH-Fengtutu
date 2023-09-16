package com.bage.controller;
/*
* 评论
* */

import com.bage.constants.SystemConstants;
import com.bage.domain.ResponseResult;
import com.bage.domain.dto.AddCommentDto;
import com.bage.domain.entity.Comment;
import com.bage.service.CommentService;
import com.bage.utils.BeanCopyUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
@Api(tags = "评论",description = "评论相关接口") //swagger的注解
public class CommentController {

    @Autowired
    private CommentService commentService;

    //文章的评论列表 SystemConstants.ARTICLE_COMMENT用来在表里区分评论类型是文章下面的还是友链下面的
    @GetMapping("/commentList")
    public ResponseResult commentList(Long articleId,Integer pageNum,Integer pageSize){
        return commentService.commentList(SystemConstants.ARTICLE_COMMENT,articleId,pageNum,pageSize);
    }

    //添加评论
//    @PostMapping
//    public ResponseResult addComment(@RequestBody Comment comment){
//        return commentService.addComment(comment);
//    }

    //因为我们新增评论用了entity下的Comment类，但是后面加入编辑还用这个类就会很乱所以我们新创建新增评论这个dto的类，让这个AddCommentDto类专门用作新增评论的类。但是要去controller去引用一下。
    @PostMapping
    public ResponseResult addComment(@RequestBody AddCommentDto addCommentDto){
        Comment comment = BeanCopyUtils.copyBean(addCommentDto, Comment.class); //把AddCommentDto转换成Comment再传给service
        return commentService.addComment(comment);
    }

    //友链的评论列表
    @GetMapping("/linkCommentList")
    @ApiOperation(value = "友链评论列表",notes = "获取一页友链评论") //swagger的注解
    @ApiImplicitParams({ //swagger的注解
        @ApiImplicitParam(name = "pageNum",value = "页号"),
        @ApiImplicitParam(name = "pageSize",value = "每页大小")
    })
    public ResponseResult linkCommentList(Integer pageNum,Integer pageSize){
        return commentService.commentList(SystemConstants.LINK_COMMENT,null,pageNum,pageSize);
    }
}
