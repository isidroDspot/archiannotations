package pl.com.dspot.archiannotations.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatUtils {

    public static String fieldToGetter(String name) {

        Matcher matcher = Pattern.compile("_(\\w)").matcher(name);
        while (matcher.find()) {
            name = name.replaceFirst(matcher.group(0), matcher.group(1).toUpperCase());
        }

        return "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static String fieldToSetter(String name) {
        Matcher matcher = Pattern.compile("_(\\w)").matcher(name);
        while (matcher.find()) {
            name = name.replaceFirst(matcher.group(0), matcher.group(1).toUpperCase());
        }

        return "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }


}
