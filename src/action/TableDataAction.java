package action;

import bean.*;
import util.MatherUtil;
import util.PrintUtil;
import util.ProjectionUtil;
import util.StringUtil;

import java.util.*;
import java.util.regex.Matcher;


/**
 * @ClassName TableDataAction
 * @Description 对表中数据进行操作的类
 * @Author 任耀
 * @Date 2019/9/14 21:24
 * @Version 1.0
 */
public class TableDataAction {
    /**
     * 用户的select操作
     *
     * @param matcherSelect 已经匹配上select子句的模式
     */
    public void select(Matcher matcherSelect) {
        List<String> tableNames = StringUtil.parseFrom(MatherUtil.getGroupByIdx(matcherSelect, 2));

        if (tableNotExist(tableNames)) {
            return;
        }
        //将 tableName 和 table.fieldMap 放入
        Map<String, Map<String, Field>> tableFiledMap = getTableFileMap(tableNames);

        Map<String, List<String>> projectionMap = getProjection(matcherSelect);
        // 通过用户输入的命令，得到命令中出现的table对应的文件的对应的数据
        // 解析连接条件，并创建连接对象 join
        List<Map<String, String>> joinConditionMapList = StringUtil.parseWhereJoin(
                MatherUtil.getWhereStrNotDelete(matcherSelect), tableFiledMap);
        List<JoinCondition> joinConditionList = getJoinConditions(tableFiledMap, projectionMap, joinConditionMapList);

        // 将需要显示的字段名按 table.filed 的型式存入 dataNameList

        List<Map<String, String>> resultData = Join.joinData(getTablesDatas(matcherSelect), joinConditionList, projectionMap);

        List<String> dataNameList = projectionToDataName(projectionMap);
        showResult(resultData, dataNameList);

    }

    private List<JoinCondition> getJoinConditions(Map<String, Map<String, Field>> tableFiledMap, Map<String, List<String>> projectionMap, List<Map<String, String>> joinConditionMapList) {
        List<JoinCondition> joinConditionList = new LinkedList<>();
        // 通过连接条件将数据链接起来.
        for (Map<String, String> joinMap : joinConditionMapList) {
            joinTableWithField(projectionMap, tableFiledMap, joinConditionList, joinMap);
        }
        return joinConditionList;
    }

    private Map<String, Map<String, Field>> getTableFileMap(List<String> tableNames) {
        Map<String, Map<String, Field>> fieldMaps = new HashMap<>(16);
        for (String tableName : tableNames) {
            Table table = Table.getTable(tableName);
            fieldMaps.put(table.getName(), table.getFieldMap());
        }
        return fieldMaps;
    }

    private Map<String, List<String>> getProjection(Matcher matcherSelect) {
        Map<String, List<String>> projectionMap = new LinkedHashMap<>();
        List<String> tableNames = StringUtil.parseFrom(MatherUtil.getGroupByIdx(matcherSelect, 2));
        for (String tableName : tableNames) {
            Table table = Table.getTable(tableName);
            //解析最终投影
            List<String> projections = ProjectionUtil.parseProjection(
                    MatherUtil.getGroupByIdx(matcherSelect, 1), tableName, table.getFieldMap());
            // 将表名与投影相关联
            projectionMap.put(tableName, projections);
        }

        return projectionMap;
    }

    /**
     * 将投影中的数据名取出.
     *
     * @param projectionMap 投影表
     * @return 属性名.
     */
    private List<String> projectionToDataName(Map<String, List<String>> projectionMap) {
        List<String> dataNameList = new LinkedList<>();
        for (Map.Entry<String, List<String>> projectionEntry : projectionMap.entrySet()) {
            String projectionKey = projectionEntry.getKey();
            List<String> projectionValues = projectionEntry.getValue();

            for (String projectionValue : projectionValues) {
                dataNameList.add(projectionKey + "." + projectionValue);
            }
        }
        return dataNameList;
    }

    private boolean tableNotExist(List<String> tableNames) {
        for (String tableName : tableNames) {
            Table table = Table.getTable(tableName);
            if (null == table) {
                PrintUtil.printTableNotFound(tableName);
                return true;
            }
        }
        return false;
    }

    /**
     * 通过用户输入的命令，得到文件中对应的数据
     *
     * @param matcherSelect 匹配过select子句的模式
     * @return
     */
    private Map<String, List<Map<String, String>>> getTablesDatas(Matcher matcherSelect) {
        Map<String, List<Map<String, String>>> tableDatasMap = new LinkedHashMap<>();
        // 得到限制条件: where 子句和对应表明tableName
        String whereStr = MatherUtil.getWhereStrNotDelete(matcherSelect);
        List<String> tableNames = StringUtil.parseFrom(MatherUtil.getGroupByIdx(matcherSelect, 2));
        // 遍历表名称
        for (String tableName : tableNames) {
            Table table = Table.getTable(tableName);
            // 解析多表选择,得到过滤器
            List<SingleFilter> singleFilters = getSingleFilterByWhereWithTable(table.getFieldMap(), whereStr, tableName);
            //读取数据并进行选择操作
            List<Map<String, String>> datas = associatedTableName(tableName, table.read(singleFilters));
            tableDatasMap.put(tableName, datas);
        }
        return tableDatasMap;
    }

    private List<SingleFilter> getSingleFilterByWhereWithTable(Map<String, Field> fieldMap, String whereStr, String tableName) {
        List<Map<String, String>> filtList = StringUtil.parseWhere(whereStr, tableName, fieldMap);
        List<SingleFilter> singleFilters = new ArrayList<>(filtList.size());

        for (Map<String, String> filtMap : filtList) {
            SingleFilter singleFilter = new SingleFilter(
                    fieldMap.get(filtMap.get(StringUtil.FIELD_NAME)),
                    filtMap.get(StringUtil.RELATIONSHIP_NAME),
                    filtMap.get(StringUtil.CONDITION));

            singleFilters.add(singleFilter);
        }
        return singleFilters;
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

    /**
     * 将域与数据表关联起来
     *
     * @param projectionMap     投影表
     * @param fieldMaps         域表
     * @param joinConditionList 连接条件
     * @param joinMap           链接
     */
    private void joinTableWithField(Map<String, List<String>> projectionMap,
                                    Map<String, Map<String, Field>> fieldMaps,
                                    List<JoinCondition> joinConditionList, Map<String, String> joinMap) {
        String tableName1 = joinMap.get(StringUtil.TABLE_NAME1);
        String tableName2 = joinMap.get(StringUtil.TABLE_NAME2);
        String fieldName1 = joinMap.get(StringUtil.FIELD1);
        String fieldName2 = joinMap.get(StringUtil.FIELD2);

        Field field1 = fieldMaps.get(tableName1).get(fieldName1);
        Field field2 = fieldMaps.get(tableName2).get(fieldName2);
        String relationshipName = joinMap.get(StringUtil.RELATIONSHIP_NAME);
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
            for (int nameLen : nameLens) {
                String next = valueIter.next();
                System.out.print("|" + next);
                for (int j = 0; j < nameLen - next.length(); j++) {
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
        Map<String, String> data = new HashMap<>(16);

        String[] fieldValues = MatherUtil.getGroupByIdx(matcherInsert, 5).trim().split(",");
        // 如果插入指定的字段
        if (null != MatherUtil.getGroupByIdx(matcherInsert, 2)) {
            if (insertSomeParams(matcherInsert, dictMap, data, fieldValues)) {
                return;
            }
        } else {//否则插入全部字段
            insertAllData(dictMap, data, fieldValues);
        }
        table.insert(data);
    }

    private boolean insertSomeParams(Matcher matcherInsert, Map<String, Field> dictMap, Map<String, String> data, String[] fieldValues) {
        String[] fieldNames = MatherUtil.getWhereStrNotDelete(matcherInsert).trim().split(",");
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

    /**
     * 所有数据都插入
     *
     * @param dictMap
     * @param data        待插入的数据
     * @param fieldValues
     */
    private void insertAllData(Map<String, Field> dictMap, Map<String, String> data, String[] fieldValues) {
        Set<String> fieldNames = dictMap.keySet();
        int i = 0;
        for (String fieldName : fieldNames) {
            String fieldValue = fieldValues[i++].trim();
            data.put(fieldName, fieldValue);
        }
    }

    /**
     * 更新操作
     *
     * @param matcherUpdate 已匹配过update的模式
     */
    public void update(Matcher matcherUpdate) {
        String whereStr = MatherUtil.getWhereStrNotDelete(matcherUpdate);

        Table table = Table.getTable(MatherUtil.getTableName(matcherUpdate));
        if (null == table) {
            PrintUtil.printTableNotFound(MatherUtil.getTableName(matcherUpdate));
            return;
        }
        Map<String, Field> fieldMap = table.getFieldMap();
        Map<String, String> data = StringUtil.parseUpdateSet(MatherUtil.getSetStr(matcherUpdate));

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
     * @param fieldMap      文件映射
     * @param data          数据集
     * @param singleFilters 过滤器
     */
    private void updateDataByWhere(String whereStr, Table table,
                                   Map<String, Field> fieldMap, Map<String, String> data,
                                   List<SingleFilter> singleFilters) {
        List<Map<String, String>> filterList = StringUtil.parseWhere(whereStr);
        for (Map<String, String> filterMap : filterList) {
            SingleFilter singleFilter = getSingleFilter(fieldMap, filterMap);

            singleFilters.add(singleFilter);
        }
        table.update(data, singleFilters);
    }

    private SingleFilter getSingleFilter(Map<String, Field> fieldMap, Map<String, String> filterMap) {
        return new SingleFilter(fieldMap.get(filterMap.get(StringUtil.FIELD_NAME))
                , filterMap.get(StringUtil.RELATIONSHIP_NAME), filterMap.get(StringUtil.CONDITION));
    }


    /**
     * 删除数据
     *
     * @param matcherDelete 已经匹配过delete子句的Matcher
     */
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
     * @param fieldMap      文件映射
     * @param singleFilters 过滤器
     */
    private void deleteByWhere(String whereStr, Table table, Map<String, Field> fieldMap,
                               List<SingleFilter> singleFilters) {
        List<Map<String, String>> filtList = StringUtil.parseWhere(whereStr);
        for (Map<String, String> filtMap : filtList) {
            SingleFilter singleFilter = getSingleFilter(fieldMap, filtMap);
            singleFilters.add(singleFilter);
        }
        table.delete(singleFilters);
    }


}
