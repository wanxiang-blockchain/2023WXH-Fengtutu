package com.bage.controller;
/**
 * 标签管理页面
 * */
import com.bage.domain.ResponseResult;
import com.bage.domain.dto.AddTagDto;
import com.bage.domain.dto.EditTagDto;
import com.bage.domain.dto.TagListDto;
import com.bage.domain.entity.Tag;
import com.bage.domain.vo.PageVo;
import com.bage.domain.vo.TagVo;
import com.bage.service.TagService;
import com.bage.utils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController //标识一下，告诉这个文件是Controller
@RequestMapping("/content/tag") //所有请求加这个前缀
public class TagController {
    @Autowired
    private TagService tagService;

    //查询标签列表
    @GetMapping("/list")
    public ResponseResult<PageVo> list(Integer pageNum, Integer pageSize, TagListDto tagListDto){ //根据标签名和备注进行搜索，可以把这两个条件封装到tagListDto里
        return tagService.pageTagList(pageNum,pageSize,tagListDto);
    }

    //新增标签
    @PostMapping
    public ResponseResult add(@RequestBody AddTagDto tagDto){ //传入的参数为备注和标签名
        Tag tag = BeanCopyUtils.copyBean(tagDto, Tag.class); //把AddTagDto转换成Tag再传给service
//        tagService.save(tag);
//        return ResponseResult.okResult();
        return tagService.addTag(tag);
    }

    //删除标签
    @DeleteMapping("/{id}")
    public ResponseResult delete(@PathVariable Long id){
//        tagService.removeById(id);//这里记住都是逻辑删除
//        return ResponseResult.okResult();
        return tagService.removeTag(id);
    }
    //编辑标签
    @PutMapping
    public ResponseResult edit(@RequestBody EditTagDto tagDto){
        Tag tag = BeanCopyUtils.copyBean(tagDto,Tag.class); //把EditTagDto转换成Tag再传给service
//        tagService.updateById(tag);
//        return ResponseResult.okResult();
        return tagService.updateTag(tag);
    }
    //获取标签详情
    @GetMapping(value = "/{id}")
    public ResponseResult getInfo(@PathVariable(value = "id")Long id){
//        Tag tag = tagService.getById(id);
//        return ResponseResult.okResult(tag);
        return tagService.getTagInfo(id);
    }

    //查询所有标签
    @GetMapping("/listAllTag")
    public ResponseResult listAllTag(){
        List<TagVo> list = tagService.listAllTag();
        return ResponseResult.okResult(list);
    }
}