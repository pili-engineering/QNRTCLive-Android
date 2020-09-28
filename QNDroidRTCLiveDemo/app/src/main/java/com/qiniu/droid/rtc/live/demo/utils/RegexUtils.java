package com.qiniu.droid.rtc.live.demo.utils;

import java.util.regex.Pattern;

public class RegexUtils {

    private static final String REGEX_MOBILE_EXACT = "^((13[0-9])|(14[57])|(15[0-35-9])|(16[2567])|(17[01235-8])|(18[0-9])|(19[189]))\\d{8}$";

    public static boolean isMobilePhoneNumber(final CharSequence input) {
        return isMatch(REGEX_MOBILE_EXACT, input);
    }

    public static boolean isMatch(final String regex, final CharSequence input) {
        return input != null && input.length() > 0 && Pattern.matches(regex, input);
    }

}
