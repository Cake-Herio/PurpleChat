package com.purplechat.controller;


import com.purplechat.annotation.GlobalInterceptor;
import com.purplechat.entity.po.AppUpdate;
import com.purplechat.entity.query.AppUpdateQuery;
import com.purplechat.entity.vo.PaginationResultVO;
import com.purplechat.entity.vo.ResponseVO;
import com.purplechat.exception.BusinessException;
import com.purplechat.service.AppUpdateService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

@RestController("adminAppUpdateController")
@RequestMapping("/admin")
public class AdminAppUpdateController extends ABaseController{

    @Resource
    AppUpdateService appUpdateService;

    @RequestMapping("/loadUpdateList")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadUpdateList(AppUpdateQuery query) {
        //TODO 好好琢磨一下
        query.setOrderBy("id desc");
        //为了适应前端 修改表示类型

        PaginationResultVO<AppUpdate> paginationResultVO = appUpdateService.findListByPage(query);
        List<AppUpdate> updateList = paginationResultVO.getList();
        for(AppUpdate appUpdate : updateList) {
            appUpdate.setUpdateDescArray(appUpdate.getUpdateDesc().split("\\|"));
        }
        return getSuccessResponseVO(paginationResultVO);

    }

    @RequestMapping("/saveUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO saveUpdate(Integer id,
                                 @NotEmpty String version,
                                 @NotEmpty String updateDesc,
                                 @NotNull Integer fileType,
                                 String outerLink,
                                 MultipartFile file) throws BusinessException, IOException {
        //id为自增长 如果填了值且合法 认定为更新操作
        AppUpdate appUpdate = new AppUpdate();
        appUpdate.setId(id);
        appUpdate.setVersion(version);
        appUpdate.setUpdateDesc(updateDesc);
        appUpdate.setFileType(fileType);
        appUpdate.setOuterLink(outerLink);
         appUpdateService.saveUpdate(appUpdate, file);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/delUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO delUpdate(@NotNull Integer id)  {
       appUpdateService.deleteAppUpdateById(id);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/postUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO postUpdate(@NotNull Integer id, @NotNull Integer status, String grayscaleUID) throws BusinessException, IOException {
        AppUpdate appUpdate = new AppUpdate();
        appUpdate.setStatus(status);
        appUpdate.setGrayscaleUid(grayscaleUID);
        this.appUpdateService.updateAppUpdateById(appUpdate, id);
        return getSuccessResponseVO(null);
    }






}
