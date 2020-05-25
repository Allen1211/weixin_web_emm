package com.allen.imsystem.common.utils;

import java.util.HashSet;
import java.util.Set;

/**
 * @ClassName ChatIdUtil
 * @Description
 * @Author XianChuLun
 * @Date 2020/5/25
 * @Version 1.0
 */
public class ChatIdUtil {

    private final static int MAX_MESSAGE_SEQ = 0xFFF;

    private static int currentSeq = 0;

    /**
     * 1）第一段 42 Bit：用于存放时间戳，最长可表示到 2109 年，足够开发者当前使用了。时间戳数据放在高位，可以保证生成的唯一 ID 是按时间有序的，这个是消息 ID 必须要满足的条件。
     * <p>
     * 2）第二段 12 Bit：用于存放自旋转 ID 。我们知道，时间戳的精度是到毫秒的，对于一套亿级 IM 系统来说，同一毫秒内产生多条消息太正常不过了，这个自旋 ID 就是在给落到同一毫秒内的消息进行自增编号。12 Bit 则意味着，同一毫秒内，单台主机中最多可以标识 4096（ 2 的 12 次方）条消息。
     * <p>
     * 3）第三段 4 Bit：用于标识会话类型。4 Bit ，最多可以标识 16 中会话，足够涵盖单聊、群聊、系统消息、聊天室、客服及公众号等常用会话类型。
     * <p>
     * 4）第四段 6 Bit：最后6个位暂时不用
     */
    public static long generate(int chatType) {
        // 42bit 时间戳
        long highBits = System.currentTimeMillis();
        // 12bit 自旋id,获 取一个自旋 ID ， highBits 左移 12 位，并将自旋 ID 拼接到低 12 位中
        int seq = getMessageSeq();
        highBits = highBits << 12;
        highBits = highBits | seq;
        // 上步的 highBits 左移 4 位，将会话类型拼接到低 4 位
        highBits = highBits << 4;
        highBits = highBits | (chatType & 0xF);
        // 最后6个bit不用，全为0
        highBits = highBits << 6;
        return highBits;
    }


    private synchronized static int getMessageSeq() {
        int ret = currentSeq++;
        if (ret > MAX_MESSAGE_SEQ) {
            currentSeq = 0;
            ret = currentSeq++;
        }
        return ret;
    }

    public static int getChatType(long chatId){
        // 右移6位，对最后4位作掩码
        return (int)((chatId >> 6) & 0xF);
    }

    public static void main(String[] args) throws Exception {
        Set<Long> set = new HashSet<>(100000);
        for (int i = 0; i < 100000; i++) {
            long chatId = generate(1);
            System.out.println(chatId);
            System.out.println(getChatType(chatId));
            if(set.contains(chatId)){
                throw new Exception();
            }else{
                set.add(chatId);
            }
        }

    }
}
