package com.purplechat.service.impl;

import com.purplechat.entity.po.UserInfo;
import com.purplechat.entity.po.UserInfoBeauty;
import com.purplechat.entity.query.UserInfoBeautyQuery;
import com.purplechat.entity.query.UserInfoQuery;
import com.purplechat.entity.vo.PaginationResultVO;
import com.purplechat.entity.query.SimplePage;
import com.purplechat.enums.BeautyAccountEnum;
import com.purplechat.enums.PageSize;
import com.purplechat.enums.ResponseCodeEnum;
import com.purplechat.exception.BusinessException;
import com.purplechat.mappers.UserInfoBeautyMapper;
import com.purplechat.mappers.UserInfoMapper;
import com.purplechat.service.UserInfoBeautyService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
/**
 * @Description:靓号表Service
 * @Author:文翔
 * @date:2024/06/10
 */
@Service("userInfoBeautyService")
public class UserInfoBeautyServiceImpl implements UserInfoBeautyService {

@Resource
private UserInfoBeautyMapper<UserInfoBeauty, UserInfoBeautyQuery> userInfoBeautyMapper;

@Resource
	UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<UserInfoBeauty> findListByParam (UserInfoBeautyQuery query) { return this.userInfoBeautyMapper.selectList(query); }
	/**
	 * 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam (UserInfoBeautyQuery query) { return this.userInfoBeautyMapper.selectCount(query); }

	/**
	 * 分页查询
	 */
	@Override
	public PaginationResultVO<UserInfoBeauty> findListByPage (UserInfoBeautyQuery query) {
		Integer count = this.findCountByParam(query);
		Integer pageSize = query.getPageNo() == null?PageSize.SIZE15.getSize():query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		List<UserInfoBeauty> list = this.findListByParam(query);
		PaginationResultVO<UserInfoBeauty> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add (UserInfoBeauty bean) { return this.userInfoBeautyMapper.insert(bean); }

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch (List<UserInfoBeautyQuery> listBean){
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userInfoBeautyMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch (List<UserInfoBeautyQuery> listBean){
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userInfoBeautyMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 根据Id查询
	 */
	 public UserInfoBeauty getUserInfoBeautyById(Integer id) { return this.userInfoBeautyMapper.selectById(id); }

	/**
	 * 根据Id修改
	 */
	 public Integer updateUserInfoBeautyById(UserInfoBeauty bean, Integer id) { return this.userInfoBeautyMapper.updateById(bean, id); }

	/**
	 * 根据Id删除
	 */
	 public Integer deleteUserInfoBeautyById(Integer id) { return this.userInfoBeautyMapper.deleteById(id); }

	/**
	 * 根据UserId查询
	 */
	 public UserInfoBeauty getUserInfoBeautyByUserId(String userId) { return this.userInfoBeautyMapper.selectByUserId(userId); }

	/**
	 * 根据UserId修改
	 */
	 public Integer updateUserInfoBeautyByUserId(UserInfoBeauty bean, String userId) { return this.userInfoBeautyMapper.updateByUserId(bean, userId); }

	/**
	 * 根据UserId删除
	 */
	 public Integer deleteUserInfoBeautyByUserId(String userId) { return this.userInfoBeautyMapper.deleteByUserId(userId); }

	/**
	 * 根据Email查询
	 */
	 public UserInfoBeauty getUserInfoBeautyByEmail(String email) { return this.userInfoBeautyMapper.selectByEmail(email); }

	/**
	 * 根据Email修改
	 */
	 public Integer updateUserInfoBeautyByEmail(UserInfoBeauty bean, String email) { return this.userInfoBeautyMapper.updateByEmail(bean, email); }

	/**
	 * 根据Email删除
	 */
	 public Integer deleteUserInfoBeautyByEmail(String email) { return this.userInfoBeautyMapper.deleteByEmail(email); }



	@Override
	public void saveBeautyAccount(UserInfoBeauty userInfoBeauty) throws BusinessException {
		//新增靓号时userid一定为空，修改时则不是
		if(null != userInfoBeauty.getId()){
			//不是空说明是修改 则需判断是否已被使用费
			UserInfoBeauty userInfo = userInfoBeautyMapper.selectById(userInfoBeauty.getId());
			//查看该id是否和数据库的id相同 防止id伪造 不同则抛出异常
			if(!userInfo.getUserId().equals(userInfoBeauty.getUserId())){
				throw new BusinessException(ResponseCodeEnum.CODE_600);
			}

			if(BeautyAccountEnum.USED.getStatus().equals(userInfo.getStatus())) {
				//已经被使用了 无法修改
				throw new BusinessException("靓号已被使用，无法修改");
			}
			//如果还未被使用
			//判断更改的邮箱是否已被占用
			UserInfoBeauty userInfoBeautyByEmail = userInfoBeautyMapper.selectByEmail(userInfoBeauty.getEmail());
			if(null != userInfoBeautyByEmail){
				throw new BusinessException("该邮箱在靓号表中已存在");
			}

			//判断该邮箱是否已经通过非靓号途径注册为用户
			UserInfo userInfoByEmail = userInfoMapper.selectByEmail(userInfoBeauty.getEmail());
			if(null != userInfoByEmail){
				throw new BusinessException("该邮箱已被注册");
			}


			//判断更改的id是否已经被使用
			UserInfoBeauty userInfoBeautyByUserId = userInfoBeautyMapper.selectByUserId(userInfoBeauty.getUserId());
			//还需查看另一个表是否和当前id相同
			UserInfo dbUserInfo = userInfoMapper.selectByUserId(userInfoBeauty.getUserId());
			if(null != userInfoBeautyByUserId || null != dbUserInfo){
				throw new BusinessException("id已被占用");
			}
			//实现更新
			userInfoBeautyMapper.updateById(userInfoBeauty, userInfoBeauty.getId());


		}else{
			//新增时的逻辑判断
			//判断邮箱是否已被占用
			UserInfoBeauty userInfoBeautyByEmail = userInfoBeautyMapper.selectByEmail(userInfoBeauty.getEmail());
			if(null != userInfoBeautyByEmail){
				throw new BusinessException("该邮箱在靓号表中已存在");
			}
			//判断该邮箱是否已经通过非靓号途径注册为用户
			UserInfo userInfoByEmail = userInfoMapper.selectByEmail(userInfoBeauty.getEmail());
			if(null != userInfoByEmail){
				throw new BusinessException("该邮箱已被注册");
			}


			//判断id是否已被占用
			UserInfoBeauty userInfoBeautyByUserId = userInfoBeautyMapper.selectByUserId(userInfoBeauty.getUserId());
			//还需查看另一个表是否和当前id相同
			UserInfo dbUserInfo = userInfoMapper.selectByUserId(userInfoBeauty.getUserId());
			if(null != userInfoBeautyByUserId || null != dbUserInfo){
				throw new BusinessException("id已被占用");
			}



			//实现插入
			userInfoBeauty.setStatus(BeautyAccountEnum.NO_USE.getStatus());
			userInfoBeautyMapper.insert(userInfoBeauty);

		}





	}


}