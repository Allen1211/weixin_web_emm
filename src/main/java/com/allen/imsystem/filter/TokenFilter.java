package com.allen.imsystem.filter;


import com.alibaba.fastjson.JSONObject;
import com.allen.imsystem.common.utils.JWTUtil;
import com.allen.imsystem.model.dto.ErrMsg;
import com.allen.imsystem.model.dto.JSONResponse;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
        System.out.println(request.getRequestURI());
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
                    success = JWTUtil.verifyLoginToken(cookie.getValue());
                    break;
                }
            }
        }
        if(success)
            filterChain.doFilter(request,response);
        else{
            JSONResponse jsonResponse = new JSONResponse(0,1004,new ErrMsg("登录信息已过期，请重新登录"));
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write(JSONObject.toJSONString(jsonResponse));
        }
    }

    @Override
    public void destroy() {

    }
}
