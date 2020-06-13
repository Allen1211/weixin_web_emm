package com.allen.imsystem.common.filter;

import com.alibaba.fastjson.JSONObject;
import com.allen.imsystem.common.bean.JSONResponse;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "CORSFilter",urlPatterns = "/*")
public class CORSFilter implements Filter {
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse) res;

        if(request.getRequestURI().equals("/imsystem/talk")){
            chain.doFilter(request,response);
            return;
        }

        if(response.getHeader("Access-Control-Allow-Origin") == null){
            String origin = request.getHeader("Origin");
            response.setHeader("Access-Control-Allow-Origin", origin);
        }else{
            System.out.println("Access-Control-Allow-Origin already exist!");
        }
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT,PATCH, HEAD");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, ContentType, Content-Type, Depth, User-Agent, X-File-Size," +
                " X-Requested-With, X-Requested-By, If-Modified-Since, X-File-Name, X-File-Type, Cache-Control, Origin,token,boundary");
        response.addHeader("Access-Control-Max-Age", "3600");
        if(request.getMethod().equals("OPTIONS")){
            handleOPTIONS(response);
            return;
        }
        chain.doFilter(request, response);
    }

    private void handleOPTIONS(HttpServletResponse response) throws IOException {
        JSONResponse jsonResponse = new JSONResponse().success();
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(JSONObject.toJSONString(jsonResponse));
    }

    public void init(FilterConfig filterConfig) {}
    public void destroy() {}
}