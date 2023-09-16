package com.bage.controller;
/**
 * 登录
 * */
import com.bage.domain.ResponseResult;
import com.bage.domain.entity.LoginUser;
import com.bage.domain.entity.Menu;
import com.bage.domain.entity.User;
import com.bage.domain.entity.UserInfoVo;
import com.bage.domain.vo.AdminUserInfoVo;
import com.bage.domain.vo.RoutersVo;
import com.bage.enums.AppHttpCodeEnum;
import com.bage.exception.SystemException;
import com.bage.service.LoginService;
import com.bage.service.MenuService;
import com.bage.service.RoleService;
import com.bage.utils.BeanCopyUtils;
import com.bage.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LoginController {
    @Autowired
    private LoginService loginService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private RoleService roleService;

    //登录
    @PostMapping("/user/login")
    public ResponseResult login(@RequestBody User user){
        if(!StringUtils.hasText(user.getUserName())){
            //提示 必须要传用户名
            throw new SystemException(AppHttpCodeEnum.REQUIRE_USERNAME);
        }
        return loginService.login(user);
    }

    //退出登录
    @PostMapping("/user/logout")
    public ResponseResult logout(){
        return loginService.logout();
    }

    //获取用户信息
    @GetMapping("getInfo")
    public ResponseResult<AdminUserInfoVo> getInfo(){
        //获取当前登录的用户
        LoginUser loginUser = SecurityUtils.getLoginUser();
        //根据用户id查询权限信息
        List<String> perms = menuService.selectPermsByUserId(loginUser.getUser().getId());//把用户id传过去
        //根据用户id查询角色信息
        List<String> roleKeyList = roleService.selectRoleKeyByUserId(loginUser.getUser().getId());

        //获取用户信息
        User user = loginUser.getUser();
        UserInfoVo userInfoVo = BeanCopyUtils.copyBean(user, UserInfoVo.class);
        //封装数据返回

        AdminUserInfoVo adminUserInfoVo = new AdminUserInfoVo(perms,roleKeyList,userInfoVo);//希望这个VO中有三个集合，1。权限集合2.角色集合3.用户信息
        return ResponseResult.okResult(adminUserInfoVo);
    }

    //获取routers
    @GetMapping("getRouters")
    public ResponseResult<RoutersVo> getRouters(){
        //获取userId
        Long userId = SecurityUtils.getUserId();
        //查询menu 结果是tree的形式
        List<Menu> menus = menuService.selectRouterMenuTreeByUserId(userId);
        //封装数据返回
        return ResponseResult.okResult(new RoutersVo(menus));
    }
}
