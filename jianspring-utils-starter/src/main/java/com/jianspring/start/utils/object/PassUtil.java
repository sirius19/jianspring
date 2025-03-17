package com.jianspring.start.utils.object;

import java.security.SecureRandom;
import java.util.*;

/**
 * @Author:  InfoInsights 
 * @date 2023/4/4 16:05
 */
public class PassUtil {
    /**
     * 生成随机密码生成方式一
     * 密码字典 -> 随机获取字符
     *
     * @param len 生成密码长度（最短为4位）
     * @return 随机密码
     */
    public static String initPass(int len) {
        // 如果len 小于等于3
        if (len <= 3) {
            throw new IllegalArgumentException("密码的位数不能小于3位！");
        }
        //生成的随机数
        int i;
        //生成的密码的长度
        int count = 0;
        // 密码字典
        char[] str = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        // 大写字母密码字典
        List<Character> upperStrList = Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');
        Set<Character> upperStrSet = new HashSet<>(upperStrList);
        // 小写字母的密码字典
        List<Character> lowerStrList = Arrays.asList('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z');
        Set<Character> lowerStrSet = new HashSet<>(lowerStrList);
        // 数字的密码字典
        List<Character> numStrList = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9');
        Set<Character> numStrSet = new HashSet<>(numStrList);

        StringBuilder builder = new StringBuilder();
        SecureRandom r = new SecureRandom();
        boolean isContainUpperChar = false;
        boolean isContainLowerChar = false;
        boolean isContainNumChar = false;
        while (count < len - 3) {
            //生成 0 ~ 密码字典-1之间的随机数
            i = r.nextInt(str.length);
            builder.append(str[i]);
            count++;
            if (!isContainUpperChar && upperStrSet.contains(str[i])) {
                isContainUpperChar = true;
            }
            if (!isContainLowerChar && lowerStrSet.contains(str[i])) {
                isContainLowerChar = true;
            }
            if (!isContainNumChar && numStrSet.contains(str[i])) {
                isContainNumChar = true;
            }
        }
        // 如果不存在的，则加，确保一定会存在数字，大写字母，小写字母
        if (!isContainUpperChar) {
            builder.append(upperStrList.get(r.nextInt(upperStrList.size())));
        }
        if (!isContainLowerChar) {
            builder.append(lowerStrList.get(r.nextInt(lowerStrList.size())));
        }
        if (!isContainNumChar) {
            builder.append(numStrList.get(r.nextInt(numStrList.size())));
        }
        while (builder.length() < len) {
            builder.append(str[r.nextInt(str.length)]);
        }
        return builder.toString();
    }
}
