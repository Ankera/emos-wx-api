package com.ankers.emos.wx.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateRange;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.ankers.emos.wx.config.SystemConstants;
import com.ankers.emos.wx.db.dao.*;
import com.ankers.emos.wx.db.pojo.TbCheckin;
import com.ankers.emos.wx.db.pojo.TbFaceModel;
import com.ankers.emos.wx.exception.EmosException;
import com.ankers.emos.wx.service.CheckinService;
import com.ankers.emos.wx.task.EmailTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Service
@Scope("prototype")
@Slf4j
public class CheckinServiceImpl implements CheckinService {

    @Autowired
    private SystemConstants constants;

    @Autowired
    private TbWorkdayDao workdayDao;

    @Autowired
    private TbHolidaysDao holidaysDao;

    @Autowired
    private TbCheckinDao checkinDao;

    @Autowired
    private TbFaceModelDao faceModel;

    @Autowired
    private TbCityDao cityDao;

    @Autowired
    private TbUserDao userDao;

    @Value("${emos.face.createFaceModelUrl}")
    private String createFaceModelUrl;

    @Value("${emos.face.checkinUrl}")
    private String checkinUrl;

    @Value("${emos.email.hr}")
    private String hrEmail;

    @Autowired
    private EmailTask emailTask;

    @Override
    public String validCanCheckIn(int userId, String date) {
        boolean bool_1 = holidaysDao.searchTodayIsWorkday() != null;
        boolean bool_2 = workdayDao.searchTodayIsWorkday() != null;
        String type = "工作日";
        if (DateUtil.date().isWeekend()) {
            type = "节假日";
        }
        if (bool_1) {
            type = "节假日";
        } else if (bool_2) {
            type = "工作日";
        }
        if (type.equals("节假日")) {
            return "节假日不需要考勤";
        } else {
            DateTime now = DateUtil.date();
            String start = DateUtil.today() + " " + constants.attendanceStartTime;
            String end = DateUtil.today() + " " + constants.attendanceEndTime;
            DateTime attendanceStart = DateUtil.parse(start);
            DateTime attendanceEnd = DateUtil.parse(end);
            if(now.isBefore(attendanceStart)){
                return "没到上班考勤开始时间";
            }
            else if(now.isAfter(attendanceEnd)){
                return "超过了上班考勤结束时间";
            }else {
                HashMap<String, Object> map=new HashMap<>();
                map.put("userId",userId);
                map.put("date",date);
                map.put("start",start);
                map.put("end",end);
                boolean bool= checkinDao.haveCheckin(map) != null;
                return bool?"今日已经考勤，不用重复考勤" : "可以考勤";
            }
        }
    }

    @Override
    public void checkin(HashMap<String, Object> param) {
        Date d1 = DateUtil.date();
        Date d2 = DateUtil.parse(DateUtil.today()+" "+constants.attendanceTime);
        Date d3 = DateUtil.parse(DateUtil.today()+" "+constants.attendanceEndTime);
        int status = 1;
        if (d1.compareTo(d2) < 0) {
            // 当前时间早于上班时间
            status = 1;
        } else if (d1.compareTo(d2) > 0 && d1.compareTo(d3) < 0) {
            // 迟到
            status = 2;
        }

        int userId = Integer.parseInt(param.get("userId").toString());
        String faceModel1 = faceModel.searchFaceModel(userId);
        if (faceModel1 == null) {
            throw new EmosException("人脸模型不存在");
        } else {
            String path = param.get("path").toString();
            // TODO 人脸模型请求忽略
            // 直接返回成功，因为没有安装人脸识别 python3 包
            int risk = 1;
            String city= (String) param.get("city");
            String district= (String) param.get("district");
            String address= (String) param.get("address");
            String country= (String) param.get("country");
            String province= (String) param.get("province");

            String code = cityDao.searchCode(city);

            // TODO 风险等级不考虑了
            // 发送邮件

            HashMap<String, String> map = userDao.searchNameAndDept(userId);
            String name = map == null ? null : map.get("name");
            String deptName = map == null ? null : map.get("dept_name");
            deptName = deptName != null ? deptName : "";
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(hrEmail);
            message.setSubject("员工" + name + "身处高风险疫情地区警告");
            message.setText(deptName + "员工" + name + "，" + DateUtil.format(new Date(), "yyyy年MM月dd日") + "处于" + address + "，属于新冠疫情高风险地区，请及时与该员工联系，核实情况！");
            emailTask.sendAsync(message);

            TbCheckin entity = new TbCheckin();
            entity.setUserId(userId);
            entity.setAddress(address);
            entity.setCountry(country);
            entity.setProvince(province);
            entity.setCity(city);
            entity.setDistrict(district);
            entity.setStatus((byte) status);
            entity.setRisk(risk);
//            entity.setDate(DateUtil.today());
            entity.setDate("2024-12-12");
            entity.setCreateTime(d1);
            try {
                checkinDao.insert(entity);
            } catch (Exception e) {
                throw new EmosException("已经签到了，不要重复签到");
            }
        }
    }

    @Override
    public void createFaceModel(int userId, String path) {
        // TODO 调用 python 人脸模型库忽略
        TbFaceModel faceModel1 = new TbFaceModel();
        faceModel1.setUserId(userId);
        faceModel1.setFaceModel(path);
        faceModel.insert(faceModel1);
    }

    @Override
    public HashMap searchTodayCheckin(int userId) {
        return checkinDao.searchTodayCheckin(userId);
    }

    @Override
    public long searchCheckinDays(int userId) {
        return checkinDao.searchCheckinDays(userId);
    }

    @Override
    public ArrayList<HashMap> searchWeekCheckin(HashMap param) {
        ArrayList<HashMap> checkinList = checkinDao.searchWeekCheckin(param);
        ArrayList<String> holidaysList = holidaysDao.searchHolidaysInRange(param);
        ArrayList<String> workdayList = workdayDao.searchWorkdayInRange(param);
        DateTime startDate = DateUtil.parseDate(param.get("startDate").toString());
        DateTime endDate = DateUtil.parseDate(param.get("endDate").toString());
        DateRange dateRange = DateUtil.range(startDate, endDate, DateField.DAY_OF_MONTH);
        ArrayList<HashMap> arrayList = new ArrayList<>();
        dateRange.forEach(one -> {
            String date = one.toString("yyyy-MM-dd");
            String type = "工作日";
            if (one.isWeekend()) {
                type = "节假日";
            }
            if (holidaysList != null && holidaysList.contains(date)) {
                type = "节假日";
            } else if (workdayList != null && workdayList.contains(date)) {
                type = "工作日";
            }

            String status = "";
            // DateUtil.compare(one, DateUtil.date())  本周这天已经结束
            if (type.equals("工作日") && DateUtil.compare(one, DateUtil.date()) <= 0) {
                status="缺勤";
                boolean flag = false;
                for (HashMap map : checkinList) {
                    if (map.containsValue(date)) {
                        status = map.get("status").toString();
                        flag=true;
                        break;
                    }
                }
                DateTime endTime=DateUtil.parse(DateUtil.today()+" "+constants.attendanceEndTime);
                String today = DateUtil.today();
                if (today.equals(date) && DateUtil.date().isBefore(endTime) && !flag) {
                    status = "";
                }
            }

            HashMap map = new HashMap();
            map.put("date",date);
            map.put("status",status);
            map.put("type",type);
            map.put("day",one.dayOfWeekEnum().toChinese("周"));
            arrayList.add(map);
        });
        return arrayList;
    }

    @Override
    public ArrayList<HashMap> searchMonthCheckin(HashMap param) {
        return this.searchWeekCheckin(param);
    }
}
