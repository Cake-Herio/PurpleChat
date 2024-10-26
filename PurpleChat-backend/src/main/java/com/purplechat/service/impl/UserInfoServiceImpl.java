package com.purplechat.service.impl;

import com.purplechat.entity.config.AppConfig;
import com.purplechat.entity.constants.Constants;
import com.purplechat.entity.dto.MessageSendDto;
import com.purplechat.entity.dto.TokenUserInfoDto;
import com.purplechat.entity.po.UserContact;
import com.purplechat.entity.po.UserInfo;
import com.purplechat.entity.po.UserInfoBeauty;
import com.purplechat.entity.query.UserContactQuery;
import com.purplechat.entity.query.UserInfoBeautyQuery;
import com.purplechat.entity.query.UserInfoQuery;
import com.purplechat.entity.vo.PaginationResultVO;
import com.purplechat.entity.query.SimplePage;
import com.purplechat.entity.vo.UserInfoVO;
import com.purplechat.enums.*;
import com.purplechat.exception.BusinessException;
import com.purplechat.mappers.UserContactMapper;
import com.purplechat.mappers.UserInfoBeautyMapper;
import com.purplechat.mappers.UserInfoMapper;
import com.purplechat.redis.RedisComponent;
import com.purplechat.redis.RedisUtils;
import com.purplechat.service.ChatSessionUserService;
import com.purplechat.service.UserContactService;
import com.purplechat.service.UserInfoService;
import com.purplechat.utils.CopyTools;
import com.purplechat.utils.SaveFile;
import com.purplechat.utils.StringTools;
import com.purplechat.websocket.netty.HandlerMessage;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description:用户信息Service
 * @Author:文翔
 * @date:2024/06/10
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

    private static final Logger logger = LoggerFactory.getLogger(UserInfoServiceImpl.class);

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private UserInfoBeautyMapper<UserInfoBeauty, UserInfoBeautyQuery> userInfoBeautyMapper;

    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private RedisComponent redisComponent;
    private UserInfoVO userInfoVO;
    @Resource
    private UserContactMapper userContactMapper;
    @Resource
    private UserContactService userContactService;
    @Resource
    private ChatSessionUserService chatSessionUserService;
    @Resource
    private HandlerMessage handlerMessage;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<UserInfo> findListByParam(UserInfoQuery query) {
        return this.userInfoMapper.selectList(query);
    }

    /**
     * 根据条件查询数量
     */
    @Override
    public Integer findCountByParam(UserInfoQuery query) {
        return this.userInfoMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    @Override
    public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageNo() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<UserInfo> list = this.findListByParam(query);
        PaginationResultVO<UserInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(UserInfo bean) {
        return this.userInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<UserInfoQuery> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增/修改
     */
    @Override
    public Integer addOrUpdateBatch(List<UserInfoQuery> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据UserId查询
     */
    public UserInfo getUserInfoByUserId(String userId) {
        return this.userInfoMapper.selectByUserId(userId);
    }

    /**
     * 根据UserId修改
     */
    public Integer updateUserInfoByUserId(UserInfo bean, String userId) {
        return this.userInfoMapper.updateByUserId(bean, userId);
    }

    /**
     * 根据UserId删除
     */
    public Integer deleteUserInfoByUserId(String userId) {
        return this.userInfoMapper.deleteByUserId(userId);
    }

    /**
     * 根据Email查询
     */
    public UserInfo getUserInfoByEmail(String email) {
        return this.userInfoMapper.selectByEmail(email);
    }

    /**
     * 根据Email修改
     */
    public Integer updateUserInfoByEmail(UserInfo bean, String email) {
        return this.userInfoMapper.updateByEmail(bean, email);
    }

    /**
     * 根据Email删除
     */
    public Integer deleteUserInfoByEmail(String email) {
        return this.userInfoMapper.deleteByEmail(email);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(String email, String password, String nickName) throws BusinessException {
        UserInfo userInfo = getUserInfoByEmail(email);
        if (userInfo != null) {
            throw new BusinessException("邮箱已存在");
        }
        //生成账号的id 两种情况
        //如果是靓号 查找靓号表是否存在，如果存在 且 该邮箱尚未被使用则允许注册

        UserInfoBeauty userInfoBeauty = userInfoBeautyMapper.selectByEmail(email);
        boolean useBeautyAccount = userInfoBeauty != null && userInfoBeauty.getStatus().equals(BeautyAccountEnum.NO_USE.getStatus());
        String id = null;
        if (!useBeautyAccount) {
            id = StringTools.getUserId();
        } else {
            id = userInfoBeauty.getUserId();
        }
        Date curDates = new Date();
        UserInfo user = new UserInfo();
        user.setUserId(id);
        user.setEmail(email);
        user.setNickName(nickName);
        user.setCreateTime(curDates);
        user.setLastOffTime(curDates.getTime());
        user.setPassword(StringTools.encodeMd5(password));
        user.setStatus(1);

        //存入到数据库
        this.userInfoMapper.insert(user);

        if (useBeautyAccount) {//如果是靓号还要更新靓号表
            UserInfoBeauty userBeauty = new UserInfoBeauty();
            userBeauty.setStatus(BeautyAccountEnum.USED.getStatus());
            userInfoBeautyMapper.updateById(userBeauty, userInfoBeauty.getId());
        }

        //创建机器人好友
        userContactService.addContact4Robot(id);



    }

    @Override
    public UserInfoVO login(String accountId, String password) throws BusinessException {

        UserInfo userInfo = getUserInfoByUserId(accountId);
        if (userInfo == null) {
            userInfo = getUserInfoByEmail(accountId);
        }
        if (userInfo == null) {
            throw new BusinessException("用户不存在");
        }
        if (!userInfo.getPassword().equals(StringTools.encodeMd5(password))) {
            throw new BusinessException("密码错误");
        }
        if (userInfo.getStatus().equals(UserStatusEnum.DISABLE.getType())) {
            throw new BusinessException("账号不可用");
        }

        return loginLogic(userInfo, null);
    }

    @Override
    public UserInfoVO loginDirect(String token) throws BusinessException {

        TokenUserInfoDto userInfoDto = (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN + token);
        if(null == userInfoDto){
            throw new BusinessException("身份失效，请使用密码登录");
        }
        String userId = userInfoDto.getUserId();
        UserInfo userInfo = getUserInfoByUserId(userId);
        if(userInfo == null){
            throw new BusinessException("未查找到用户，请核实ID后登录");
        }
        if (userInfo.getStatus().equals(UserStatusEnum.DISABLE.getType())) {
            throw new BusinessException("账号已被封禁");
        }
        return loginLogic(userInfo, token);
    }

    @Override
    public UserInfoVO loginForget(String email) throws BusinessException {
        //查email是否存在
        UserInfo userInfo = getUserInfoByEmail(email);
        if(null == userInfo){
            throw new BusinessException("用户不存在");
        }
        if (userInfo.getStatus().equals(UserStatusEnum.DISABLE.getType())) {
            throw new BusinessException("账号不可用");
        }
        return loginLogic(userInfo, null);
    }

    private UserInfoVO loginLogic(UserInfo userInfo, String token) {
        TokenUserInfoDto userInfoDto = getTokenUserInfoDto(userInfo);
        //redis检测是否已经登入， 若登录更换token
        Long userHeart = redisComponent.getUserHeart(userInfo.getUserId());
        if (userHeart != null) {
            MessageSendDto messageSendDto = new MessageSendDto();
            messageSendDto.setContactId(userInfo.getUserId());
            messageSendDto.setMessageType(MessageTypeEnum.FORCE_OFF_LINE.getType());
            messageSendDto.setMessageContent("您的账号在别处登录,强制下线");
            handlerMessage.sendMessage(messageSendDto);
        }
        //查询该账号的所有联系人
        UserContactQuery query = new UserContactQuery();
        query.setUserId(userInfo.getUserId());
        query.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        List<UserContact> userContacts = userContactMapper.selectList(query);
        //只需要把id放在redis中即可
        List<String> contactIdList = userContacts.stream().map(UserContact::getContactId).collect(Collectors.toList());


        redisComponent.clearUserContact(userInfo.getUserId());
        if(!contactIdList.isEmpty()){
            redisComponent.addUserContactBatch(userInfo.getUserId(), contactIdList);
        }

        //如果有token 说明是快速登陆
        if(StringTools.isEmpty(token)){
            //没有登入则建立登录信息
            //设置口令token  id + 20位随机值 然后md5加强唯一性
            token = StringTools.encodeMd5(userInfoDto.getUserId() + StringTools.getRandomString(Constants.LENGTH_20));
            userInfoDto.setToken(token);
            //删除redis之前的token 并保存新的token
            redisComponent.removeTokenUserInfoDto(userInfo.getUserId());
            redisComponent.saveTokenUserInfoDto(userInfoDto);
        }

        UserInfoVO userInfoVO = CopyTools.copy(userInfo, UserInfoVO.class);
        userInfoVO.setAdmin(userInfoDto.isAdmin());
        userInfoVO.setToken(token);
        return userInfoVO;
    }

    @Override
    public void updateUserInfo(UserInfo userInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws BusinessException, IOException {
        if(avatarFile != null) {
            SaveFile.saveAvatarFile(avatarFile, avatarCover, appConfig, userInfo.getUserId());
            //TODO 发消息给所有人 使得及时更换头像
            logger.info("发送消息给所有人，更换头像");
            MessageSendDto sendDto = new MessageSendDto();
            redisComponent.getUserContactList(userInfo.getUserId()).forEach(contactId -> {
                sendDto.setMessageType(MessageTypeEnum.UPDATE_AVATAR.getType());
                sendDto.setContactId(contactId);
                sendDto.setSendUserId(userInfo.getUserId());
                sendDto.setContactType(UserContactTypeEnum.USER.getType());
                handlerMessage.sendMessage(sendDto);
            });

            //还要发给自己
            logger.info("发送消息给自己，更换头像");
            sendDto.setMessageType(MessageTypeEnum.UPDATE_AVATAR.getType());
            sendDto.setContactId(userInfo.getUserId());
            sendDto.setContactType(UserContactTypeEnum.USER.getType());
            handlerMessage.sendMessage(sendDto);

        }

        //查询昵称信息是否更改
        UserInfo dbInfo = userInfoMapper.selectByUserId(userInfo.getUserId());

        //更新信息
        userInfoMapper.updateByUserId(userInfo, userInfo.getUserId());

        //更改昵称后的逻辑
        chatSessionUserService. updateNickName(userInfo.getNickName(), userInfo.getUserId());




    }

    @Override
    public void updateUserStatus(Integer status, String userId) throws BusinessException {
        UserContactStatusEnum userContactStatusEnum = UserContactStatusEnum.getEnumByStatus(status);
        if(null == userContactStatusEnum) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //因为拦截器已经做了校验

        UserInfo userInfo = new UserInfo();
        userInfo.setStatus(status);
        userInfoMapper.updateByUserId(userInfo, userId);
    }

    @Override
    public void forceOffLine(String userId) throws BusinessException {
        MessageSendDto sendDto = new MessageSendDto();
        sendDto.setContactType(UserContactTypeEnum.USER.getType());
        sendDto.setMessageType(MessageTypeEnum.FORCE_OFF_LINE.getType());
        sendDto.setMessageContent("您的账号被系统强制下线");
        sendDto.setContactId(userId);

        handlerMessage.sendMessage(sendDto);
    }


    private TokenUserInfoDto getTokenUserInfoDto(UserInfo userInfo) {
        //都成功后 ，封装信息到传输对象dto中
        TokenUserInfoDto tokenUserInfoDto = new TokenUserInfoDto();
        tokenUserInfoDto.setUserId(userInfo.getUserId());
        tokenUserInfoDto.setNickName(userInfo.getNickName());
        //解析配置文件得到
        String adminEmails = appConfig.getAdminEmails();
        if (!StringTools.isEmpty(adminEmails) && ArrayUtils.contains(adminEmails.split(","), userInfo.getEmail())) {
            tokenUserInfoDto.setAdmin(true);
        } else {
            tokenUserInfoDto.setAdmin(false);
        }

        return tokenUserInfoDto;
    }


}


