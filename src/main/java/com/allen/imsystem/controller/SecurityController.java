package com.allen.imsystem.controller;

import com.allen.imsystem.common.ICacheHolder;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.common.utils.VertifyCodeUtil;
import com.allen.imsystem.model.dto.JSONResponse;
import com.allen.imsystem.service.ISecurityService;
import com.allen.imsystem.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

@Controller
@RequestMapping("/api/security")
public class SecurityController {

    @Autowired
    private IUserService userService;

    @Autowired
    private ISecurityService securityService;

    @Qualifier("AttrCacheHolder")
    @Autowired
    private ICacheHolder cacheHolder;

    @RequestMapping(value = "/getCodeImage",method = RequestMethod.GET)
    public void getCodeImage(HttpSession session, HttpServletResponse response) throws IOException {

        Map<String,Object> codeImage = VertifyCodeUtil.getCodeImage();
        String code = (String) codeImage.get("code");
        BufferedImage image = (BufferedImage) codeImage.get("image");
        // 验证码保存到session
        session.setAttribute("imageCode", code);

        System.out.println("new ImageCode:"+code);
        //浏览器不要缓存，防止验证码图片不能刷新
        response.setDateHeader("expries", -1);
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma","no-cache");
        //通知浏览器应该以图片形式打开
        response.setHeader("Content-Type", "image/jpeg");
        //写出到客户端
        ImageIO.write(image, "jpg", response.getOutputStream());
    }


    @RequestMapping(value = "/sendEmailCode",method = RequestMethod.GET)
    @ResponseBody
    public JSONResponse sendCheckCodeEmail(@RequestParam("email") String email, @RequestParam("type")Integer type){
        boolean isRegisted = userService.isEmailRegisted(email);
        switch (type){
            case 1:{
                if(isRegisted){
                    throw new BusinessException(ExceptionType.EMAIL_HAS_BEEN_REGISTED);
                }
                break;
            }
            case 2:{
                if(!isRegisted){
                    throw new BusinessException(ExceptionType.USER_NOT_FOUND,"该邮箱还未注册");
                }
                break;
            }
            default:{
                throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL,"不支持的type类型");
            }
        }

        boolean sendSuccess = securityService.sendCheckEmail(type,email);

        // 发送成功的话, 将验证码和过期时间拼接添加到session
        if(sendSuccess){
            return new JSONResponse(1);
        }else{
            throw new BusinessException(ExceptionType.SERVER_ERROR,"发送失败");
        }

    }

    @RequestMapping(value = "/verifyLocalStorageData",method = RequestMethod.GET)
    @ResponseBody
    public JSONResponse verifyLocalStorageData(){
        return new JSONResponse(1);
    }
}
