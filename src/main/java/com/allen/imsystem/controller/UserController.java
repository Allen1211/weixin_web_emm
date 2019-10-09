package com.allen.imsystem.controller;

import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.model.dto.JSONResponse;
import com.allen.imsystem.model.dto.RegistFormDTO;
import com.allen.imsystem.model.pojo.User;
import com.allen.imsystem.service.ISecurityService;
import com.allen.imsystem.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    IUserService userService;
    @Autowired
    ISecurityService securityService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public JSONResponse login(@RequestBody Map<String,Object> params,
                              HttpServletRequest request, HttpServletResponse response, HttpSession session){
        String uidOrEmail = (String) params.get("uid");
        String password = (String)params.get("password");
        String code = (String)params.get("code");
        String correctCode = (String) session.getAttribute("imageCode");

        securityService.verifyImageCode(code,correctCode);
        // 登录服务
        Map<String,Object> map = userService.login(uidOrEmail,password);
        User user = (User) map.get("user");
        // 登录成功， 把存在session中的邮箱验证码去除。
        session.removeAttribute("code");
        // 把当前会话用户的信息缓存到session
        session.setAttribute("user",user);
        /**
         * 把token以cookie形成返回到浏览器
         */
        String newToken = (String) map.get("newToken");
        Cookie cookie = new Cookie("token",newToken);
        cookie.setMaxAge(60*60);
        cookie.setPath("/");
        response.addCookie(cookie);

        JSONResponse jsonResponse = new JSONResponse(1);
        jsonResponse.putData("uid",user.getUid());
        return jsonResponse;
    }

    @RequestMapping(value = "/regist", method = RequestMethod.POST)
    @ResponseBody
    public JSONResponse regist(@RequestBody @Validated RegistFormDTO registFormDTO, HttpSession session){
        String email = registFormDTO.getEmail();
        String password = registFormDTO.getPassword();
        String username = registFormDTO.getUsername();
        String code = registFormDTO.getCode();
        String emailCode = registFormDTO.getEmailCode();

        String correctCode = (String) session.getAttribute("imageCode");
        String emailCodeToken = (String) session.getAttribute(email);

        securityService.verifyImageCode(code,correctCode);
//        securityService.verifyEmailCode(emailCode,emailCodeToken);

        if(userService.isEmailRegisted(email)){
            throw new BusinessException(ExceptionType.EMAIL_HAS_BEEN_REGISTED);
        }
        userService.regist(email,password,username);
        session.removeAttribute("code");
        session.removeAttribute(email); // 注册成功， 把存在session中的邮箱验证码去除。
        JSONResponse jsonResponse = new JSONResponse(1);
        return jsonResponse;
    }


    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    @ResponseBody
    public JSONResponse logout(HttpServletRequest request,HttpServletResponse response,
                               HttpSession session){
        /**
         * 把本地保存的token cookie去除
         */
        Cookie cookie = new Cookie("token",null);
        cookie.setMaxAge(0);
        cookie.setPath(request.getContextPath());
        response.addCookie(cookie);
        User user = (User) session.getAttribute("user");
        if(user != null){
            userService.logout(user.getUid());
        }
        session.invalidate();   //销毁session
        return new JSONResponse(1);
    }

    @RequestMapping("/*")
    public void urlWrong(){
        throw new BusinessException(ExceptionType.URL_NOT_FOUND);
    }
}
