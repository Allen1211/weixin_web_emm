package com.allen.imsystem.controller;

import com.allen.imsystem.common.ICacheHolder;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.common.utils.HTTPUtil;
import com.allen.imsystem.common.utils.JWTUtil;
import com.allen.imsystem.model.dto.EditUserInfoDTO;
import com.allen.imsystem.model.dto.JSONResponse;
import com.allen.imsystem.model.dto.RegistFormDTO;
import com.allen.imsystem.model.pojo.User;
import com.allen.imsystem.model.pojo.UserInfo;
import com.allen.imsystem.service.IFileService;
import com.allen.imsystem.service.ISecurityService;
import com.allen.imsystem.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private IUserService userService;
    @Autowired
    private ISecurityService securityService;
    @Autowired
    private IFileService fileService;

    @Qualifier("AttrCacheHolder")
    @Autowired
    private ICacheHolder cacheHolder;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    @CrossOrigin
    public JSONResponse login(@RequestBody Map<String, Object> params,
                              HttpServletRequest request, HttpServletResponse response) {
        String uidOrEmail = (String) params.get("uid");
        String password = (String) params.get("password");
        String code = (String) params.get("code");
//        HttpSession session = request.getSession();
//        String correctCode = (String) session.getAttribute("imageCode");

//        securityService.verifyImageCode(code,correctCode);
        // 登录服务
        Map<String, Object> map = userService.login(uidOrEmail, password);
        User user = (User) map.get("user");
        // 登录成功， 把存在session中的邮箱验证码去除。
//        session.removeAttribute("code");
        // 把当前会话用户的信息缓存到session
//        session.setAttribute("user",user);

        /**
         * 把token返回到浏览器
         */
        String newToken = (String) map.get("newToken");

        JSONResponse jsonResponse = new JSONResponse(1);
        jsonResponse.putData("uid", user.getUid());
        jsonResponse.putData("token", newToken);
        return jsonResponse;
    }


    @RequestMapping(value = "/regist", method = RequestMethod.POST)
    @ResponseBody
    public JSONResponse regist(@RequestBody @Validated RegistFormDTO registFormDTO, HttpSession session) {
        String email = registFormDTO.getEmail();
        String password = registFormDTO.getPassword();
        String username = registFormDTO.getUsername();
        String code = registFormDTO.getCode();
        String emailCode = registFormDTO.getEmailCode();

//        String correctCode = (String) session.getAttribute("imageCode");
        String emailCodeToken = cacheHolder.getEmailCode(email);

//        securityService.verifyImageCode(code,correctCode);
//        securityService.verifyEmailCode(emailCode,emailCodeToken);

        if (userService.isEmailRegisted(email)) {
            throw new BusinessException(ExceptionType.EMAIL_HAS_BEEN_REGISTED);
        }
        userService.regist(email, password, username);
//        session.removeAttribute("code");
//        session.removeAttribute(email); // 注册成功， 把存在session中的邮箱验证码去除。
        JSONResponse jsonResponse = new JSONResponse(1);
        return jsonResponse;
    }


    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    @ResponseBody
    public JSONResponse logout(HttpServletRequest request, HttpServletResponse response,
                               HttpSession session) {
        /**
         * 把本地保存的token cookie去除
         */
//        Cookie cookie = new Cookie("token",null);
//        cookie.setMaxAge(0);
//        cookie.setPath("/");
//        response.addCookie(cookie);
//        User user = (User) session.getAttribute("user");
        String uid = JWTUtil.getMsgFromToken(HTTPUtil.getTokenFromHeader(request), "uid", String.class);
        if (uid != null) {
            userService.logout(uid);
        }
        return new JSONResponse(1);
    }

    @RequestMapping(value = "/uploadAvatar", method = RequestMethod.POST)
    @ResponseBody
    public JSONResponse uploadAvatar(@RequestParam("avatar") MultipartFile multipartFile, HttpServletRequest request) {
        String uid = cacheHolder.getUid(request);
        String avatarURL = userService.uploadAvatar(multipartFile, uid);
        return new JSONResponse(1).putData("avatar", avatarURL);
    }

    @RequestMapping(value = "/updateUserInfo", method = RequestMethod.POST)
    @ResponseBody
    public JSONResponse updateUserInfo(@RequestBody EditUserInfoDTO editUserInfoDTO, HttpServletRequest request) {
        String uid = cacheHolder.getUid(request);
        editUserInfoDTO.setUid(uid);
        userService.updateUserInfo(editUserInfoDTO, uid);
        return new JSONResponse(1);
    }

    @RequestMapping(value = "/getSelfInfo", method = RequestMethod.GET)
    public JSONResponse getSelfInfo(HttpServletRequest request) {
        Integer userId = cacheHolder.getUserId(request);
        EditUserInfoDTO selfInfo = userService.getSelfInfo(userId);
        return new JSONResponse(1).putData("userInfo", selfInfo);
    }

    @RequestMapping(value = "/getUserOnlineStatus",method = RequestMethod.GET)
    public JSONResponse getUserOnlineStatus(@RequestParam("uid")String uid){
        Integer onlineStatus = userService.getUserOnlineStatus(uid);
        return new JSONResponse(1).putData("onlineStatus",onlineStatus);
    }

    @RequestMapping("/*")
    public void urlWrong() {
        throw new BusinessException(ExceptionType.URL_NOT_FOUND);
    }
}
