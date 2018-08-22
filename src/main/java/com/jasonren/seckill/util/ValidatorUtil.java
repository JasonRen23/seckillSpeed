package com.jasonren.seckill.util;

import org.thymeleaf.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidatorUtil {
    private static Pattern mobile_pattern = Pattern.compile("1\\d{10}");

    public static boolean isMoboile(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }

        Matcher m = mobile_pattern.matcher(str);
        return m.matches();
    }

    public static void main(String[] args) {
        System.out.println(isMoboile("18912341234"));
        System.out.println(isMoboile("189123123"));
    }
}
