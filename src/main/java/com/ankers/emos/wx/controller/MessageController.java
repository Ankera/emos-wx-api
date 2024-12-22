package com.ankers.emos.wx.controller;

import com.ankers.emos.wx.common.util.R;
import com.ankers.emos.wx.controller.form.DeleteMessageRefByIdForm;
import com.ankers.emos.wx.controller.form.SearchMessageByIdForm;
import com.ankers.emos.wx.controller.form.SearchMessageByPageForm;
import com.ankers.emos.wx.controller.form.UpdateUnreadMessageForm;
import com.ankers.emos.wx.service.MessageService;
import com.ankers.emos.wx.shiro.JwtUtil;
import com.ankers.emos.wx.task.MessageTask;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/message")
@Api("消息模块网络接口")
public class MessageController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageTask messageTask;

    @PostMapping("/searchMessageByPage")
    @ApiOperation("获取分页消息列表")
    public R searchMessageByPage(@Valid @RequestBody SearchMessageByPageForm form, @RequestHeader String token) {
        int userId = jwtUtil.getUserId(token);
        int page = form.getPage();
        int length = form.getLength();
        long start = (long) (page - 1) * length;
        List<HashMap> hashMaps = messageService.searchMessageByPage(userId, start, length);
        return R.ok("查询成功").put("result", hashMaps);
    }

    @PostMapping("/searchMessageById")
    @ApiOperation("根据ID查询消息")
    public R searchMessageById(@RequestBody SearchMessageByIdForm form) {
        HashMap hashMap = messageService.searchMessageById(form.getId());
        return R.ok().put("result", hashMap);
    }

    @PostMapping("/updateUnreadMessage")
    @ApiOperation("未读消息更新成已读消息")
    public R updateUnreadMessage(@Valid @RequestBody UpdateUnreadMessageForm form) {
        long rows = messageService.updateUnreadMessage(form.getId());
        return R.ok().put("result", rows == 1);
    }

    @PostMapping("/deleteMessageRefById")
    @ApiOperation("删除消息")
    public R deleteMessageRefById(@Valid @RequestBody DeleteMessageRefByIdForm form) {
        long rows = messageService.deleteMessageRefById(form.getId());
        return R.ok().put("result", rows == 1);
    }

    @GetMapping("/refreshMessage")
    @ApiOperation("刷新用户消息")
    public R refreshMessage(@RequestHeader("token") String token){
        int userId=jwtUtil.getUserId(token);
        messageTask.receiveAsync(userId+"");
        long lastRows=messageService.searchLastCount(userId);
        long unreadRows=messageService.searchUnreadCount(userId);
        return Objects.requireNonNull(R.ok().put("lastRows", lastRows)).put("unreadRows",unreadRows);
    }
}
