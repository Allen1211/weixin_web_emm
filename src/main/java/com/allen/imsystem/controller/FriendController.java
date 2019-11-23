package com.allen.imsystem.controller;

import com.allen.imsystem.common.ICacheHolder;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.model.dto.*;
import com.allen.imsystem.service.IFriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 好友模块，负责处理与好友相关的请求
 */
@RestController
@RequestMapping("/api/friend")
public class FriendController {

    @Autowired
    private IFriendService friendService;

    @Qualifier("AttrCacheHolder")
    @Autowired
    private ICacheHolder cacheHolder;

    /**
     * 通过关键字搜索陌生人
     */
    @RequestMapping(value = "/searchStranger",method = RequestMethod.POST)
    public JSONResponse searchStranger(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String uid = cacheHolder.getUid(request);
        String keyword = params.get("keyWord");
        List<UserSearchResult> resultList = friendService.searchStranger(uid, keyword);
        return new JSONResponse().success().putData("selectList", resultList);
    }

    /**
     * 通过好友申请
     */
    @RequestMapping(value = "/acceptFriendApplication",method = RequestMethod.POST)
    public JSONResponse acceptFriendApplication(@RequestBody Map<String, String> params, HttpServletRequest request) {
        // 1、更新数据库
        String uid = cacheHolder.getUid(request);
        String friendId = (String) params.get("friendId");
        Integer groupId = null;
        if(params.get("groupId") != null){
            groupId = Integer.valueOf(params.get("groupId"));
        }
        boolean updateSuccess = friendService.passFriendApply(uid, friendId, groupId);

        // TODO 2、通知申请的用户，申请已通过

        if (updateSuccess) {
            return new JSONResponse().success();
        } else {
            throw new BusinessException(ExceptionType.SERVER_ERROR);
        }
    }

    /**
     * 申请添加好友
     *
     */
    @RequestMapping(value = "/applyAddFriend",method = RequestMethod.POST)
    public JSONResponse addFriendApply(@RequestBody @Validated ApplyAddFriendDTO params, HttpServletRequest request) {
        String uid = cacheHolder.getUid(request);
        // 更新数据库
        boolean isSuccess = friendService.addFriendApply(params, uid);

        if (isSuccess) {
            return new JSONResponse().success();
        } else {
            throw new BusinessException(ExceptionType.SERVER_ERROR);
        }
    }

    /**
     * 获取用户的历史好友申请消息。未读+已读，一个月内50条
     *
     * @return
     */
    @RequestMapping(value = "/getFriendApplication",method = RequestMethod.GET)
    public JSONResponse getFriendApplication(HttpServletRequest request) {
        String uid = cacheHolder.getUid(request);
        // 更新数据库
        List<FriendApplicationDTO> applicationList = friendService.getFriendApplicationList(uid);

        return new JSONResponse().success().putData("applicationList", applicationList);
    }

    /**
     * 获取用户的分组列表
     */
    @RequestMapping(value = "/getFriendGroup", method = RequestMethod.GET)
    public JSONResponse getFriendGroup(HttpServletRequest request) {
        String uid = cacheHolder.getUid(request);
        List<FriendGroup> friendGroupList = friendService.getFriendGroupList(uid);
        return new JSONResponse().success().putData("groupList", friendGroupList);
    }

    /**
     * 新建一个好友分组
     */
    @RequestMapping(value = "/addFriendGroup", method = RequestMethod.POST)
    @ResponseBody
    public JSONResponse addFriendGroup(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String uid = cacheHolder.getUid(request);
        String groupName = (String) params.get("groupName");
        Integer groupId = friendService.addFriendGroup(uid, groupName,false);
        if (groupId != 0) {
            return new JSONResponse().success().putData("groupId",groupId);
        } else {
            throw new BusinessException(ExceptionType.SERVER_ERROR, "新建失败");
        }
    }

    /**
     * 获取好友列表
     */
    @RequestMapping(value = "/getFriendList",method = RequestMethod.GET)
    public JSONResponse getFriendList(HttpServletRequest request){
        String uid = cacheHolder.getUid(request);
        Set<UserInfoDTO> friendList = friendService.getFriendList(uid);
        return new JSONResponse().success().putData("friendList",friendList);
    }

    /**
     * 获取好友列表，群聊拉人用
     */
    @RequestMapping(value = "/getFriendListForInvite",method = RequestMethod.GET)
    @ResponseBody
    public JSONResponse getFriendListForInvite(@RequestParam("gid")String gid, HttpServletRequest request){
        String uid = cacheHolder.getUid(request);
        List<FriendInfoForInvite> friendList = friendService.getFriendListForInvite(uid,gid);
        return new JSONResponse().success().putData("friendList",friendList);
    }

    /**
     * 按分组获取好友列表
     */
    @RequestMapping(value = "/getFriendListByGroup",method = RequestMethod.GET)
    public JSONResponse getFriendListByGroup(HttpServletRequest request){
        String uid = cacheHolder.getUid(request);
        Map<String,Object> result = friendService.getFriendListByGroup(uid);
        return new JSONResponse(1).putAllData(result);
    }

    /**
     * 获取好友的用户信息
     */
    @RequestMapping(value = "/getFriendInfo",method = RequestMethod.GET)
    public JSONResponse getFriendInfo(@RequestParam("friendId")String friendId, HttpServletRequest request){
        String uid = cacheHolder.getUid(request);
        UserInfoDTO friendInfo = friendService.getFriendInfo(uid,friendId);
        return new JSONResponse(1).success().putData("friendInfo",friendInfo);
    }

    /**
     * 删除好友
     */
    @RequestMapping(value = "/deleteFriend",method = RequestMethod.POST)
    public JSONResponse deleteFriendInfo(@RequestBody Map<String,String> params, HttpServletRequest request){
        String friendId = params.get("friendId");
        String uid = cacheHolder.getUid(request);
        boolean success = friendService.deleteFriend(uid,friendId);
        return new JSONResponse(1).success();
    }

    /**
     * 修改用户分组名
     */
    @RequestMapping(value = "/updateFriendGroupName",method = RequestMethod.POST)
    @ResponseBody
    public JSONResponse updateFriendGroupName(@RequestBody Map<String,String> params, HttpServletRequest request){
        Integer groupId = Integer.valueOf(params.get("groupId"));
        String newGroupName = params.get("newGroupName");
        String uid = cacheHolder.getUid(request);
        boolean success = friendService.updateFriendGroupName(groupId,newGroupName,uid);
        if (success) {
            return new JSONResponse(1);
        } else {
            throw new BusinessException(ExceptionType.SERVER_ERROR, "更改失败");
        }
    }

    /**
     * 删除用户分组
     */
    @RequestMapping(value = "/deleteFriendGroup",method = RequestMethod.POST)
    public JSONResponse deleteFriendGroup(@RequestBody Map<String,String> params, HttpServletRequest request){
        Integer groupId = Integer.valueOf(params.get("groupId"));
        String uid = cacheHolder.getUid(request);
        boolean success = friendService.deleteFriendGroup(groupId,uid);
        return new JSONResponse(1);
    }

    /**
     * 将好友移动到别的分组
     */
    @RequestMapping(value = "/moveFriendToOtherGroup",method = RequestMethod.POST)
    public JSONResponse moveFriendToOtherGroup(@RequestBody Map<String,String> params, HttpServletRequest request){
        Integer oldGroupId = Integer.valueOf(params.get("oldGroupId"));
        Integer newGroupId = Integer.valueOf(params.get("newGroupId"));
        String friendId = params.get("friendId");
        String uid = cacheHolder.getUid(request);
        boolean isSuccess = friendService.moveFriendToOtherGroup(uid,friendId,oldGroupId,newGroupId);
        return new JSONResponse(1);
    }

    /**
     * 检验这个friendId的合法性
     */
    @RequestMapping(value = "/validateFriendId",method = RequestMethod.GET)
    public JSONResponse moveFriendToOtherGroup(@RequestParam("friendId")String friendId, HttpServletRequest request){
        String uid = cacheHolder.getUid(request);
        boolean hasThisFriend = friendService.checkIsMyFriend(uid,friendId);
        return new JSONResponse(1).putData("hasThisFriend",hasThisFriend);
    }


    @RequestMapping("/*")
    public void urlWrong(){
        throw new BusinessException(ExceptionType.URL_NOT_FOUND);
    }

}
