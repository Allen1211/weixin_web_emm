package com.allen.imsystem.controller;

import com.allen.imsystem.model.dto.JSONResponse;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RequestMapping("/api/talk")
@RestController
public class TalkController {

    @RequestMapping(value = "/getTalkData",method = RequestMethod.GET)
    public JSONResponse getTalkData(@RequestParam("talkId") String talkIdStr, HttpServletRequest request){
        Long talkId = Long.valueOf(talkIdStr);
        return null;
    }
}
