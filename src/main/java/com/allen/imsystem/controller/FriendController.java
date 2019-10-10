package com.allen.imsystem.controller;

import com.allen.imsystem.common.ICacheHolder;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.common.utils.HTTPUtil;
import com.allen.imsystem.model.dto.*;
import com.allen.imsystem.service.IFriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/friend")
public class FriendController {

    @Autowired
    private IFriendService friendService;

    @Qualifier("defaultCacheHolder")
    @Autowired
    private ICacheHolder cacheHolder;

    /**
     * 用户搜索功能
     *
     * @param params
     * @return
     */
    @RequestMapping("/searchStranger")
    @ResponseBody
    public JSONResponse searchStranger(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String uid = cacheHolder.getUid(HTTPUtil.getTokenFromHeader(request));
        String keyword = params.get("keyword");
        List<UserSearchResult> resultList = friendService.searchStranger(uid, keyword);
        JSONResponse jsonResponse = new JSONResponse(1);
        jsonResponse.putData("selectList", resultList);
        return jsonResponse;
    }

    /**
     * 通过好友申请
     *
     * @param params
     * @return
     */
    @RequestMapping("/acceptFriendApplication")
    @ResponseBody
    public JSONResponse acceptFriendApplication(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        // 1、更新数据库
        String uid = cacheHolder.getUid(HTTPUtil.getTokenFromHeader(request));
        String friendId = (String) params.get("friendId");
        Integer groupId = Integer.valueOf((String) params.get("groupId"));
        boolean updateSuccess = friendService.passFriendApply(uid, friendId, groupId);

        // TODO 2、通知申请的用户，申请已通过

        if (updateSuccess) {
            return new JSONResponse(1);
        } else {
            throw new BusinessException(ExceptionType.SERVER_ERROR);
        }
    }

    /**
     * 申请添加好友
     *
     * @param params
     * @return
     */
    @RequestMapping("/applyAddFriend")
    @ResponseBody
    public JSONResponse addFriendApply(@RequestBody @Validated ApplyAddFriendDTO params, HttpServletRequest request) {
        String uid = cacheHolder.getUid(HTTPUtil.getTokenFromHeader(request));
        // 更新数据库
        boolean isSuccess = friendService.addFriendApply(params, uid);

        // TODO 通知被申请的用户，新好友申请通知

        if (isSuccess) {
            return new JSONResponse(1);
        } else {
            throw new BusinessException(ExceptionType.SERVER_ERROR);
        }
    }

    /**
     * 获取用户的历史好友申请消息。未读+已读，一个月内50条
     *
     * @return
     */
    @RequestMapping(value = "/getFriendApplication")
    @ResponseBody
    public JSONResponse getFriendApplication(HttpServletRequest request) {
        String uid = cacheHolder.getUid(HTTPUtil.getTokenFromHeader(request));
        // 更新数据库
        List<FriendApplicationDTO> applicationList = friendService.getFriendApplicationList(uid);

        // TODO 通知被申请的用户，新好友申请通知

        JSONResponse jsonResponse = new JSONResponse(1);
        jsonResponse.putData("applicationList", applicationList);
        return jsonResponse;
    }

    /**
     * 获取用户的分组列表
     */
    @RequestMapping(value = "/getFriendGroup", method = RequestMethod.GET)
    @ResponseBody
    public JSONResponse getFriendGroup(HttpServletRequest request) {
        String uid = cacheHolder.getUid(HTTPUtil.getTokenFromHeader(request));
        List<FriendGroup> friendGroupList = friendService.getFriendGroupList(uid);
        JSONResponse jsonResponse = new JSONResponse(1);
        jsonResponse.putData("groupList", friendGroupList);
        return jsonResponse;
    }

    /**
     * 新建一个好友分组
     */
    @RequestMapping(value = "/addFriendGroup", method = RequestMethod.POST)
    @ResponseBody
    public JSONResponse addFriendGroup(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String uid = cacheHolder.getUid(HTTPUtil.getTokenFromHeader(request));
        Integer userId = cacheHolder.getUserId(HTTPUtil.getTokenFromHeader(request));
        String groupName = (String) params.get("groupName");
        Integer groupId = friendService.addFriendGroup(userId,uid, groupName);
        if (groupId != 0) {
            JSONResponse jsonResponse = new JSONResponse(1);
            jsonResponse.putData("groupId",groupId);
            return jsonResponse;
        } else {
            throw new BusinessException(ExceptionType.SERVER_ERROR, "新建失败");
        }
    }

    /**
     * 获取好友列表
     */
    @RequestMapping("/getFriendList")
    @ResponseBody
    public JSONResponse getFriendList(HttpServletRequest request){
        String uid = cacheHolder.getUid(HTTPUtil.getTokenFromHeader(request));
        List<UserInfoDTO> friendList = friendService.getFriendList(uid);
        JSONResponse jsonResponse = new JSONResponse(1);
        jsonResponse.putData("friendList",friendList);
        return jsonResponse;
    }

    @RequestMapping("/getFriendInfo")
    @ResponseBody
    public JSONResponse getFriendInfo(@RequestParam("friendId")String friendId,HttpServletRequest request){
        String uid = cacheHolder.getUid(HTTPUtil.getTokenFromHeader(request));
        UserInfoDTO friendInfo = friendService.getFriendInfo(uid,friendId);
        JSONResponse jsonResponse = new JSONResponse(1);
        jsonResponse.putData("friendInfo",friendInfo);
        return jsonResponse;
    }

    @RequestMapping("/deleteFriend")
    @ResponseBody
    public JSONResponse deleteFriendInfo(@RequestBody Map<String,String> params,HttpServletRequest request){
        String friendId = params.get("friendId");
        String uid = cacheHolder.getUid(HTTPUtil.getTokenFromHeader(request));
        boolean success = friendService.deleteFriend(uid,friendId);
        System.out.println("try to delete a friend that not your friend");
        return new JSONResponse(1);
    }

    @RequestMapping("/updateFriendGroupName")
    @ResponseBody
    public JSONResponse updateFriendGroupName(@RequestBody Map<String,String> params,HttpServletRequest request){
        Integer groupId = Integer.valueOf(params.get("groupId"));
        String newGroupName = params.get("newGroupName");
        Integer userId = cacheHolder.getUserId(HTTPUtil.getTokenFromHeader(request));
        boolean success = friendService.updateFriendGroupName(groupId,newGroupName,userId);
        if (success) {
            return new JSONResponse(1);
        } else {
            throw new BusinessException(ExceptionType.SERVER_ERROR, "更改失败");
        }
    }

    @RequestMapping("/deleteFriendGroup")
    @ResponseBody
    public JSONResponse deleteFriendGroup(@RequestBody Map<String,String> params,HttpServletRequest request){
        Integer groupId = Integer.valueOf(params.get("groupId"));
        String uid = cacheHolder.getUid(HTTPUtil.getTokenFromHeader(request));
        boolean success = friendService.deleteFriendGroup(groupId,uid);
        if (success) {
            return new JSONResponse(1);
        } else {
            throw new BusinessException(ExceptionType.SERVER_ERROR, "删除失败");
        }
    }

    @RequestMapping("/moveFriendToOtherGroup")
    @ResponseBody
    public JSONResponse moveFriendToOtherGroup(@RequestBody Map<String,String> params,HttpServletRequest request){
        Integer oldGroupId = Integer.valueOf(params.get("oldGroupId"));
        Integer newGroupId = Integer.valueOf(params.get("newGroupId"));
        String friendId = params.get("friendId");
        String uid = cacheHolder.getUid(HTTPUtil.getTokenFromHeader(request));
        boolean isSucess = friendService.moveFriendToOtherGroup(uid,friendId,oldGroupId,newGroupId);
        if (isSucess) {
            return new JSONResponse(1);
        } else {
            throw new BusinessException(ExceptionType.SERVER_ERROR, "更改失败");
        }
    }


    @RequestMapping("/*")
    public void urlWrong(){
        throw new BusinessException(ExceptionType.URL_NOT_FOUND);
    }

}
