package util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName PatternModelStr
 * @Description TODO
 * @Author 任耀
 * @Date 2019/9/14 21:53
 * @Version 1.0
 */
public class PatternModelStr {
    public static final Pattern PATTERN_INSERT = Pattern.compile("insert\\s+into\\s+(\\w+)(\\(((\\w+,?)+)\\))?\\s+\\w+\\((([^\\)]+,?)+)\\);?");
    public static final Pattern PATTERN_CREATE_TABLE = Pattern.compile("create\\stable\\s(\\w+)\\s?\\(((?:\\s?\\w+\\s\\w+,?)+)\\)\\s?;");
    public static final Pattern PATTERN_ALTER_TABLE_ADD = Pattern.compile("alter\\stable\\s(\\w+)\\sadd\\s(\\w+\\s\\w+)\\s?;");
    public static final Pattern PATTERN_DELETE = Pattern.compile("delete\\sfrom\\s(\\w+)(?:\\swhere\\s(\\w+\\s?[<=>]\\s?[^\\s\\;]+(?:\\sand\\s(?:\\w+)\\s?(?:[<=>])\\s?(?:[^\\s\\;]+))*))?\\s?;");
    public static final Pattern PATTERN_UPDATE = Pattern.compile("update\\s(\\w+)\\sset\\s(\\w+\\s?=\\s?[^,\\s]+(?:\\s?,\\s?\\w+\\s?=\\s?[^,\\s]+)*)(?:\\swhere\\s(\\w+\\s?[<=>]\\s?[^\\s\\;]+(?:\\sand\\s(?:\\w+)\\s?(?:[<=>])\\s?(?:[^\\s\\;]+))*))?\\s?;");
    public static final Pattern PATTERN_DROP_TABLE = Pattern.compile("drop\\stable\\s(\\w+);");
    public static final Pattern PATTERN_SELECT = Pattern.compile("select\\s(\\*|(?:(?:\\w+(?:\\.\\w+)?)+(?:\\s?,\\s?\\w+(?:\\.\\w+)?)*))\\sfrom\\s(\\w+(?:\\s?,\\s?\\w+)*)(?:\\swhere\\s([^\\;]+\\s?;))?");
    public static final Pattern PATTERN_DELETE_INDEX = Pattern.compile("delete\\sindex\\s(\\w+)\\s?;");
    public static final Pattern PATTERN_GRANT_ADMIN = Pattern.compile("grant\\sadmin\\sto\\s([^;\\s]+)\\s?;");
    public static final Pattern PATTERN_REVOKE_ADMIN = Pattern.compile("revoke\\sadmin\\sfrom\\s([^;\\s]+)\\s?;");


}
