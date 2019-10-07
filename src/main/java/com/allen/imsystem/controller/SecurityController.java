package com.allen.imsystem.controller;

import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.common.utils.VertifyCodeUtil;
import com.allen.imsystem.model.dto.JSONResponse;
import com.allen.imsystem.service.IMailService;
import com.allen.imsystem.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Controller
@RequestMapping("/api/security")
public class SecurityController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IMailService mailService;

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


    @RequestMapping(value = "/sendRegistEmailCode",method = RequestMethod.GET)
    @ResponseBody
    public JSONResponse sendCheckCodeEmail(@RequestParam String email, HttpSession session){

        if(userService.isEmailRegisted(email)){
            throw new BusinessException(ExceptionType.EMAIL_HAS_BEEN_REGISTED);
        }

        Map<String, Object> model = new HashMap<>(2);
        Random random = new Random(System.currentTimeMillis());
        String emailCode = String.valueOf(random.nextInt(899999) + 100000);
        model.put("emailCode",emailCode);
        model.put("sendTime",new Date());
        boolean sendSuccess = mailService.sendMailVelocity("Imsystem注册邮箱验证",null,email,model);

        // 发送成功的话, 将验证码和过期时间拼接添加到session
        if(sendSuccess){
            Long expriedTime = System.currentTimeMillis() + 20*60*1000; // 过期时间20分钟
            String emailCodeToken = emailCode + "#" + expriedTime;  // 拼接
            session.setAttribute(email,emailCodeToken);
            return new JSONResponse(1);
        }else{
            throw new BusinessException(ExceptionType.SERVER_ERROR);
        }

    }


}
