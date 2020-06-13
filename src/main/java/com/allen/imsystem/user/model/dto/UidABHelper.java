package com.allen.imsystem.user.model.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName UidABHelper
 * @Description
 * @Author XianChuLun
 * @Date 2020/5/23
 * @Version 1.0
 */
@Getter
@Setter
public class UidABHelper {

    private String uidA;
    private String uidB;

    public static UidABHelper sortAndCreate(String uidA, String uidB){
        return new UidABHelper(uidA,uidB);
    }

    private UidABHelper(String uidA, String uidB) {
        if(uidA.compareTo(uidB) <= 0){
            this.uidA = uidA;
            this.uidB = uidB;
        }else{
            this.uidB = uidA;
            this.uidA = uidB;
        }
    }
}
