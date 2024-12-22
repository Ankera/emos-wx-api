package com.ankers.emos.wx.service.impl;

import com.ankers.emos.wx.db.dao.TbMeetingDao;
import com.ankers.emos.wx.db.pojo.TbMeeting;
import com.ankers.emos.wx.service.MeetingService;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class MeetingServiceImpl implements MeetingService {

    @Autowired
    private TbMeetingDao meetingDao;

    @Override
    public void insertMeeting(TbMeeting entity) {
        meetingDao.insertMeeting(entity);
    }

    @Override
    public ArrayList<HashMap> searchMyMeetingListByPage(HashMap param) {
        ArrayList<HashMap> list = meetingDao.searchMyMeetingListByPage(param);
        String date = null;
        ArrayList<HashMap> resultList = new ArrayList<>();
        HashMap resultMap = null;
        ArrayList array = null;
        for (HashMap map : list) {
            String temp = map.get("date").toString();
            if (!temp.equals(date)) {
                date = temp;
                array = new ArrayList();
                resultMap = new HashMap<>();
                resultMap.put("date", date);
                resultMap.put("list", array);
                resultList.add(resultMap);
            }
            array.add(map);
        }
        return resultList;
    }

    @Override
    public HashMap searchMeetingById(int id) {
        HashMap hashMap = meetingDao.searchMeetingById(id);
        return hashMap;
    }

    @Override
    public void updateMeetingInfo(HashMap param) {
        meetingDao.updateMeetingInfo(param);
    }

    @Override
    public void deleteMeetingById(int id) {

    }

    @Override
    public Long searchRoomIdByUUID(String uuid) {
        return 0L;
    }

    @Override
    public List<String> searchUserMeetingInMonth(HashMap param) {
        return null;
    }
}
