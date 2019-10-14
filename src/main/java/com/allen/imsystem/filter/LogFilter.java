package com.allen.imsystem.filter;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class LogFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        System.out.println("request time: "+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        System.out.println("request url: " + request.getRequestURI());
        System.out.println("request mehtod: "+request.getMethod());
        System.out.println("request origin: "+request.getHeader("Origin"));
        System.out.println();
//        logRequest(request,response);
        filterChain.doFilter(request,response);
        System.out.println("Response: Access-Control-Allow-Origin: "+response.getHeader("Access-Control-Allow-Origin"));
        System.out.println();
        //        logResponse(request,response);
    }

    private void logRequest(HttpServletRequest request, HttpServletResponse resposne){
        System.out.println("***************request***********************");
        System.out.println("request time: "+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        System.out.println("request origin : " + request.getHeader("origin"));
        System.out.println("request Referer:  " + request.getHeader("Referer"));
        System.out.println("request url: " + request.getRequestURI());
        System.out.println("request mehtod: "+request.getMethod());

        Cookie[] cookies = request.getCookies();
        if(cookies!=null){
            System.out.println("-----request carry cookies :");
            for(Cookie cookie : cookies){
                System.out.println(cookie.getName() + "  domain: "+cookie.getDomain());
                System.out.println("value: "  + cookie.getValue());
            }
            System.out.println("-----end request carry cookies");
        }else{
            System.out.println("request cookies: null");
        }
        System.out.println("****************request*end*******************");
    }

    private void logResponse(HttpServletRequest request, HttpServletResponse resposne){
        System.out.println("****************response***********************");
        for (String name : resposne.getHeaderNames()){
            System.out.println("Header name : "+name);
            for(String val : resposne.getHeaders(name)){
                System.out.println("   value:" + val);
            }
        }
        System.out.println("******************response*end*****************");
        System.out.println();
    }
}
