package com.ankers.emos.wx.service;

import com.ankers.emos.wx.db.pojo.MessageEntity;
import com.ankers.emos.wx.db.pojo.MessageRefEntity;

import java.util.HashMap;
import java.util.List;

public interface MessageService {
    String insertMessage(MessageEntity entity);
    List<HashMap> searchMessageByPage(int userId, long start, int length);
    HashMap searchMessageById(String id);
    String insertRef(MessageRefEntity entity);
    long searchUnreadCount(int userId);
    long searchLastCount(int userId);
    long updateUnreadMessage(String id);
    long deleteMessageRefById(String id);
    long deleteUserMessageRef(int userId);
}
