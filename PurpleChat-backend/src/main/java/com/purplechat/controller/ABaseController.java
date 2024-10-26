package com.purplechat.controller;

import com.purplechat.entity.constants.Constants;
import com.purplechat.entity.dto.TokenUserInfoDto;
import com.purplechat.enums.ResponseCodeEnum;

import com.purplechat.entity.vo.ResponseVO;
import com.purplechat.exception.BusinessException;
import com.purplechat.redis.RedisUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

public class ABaseController{

	@Resource
	RedisUtils redisUtils;
	protected static final String STATUS_SUCCESS = "success";
	protected static final String STATUS_ERROR = "error";
	protected <T> ResponseVO getSuccessResponseVO(T t) {
		ResponseVO<T> responseVO = new ResponseVO<>();
		responseVO.setStatus(STATUS_SUCCESS);
		responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
		responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
		responseVO.setData(t);
		return responseVO;
	}

	protected <T> ResponseVO getBusinessErrorResponseVO(BusinessException e, T t){
		ResponseVO vo = new ResponseVO();
		vo.setStatus(STATUS_ERROR);
		if(e.getCode() == null) {//
			vo.setCode(ResponseCodeEnum.CODE_600.getCode());
		} else {
			vo.setCode((e.getCode()));
		}
		vo.setInfo(e.getMessage());
		vo.setData(t);
		return vo;
	}

	protected <T> ResponseVO getServerErrorResponseVO(T t) {
		ResponseVO<T> responseVO = new ResponseVO<>();
		responseVO.setStatus(STATUS_ERROR);
		responseVO.setCode(ResponseCodeEnum.CODE_500.getCode());
		responseVO.setInfo(ResponseCodeEnum.CODE_500.getMsg());
		responseVO.setData(t);
		return responseVO;
	}

	protected TokenUserInfoDto getUserInfoByToken(HttpServletRequest request) {
		String token = request.getHeader("token");
        return (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN + token);
	}


}
