package com.purplechat.controller;


import com.purplechat.annotation.GlobalInterceptor;
import com.purplechat.entity.config.AppConfig;
import com.purplechat.entity.constants.Constants;
import com.purplechat.entity.po.AppUpdate;
import com.purplechat.entity.vo.AppUpdateVO;
import com.purplechat.entity.vo.ResponseVO;
import com.purplechat.enums.AppUpdateFileTypeEnum;
import com.purplechat.exception.BusinessException;
import com.purplechat.service.AppUpdateService;
import com.purplechat.utils.CopyTools;
import com.purplechat.utils.StringTools;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Arrays;

@RestController("updateController")
@RequestMapping("/update")
public class UpdateController extends ABaseController{

    @Resource
    AppUpdateService appUpdateService;
    @Resource
    AppConfig appConfig;

    @RequestMapping("/checkVersion")
//    @GlobalInterceptor
    public ResponseVO checkVersion(String curVersion, String uid) {

        //如果上传的版本为空直接返回
        if(StringTools.isEmpty(curVersion)){
            return getSuccessResponseVO(null);
        }

        AppUpdate latestUpdate = appUpdateService.getLatestUpdate(curVersion, uid);
        if(latestUpdate == null){
            return getSuccessResponseVO(null);
        }
        //拷贝属性到vo
        AppUpdateVO appUpdateVO = CopyTools.copy(latestUpdate, AppUpdateVO.class);
        //本地可以计算大小
        if(AppUpdateFileTypeEnum.LOCAL_FILE.getType().equals(latestUpdate.getFileType())){
            appUpdateVO.setSize(new File(appConfig.getProjectFolder() + Constants.APP_UPDATE_FOLDER + latestUpdate.getId() + Constants.APP_EXE_SUFFIX).length());
        }else{
            appUpdateVO.setSize(0L);
        }
        appUpdateVO.setUpdateList(Arrays.asList(latestUpdate.getUpdateDescArray())); //更新的描述设置
        String fileName = Constants.APP_NAME + latestUpdate.getVersion() + Constants.APP_EXE_SUFFIX;
        appUpdateVO.setFileName(fileName);
        return getSuccessResponseVO(appUpdateVO);
    }

    @RequestMapping("/download")
    @GlobalInterceptor
    public void download(HttpServletRequest request, HttpServletResponse response, String userId,String version) throws BusinessException {

        AppUpdate latestUpdate = appUpdateService.getLatestUpdate(version, userId);
        Integer fileId = latestUpdate.getId();

        String appPath = appConfig.getProjectFolder() + Constants.APP_UPDATE_FOLDER + "/" + fileId + Constants.APP_EXE_SUFFIX;
        File appFile = new File(appPath);
        if(!appFile.exists()){
            throw new BusinessException("获取异常，请联系管理员");
        }

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + appFile.getName());
        response.setContentLengthLong(appFile.length());

        // 传输文件到前端
        try (InputStream inputStream = new FileInputStream(appFile);
             OutputStream outputStream = response.getOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            // 逐块读取文件并写入到响应的输出流
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            // 刷新输出流，确保所有数据都发送到客户端
            outputStream.flush();
        } catch (IOException e) {
            throw new BusinessException("文件传输失败", e);
        }



    }



}
