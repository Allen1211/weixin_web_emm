package com.allen.imsystem.chat.model.param;

import com.allen.imsystem.chat.model.vo.GroupMemberView;
import lombok.Data;

import java.util.List;

/**
 * @ClassName KickMemberParam
 * @Description
 * @Author XianChuLun
 * @Date 2020/6/15
 * @Version 1.0
 */
@Data
public class KickMemberParam {
    private String gid;
    private List<GroupMemberView> memberList;
}
