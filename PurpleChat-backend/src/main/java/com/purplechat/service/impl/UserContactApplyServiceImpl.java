package com.purplechat.service.impl;

import com.purplechat.entity.po.UserContact;
import com.purplechat.entity.po.UserContactApply;
import com.purplechat.entity.query.UserContactApplyQuery;
import com.purplechat.entity.query.UserContactQuery;
import com.purplechat.entity.vo.PaginationResultVO;
import com.purplechat.entity.query.SimplePage;
import com.purplechat.enums.*;
import com.purplechat.exception.BusinessException;
import com.purplechat.mappers.UserContactApplyMapper;
import com.purplechat.mappers.UserContactMapper;
import com.purplechat.service.UserContactApplyService;
import com.purplechat.service.UserContactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @Description:联系人申请Service
 * @Author:文翔
 * @date:2024/07/16
 */
@Service("userContactApplyService")
public class UserContactApplyServiceImpl implements UserContactApplyService {

    @Resource
    private UserContactApplyMapper<UserContactApply, UserContactApplyQuery> userContactApplyMapper;
    @Resource
    private UserContactService userContactService;

    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

    private static final Logger logger = LoggerFactory.getLogger(UserContactApplyServiceImpl.class);

    /**
     * 根据条件查询列表
     */
    @Override
    public List<UserContactApply> findListByParam(UserContactApplyQuery query) {
        return this.userContactApplyMapper.selectList(query);
    }

    /**
     * 根据条件查询数量
     */
    @Override
    public Integer findCountByParam(UserContactApplyQuery query) {
        return this.userContactApplyMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    @Override
    public PaginationResultVO<UserContactApply> findListByPage(UserContactApplyQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageNo() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<UserContactApply> list = this.findListByParam(query);
        PaginationResultVO<UserContactApply> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(UserContactApply bean) {
        return this.userContactApplyMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<UserContactApplyQuery> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userContactApplyMapper.insertBatch(listBean);
    }

    /**
     * 批量新增/修改
     */
    @Override
    public Integer addOrUpdateBatch(List<UserContactApplyQuery> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userContactApplyMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据ApplyId查询
     */
    public UserContactApply getUserContactApplyByApplyId(Integer applyId) {
        return this.userContactApplyMapper.selectByApplyId(applyId);
    }

    /**
     * 根据ApplyId修改
     */
    public Integer updateUserContactApplyByApplyId(UserContactApply bean, Integer applyId) {
        return this.userContactApplyMapper.updateByApplyId(bean, applyId);
    }

    /**
     * 根据ApplyId删除
     */
    public Integer deleteUserContactApplyByApplyId(Integer applyId) {
        return this.userContactApplyMapper.deleteByApplyId(applyId);
    }

    /**
     * 根据ApplyUserIdAndReceiveUserIdAndContactId查询
     */
    public UserContactApply getUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(String applyUserId, String receiveUserId, String contactId) {
        return this.userContactApplyMapper.selectByApplyUserIdAndReceiveUserIdAndContactId(applyUserId, receiveUserId, contactId);
    }

    /**
     * 根据ApplyUserIdAndReceiveUserIdAndContactId修改
     */
    public Integer updateUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(UserContactApply bean, String applyUserId, String receiveUserId, String contactId) {
        return this.userContactApplyMapper.updateByApplyUserIdAndReceiveUserIdAndContactId(bean, applyUserId, receiveUserId, contactId);
    }

    /**
     * 根据ApplyUserIdAndReceiveUserIdAndContactId删除
     */
    public Integer deleteUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(String applyUserId, String receiveUserId, String contactId) {
        return this.userContactApplyMapper.deleteByApplyUserIdAndReceiveUserIdAndContactId(applyUserId, receiveUserId, contactId);
    }

    @Override
    public void dealWithApply(String userId, Integer applyId, Integer status) throws BusinessException {
        UserContactApplyStatusEnum statusEnum = UserContactApplyStatusEnum.getByStatus(status);
        if (statusEnum == null || UserContactApplyStatusEnum.INIT.getStatus().equals(statusEnum.getStatus())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        //校验处理人是否是申请的接收者 防止错误处理
        UserContactApply userContactApply = this.userContactApplyMapper.selectByApplyId(applyId);
        if (userContactApply == null || !userId.equals(userContactApply.getReceiveUserId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

//        单向流操作 乐观锁
        //更新apply中的状态
        //currentTimeMillis()获取当前时间戳
        //date.getTime()获取时间戳
        UserContactApply updateBean = new UserContactApply();
        updateBean.setStatus(statusEnum.getStatus());
        updateBean.setLastApplyTime(System.currentTimeMillis());
        updateBean.setStatusName(UserContactStatusEnum.getByStatus(status));

        //用query查询更新 条件是applyId 且 status 为 0 单向流
        UserContactApplyQuery userContactApplyQuery = new UserContactApplyQuery();
        userContactApplyQuery.setApplyId(applyId);
        userContactApplyQuery.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
        Integer updateCount = this.userContactApplyMapper.updateByParam(updateBean, userContactApplyQuery);

        if (updateCount == 0) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);  //没有可修改的 因此报错误
        }

        //通过前端操作得到的状态分别对数据库进行修改
        Date curDate = new Date();
        if (UserContactApplyStatusEnum.PASS.getStatus().equals(statusEnum.getStatus())) {
            userContactService.addContact(userContactApply.getApplyUserId(), userContactApply.getReceiveUserId(), userContactApply.getContactId(), userContactApply.getContactType(), userContactApply.getApplyInfo());
        }

        if (UserContactApplyStatusEnum.BLACKLIST.getStatus().equals(statusEnum.getStatus())) {
            //在contact表中新增一个记录 被拉黑
            UserContact userContact = new UserContact();
            userContact.setUserId(userContactApply.getApplyUserId());
            userContact.setContactId(userContactApply.getContactId());
            userContact.setContactType(userContactApply.getContactType());
            userContact.setCreateTime(curDate);
            userContact.setLastUpdateTime(curDate);
            userContact.setStatus(UserContactStatusEnum.BLACKLIST_BE_FIRST.getStatus());

            //如果之前存在就直接更新 避免重复
            this.userContactMapper.insertOrUpdate(userContact);


        }


    }





}