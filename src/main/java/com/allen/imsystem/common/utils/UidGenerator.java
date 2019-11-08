package com.allen.imsystem.common.utils;

import com.allen.imsystem.dao.mappers.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UidGenerator {

    @Autowired
    private UserMapper userMapper;

//    public void generateUid(){
//        Set<String> set = new HashSet<>(10000);
//        StringBuilder base = new StringBuilder("60806040");
//        for(int i=0;i<=9999;i++){
//            char[] seq = String.format("%04d",i).toCharArray();
//            base.setCharAt(1,seq[0]);
//            base.setCharAt(3,seq[1]);
//            base.setCharAt(5,seq[2]);
//            base.setCharAt(7,seq[3]);
//            set.add(base.toString());
//        }
//        List<String> list = new ArrayList<>(set);
//        Integer affected = userDao.insertBatchIntoUidPool(list);
//        System.out.println(affected);
//    }

    public void generateGid(){
        Set<String> set = new HashSet<>(10000);
        StringBuilder base = new StringBuilder("108060402");
        for(int i=0;i<=9999;i++){
            char[] seq = String.format("%04d",i).toCharArray();
            base.setCharAt(1,seq[0]);
            base.setCharAt(3,seq[1]);
            base.setCharAt(5,seq[2]);
            base.setCharAt(7,seq[3]);
            set.add(base.toString());
        }
        List<String> list = new ArrayList<>(set);
        Integer affected = userMapper.insertBatchIntoGidPool(list);
        System.out.println(affected);
    }


    public static void main(String[] args) {


    }
}
