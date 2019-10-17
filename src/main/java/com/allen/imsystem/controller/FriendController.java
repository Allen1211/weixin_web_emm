package com.allen.imsystem.controller;

import com.allen.imsystem.common.ICacheHolder;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.model.dto.*;
import com.allen.imsystem.service.IFriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/api/friend")
public class FriendController {

    @Autowired
    private IFriendService friendService;

    @Qualifier("AttrCacheHolder")
    @Autowired
    private ICacheHolder cacheHolder;

    /**
     * 用户搜索功能
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/searchStranger",method = RequestMethod.POST)
    @ResponseBody
    public JSONResponse searchStranger(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String uid = cacheHolder.getUid(request);
        String keyword = params.get("keyWord");
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
    @RequestMapping(value = "/acceptFriendApplication",method = RequestMethod.POST)
    @ResponseBody
    public JSONResponse acceptFriendApplication(@RequestBody Map<String, String> params, HttpServletRequest request) {
        // 1、更新数据库
        String uid = cacheHolder.getUid(request);
        String friendId = (String) params.get("friendId");
        Integer groupId = 1;    // 不传groupId，默认放到默认组
        if(params.get("groupId") != null){
            groupId = Integer.valueOf(params.get("groupId"));
        }
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
    @RequestMapping(value = "/applyAddFriend",method = RequestMethod.POST)
    @ResponseBody
    public JSONResponse addFriendApply(@RequestBody @Validated ApplyAddFriendDTO params, HttpServletRequest request) {
        String uid = cacheHolder.getUid(request);
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
    @RequestMapping(value = "/getFriendApplication",method = RequestMethod.GET)
    @ResponseBody
    public JSONResponse getFriendApplication(HttpServletRequest request) {
        String uid = cacheHolder.getUid(request);
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
        String uid = cacheHolder.getUid(request);
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
        String uid = cacheHolder.getUid(request);
        Integer userId = cacheHolder.getUserId(request);
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
    @RequestMapping(value = "/getFriendList",method = RequestMethod.GET)
    @ResponseBody
    public JSONResponse getFriendList(HttpServletRequest request){
        String uid = cacheHolder.getUid(request);
        Set<UserInfoDTO> friendList = friendService.getFriendList(uid);
        JSONResponse jsonResponse = new JSONResponse(1);
        jsonResponse.putData("friendList",friendList);
        return jsonResponse;
    }

    /**
     * 按分组获取好友列表
     */
    @RequestMapping(value = "/getFriendListByGroup",method = RequestMethod.GET)
    @ResponseBody
    public JSONResponse getFriendListByGroup(HttpServletRequest request){
        String uid = cacheHolder.getUid(request);
        List<FriendListByGroupDTO> result = friendService.getFriendListByGroup("28661270");
        JSONResponse jsonResponse = new JSONResponse(1);
        jsonResponse.putData("groupList",result);
        return jsonResponse;
    }


    @RequestMapping(value = "/getFriendInfo",method = RequestMethod.GET)
    @ResponseBody
    public JSONResponse getFriendInfo(@RequestParam("friendId")String friendId,HttpServletRequest request){
        String uid = cacheHolder.getUid(request);
        UserInfoDTO friendInfo = friendService.getFriendInfo(uid,friendId);
        JSONResponse jsonResponse = new JSONResponse(1);
        jsonResponse.putData("friendInfo",friendInfo);
        return jsonResponse;
    }

    @RequestMapping(value = "/deleteFriend",method = RequestMethod.POST)
    @ResponseBody
    public JSONResponse deleteFriendInfo(@RequestBody Map<String,String> params,HttpServletRequest request){
        String friendId = params.get("friendId");
        String uid = cacheHolder.getUid(request);
        boolean success = friendService.deleteFriend(uid,friendId);
        System.out.println("try to delete a friend that not your friend");
        return new JSONResponse(1);
    }

    @RequestMapping(value = "/updateFriendGroupName",method = RequestMethod.POST)
    @ResponseBody
    public JSONResponse updateFriendGroupName(@RequestBody Map<String,String> params,HttpServletRequest request){
        Integer groupId = Integer.valueOf(params.get("groupId"));
        String newGroupName = params.get("newGroupName");
        Integer userId = cacheHolder.getUserId(request);
        boolean success = friendService.updateFriendGroupName(groupId,newGroupName,userId);
        if (success) {
            return new JSONResponse(1);
        } else {
            throw new BusinessException(ExceptionType.SERVER_ERROR, "更改失败");
        }
    }

    @RequestMapping(value = "/deleteFriendGroup",method = RequestMethod.POST)
    @ResponseBody
    public JSONResponse deleteFriendGroup(@RequestBody Map<String,String> params,HttpServletRequest request){
        Integer groupId = Integer.valueOf(params.get("groupId"));
        String uid = cacheHolder.getUid(request);
        boolean success = friendService.deleteFriendGroup(groupId,uid);
        if (success) {
            return new JSONResponse(1);
        } else {
            throw new BusinessException(ExceptionType.SERVER_ERROR, "删除失败");
        }
    }

    @RequestMapping(value = "/moveFriendToOtherGroup",method = RequestMethod.POST)
    @ResponseBody
    public JSONResponse moveFriendToOtherGroup(@RequestBody Map<String,String> params,HttpServletRequest request){
        Integer oldGroupId = Integer.valueOf(params.get("oldGroupId"));
        Integer newGroupId = Integer.valueOf(params.get("newGroupId"));
        String friendId = params.get("friendId");
        String uid = cacheHolder.getUid(request);
        boolean isSucess = friendService.moveFriendToOtherGroup(uid,friendId,oldGroupId,newGroupId);
        if (isSucess) {
            return new JSONResponse(1);
        } else {
            throw new BusinessException(ExceptionType.SERVER_ERROR, "更改失败，数据不匹配");
        }
    }




    @RequestMapping("/*")
    public void urlWrong(){
        throw new BusinessException(ExceptionType.URL_NOT_FOUND);
    }

}
