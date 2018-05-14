package com.smartmi.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xujifu
 * @time 2018-04-17 00:00:00
 * @description 计算邀请码
 *
 * 邀请码算法说明：
 *
 * 邀请码特点，每个人不同，不易破解
 *
 * 简易邀请码生成器，通过base64编码某组特定的数字，base64原始key打乱，不易被简单的base64破译，
 * 不过由于具有一定的规律，在多组连续邀请码时会产生破译的可能。
 *
 * 这里设计了一组稍微复杂的邀请码生成器，生成的邀请码规律不明显，生成速度快。如果只是使用可以
 * 直接上手，如果需要了解，首先需要提前了解RC4。
 *
 * 第一步：首先由于每个人邀请码不同，因此基数上有两种方法进行获取，一种随机也就是uuid，不过这
 * 种结果很长，不适于生成邀请码（邀请码一般情况下都比较段，毕竟可能涉及到手动输入），而最让人
 * 心动的就是用户id。因此这里获取用户id作为基础，这里取用户ID的低40位，40位能够表达的用户数量
 * 为2^40约1,000,000,000,000。基本上满足我们对用户ID的取值范围。这里为什么选择40位，主要是如
 * 果位数过低，用户数量到达一定程度时不能满足每人邀请码不同。如果过高，最终生成的邀请码就会相
 * 应的增长。不利于用户输入。
 * -----------------------------------------------------------------------------------
 * |40bit value = Long uid -> byte[5]                                                |
 * -----------------------------------------------------------------------------------
 * 第二步：将上一步得到的40位跟8位的预留字节合并，这里预留了8位主要是考虑到以后改造过程如果需
 * 要，可以使用这8位字符。
 * -----------------------------------------------------------------------------------
 * |48bit source = 40bit value + 8bit bak                                            |
 * -----------------------------------------------------------------------------------
 * 第三步：将上面得到的48bit原字符串进行加密，key为随机生成的一个key。
 * -----------------------------------------------------------------------------------
 * |48bit encodeone = RC4(key, 48bit source)                                         |
 * -----------------------------------------------------------------------------------
 * 第四步：对上面得到的加密串计算一次hash，这个hash可以用于校验数据正确性
 * -----------------------------------------------------------------------------------
 * |5bit hashcode = (1bit version + 48bit encodeone) % 32 & 0x1F                     |
 * -----------------------------------------------------------------------------------
 * 第五步：这一步是关键性的一步，因为根据上面的算法计算得到的依旧是有规律的字符串，这时就需要
 * 一个变量去打乱这个规律，本来通过时间戳打乱是比较合适的方案，但由于时间戳的不可再获取性，因
 * 此选用了上一步计算出来的hashcode，因为计算出的hashcode值基本上越临近，相似度越低。因此这
 * 里使用hashcode作为key再次进行RC4编码。
 * -----------------------------------------------------------------------------------
 * |48bit encodetwo = RC4(key = 5bit hashcode, 48bit encodeone)                      |
 * -----------------------------------------------------------------------------------
 * 第六步：将hashcode、加密串、版本合并成54位的最终串。这里为什么是54位，因为6*9=54。6bit刚
 * 好可以使用base64进行再编码
 * -----------------------------------------------------------------------------------
 * |54bit invatecode = 5bit hashcode + 48bit encode + 1bit version                   |
 * -----------------------------------------------------------------------------------
 * 第七步：将上一步得到的54位长度的邀请码，6位一份整合到9个字符中，再通过自定义的base64编码
 * 进行编码，最终得到最后的9位邀请码
 * -----------------------------------------------------------------------------------
 * |9char invatecode = customize_base64(54bit-> (char[9] & 0x3F))                    |
 * -----------------------------------------------------------------------------------
 *
 * 解码原理就是以上的逆操作，不在复述
 */
public class InvateCodeAlgorithms {

    private static Logger logger = LoggerFactory.getLogger(InvateCodeAlgorithms.class);
    //打乱的base64编码，本应去除O、o、0、I等易出现歧义字符，不过没有找到合适的暂时不去除
    private static String basecode = "DXqwGWfs1hyFKTarRvn69+7SlkPAc4OVgYuiIBUJExe#tmbN82CZ0Hz5QjLpM3od";
    //原始的RC4秘钥，该秘钥为随机生成
    private static String key = "20701287458e65fb73cd6aa31263f83a";
    //邀请码版本信息，目前为1，有需要可提高版本
    private static char version = 0x01;
    //邀请码预留位，00011000
    private static char bak = 0x18;
    //向量池长度
    private final int RC4_LENGTH = 256;
    //状态向量
    private int[] vectorS = new int[RC4_LENGTH];
    //临时向量
    private char[] vectorT = new char[RC4_LENGTH];

    private void swap(int i, int j) {
        vectorS[i] ^= vectorS[j];
        vectorS[j] ^= vectorS[i];
        vectorS[i] ^= vectorS[j];
    }

    /**
     * RC4的原理分为三步：
     * 1、密钥流：RC4算法的关键是根据明文和密钥生成相应的密钥流，密钥流的长度和明文的长度是对应的，
     *    也就是说明文的长度是500字节，那么密钥流也是500字节。当然，加密生成的密文也是500字节，
     *    因为密文第i字节=明文第i字节^密钥流第i字节；
     * 2、状态向量S：长度为256，S[0],S[1].....S[255]。每个单元都是一个字节，算法运行的任何时候，
     *    S都包括0-255的8比特数的排列组合，只不过值的位置发生了变换；
     * 3、临时向量T：长度也为256，每个单元也是一个字节。如果密钥的长度是256字节，就直接把密钥的值
     *    赋给T，否则，轮转地将密钥的每个字节赋给T；
     * 4、密钥K：长度为1-256字节，注意密钥的长度keylen与明文长度、密钥流的长度没有必然关系，通常
     *    密钥的长度趣味16字节（128比特）。
     * @param key 秘钥
     * @param src 需要加密的串
     * @return 加密后的串
     */
    private  String rc4code(String key, String src) {
        int len = src.length();
        char[] keystream = new char[len];
        //初始化S和T
        for (int i = 0; i < 256; i++) {
            vectorS[i] = i;
            vectorT[i] = key.charAt(i % key.length());
        }
        int j = 0;
        //初始排列S
        for (int i = 0; i < 256; i++) {
            j = (j + vectorS[i] + vectorT[i]) & 0xFF;
            swap(i, j);
        }
        //产生密钥流
        for (int m = 0, n = 0, r = 0; r < len; r++) {
            m = (m + 1) & 0xFF;
            n = (n + vectorS[m]) & 0xFF;
            swap(m, n);
            keystream[r] = (char) vectorS[(vectorS[m] + vectorS[n]) & 0xFF];
        }
        StringBuilder sb = new StringBuilder();
        //加密
        for (int i = 0; i < len; i++) {
            sb.append((char) (src.charAt(i) ^ keystream[i]));
        }
        return sb.toString();
    }

    /**
     * 提取uid的低40位
     * @param uid 用户id
     * @return 返回5个char
     */
    private char[] longToChar(long uid) {
        char[] chars = new char[5];
        chars[4] = (char) (uid & 0xFF);
        chars[3] = (char) (uid >>> 8 & 0xFF);
        chars[2] = (char) (uid >>> 16 & 0xFF);
        chars[1] = (char) (uid >>> 24 & 0xFF);
        chars[0] = (char) (uid >>> 32 & 0xFF);
        return chars;
    }

    /**
     * 将加密串，版本，hashcode进行拼装
     * @param version 版本信息
     * @param cipher 加密串
     * @param hashcode hashcode值
     * @return 9位字符数组
     */
    private char[] codesec(char version, char[] cipher, char hashcode) {
        long tmp = 0;
        tmp |= (hashcode & 0x1F);
        for (char c : cipher) {
            tmp = tmp << 8;
            tmp |= c;
        }
        tmp = tmp << 1;
        tmp |= version;

        char[] res = new char[9];
        for (int i = 0; i < 9; i++) {
            res[i] = (char) ((tmp >>> ((8 - i) * 6)) & 0x3F);
        }
        return res;
    }

    /**
     * 根据uid生成邀请码
     * @param uid 用户id
     * @return 邀请码
     */
    public String fcode(long uid) {
        String source = String.valueOf(longToChar(uid)) + bak;
        String encodeone = rc4code(key, source);
        char hashcode = (char) (((version + encodeone).hashCode() % 32) & 0x1F);
        String key2 = Integer.toBinaryString(hashcode & 0xFF);
        String encodetwo = rc4code(key2, encodeone);
        char[] ma = codesec(version, encodetwo.toCharArray(), hashcode);
        StringBuilder sb = new StringBuilder();
        for (char m : ma) {
            sb.append(basecode.charAt(m));
        }
        return sb.toString();
    }

    /**
     * 解密邀请码
     * @param fcode 邀请码
     * @return 用户id
     */
    public long funcode(String fcode){
        long tmp = 0;
        for(char code : fcode.toCharArray()){
            if(basecode.indexOf(code) == -1){
                //该字符不在加密串中，说明是错误的校验码
                return -1;
            }
            tmp = tmp << 6;
            tmp = tmp | ((char)basecode.indexOf(code) & 0x3F);
        }
        if((char)(tmp & version) == 0x00){
            //版本不对，是错误的校验码
            return -1;
        }
        char hashcode = (char)((tmp >>> 49) & 0x1F);
        tmp = (tmp >>> 1) & 0x0000FFFFFFFFFFFFL;

        char[] chars = {
            (char)((tmp >>> 40) & 0xFF),
            (char)((tmp >>> 32) & 0xFF),
            (char)((tmp >>> 24) & 0xFF),
            (char)((tmp >>> 16) & 0xFF),
            (char)((tmp >>>  8) & 0xFF),
            (char)((tmp) & 0xFF)};

        String key2 = Integer.toBinaryString(hashcode);
        String decodeone = rc4code(key2, new String(chars));
        if((char) (((version + decodeone).hashCode() % 32) & 0x1F) != hashcode){
            //hash计算对不上
            return -1;
        }
        String decodetwo = rc4code(key, decodeone);
        if(decodetwo.charAt(decodetwo.length() - 1) != bak){
            //预留字段不符
            return -1;
        }
        long uid = 0;
        for(int i = 0; i < decodetwo.length() - 1; i ++){
            uid = uid << 8;
            uid |= decodetwo.charAt(i);
        }
        return uid;
    }

    public static void main(String[] args){
        long uid = 800232;

        TreeSet<String> tree = new TreeSet<String>();
        List<String> code = new ArrayList<String>();
        InvateCodeAlgorithms a = new InvateCodeAlgorithms();
        //logger.info(134425453 + "," + a.fcode(1401204008) + "," + a.funcode(a.fcode(134425453)));
//        for(int i = 123456; i < 320000; i ++) {
//            tree.add(a.fcode(i));
//            code.add(a.fcode(i));
//            System.out.println(a.fcode(i) + "," + a.funcode(a.fcode(i)));
//        }
//        System.out.println(tree.size() - code.size());
    }
}
