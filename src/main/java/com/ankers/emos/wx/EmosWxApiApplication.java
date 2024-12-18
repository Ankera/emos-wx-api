package com.ankers.emos.wx;

import cn.hutool.core.util.StrUtil;
import com.ankers.emos.wx.config.SystemConstants;
import com.ankers.emos.wx.db.dao.SysConfigDao;
import com.ankers.emos.wx.db.pojo.SysConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PostConstruct;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

@SpringBootApplication
@ServletComponentScan
@Slf4j
@EnableAsync
public class EmosWxApiApplication {

    @Autowired
    SysConfigDao sysConfigDao;

    @Autowired
    SystemConstants constants;

    @Value("${emos.image-folder}")
    private String imageFolder;

    public static void main(String[] args) {
        SpringApplication.run(EmosWxApiApplication.class, args);
    }

    @PostConstruct
    public void init() {
        List<SysConfig> sysConfigs = sysConfigDao.selectAllParam();
        sysConfigs.forEach(sysConfig -> {
            String paramKey = sysConfig.getParamKey();
            paramKey = StrUtil.toCamelCase(paramKey);
            String paramValue = sysConfig.getParamValue();
            try {
                Field field = constants.getClass().getDeclaredField(paramKey);
                field.set(constants, paramValue);
            } catch (Exception e) {
                log.error("读取常量异常" + e.getMessage());
            }
        });

        new File(imageFolder).mkdirs();
    }
}
