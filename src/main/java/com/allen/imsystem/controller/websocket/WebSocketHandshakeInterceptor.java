package com.allen.imsystem.controller.websocket;

import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.common.utils.JWTUtil;
import com.auth0.jwt.interfaces.Claim;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object
                > attributes) throws Exception {
        String query = request.getURI().getQuery();
        String[] params = query.split("&");
        String token = null;
        for(String param : params){
            String paramName = param.split("=")[0];
            String paramVal = param.split("=")[1];
            if(paramName.equals("token") && paramVal!=null){
                token = paramVal;
                break;
            }
        }
        if(token != null){
            Map<String, Claim> result = JWTUtil.verifyLoginToken(token,"");
            if(result == null){
                return false;
            }
            String uid = result.get("uid").asString();
            Integer userId = result.get("userId").asInt();
            attributes.put("uid",uid);
            attributes.put("userId",userId);
            return true;
        }else{
            throw new BusinessException(ExceptionType.TOKEN_EXPIRED_ERROR,"token解析错误或已过期,请重新登录");
        }
    }


    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}