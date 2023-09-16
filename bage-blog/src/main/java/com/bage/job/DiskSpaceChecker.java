package com.bage.job;

import com.bage.constants.SystemConstants;
import com.bage.domain.entity.AiUserImgs;
import com.bage.mapper.AiUserImgsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户图片清除定时任务
 **/
@Component
public class DiskSpaceChecker {
    @Autowired
    private AiUserImgsMapper aiUserImgsMapper;

//    @Scheduled(fixedRate = 180000)
    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点
    public void checkMemoryAndDeleteImages() {
        // 从数据库获取img_status为0的图片名字列表
        List<AiUserImgs> aiUserImgsList = aiUserImgsMapper.selectList(null);

        // 创建一个集合来保存数据库中有记录的图片名字
        Set<String> imageNamesInDatabase = new HashSet<>();
        // 创建一个集合来保存数据库中img_status为1的图片名字
        Set<String> imageNamesStatus1 = new HashSet<>();
        for (AiUserImgs img : aiUserImgsList) {
            if (img.getImgStatus() == SystemConstants.USER_IMG_STATUS_DEL) {
                imageNamesInDatabase.add(img.getImgUrl()); // 假设 getImgUrl() 返回图片的名字
            } else {
                imageNamesStatus1.add(img.getImgUrl());
            }
        }

        // 删除数据库中img_status为0的图片(用户没保存的图片)记录并删除服务器文件夹中对应的图片
        for (AiUserImgs img : aiUserImgsList) {
            if (img.getImgStatus() == SystemConstants.USER_IMG_STATUS_DEL) {
                // 从数据库中删除图片记录
                aiUserImgsMapper.deleteById(img.getId());

                // 从服务器文件夹中删除对应的图片
                String imagePath = img.getImgUrl(); // 假设 getImgUrl() 返回图片的名字
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    if (imageFile.delete()) {
                        System.out.println("已删除图片文件: " + imageFile.getAbsolutePath());
                    } else {
                        System.out.println("无法删除图片文件: " + imageFile.getAbsolutePath());
                    }
                } else {
                    System.out.println("图片文件不存在: " + imageFile.getAbsolutePath());
                }
            }
        }
    }
}
