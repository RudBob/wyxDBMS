package util;

import bean.Field;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ProjectionUtil {

    private static final String STAR = "*";

    /**
     * 解析投影
     *
     * @param str       解析字符串
     * @param tableName 表名
     * @param fieldMap  此表字段
     * @return 投影的字段集
     */
    public static List<String> parseProjection(String str, String tableName, Map<String, Field> fieldMap) {
        List<String> projectionList = new LinkedList<>();
        //如果是 全匹配符号‘*’ 那么投影所有字段
        // 例： SELECT * FROM table1 WHERE ...;
        if (STAR.equals(str)) {
            for (String key : fieldMap.keySet()) {
                projectionList.add(key);
            }
        } else {
            // 非 '*',则将每个属性挨个加入到映射列表 projectionList 中.
            String[] projectionNames = str.trim().split(",");
            for (String projectionName : projectionNames) {
                projectionAddParam(tableName, fieldMap, projectionList, projectionName);
            }
        }
        return projectionList;
    }

    /**
     * 向投影列表中加入参数。
     *
     * @param tableName      表名
     * @param fieldMap       文件列表
     * @param projectionList 映射列表
     * @param projectionName 映射名(属性名)
     */
    private static void projectionAddParam(String tableName, Map<String, Field> fieldMap,
                                           List<String> projectionList, String projectionName) {
        projectionName = projectionName.trim();
        // 如果包含table.id这样的型式，将table名进行匹配，如果不匹配则跳过
        if (projectionName.contains(".")) {
            String[] projection = projectionName.split("\\.");
            //如果不匹配就跳过
            if (!tableName.equals(projection[0])) {
                PrintUtil.printTableNotRight(projection[0]);
                return;
            } else {
                // 匹配
                projectionName = projection[1];
            }
        }

        Field field = fieldMap.get(projectionName);
        if (null != field) {
            projectionList.add(projectionName);
        } else {
            // 属性不存在
            PrintUtil.paramNotFound(projectionName);
        }
    }
}
