package com.allen.imsystem.common.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;

import javax.servlet.http.HttpServletRequest;

public class DispatcherServletWrapper extends DispatcherServlet {

    @Override
    protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
        HandlerExecutionChain superChain = super.getHandler(request);
        Object superHandler = superChain.getHandler();

        if (!(superHandler instanceof HandlerMethod)) {
            return superChain;
        }

        HandlerMethod hm = (HandlerMethod)superHandler;
        if (!hm.getBeanType().isAnnotationPresent(Controller.class)) {
            return superChain;
        }

        //仅处理@Controller注解的Bean
        return new HandlerExecutionChainWrapper(superChain,request,getWebApplicationContext());
    }

}
