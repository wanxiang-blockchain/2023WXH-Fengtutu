package com.bage.controller;

import com.bage.domain.ResponseResult;
import com.bage.domain.dto.AddArticleDto;
import com.bage.domain.dto.ArticleDto;
import com.bage.domain.entity.Article;
import com.bage.domain.vo.PageVo;
import com.bage.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author 文章相关
 */
@RestController
@RequestMapping("/content/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    //文章列表接口
    @GetMapping("/list")
    public ResponseResult list(Article article, Integer pageNum, Integer pageSize) {
        return articleService.selectArticlePage(article,pageNum,pageSize);
    }

    //新增文章接口
    @PostMapping
    public ResponseResult add(@RequestBody AddArticleDto article){
        return articleService.add(article);
    }

    //查询文章详情接口
    @GetMapping(value = "/{id}")
    public ResponseResult getInfo(@PathVariable(value = "id")Long id){
        return articleService.getInfo(id);
    }

    //更新文章
    @PutMapping
    public ResponseResult edit(@RequestBody ArticleDto article){
        return articleService.edit(article);
    }

    //删除文章
    @DeleteMapping("/{id}")
    public ResponseResult delete(@PathVariable Long id){
        //这里偷个懒，直接在controller里调用service的方法了，正常应在Service中处理返回逻辑的。但是这样虽然不规范却能节省很多代码量
        articleService.removeById(id);
        return ResponseResult.okResult();
    }
}