package com.allen.imsystem.common.security;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 在Spring MVC调用Controller前，
 * 通过动态代理和反射机制对Controller的调用进行拦截，
 * 并在挡截中对Method参数的值进行XSS过滤替换。
 */
public class HandlerExecutionChainWrapper extends HandlerExecutionChain {

    private BeanFactory beanFactory;
    private HttpServletRequest request;
    private HandlerMethod handlerWrapper;   // 获取bean方法参数、注解、返回值的便利对象
    private byte[] lock = new byte[0];

    public HandlerExecutionChainWrapper(HandlerExecutionChain chain, HttpServletRequest request,
                                        BeanFactory beanFactory){
        super(chain.getHandler(),chain.getInterceptors());
        this.request = request;
        this.beanFactory = beanFactory;
    }

    @Override
    public Object getHandler(){
        if (handlerWrapper != null){
            return handlerWrapper;
        }
        synchronized (lock){
            if (handlerWrapper != null){
                return handlerWrapper;
            }
            HandlerMethod superMethodHandler = (HandlerMethod) super.getHandler();
            Object proxyHandler = createProxyBean(superMethodHandler);
            handlerWrapper = new HandlerMethod(proxyHandler,superMethodHandler.getMethod());
            return handlerWrapper;
        }
    }

    /**
     * 动态代理设计模式
     * 创建代理对象，实现在调用真正的controller bean的mapping方法前的Xss过滤切面逻辑
     * @param handler
     * @return
     */
    private Object createProxyBean(HandlerMethod handler){
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(handler.getBeanType());  // cglib子类代理
        Object bean = handler.getBean();
        if (bean instanceof String){
            bean = beanFactory.getBean((String)bean);
        }
        ControllerXssInterceptor xssInterceptor = new ControllerXssInterceptor(bean);   //代理对象
        xssInterceptor.setRequest(request);
        enhancer.setCallback(xssInterceptor);
        return enhancer.create();
    }


    /**
     * 动态代理设计模式
     * 切入切面逻辑
     */
    public static class ControllerXssInterceptor implements MethodInterceptor {

        private Object target;
        private HttpServletRequest request;
        // 自定义需要进行XSS过滤的对象的包名前缀， 一般为DTO、VO对象等，应为标准Javabean
        private String[] objectMatchPackages = {"com.allen.imsystem.model.pojo.dto"};

        public ControllerXssInterceptor(Object target) {
            this.target = target;
        }

        public void setRequest(HttpServletRequest request){
            this.request = request;
        }

        @Override
        public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Exception {
            if(args != null){
                for(int i=0;i<args.length;i++){
                    if(args[i] == null) continue;
                    if(args[i] instanceof String){
                        args[i] = HtmlUtils.htmlEscape((String)args[i]);
                        continue;
                    }
                    for(String pk:objectMatchPackages){
                        if(args[i].getClass().getName().startsWith(pk)){
                            ObjectXssReplace(args[i]);
                            break;
                        }
                    }
                }
            }
            return method.invoke(target,args);
        }

        /**
         * 对要进行过滤的javabean对象通过反射获取对所有field的值过滤XSS
         * @param obj
         */
        private void ObjectXssReplace(final Object obj){
            if(obj == null){
                return;
            }
            ReflectionUtils.doWithFields(obj.getClass(), new ReflectionUtils.FieldCallback() {
                @Override
                public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                    ReflectionUtils.makeAccessible(field);  // 获取field的权限
                    String value = (String)field.get(obj);
                    if(value != null){
                        value = HtmlUtils.htmlEscape(value);
                        field.set(obj,value);
                    }
                }
            }, field -> {
                boolean matchesType = String.class.equals(field.getType());
                /**
                 * 对于get请求，不在参数列表的javabean不过滤
                 */
                if(request != null && "GET".equals(request.getMethod())){
                    boolean mathchesReqMethod = request.getParameterMap().containsKey(field.getName());
                    return matchesType && mathchesReqMethod;
                }
                return matchesType;
            });
        }
    }


}
