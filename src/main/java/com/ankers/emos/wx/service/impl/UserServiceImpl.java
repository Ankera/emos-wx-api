package com.ankers.emos.wx.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ankers.emos.wx.db.dao.TbUserDao;
import com.ankers.emos.wx.db.pojo.TbUser;
import com.ankers.emos.wx.exception.EmosException;
import com.ankers.emos.wx.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@Scope("prototype")
public class UserServiceImpl implements UserService {

    @Value("${wx.app-id}")
    private String appId;

    @Value("${wx.app-secret}")
    private String appSecret;

    @Autowired
    private TbUserDao userDao;

    private String getOpenId(String code) {
        String url="https://api.weixin.qq.com/sns/jscode2session";
        url  = url + "?appid="+appId+"&secret="+appSecret+"&js_code="+code + "&grant_type=authorization_code";
        String response= HttpUtil.get(url);
        JSONObject json= JSONUtil.parseObj(response);
        String openId=json.getStr("openid");
        if(openId==null|| openId.isEmpty()){
            throw new RuntimeException("临时登陆凭证错误");
        }
        return openId;
    }

    @Override
    public int registerUser(String registerCode, String code, String nickname, String photo) {
        if (registerCode.equals("000000")) {
            boolean bool = userDao.haveRootUser();
            if (!bool) {
                String openId = getOpenId(code);
                HashMap<String, Object> param = new HashMap<String, Object>();
                param.put("openId", openId);
                param.put("nickname", nickname);
                param.put("photo", photo);
                param.put("role", "[0]");
                param.put("status", 1);
                param.put("createTime", new Date());
                param.put("root", true);
                userDao.insert(param);
                System.out.println("user register success" + userDao);
                return userDao.searchIdByOpenId(openId);
            } else {
                throw new EmosException("无法绑定超级管理员账号");
            }
        }
        return 0;
    }

    @Override
    public Set<String> searchUserPermissions(int userId) {
        return userDao.searchUserPermissions(userId);
    }

    @Override
    public Integer login(String code) {
        String openId = getOpenId(code);
        Integer id = userDao.searchIdByOpenId(openId);
        if (id == null) {
            throw new EmosException("该用户不存在");
        }
        // TODO
        return id;
    }

    @Override
    public TbUser searchById(int userId) {
        return userDao.searchById(userId);
    }

    @Override
    public String searchUserHiredate(int userId) {
        return userDao.searchUserHiredate(userId);
    }

    @Override
    public HashMap searchUserSummary(int userId) {
        return userDao.searchUserSummary(userId);
    }

    @Override
    public ArrayList<HashMap> searchUserGroupByDept(String keyword) {
        return null;
    }

    @Override
    public ArrayList<HashMap> searchMembers(List param) {
        return null;
    }

    @Override
    public List<HashMap> selectUserPhotoAndName(List param) {
        return null;
    }

    @Override
    public String searchMemberEmail(int id) {
        return "";
    }
}
