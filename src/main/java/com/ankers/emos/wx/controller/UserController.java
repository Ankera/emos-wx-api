package com.ankers.emos.wx.controller;

import com.ankers.emos.wx.common.util.R;
import com.ankers.emos.wx.controller.form.LoginForm;
import com.ankers.emos.wx.controller.form.RegisterForm;
import com.ankers.emos.wx.controller.form.SearchMembersForm;
import com.ankers.emos.wx.controller.form.SearchUserGroupByDeptForm;
import com.ankers.emos.wx.exception.EmosException;
import com.ankers.emos.wx.service.UserService;
import com.ankers.emos.wx.shiro.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Api("用户模块Web接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire;

    @PostMapping("/register")
    @ApiOperation("注册用户")
    public R register(@Valid @RequestBody RegisterForm form) {
        int i = userService.registerUser(form.getRegisterCode(), form.getCode(), form.getNickname(), form.getPhoto());
        String token = jwtUtil.createToken(i);
        Set<String> permsSet = userService.searchUserPermissions(i);
        saveCacheToken(token, i);
        return R.ok("用户注册成功").put("token",token).put("permission",permsSet);
    }

    private void saveCacheToken(String token,int userId){
        redisTemplate.opsForValue().set(token,userId + "", cacheExpire, TimeUnit.DAYS);
    }

    @PostMapping("/login")
    @ApiOperation("登录系统")
    public R login(@Valid @RequestBody LoginForm form) {
        int id = userService.login(form.getCode());
        String token = jwtUtil.createToken(id);
        Set<String> permsSet = userService.searchUserPermissions(id);
        saveCacheToken(token, id);
        return Objects.requireNonNull(R.ok("登录成功").put("token", token)).put("permission",permsSet);
    }

    @GetMapping("/searchUserSummary")
    @ApiOperation("用户信息")
    public R searchUserSummary(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        HashMap hashMap = userService.searchUserSummary(userId);
        return R.ok("用户信息").put("result", hashMap);
    }

    @PostMapping("/searchUserGroupByDept")
    @ApiOperation("查询员工列表，按照部门分组排列")
    @RequiresPermissions(value = {"ROOT","EMPLOYEE:SELECT"},logical = Logical.OR)
    public R searchUserGroupByDept(@Valid @RequestBody SearchUserGroupByDeptForm form) {
        ArrayList<HashMap> hashMaps = userService.searchUserGroupByDept(form.getKeyword());
        return R.ok().put("result", hashMaps);
    }

    @PostMapping("/searchMembers")
    @ApiOperation("查询成员")
    @RequiresPermissions(value = {"ROOT", "MEETING:INSERT", "MEETING:UPDATE"},logical = Logical.OR)
    public R searchMembers(@Valid @RequestBody SearchMembersForm form) {
        if (form.getMembers().isEmpty()) {
            throw new EmosException("参数不是JSON数组");
        }
        System.out.println("====getMembers====" + form.getMembers());
        List<String> str = List.of(form.getMembers().split(","));
        ArrayList list = userService.searchMembers(str);
        return R.ok().put("result",list);
    }
}
