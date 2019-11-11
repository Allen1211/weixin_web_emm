package com.allen.imsystem.filter;


import com.alibaba.fastjson.JSONObject;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.utils.JWTUtil;
import com.allen.imsystem.model.dto.ErrMsg;
import com.allen.imsystem.model.dto.JSONResponse;
import com.allen.imsystem.model.pojo.User;
import com.auth0.jwt.interfaces.Claim;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class TokenFilter implements Filter {


    private static String[] passURIs = {"/imsystem/api/user/login",
            "/imsystem/api/user/regist",
            "/imsystem/api/user/resetUserPassword",
            "/imsystem/api/user/logout",
            "/imsystem/api/security/sendEmailCode",
            "/imsystem/api/security/getCodeImage",
            "/imsystem/talk"};

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String URI = request.getRequestURI();
        String method = request.getMethod();
        if(method.equals("OPTIONS")){
            filterChain.doFilter(request,response);
            return;
        }
        for(String pass : passURIs){
            if(pass.equals(URI)){
                filterChain.doFilter(servletRequest,servletResponse);
                return;
            }
        }
        String token = request.getHeader("token");
        if(token == null){
            handleFail(response,"请求无token信息，请重新登录");
            return;
        }
        try {
            Map<String, Claim> claims = JWTUtil.verifyLoginToken(token,null);
            if(claims == null){
                handleFail(response, "token解析错误，请重新登录");
                return;
            }
            String uid = claims.get("uid").asString();
            Integer userId = claims.get("userId").asInt();
            request.setAttribute("uid",uid);
            request.setAttribute("userId",userId);

            filterChain.doFilter(request,response);
        }catch(Exception e){
            handleFail(response, "token解析错误，请重新登录");
        }
    }

    private void handleFail(HttpServletResponse response, String errMsg) throws IOException {
        JSONResponse jsonResponse = new JSONResponse(0,1004,new ErrMsg(errMsg));
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(JSONObject.toJSONString(jsonResponse));
    }


    @Override
    public void destroy() {

    }
}
