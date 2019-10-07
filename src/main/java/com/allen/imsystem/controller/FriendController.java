package com.allen.imsystem.controller;

import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.model.dto.*;
import com.allen.imsystem.service.IFriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/friend")
public class FriendController {

    @Autowired
    private IFriendService friendService;

    /**
     * 用户搜索功能
     * @param params
     * @return
     */
    @RequestMapping("/searchStranger")
    @ResponseBody
    public JSONResponse searchStranger(@RequestBody Map<String,String> params){
        String uid = params.get("uid");
        String keyword = params.get("keyword");
        List<UserSearchResult> resultList = friendService.searchStranger(uid,keyword);
        JSONResponse jsonResponse = new JSONResponse(1);
        jsonResponse.putData("selectList",resultList);
        return jsonResponse;
    }

    /**
     * 通过好友申请
     * @param params
     * @return
     */
    @RequestMapping("/acceptFriendApplication")
    @ResponseBody
    public JSONResponse acceptFriendApplication(@RequestBody Map<String,Object> params ){
        // 1、更新数据库
        String uid = (String) params.get("uid");
        String friendId = (String) params.get("friendId");
        Integer groupId = Integer.valueOf((String) params.get("groupId"));
        boolean updateSuccess = friendService.passFriendApply(uid,friendId,groupId);

        // TODO 2、通知申请的用户，申请已通过

        if(updateSuccess){
            return new JSONResponse(1);
        }else{
            throw new BusinessException(ExceptionType.SERVER_ERROR);
        }
    }

    /**
     * 申请添加好友
     * @param params
     * @return
     */
    @RequestMapping("/applyAddFriend")
    @ResponseBody
    public JSONResponse applyAddFriend(@RequestBody @Validated ApplyAddFriendDTO params){
        // 更新数据库
        boolean isSuccess = friendService.addFriendApply(params);

        // TODO 通知被申请的用户，新好友申请通知

        if(isSuccess){
            return new JSONResponse(1);
        }else{
            throw new BusinessException(ExceptionType.SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/getFriendApplication",method = RequestMethod.GET)
    @ResponseBody
    public JSONResponse getFriendApplication(@RequestParam("uid")String uid){
        // 更新数据库
        List<FriendApplicationDTO> applicationList = friendService.getFriendApplicationList(uid);

        // TODO 通知被申请的用户，新好友申请通知

        JSONResponse jsonResponse = new JSONResponse(1);
        jsonResponse.putData("applicationList",applicationList);
        return jsonResponse;
    }

    /**
     * 获取用户的分组列表
     */
    @RequestMapping(value = "/getFriendGroup",method = RequestMethod.GET)
    @ResponseBody
    public JSONResponse getFriendGroup(@RequestParam("uid")String uid){
        List<FriendGroup> friendGroupList = friendService.getFriendGroupList(uid);
        JSONResponse jsonResponse = new JSONResponse(1);
        jsonResponse.putData("groupList",friendGroupList);
        return jsonResponse;
    }
}
