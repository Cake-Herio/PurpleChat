package com.purplechat.controller;

import com.purplechat.annotation.GlobalInterceptor;
import com.purplechat.entity.po.GroupInfo;
import com.purplechat.entity.query.GroupInfoQuery;
import com.purplechat.entity.vo.PaginationResultVO;
import com.purplechat.entity.vo.ResponseVO;
import com.purplechat.exception.BusinessException;
import com.purplechat.service.GroupInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;


@RestController("adminGroupController")
@RequestMapping("/admin")
public class AdminGroupController extends ABaseController {

    @Resource
    GroupInfoService groupInfoService;



    @RequestMapping("/loadGroup")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadGroupList(GroupInfoQuery query){
        query.setOrderBy("create_time desc"); //按时间倒序排
        //需要关联查群主昵称 和 群总人数 因此需要调用 user_info表和contact表
        query.setQueryGroupOwnerName(true);
        query.setQueryGroupMemberCount(true);
        PaginationResultVO<GroupInfo> resultVO = groupInfoService.findListByPage(query);



        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/dissolutionGroup")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO dissolutionGroup(@NotEmpty String groupId) throws BusinessException {

        //拿到群主人的id
        GroupInfo groupInfo = groupInfoService.getGroupInfoByGroupId(groupId);
        if(null == groupInfo) {
            throw new BusinessException("群不存在");
        }
        groupInfoService.dissolutionGroup(groupInfo.getGroupOwnerId(), groupId);
        return getSuccessResponseVO(null);
    }


}
