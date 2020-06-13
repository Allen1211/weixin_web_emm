package com.allen.imsystem.file.utils;

public class ByteUtil {
    private final static char[] HEX = "0123456789abcdef".toCharArray();

    /**
     * 将字节数组转成 16 进制的字符串来表示，每个字节采用两个字符表表示
     *
     * @param bys 需要转换成 16 进制的字节数组
     * @return
     */
    public static String bytes2Hex(byte[] bys) {
        char[] chs = new char[bys.length * 2];
        for(int i = 0, offset = 0; i < bys.length; i++) {
            chs[offset++] = HEX[bys[i] >> 4 & 0xf];
            chs[offset++] = HEX[bys[i] & 0xf];
        }
        return new String(chs);
    }
}