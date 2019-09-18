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

    public static String getWhereStrNotDelete(Matcher matcherSelect) {
        return matcherSelect.group(3);
    }

    public static String getWhereStrDelete(Matcher matcherSelect) {
        return matcherSelect.group(2);
    }

    public static String getSetStr(Matcher matcherUpdate) {
        return matcherUpdate.group(2);
    }

    public static String getGroupByIdx(Matcher matcherSelect, int i) {
        return matcherSelect.group(i);
    }
}
