package com.tc.dynastqin.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tantao on 14-11-28.
 */
public class GenerateUtils {
    /**
     * 转换成驼峰
     * 例如：hello_word->helloWorld
     *
     * @param source
     * @return
     */
    public static String convertCamel(String source) {

        StringBuilder result = new StringBuilder();
        if (source == null || source.isEmpty()) return "";

        if (!source.contains("_")) {
            return source.substring(0, 1).toLowerCase() + source.substring(1);
        }
        String[] columns = source.split("_");
        for (String columnSplit : columns) {
            if (columnSplit.isEmpty()) {
                continue;
            }
            if (result.length() == 0) {
                result.append(columnSplit.toLowerCase());
            } else {
                result.append(columnSplit.substring(0, 1).toUpperCase()).append(columnSplit.substring(1).toLowerCase());
            }
        }
        return result.toString();

    }

    /**
     * 给字符串末尾增加斜线，用于拼接文件路径
     * @param source
     * @return
     */
    public static String appendBias(String source){
        if(source.endsWith("/"))
            return source;
        else
            return source+"/";

    }

    public static void main(String[] args) {
        String a="abc";
        System.out.println(String.format(a,"#"));

    }
}
