package com.ankers.emos.wx.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.ankers.emos.wx.common.util.R;
import com.ankers.emos.wx.controller.form.InsertMeetingForm;
import com.ankers.emos.wx.controller.form.SearchMeetingByIdFrom;
import com.ankers.emos.wx.controller.form.SearchMyMeetingListByPageForm;
import com.ankers.emos.wx.controller.form.UpdateMeetingInfoForm;
import com.ankers.emos.wx.db.pojo.TbMeeting;
import com.ankers.emos.wx.exception.EmosException;
import com.ankers.emos.wx.service.MeetingService;
import com.ankers.emos.wx.shiro.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RequestMapping("/meeting")
@RestController
@Api("会议模块")
@Slf4j
public class MeetingController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MeetingService meetingService;

    @PostMapping("/searchMyMeetingListByPage")
    @ApiOperation("查询会议列表分页数据")
    public R searchMyMeetingListByPage(@Valid @RequestBody SearchMyMeetingListByPageForm form, @RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        int page = form.getPage();
        int length = form.getLength();
        long start= (long) (page - 1) *length;
        HashMap hashMap = new HashMap();
        hashMap.put("userId", userId);
        hashMap.put("start", start);
        hashMap.put("length", length);
        ArrayList list = meetingService.searchMyMeetingListByPage(hashMap);
        return R.ok("会议列表查询成功").put("result", list);
    }

    @PostMapping("/insertMeeting")
    @ApiOperation("添加会议")
    @RequiresPermissions(value = {"ROOT", "MEETING:INSERT"},logical = Logical.OR)
    public R insertMeeting (@Valid @RequestBody InsertMeetingForm form, @RequestHeader("token") String token) {
        long userId = jwtUtil.getUserId(token);
        if(form.getType()==2&&(form.getPlace()==null||form.getPlace().length()==0)){
            throw new EmosException("线下会议地点不能为空");
        }

        DateTime d1= DateUtil.parse(form.getDate()+" "+form.getStart()+":00");
        DateTime d2= DateUtil.parse(form.getDate()+" "+form.getEnd()+":00");
        if(d2.isBeforeOrEquals(d1)){
            throw new EmosException("结束时间必须大于开始时间");
        }

        TbMeeting entity = new TbMeeting();
        entity.setUuid(UUID.randomUUID().toString());
        entity.setInstanceId(UUID.randomUUID().toString());
        entity.setTitle(form.getTitle());
        entity.setCreatorId(userId);
        entity.setDate(form.getDate());
        entity.setPlace(form.getPlace());
        entity.setStart(form.getStart() + ":00");
        entity.setEnd(form.getEnd() + ":00");
        entity.setType((short)form.getType());
        entity.setMembers(form.getMembers());
        entity.setDesc(form.getDesc());
        entity.setStatus((short)1);
        meetingService.insertMeeting(entity);
        return R.ok().put("result","success");
    }

    @PostMapping("/searchMeetingById")
    @ApiOperation("根据ID查询会议")
    @RequiresPermissions(value = {"ROOT", "MEETING:SELECT"}, logical = Logical.OR)
    public R searchMeetingById(@Valid @RequestBody SearchMeetingByIdFrom form){
        HashMap map=meetingService.searchMeetingById(form.getId());
        return R.ok().put("result",map);
    }

    @PostMapping("/updateMeetingInfo")
    @ApiOperation("更新会议")
    @RequiresPermissions(value = {"ROOT", "MEETING:UPDATE"}, logical = Logical.OR)
    public R updateMeetingInfo(@Valid @RequestBody UpdateMeetingInfoForm form) throws JsonProcessingException {
        if(form.getType()==2&&(form.getPlace()==null||form.getPlace().length()==0)){
            throw new EmosException("线下会议地点不能为空");
        }
        DateTime d1= DateUtil.parse(form.getDate()+" "+form.getStart()+":00");
        DateTime d2= DateUtil.parse(form.getDate()+" "+form.getEnd()+":00");
        if(d2.isBeforeOrEquals(d1)){
            throw new EmosException("结束时间必须大于开始时间");
        }
        String json = form.getMembers();
        HashMap param=new HashMap();
        param.put("title", form.getTitle());
        param.put("date", form.getDate());
        param.put("place", form.getPlace());
        param.put("start", form.getStart() + ":00");
        param.put("end", form.getEnd() + ":00");
        param.put("type", form.getType());
        param.put("members", json);
        param.put("desc", form.getDesc());
        param.put("id", form.getId());
        param.put("instanceId", form.getInstanceId());
        param.put("status", 1);
        meetingService.updateMeetingInfo(param);
        return R.ok().put("result","success");
    }
}
