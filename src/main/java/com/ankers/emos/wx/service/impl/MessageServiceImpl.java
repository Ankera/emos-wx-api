package com.ankers.emos.wx.service.impl;

import com.ankers.emos.wx.db.dao.MessageDao;
import com.ankers.emos.wx.db.dao.MessageRefDao;
import com.ankers.emos.wx.db.pojo.MessageEntity;
import com.ankers.emos.wx.db.pojo.MessageRefEntity;
import com.ankers.emos.wx.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageDao messageDao;

    @Autowired
    private MessageRefDao messageRefDao;

    @Override
    public String insertMessage(MessageEntity entity) {
        return messageDao.insert(entity);
    }

    @Override
    public List<HashMap> searchMessageByPage(int userId, long start, int length) {
        return messageDao.searchMessageByPage(userId, start, length);
    }

    @Override
    public HashMap searchMessageById(String id) {
        return messageDao.searchMessageById(id);
    }

    @Override
    public String insertRef(MessageRefEntity entity) {
        return messageRefDao.insert(entity);
    }

    @Override
    public long searchUnreadCount(int userId) {
        return messageRefDao.searchUnreadCount(userId);
    }

    @Override
    public long searchLastCount(int userId) {
        return messageRefDao.searchLastCount(userId);
    }

    @Override
    public long updateUnreadMessage(String id) {
        return messageRefDao.updateUnreadMessage(id);
    }

    @Override
    public long deleteMessageRefById(String id) {
        return messageRefDao.deleteMessageRefById(id);
    }

    @Override
    public long deleteUserMessageRef(int userId) {
        return messageRefDao.deleteUserMessageRef(userId);
    }
}
