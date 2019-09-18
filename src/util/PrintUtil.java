package util;

/**
 * @ClassName PrintUtil
 * @Description TODO
 * @Author 任耀
 * @Date 2019/9/14 21:26
 * @Version 1.0
 */
public class PrintUtil {
    public static void printTableNotFound(String tableName) {
        System.err.println("未找到表：" + tableName);
    }

    public static void printTableNotRight(String tableName) {
        System.err.println("表不对应 : " + tableName);
    }

    public static void paramNotFound(String projectionName) {
        System.err.println("属性不存在：" + projectionName);
    }
}
