package com.ankers.emos.wx;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.ankers.emos.wx.db.dao.TbUserDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;

@SpringBootTest
class EmosWxApiApplicationTests {

    @Autowired
    private TbUserDao userDao;

    @Test
    void test01() {
        HashMap hashMap = userDao.searchNameAndDept(7);
        System.out.println(hashMap);
    }

    @Test
    void test02() {
        DateTime hiredate = DateUtil.parse("2023-12-15");
        DateTime startDate = DateUtil.parse("2024-12-15");
        if (startDate.isBefore(hiredate)) {
            System.out.println("============before");
        } else {
            System.out.println("============after");
        }

    }

}
