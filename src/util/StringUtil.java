package util;

import bean.Field;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
    /**
     * 匹配表的选择关系
     */
    private final static Pattern SINGLE_REL_PATTERN = Pattern.compile("(\\w+(?:\\.\\w+)?)\\s?([<=>])\\s?([^\\s\\;\\.]+)[\\s;]");
    /**
     * 匹配多个表的连接关系
     */
    private final static Pattern JOIN_CONNECTION_REL_PATTERN = Pattern.compile("(\\w+(?:\\.\\w+)?)\\s?([<=>])\\s?(\\w+\\.\\w+)");
    // private final static Pattern updateSetPattern=Pattern.compile("(\\w+)\\s?=\\s?([^\\s\\;]+)")

    public static final String FIELD_NAME = "fieldName";
    public static final String RELATIONSHIP_NAME = "relationshipName";
    public static final String CONDITION = "condition";

    public static final String TABLE_NAME1 = "tableName1";
    public static final String TABLE_NAME2 = "tableName2";
    public static final String FIELD1 = "field1";
    public static final String FIELD2 = "field2";

    public static List<String> parseFrom(String tableNamesStr) {
        String[] tableNames = tableNamesStr.trim().split(",");
        List<String> tableNameList = new ArrayList<>();
        for (String tableName : tableNames) {
            tableNameList.add(tableName.trim());
        }
        return tableNameList;
    }

    /**
     * 解析单表选择
     *
     * @param str
     * @return
     */
    public static List<Map<String, String>> parseWhere(String str) {

        List<Map<String, String>> filtList = new LinkedList<>();
        //修改了正则规则，需要末尾加;或空格才能匹配
        Matcher singleMatcher = SINGLE_REL_PATTERN.matcher(str + ";");
        while (singleMatcher.find()) {
            Map<String, String> filtMap = new LinkedHashMap<>();
            filtMap.put(FIELD_NAME, singleMatcher.group(1));
            filtMap.put(RELATIONSHIP_NAME, singleMatcher.group(2));
            filtMap.put(CONDITION, singleMatcher.group(3));

            filtList.add(filtMap);
        }
        return filtList;
    }

    /**
     * 解析多表选择
     *
     * @param str       解析字符串
     * @param tableName 表名
     * @param fieldMap  表中字段
     * @return
     */
    public static List<Map<String, String>> parseWhere(String str, String tableName, Map<String, Field> fieldMap) {
        List<Map<String, String>> filtList = new LinkedList<>();
        if (null == str) {
            return filtList;
        }
        Matcher singleMatcher = SINGLE_REL_PATTERN.matcher(str);
        while (singleMatcher.find()) {
            String fieldName = singleMatcher.group(1);
            // 如果包含table.id这样的型式，将table名进行匹配，如果不匹配则跳过
            if (fieldName.contains(".")) {
                String[] field = fieldName.split("\\.");
                // 如果不匹配就跳过
                if (tableName.equals(field[0])) {
                    // 与表名匹配
                    fieldName = field[1];
                } else {
                    continue;
                }
            }
            putFilterIntoList(fieldMap, filtList, singleMatcher, fieldName);
        }
        return filtList;
    }


    private static void putFilterIntoList(Map<String, Field> fieldMap, List<Map<String, String>> filtList, Matcher singleMatcher, String fieldName) {
        Field field = fieldMap.get(fieldName);
        if (null != field) {
            Map<String, String> filtMap = new LinkedHashMap<>();
            filtMap.put(FIELD_NAME, fieldName);
            filtMap.put(RELATIONSHIP_NAME, singleMatcher.group(2));
            filtMap.put(CONDITION, singleMatcher.group(3));

            filtList.add(filtMap);
        }
    }


    /**
     * 解析多表连接条件
     *
     * @param where     where语句
     * @param fieldMaps 连接的所有表的字段集合
     * @return joinConditionList, 多表连接条件的列表
     */
    public static List<Map<String, String>> parseWhereJoin(String where, Map<String, Map<String, Field>> fieldMaps) {

        List<Map<String, String>> joinConditionList = new LinkedList<>();

        if (null == where) {
            return joinConditionList;
        }
        Matcher joinMatcher = JOIN_CONNECTION_REL_PATTERN.matcher(where);
        while (joinMatcher.find()) {
            //连接关系
            joinConditionsAdd(joinMatcher, fieldMaps, joinConditionList);
        }
        return joinConditionList;
    }

    /**
     * 如果
     * @param joinMatcher
     * @param fieldMaps
     * @param joinConditionList
     */
    private static void joinConditionsAdd(Matcher joinMatcher,
                                          Map<String, Map<String, Field>> fieldMaps,
                                          List<Map<String, String>> joinConditionList) {
        String leftStr = joinMatcher.group(1);
        String relationshipName = joinMatcher.group(2);
        String rightStr = joinMatcher.group(3);

        String[] leftRel = leftStr.split("\\.");
        String[] rightRel = rightStr.split("\\.");

        Map<String, String> connRel = new LinkedHashMap<>();
        if (null != fieldMaps.get(leftRel[0])
                && null != fieldMaps.get(leftRel[0]).get(leftRel[1])
                && null != fieldMaps.get(rightRel[0])
                && null != fieldMaps.get(rightRel[0]).get(rightRel[1])) {

            connRel.put(TABLE_NAME1, leftRel[0]);
            connRel.put(FIELD1, leftRel[1]);
            connRel.put(RELATIONSHIP_NAME, relationshipName);
            connRel.put(TABLE_NAME2, rightRel[0]);
            connRel.put(FIELD2, leftRel[1]);

            joinConditionList.add(connRel);
        }
    }


    /**
     * 解析创建表的字段语句
     *
     * @param fieldsStr
     * @return
     */
    public static Map<String, Field> parseCreateTable(String fieldsStr) {
        String[] lines = fieldsStr.trim().split(",");
        Map<String, Field> fieldMap = new LinkedHashMap<>();
        for (String line : lines) {
            String[] property = line.trim().split(" ");

            Field field = new Field();

            field.setName(property[0]);
            field.setType(property[1]);
            //如果是主键字段后面加*
            if (3 == property.length && "*".equals(property[2])) {
                field.setPrimaryKey(true);
            } else {
                field.setPrimaryKey(false);
            }
            fieldMap.put(property[0], field);
        }

        return fieldMap;
    }

    /**
     * @param str
     * @return
     */
    public static Map<String, String> parseUpdateSet(String str) {
        Map<String, String> dataMap = new LinkedHashMap<>();
        String[] setStrs = str.trim().split(",");
        for (String setStr : setStrs) {
            //修改了正则规则，需要末尾加;或空格才能匹配
            Matcher relMatcher = SINGLE_REL_PATTERN.matcher(setStr + ";");
            relMatcher.find();
            //将组1做为key，组3作为value
            dataMap.put(relMatcher.group(1), relMatcher.group(3));
        }
        return dataMap;
    }
}
