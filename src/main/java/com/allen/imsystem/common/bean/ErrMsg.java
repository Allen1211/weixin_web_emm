package com.allen.imsystem.common.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ErrMsg {
    private String text;
    private Map<String,String> parameters;

    public ErrMsg() {
        this.text = "";
        this.parameters = new HashMap<>();
    }

    public ErrMsg(String text) {
        this.text = text;
        this.parameters = new HashMap<>();
    }

    public ErrMsg(String text, Map<String, String> parameters) {
        this.text = text;
        this.parameters = parameters;
    }

    public void addError(String fieldName, String errText){
        this.parameters.put(fieldName,errText);
    }
}
