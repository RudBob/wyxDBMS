package action;

import bean.*;
import util.MatherUtil;
import util.PatternModelStr;
import util.PrintUtil;
import util.StringUtil;

import java.util.*;
import java.util.regex.Matcher;


/**
 * @ClassName DataAction
 * @Description 对表中数据进行操作的类
 * @Author 任耀
 * @Date 2019/9/14 21:24
 * @Version 1.0
 */
public class DataAction {
    /**
     * 用户的select操作
     *
     * @param matcherSelect 已经匹配上select子句的模式
     */
    public void select(Matcher matcherSelect) {
        // 将读到的所有数据放到tableDatasMap中
        Map<String, List<Map<String, String>>> tableDatasMap = new LinkedHashMap<>();

        // 将投影放在Map<String,List<String>> projectionMap中
        Map<String, List<String>> projectionMap = new LinkedHashMap<>();

        // where 子句的条件
        String whereStr = MatherUtil.getWhereStr(matcherSelect);

        //将tableName和table.fieldMap放入
        Map<String, Map<String, Field>> fieldMaps = new HashMap<>(16);

        if (getTablesDatas(matcherSelect, tableDatasMap, projectionMap, whereStr, fieldMaps)) {
            return;
        }


        //解析连接条件，并创建连接对象 join
        List<Map<String, String>> joinConditionMapList = StringUtil.parseWhere_join(whereStr, fieldMaps);
        List<JoinCondition> joinConditionList = new LinkedList<>();

        for (Map<String, String> joinMap : joinConditionMapList) {
            joinTableWithField(projectionMap, fieldMaps, joinConditionList, joinMap);
        }

        //将需要显示的字段名按table.filed的型式存入dataNameList
        List<String> dataNameList = new LinkedList<>();
        for (Map.Entry<String, List<String>> projectionEntry : projectionMap.entrySet()) {
            String projectionKey = projectionEntry.getKey();
            List<String> projectionValues = projectionEntry.getValue();
            for (String projectionValue : projectionValues) {
                dataNameList.add(projectionKey + "." + projectionValue);
            }

        }
        List<Map<String, String>> resultData = Join.joinData(tableDatasMap, joinConditionList, projectionMap);

        showResult(resultData, dataNameList);

    }


    private boolean getTablesDatas(Matcher matcherSelect, Map<String, List<Map<String, String>>> tableDatasMap, Map<String, List<String>> projectionMap, String whereStr, Map<String, Map<String, Field>> fieldMaps) {
        List<String> tableNames = StringUtil.parseFrom(matcherSelect.group(2));
        for (String tableName : tableNames) {
            Table table = Table.getTable(tableName);
            if (null == table) {
                PrintUtil.printTableNotFound(tableName);
                return true;
            }
            Map<String, Field> fieldMap = table.getFieldMap();
            fieldMaps.put(tableName, fieldMap);

            //解析选择
            List<SingleFilter> singleFilters = new ArrayList<>();
            List<Map<String, String>> filtList = StringUtil.parseWhere(whereStr, tableName, fieldMap);
            for (Map<String, String> filtMap : filtList) {
                SingleFilter singleFilter = new SingleFilter(fieldMap.get(filtMap.get("fieldName"))
                        , filtMap.get("relationshipName"), filtMap.get("condition"));

                singleFilters.add(singleFilter);
            }

            //解析最终投影
            List<String> projections = StringUtil.parseProjection(matcherSelect.group(1), tableName, fieldMap);
            projectionMap.put(tableName, projections);


            //读取数据并进行选择操作
            List<Map<String, String>> srcDatas = table.read(singleFilters);
            List<Map<String, String>> datas = associatedTableName(tableName, srcDatas);

            tableDatasMap.put(tableName, datas);
        }
        return false;
    }

    /**
     * 将数据整理成tableName.fieldName dataValue的型式
     *
     * @param tableName 表名
     * @param srcDatas  原数据
     * @return 添加表名后的数据
     */
    private List<Map<String, String>> associatedTableName(String tableName, List<Map<String, String>> srcDatas) {
        List<Map<String, String>> destDatas = new ArrayList<>();
        for (Map<String, String> srcData : srcDatas) {
            Map<String, String> destData = new LinkedHashMap<>();
            for (Map.Entry<String, String> data : srcData.entrySet()) {
                destData.put(tableName + "." + data.getKey(), data.getValue());
            }
            destDatas.add(destData);
        }
        return destDatas;
    }

    private void joinTableWithField(Map<String, List<String>> projectionMap, Map<String, Map<String, Field>> fieldMaps, List<JoinCondition> joinConditionList, Map<String, String> joinMap) {
        String tableName1 = joinMap.get("tableName1");
        String tableName2 = joinMap.get("tableName2");
        String fieldName1 = joinMap.get("field1");
        String fieldName2 = joinMap.get("field2");
        Field field1 = fieldMaps.get(tableName1).get(fieldName1);
        Field field2 = fieldMaps.get(tableName2).get(fieldName2);
        String relationshipName = joinMap.get("relationshipName");
        JoinCondition joinCondition = new JoinCondition(tableName1, tableName2, field1, field2, relationshipName);

        joinConditionList.add(joinCondition);

        //将连接条件的字段加入投影中
        projectionMap.get(tableName1).add(fieldName1);
        projectionMap.get(tableName2).add(fieldName2);
    }

    private void showResult(List<Map<String, String>> resultDatas, List<String> dataNameList) {
        //计算名字长度，用来对齐数据
        int[] nameLens = new int[dataNameList.size()];
        Iterator<String> dataNames = dataNameList.iterator();
        for (int i = 0; i < dataNameList.size(); i++) {
            String dataName = dataNames.next();
            nameLens[i] = dataName.length();
            System.out.printf("|%s", dataName);
        }

        System.out.println("|");
        for (int ls : nameLens) {
            for (int l = 0; l <= ls; l++) {
                System.out.print("-");
            }
        }
        System.out.println("|");

        for (Map<String, String> line : resultDatas) {
            Iterator<String> valueIter = line.values().iterator();
            for (int i = 0; i < nameLens.length; i++) {
                String next = valueIter.next();
                System.out.print("|" + next);
                for (int j = 0; j < nameLens[i] - next.length(); j++) {
                    System.out.print(" ");
                }
            }
            System.out.println("|");
        }
    }

    /**
     * insert数据
     *
     * @param matcherInsert 匹配insert语句
     */
    public void insert(Matcher matcherInsert) {
        String tableName = MatherUtil.getTableName(matcherInsert);
        Table table = Table.getTable(tableName);
        if (null == table) {
            PrintUtil.printTableNotFound(tableName);
            return;
        }
        Map<String, Field> dictMap = table.getFieldMap();
        Map<String, String> data = new HashMap<>();

        String[] fieldValues = matcherInsert.group(5).trim().split(",");
        // 如果插入指定的字段
        if (null != matcherInsert.group(2)) {
            if (insertSomeParams(matcherInsert, dictMap, data, fieldValues)) {
                return;
            }
        } else {//否则插入全部字段
            insertAllData(dictMap, data, fieldValues);
        }
        table.insert(data);
    }

    private boolean insertSomeParams(Matcher matcherInsert, Map<String, Field> dictMap, Map<String, String> data, String[] fieldValues) {
        String[] fieldNames = MatherUtil.getWhereStr(matcherInsert).trim().split(",");
        //如果insert的名值数量不相等，错误
        if (fieldNames.length != fieldValues.length) {
            return true;
        }
        for (int i = 0; i < fieldNames.length; i++) {
            String fieldName = fieldNames[i].trim();
            String fieldValue = fieldValues[i].trim();
            //如果在数据字典中未发现这个字段，返回错误
            if (!dictMap.containsKey(fieldName)) {
                return true;
            }
            data.put(fieldName, fieldValue);
        }
        return false;
    }

    private void insertAllData(Map<String, Field> dictMap, Map<String, String> data, String[] fieldValues) {
        Set<String> fieldNames = dictMap.keySet();
        int i = 0;
        for (String fieldName : fieldNames) {
            String fieldValue = fieldValues[i].trim();
            data.put(fieldName, fieldValue);
            i++;
        }
    }

    // update
    public void update(Matcher matcherUpdate) {
        String tableName = MatherUtil.getTableName(matcherUpdate);
        String setStr = MatherUtil.getSetStr(matcherUpdate);
        String whereStr = MatherUtil.getWhereStr(matcherUpdate);

        Table table = Table.getTable(tableName);
        if (null == table) {
            PrintUtil.printTableNotFound(tableName);

            return;
        }
        Map<String, Field> fieldMap = table.getFieldMap();
        Map<String, String> data = StringUtil.parseUpdateSet(setStr);


        List<SingleFilter> singleFilters = new ArrayList<>();
        if (null == whereStr) {
            table.update(data, singleFilters);
        } else {
            updateDataByWhere(whereStr, table, fieldMap, data, singleFilters);
        }
    }

    /**
     * 通过where子句的内容更新数据库中的数据
     *
     * @param whereStr      where子句
     * @param table         目标表
     * @param fieldMap
     * @param data
     * @param singleFilters
     */
    private void updateDataByWhere(String whereStr, Table table, Map<String, Field> fieldMap, Map<String, String> data, List<SingleFilter> singleFilters) {
        List<Map<String, String>> filtList = StringUtil.parseWhere(whereStr);
        for (Map<String, String> filtMap : filtList) {
            SingleFilter singleFilter = new SingleFilter(fieldMap.get(filtMap.get("fieldName"))
                    , filtMap.get("relationshipName"), filtMap.get("condition"));

            singleFilters.add(singleFilter);
        }
        table.update(data, singleFilters);
    }

    // delete

    public void delete(Matcher matcherDelete) {
        String tableName = MatherUtil.getTableName(matcherDelete);
        String whereStr = MatherUtil.getWhereStrDelete(matcherDelete);
        Table table = Table.getTable(tableName);
        if (null == table) {
            PrintUtil.printTableNotFound(tableName);
            return;
        }

        Map<String, Field> fieldMap = table.getFieldMap();

        List<SingleFilter> singleFilters = new ArrayList<>();
        if (null == whereStr) {
            table.delete(singleFilters);
        } else {
            deleteByWhere(whereStr, table, fieldMap, singleFilters);
        }
    }

    /**
     * 通过where子句删除数据
     *
     * @param whereStr      条件子句
     * @param table         目标表
     * @param fieldMap
     * @param singleFilters
     */
    private void deleteByWhere(String whereStr, Table table, Map<String, Field> fieldMap, List<SingleFilter> singleFilters) {
        List<Map<String, String>> filtList = StringUtil.parseWhere(whereStr);
        for (Map<String, String> filtMap : filtList) {
            SingleFilter singleFilter = new SingleFilter(fieldMap.get(filtMap.get("fieldName"))
                    , filtMap.get("relationshipName"), filtMap.get("condition"));

            singleFilters.add(singleFilter);
        }
        table.delete(singleFilters);
    }


    public void deleteIndex(Matcher matcherDeleteIndex) {

        String tableName = MatherUtil.getTableName(matcherDeleteIndex);
        Table table = Table.getTable(tableName);
        if (table == null) {
            System.err.println(tableName + "不存在");
        } else {
            System.out.println(table.deleteIndex());
        }
    }

}
