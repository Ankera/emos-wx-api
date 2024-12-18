package com.ankers.emos.wx.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.ankers.emos.wx.common.util.R;
import com.ankers.emos.wx.config.SystemConstants;
import com.ankers.emos.wx.controller.form.CheckinForm;
import com.ankers.emos.wx.controller.form.SearchMonthCheckinForm;
import com.ankers.emos.wx.exception.EmosException;
import com.ankers.emos.wx.service.CheckinService;
import com.ankers.emos.wx.service.UserService;
import com.ankers.emos.wx.shiro.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

@RequestMapping("/checkin")
@RestController
@Api("签到模块Web接口")
@Slf4j
public class CheckinController {

    @Value("${emos.image-folder}")
    private String imageFolder;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    CheckinService checkinService;

    @Autowired
    private UserService userService;

    @Autowired
    private SystemConstants constants;

    @GetMapping("/validCanCheckIn")
    @ApiOperation("查看用户今天是否可以签到")
    public R validCanCheckIn(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        String s = checkinService.validCanCheckIn(userId, DateUtil.today());
        return R.ok(s);
    }

    @PostMapping("/checkin")
    @ApiOperation("签到")
    public R checkin(@Validated CheckinForm form, @RequestParam("photo") MultipartFile file, @RequestHeader("token") String token) {
        if (file == null || file.isEmpty()) {
            return R.error("图片不能为空");
        }
        int userId = jwtUtil.getUserId(token);
        String filename = Objects.requireNonNull(file.getOriginalFilename()).toLowerCase();
        if(!(filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png"))) {
            return R.error("必须提交JPG|JPEG|PNG格式图片");
        } else {
            String path = imageFolder + "/" + filename;
            try {
                file.transferTo(Paths.get(path));
                HashMap<String, Object> param =new HashMap<>();
                param.put("userId",userId);
                param.put("path",path);
                param.put("city",form.getCity());
                param.put("district",form.getDistrict());
                param.put("address",form.getAddress());
                param.put("country",form.getCountry());
                param.put("province",form.getProvince());

                checkinService.checkin(param);
                return R.ok("签到成功");
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new EmosException("图片保存错误");
            } finally {
                FileUtil.del(path);
            }
        }
    }

    @PostMapping("/createFaceModel")
    @ApiOperation("创建人脸模型")
    public R createFaceModel(@RequestParam("photo") MultipartFile file, @RequestHeader("token") String token) {
        if (file == null || file.isEmpty()) {
            return R.error("图片不能为空");
        }
        int userId = jwtUtil.getUserId(token);
        String filename = Objects.requireNonNull(file.getOriginalFilename()).toLowerCase();
        if(!(filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png"))) {
            return R.error("必须提交JPG|JPEG|PNG格式图片");
        } else {
            String path = imageFolder + "/" + filename;
            try {
                file.transferTo(Paths.get(path));
                checkinService.createFaceModel(userId, path);
                return R.ok("人脸建模成功");
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new EmosException("图片保存错误");
            } finally {
                FileUtil.del(path);
            }
        }
    }

    @GetMapping("/searchTodayCheckin")
    @ApiOperation("查询个人信息")
    public R searchTodayCheckin(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        HashMap map = checkinService.searchTodayCheckin(userId);
        map.put("attendanceTime",constants.attendanceTime);
        map.put("closingTime",constants.closingTime);
        long days = checkinService.searchCheckinDays(userId);
        map.put("checkinDays",days);

        DateTime hiredate = DateUtil.parse(userService.searchUserHiredate(userId));
        DateTime startDate = DateUtil.beginOfWeek(DateUtil.date());
        if (hiredate.isBefore(startDate)) {
            startDate = hiredate;
        }

        DateTime endDate=DateUtil.endOfWeek(DateUtil.date());
        HashMap param=new HashMap();
        param.put("startDate",startDate.toString());
        param.put("endDate",endDate.toString());
        param.put("userId",userId);

        ArrayList<HashMap> list = checkinService.searchWeekCheckin(param);
        map.put("weekCheckin",list);
        return R.ok().put("result",map);
    }

    @PostMapping("/searchMonthCheckin")
    @ApiOperation("查询月考勤成功")
    public R searchMonthCheckin(@Valid @RequestBody SearchMonthCheckinForm form, @RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        DateTime hiredate = DateUtil.parse(userService.searchUserHiredate(userId));
        String month = form.getMonth() < 10 ? "0" + form.getMonth(): String.valueOf(form.getMonth());
        DateTime startDate = DateUtil.parse(form.getYear() + "-" + month + "-01");
        if (startDate.isBefore(hiredate)) {
            startDate = hiredate;
        }
        DateTime endDate = DateUtil.endOfMonth(startDate);
        HashMap param=new HashMap();
        param.put("startDate",startDate.toString());
        param.put("endDate",endDate.toString());
        param.put("userId",userId);
        ArrayList<HashMap> list = checkinService.searchMonthCheckin(param);
        int sum_1 = 0, sum_2 = 0, sum_3 = 0;
        for(HashMap one:list){
            String type = one.get("type").toString();
            String status = one.get("status").toString();
            if (type.equals("工作日")) {
                switch (status) {
                    case "正常":
                        sum_1++;
                        break;
                    case "迟到":
                        sum_2++;
                        break;
                    case "缺勤":
                        sum_3++;
                        break;
                }
            }
        }
        return R.ok("查询月考勤成功").put("list",list).put("sum_1",sum_1).put("sum_2",sum_2).put("sum_3",sum_3);
    }
}
