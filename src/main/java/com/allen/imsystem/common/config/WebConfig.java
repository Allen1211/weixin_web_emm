package com.allen.imsystem.common.config;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters){
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        converter.setFastJsonConfig(new FastJsonConfigExt());
        List<MediaType> medias = new ArrayList<>();
        medias.add(MediaType.TEXT_HTML);
        medias.add(MediaType.APPLICATION_JSON);
        converter.setSupportedMediaTypes(medias);
        converters.add(converter);
    }

    @Bean
    public MultipartResolver multipartResolver(){
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(83886080);
        multipartResolver.setMaxInMemorySize(0);
        multipartResolver.setResolveLazily(true);
        multipartResolver.setDefaultEncoding("UTF-8");
        return multipartResolver;
    }

}
