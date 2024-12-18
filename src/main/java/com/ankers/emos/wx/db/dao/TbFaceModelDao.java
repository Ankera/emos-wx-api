package com.ankers.emos.wx.db.dao;

import com.ankers.emos.wx.db.pojo.TbFaceModel;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TbFaceModelDao {
    String searchFaceModel(int userId);
    void insert(TbFaceModel faceModel);
    int deleteFaceModel(int userId);
}