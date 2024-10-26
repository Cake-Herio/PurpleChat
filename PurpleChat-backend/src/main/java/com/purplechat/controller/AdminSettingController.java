package com.purplechat.controller;


import com.purplechat.annotation.GlobalInterceptor;
import com.purplechat.entity.config.AppConfig;
import com.purplechat.entity.constants.Constants;
import com.purplechat.entity.dto.SysSettingDto;
import com.purplechat.entity.vo.ResponseVO;
import com.purplechat.redis.RedisComponent;
import com.purplechat.utils.SaveFile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;

@RestController("adminSettingController")
@RequestMapping("/admin")
public class AdminSettingController extends ABaseController{
    @Resource
    RedisComponent redisComponent;
    @Resource
    AppConfig appConfig;


    @RequestMapping("/getSysSetting")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO getSysSetting(){
        //返回系统设置
        SysSettingDto sysSetting = redisComponent.getSysSetting();
        return getSuccessResponseVO(sysSetting);
    }


    @RequestMapping("/saveSysSetting")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO saveSysSetting(MultipartFile robotFile, MultipartFile robotCover,Integer maxImageSize, Integer maxVideoSize,
                                     Integer maxFileSize, Integer maxGroupCount, Integer maxGroupMemberCount, String robotUid,
                                     String robotNickName, String robotWelcome) throws IOException {

        //存redis
        SysSettingDto sysSetting = new SysSettingDto();
        sysSetting.setMaxFileSize(maxFileSize);
        sysSetting.setMaxImageSize(maxImageSize);
        sysSetting.setMaxVideoSize(maxVideoSize);
        sysSetting.setMaxGroupCount(maxGroupCount);
        sysSetting.setMaxGroupMemberCount(maxGroupMemberCount);
        sysSetting.setRobotNickName(robotNickName);
        sysSetting.setRobotWelcome(robotWelcome);

        redisComponent.saveSysSetting(sysSetting);


        if(robotFile != null){
            SaveFile.saveAvatarFile(robotFile, robotCover,appConfig, Constants.ROBOT_UID);
        }
        return getSuccessResponseVO(null);
    }
}
