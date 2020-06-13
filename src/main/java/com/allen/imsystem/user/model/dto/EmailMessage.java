package com.allen.imsystem.user.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class EmailMessage implements Serializable {

    private String subject;

    private String emailAddress;

    private String fileLocation;

    private Map<String,Object> model;

    public EmailMessage() {
    }

    public EmailMessage(String subject, String emailAddress, String fileLocation, Map<String, Object> model) {
        this.subject = subject;
        this.emailAddress = emailAddress;
        this.fileLocation = fileLocation;
        this.model = model;
    }
}
