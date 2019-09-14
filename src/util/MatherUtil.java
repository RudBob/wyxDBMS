package util;

import java.util.regex.Matcher;

/**
 * @ClassName MatherUtil
 * @Description TODO
 * @Author 任耀
 * @Date 2019/9/12 11:27
 * @Version 1.0
 */
public class MatherUtil {
    public static String getTableName(Matcher matcherAlterTable_add) {
        return matcherAlterTable_add.group(1);
    }
}
