package com.allen.imsystem.controller;

import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.common.utils.SessionUtil;
import com.allen.imsystem.model.dto.*;
import com.allen.imsystem.service.IFriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/friend")
public class FriendController {

    @Autowired
    private IFriendService friendService;

    /**
     * 用户搜索功能
     *
     * @param params
     * @return
     */
    @RequestMapping("/searchStranger")
    @ResponseBody
    public JSONResponse searchStranger(@RequestBody Map<String, String> params, HttpSession session) {
        String uid = SessionUtil.getUidFromSession(session);
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
    public JSONResponse acceptFriendApplication(@RequestBody Map<String, Object> params, HttpSession session) {
        // 1、更新数据库
        String uid = SessionUtil.getUidFromSession(session);
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
    public JSONResponse addFriendApply(@RequestBody @Validated ApplyAddFriendDTO params, HttpSession session) {
        String uid = SessionUtil.getUidFromSession(session);
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
    public JSONResponse getFriendApplication(HttpSession session) {
        String uid = SessionUtil.getUidFromSession(session);
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
    public JSONResponse getFriendGroup(HttpSession session) {
        String uid = SessionUtil.getUidFromSession(session);
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
    public JSONResponse addFriendGroup(@RequestBody Map<String, Object> params, HttpSession session) {
        String uid = SessionUtil.getUidFromSession(session);
        String groupName = (String) params.get("groupName");
        boolean success = friendService.addFriendGroup(uid, groupName);
        if (success) {
            return new JSONResponse(1);
        } else {
            throw new BusinessException(ExceptionType.SERVER_ERROR, "新建失败");
        }
    }

    /**
     * 获取好友列表
     */
    @RequestMapping("/getFriendList")
    @ResponseBody
    public JSONResponse getFriendList(HttpSession session){
        String uid = SessionUtil.getUidFromSession(session);
        List<UserInfoDTO> friendList = friendService.getFriendList(uid);
        JSONResponse jsonResponse = new JSONResponse(1);
        jsonResponse.putData("friendList",friendList);
        return jsonResponse;
    }

    @RequestMapping("/getFriendInfo")
    @ResponseBody
    public JSONResponse getFriendInfo(@RequestParam("friendId")String friendId,HttpSession session){
        String uid = SessionUtil.getUidFromSession(session);
        UserInfoDTO friendInfo = friendService.getFriendInfo(uid,friendId);
        JSONResponse jsonResponse = new JSONResponse(1);
        jsonResponse.putData("friendInfo",friendInfo);
        return jsonResponse;
    }

    @RequestMapping("/deleteFriend")
    @ResponseBody
    public JSONResponse deleteFriendInfo(@RequestBody Map<String,String> params,HttpSession session){
        String friendId = params.get("friendId");
        String uid = SessionUtil.getUidFromSession(session);
        boolean success = friendService.deleteFriend(uid,friendId);
        System.out.println("try to delete a friend that not your friend");
        return new JSONResponse(1);
    }

    @RequestMapping("/updateFriendGroupName")
    @ResponseBody
    public JSONResponse updateFriendGroupName(@RequestBody Map<String,String> params,HttpSession session){
        String groupId = params.get("groupId");
        String newGroupName = params.get("newGroupName");
        Integer userId = SessionUtil.getUserIdFromSession(session);
        return null;
    }

    @RequestMapping("/*")
    public void urlWrong(){
        throw new BusinessException(ExceptionType.URL_NOT_FOUND);
    }

}
