package com.ankers.emos.wx.controller;

import com.ankers.emos.wx.common.util.R;
import com.ankers.emos.wx.controller.form.TestSayHelloFrom;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/test")
@Api("测试接口")
public class TestController {

    @PostMapping("/sayHello")
    @ApiOperation("/最简单的测试方法")
    public R sayHello(@Valid @RequestBody TestSayHelloFrom form) {
        return R.ok().put("message", "HelloWorld" + form.getName());
    }

    @PostMapping("/addUser")
    @ApiOperation("添加用户")
    @RequiresPermissions(value = {"ROOT","USER:ADD"},logical = Logical.OR)
    public R addUser(){
        return R.ok("用户添加成功");
    }
}
