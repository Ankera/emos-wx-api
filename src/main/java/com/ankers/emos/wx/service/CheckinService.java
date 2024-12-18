package com.ankers.emos.wx.service;

import java.util.ArrayList;
import java.util.HashMap;

public interface CheckinService {
    String validCanCheckIn(int userId,String date);
    void checkin(HashMap<String, Object> param);
    void createFaceModel(int userId,String path);
    HashMap<String, Object> searchTodayCheckin(int userId);
    long searchCheckinDays(int userId);
    ArrayList<HashMap> searchWeekCheckin(HashMap param);
    ArrayList<HashMap> searchMonthCheckin(HashMap param);
}
