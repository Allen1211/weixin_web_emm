package com.allen.imsystem.filter;


import com.alibaba.fastjson.JSONObject;
import com.allen.imsystem.common.utils.JWTUtil;
import com.allen.imsystem.model.dto.ErrMsg;
import com.allen.imsystem.model.dto.JSONResponse;
import com.allen.imsystem.model.pojo.User;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TokenFilter implements Filter {


    private static String[] passURIs = {"/imsystem/api/user/login","/imsystem/api/user/regist",
            "/imsystem/api/user/logout","/imsystem/api/security/sendRegistEmailCode"
            ,"/imsystem/api/security/getCodeImage"};



    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        Cookie[] cookiess = request.getCookies();
        HttpSession session = request.getSession();
        System.out.println(new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").format(new Date())
                + "->" + request.getRequestURI());
        System.out.println(request.getMethod());
        if(request.getMethod().equals("OPTIONS")){
        }

        if(cookiess != null){
            for(Cookie cookie :cookiess){
                if(cookie.getName().equals("token")){
                    System.out.println(cookie.getValue());
                }
            }
        }
        String URI = request.getRequestURI();
        for(String pass : passURIs){
            if(pass.equals(URI)){
                filterChain.doFilter(servletRequest,servletResponse);
                return;
            }
        }
        boolean success = false;
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for(Cookie cookie :cookies){
                if(cookie.getName().equals("token")){
                    success = checkAuth(cookie.getValue(),request.getHeader("token"),request.getSession());
                    break;
                }
            }
        }
        if(success)
            filterChain.doFilter(request,response);
        else{
            //销毁cookie
            Cookie cookie = new Cookie("token",null);
            cookie.setMaxAge(0);
            cookie.setPath(request.getContextPath());
            response.addCookie(cookie);
            //销毁session
            request.getSession().invalidate();
            JSONResponse jsonResponse = new JSONResponse(0,1004,new ErrMsg("登录信，请重新登录"));
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write(JSONObject.toJSONString(jsonResponse));
        }
    }

    private boolean checkAuth(String cookieToken,String paramToken, HttpSession session){
        User loginUser = (User) session.getAttribute("user");
//        return loginUser!=null && cookieToken.equals(paramToken) && JWTUtil.verifyLoginToken(cookieToken,loginUser.getUid());
        return loginUser!=null &&  JWTUtil.verifyLoginToken(cookieToken,loginUser.getUid());
    }

    @Override
    public void destroy() {

    }
}
