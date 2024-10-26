package com.purplechat.service.impl;

import com.purplechat.entity.config.AppConfig;
import com.purplechat.entity.po.AppUpdate;
import com.purplechat.entity.query.AppUpdateQuery;
import com.purplechat.entity.vo.PaginationResultVO;
import com.purplechat.entity.query.SimplePage;
import com.purplechat.enums.AppUpdateFileTypeEnum;
import com.purplechat.enums.AppUpdateStatusEnum;
import com.purplechat.enums.PageSize;
import com.purplechat.enums.ResponseCodeEnum;
import com.purplechat.exception.BusinessException;
import com.purplechat.mappers.AppUpdateMapper;
import com.purplechat.service.AppUpdateService;
import com.purplechat.utils.SaveFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @Description:app发布Service
 * @Author:文翔
 * @date:2024/07/26
 */
@Service("appUpdateService")
public class AppUpdateServiceImpl implements AppUpdateService {

    @Resource
    private AppUpdateMapper<AppUpdate, AppUpdateQuery> appUpdateMapper;
    @Resource
    private AppConfig appConfig;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<AppUpdate> findListByParam(AppUpdateQuery query) {
        return this.appUpdateMapper.selectList(query);
    }

    /**
     * 根据条件查询数量
     */
    @Override
    public Integer findCountByParam(AppUpdateQuery query) {
        return this.appUpdateMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    @Override
    public PaginationResultVO<AppUpdate> findListByPage(AppUpdateQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageNo() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<AppUpdate> list = this.findListByParam(query);
        PaginationResultVO<AppUpdate> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(AppUpdate bean) {
        return this.appUpdateMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<AppUpdateQuery> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.appUpdateMapper.insertBatch(listBean);
    }

    /**
     * 批量新增/修改
     */
    @Override
    public Integer addOrUpdateBatch(List<AppUpdateQuery> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.appUpdateMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据Id查询
     */
    public AppUpdate getAppUpdateById(Integer id) {
        return this.appUpdateMapper.selectById(id);
    }

    /**
     * 根据Id修改
     */
    public Integer updateAppUpdateById(AppUpdate bean, Integer id) {
        return this.appUpdateMapper.updateById(bean, id);
    }

    /**
     * 根据Id删除
     */
    public Integer deleteAppUpdateById(Integer id) {
        return this.appUpdateMapper.deleteById(id);
    }

    /**
     * 根据Version查询
     */
    public AppUpdate getAppUpdateByVersion(String version) {
        return this.appUpdateMapper.selectByVersion(version);
    }

    /**
     * 根据Version修改
     */
    public Integer updateAppUpdateByVersion(AppUpdate bean, String version) {
        return this.appUpdateMapper.updateByVersion(bean, version);
    }

    /**
     * 根据Version删除
     */
    public Integer deleteAppUpdateByVersion(String version) {
        return this.appUpdateMapper.deleteByVersion(version);
    }

    @Override
    public void saveUpdate(AppUpdate appUpdate, MultipartFile file) throws BusinessException, IOException {
        //判断是外链还是本地
        AppUpdateFileTypeEnum fileTypeEnum = AppUpdateFileTypeEnum.getByType(appUpdate.getFileType());
        if (null == fileTypeEnum) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }




        //新版本一定要比之前的更大  先查询最新版本号
        AppUpdateQuery query = new AppUpdateQuery();
        query.setOrderBy("id desc");
        query.setSimplePage(new SimplePage(0, 1));
        List<AppUpdate> list = this.findListByParam(query);

        if (!list.isEmpty()) {

            //查询版本号是否存在 , 已存在则无法新增
            AppUpdate appByVersion = appUpdateMapper.selectByVersion(appUpdate.getVersion());
            if (null != appByVersion) {
                throw new BusinessException("版本号已存在");
            }


            //比较版本号
            AppUpdate lastAppUpdate = list.get(0);
            Long dbVersion = Long.parseLong(lastAppUpdate.getVersion().replace(".", ""));
            Long currentVersion = Long.parseLong(appUpdate.getVersion().replace(".", ""));

            //id有值时 说明是更新操作
            if (appUpdate.getId() != null) {
                //首先根据这个值查找是否存在对应的数据
                AppUpdate app = appUpdateMapper.selectById(appUpdate.getId());
                if (null == app || !Objects.equals(app.getId(), appUpdate.getId())) {
                    throw new BusinessException("数据不存在");
                }
                //发布状态不允许修改
                if(!AppUpdateStatusEnum.INIT.getCode().equals(app.getStatus())){
                    throw new BusinessException("发布状态不允许修改");
                }


                //能找到对应的数据，校验版本号
                if (currentVersion <= dbVersion) {
                    throw new BusinessException("当前版本必须大于历史版本");
                }

                //前端只能修改版本号和文件类型 不能修改发布状态 灰度测试账号 修改时应该保持原状态
                appUpdate.setStatus(null);
                appUpdate.setGrayscaleUid(null);
                appUpdateMapper.updateByParam(appUpdate, appUpdate.getId());


            } else {
                //版本号不应该小
                if (dbVersion >= currentVersion) {
                    throw new BusinessException("当前版本必须大于历史版本");
                }

                appUpdate.setCreateTime(new Date());
                appUpdate.setStatus(AppUpdateStatusEnum.INIT.getCode());
                appUpdateMapper.insert(appUpdate);

            }

            if (null != file) {
                SaveFile.saveProjectFile(file, appConfig, appUpdate.getId());
            }
        }else{
            //不存在历史版本 直接插入
            appUpdate.setCreateTime(new Date());
            appUpdate.setStatus(AppUpdateStatusEnum.INIT.getCode());
            appUpdateMapper.insert(appUpdate);

        }
    }

    @Override
    public void postUpdate(Integer id, Integer status, String grayscaleUID) throws BusinessException, IOException {

        AppUpdateStatusEnum statusEnum = AppUpdateStatusEnum.getByCode(status);
        if (null == statusEnum) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        //灰度发布 且 账号为空 错误
        if(AppUpdateStatusEnum.GRAYSCALE.getCode().equals(status) && null == grayscaleUID){
            throw new BusinessException("灰度发布账号不能为空");
        }

        //全网发布需要将灰度账号清空
        if(AppUpdateStatusEnum.ALL.getCode().equals(status)){
            grayscaleUID = "";
        }

        //更新数据库
        AppUpdate appUpdate = new AppUpdate();
        appUpdate.setStatus(status);
        appUpdate.setGrayscaleUid(grayscaleUID);
        appUpdateMapper.updateByParam(appUpdate, id);





    }

    public AppUpdate getLatestUpdate(String appVersion, String uid) {
        return appUpdateMapper.selectLastedUpdate(appVersion, uid);
    }
}