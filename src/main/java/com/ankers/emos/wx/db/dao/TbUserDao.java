package com.ankers.emos.wx.db.dao;

import com.ankers.emos.wx.db.pojo.TbUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Mapper
public interface TbUserDao {
    boolean haveRootUser();

    int insert(HashMap<String, Object> params);

    Integer searchIdByOpenId(String openId);

    Set<String> searchUserPermissions(int userId);

    TbUser searchById(int userId);

    HashMap searchNameAndDept(int userId);

    String searchUserHiredate(int userId);

    HashMap searchUserSummary(int userId);

    ArrayList<HashMap> searchUserGroupByDept(String keyword);

    ArrayList<HashMap> searchMembers(List param);

    HashMap searchUserInfo(int userId);

    int searchDeptManagerId(int id);

    int searchGmId();

    List<HashMap> selectUserPhotoAndName(List param);

    String searchMemberEmail(int id);
}