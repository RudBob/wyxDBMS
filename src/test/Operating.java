package test;

import bean.*;
import exception.LoginFailException;
import util.MatherUtil;
import util.StringUtil;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Operating {
    private static final Pattern PATTERN_INSERT = Pattern.compile("insert\\s+into\\s+(\\w+)(\\(((\\w+,?)+)\\))?\\s+\\w+\\((([^\\)]+,?)+)\\);?");
    private static final Pattern PATTERN_CREATE_TABLE = Pattern.compile("create\\stable\\s(\\w+)\\s?\\(((?:\\s?\\w+\\s\\w+,?)+)\\)\\s?;");
    private static final Pattern PATTERN_ALTER_TABLE_ADD = Pattern.compile("alter\\stable\\s(\\w+)\\sadd\\s(\\w+\\s\\w+)\\s?;");
    private static final Pattern PATTERN_DELETE = Pattern.compile("delete\\sfrom\\s(\\w+)(?:\\swhere\\s(\\w+\\s?[<=>]\\s?[^\\s\\;]+(?:\\sand\\s(?:\\w+)\\s?(?:[<=>])\\s?(?:[^\\s\\;]+))*))?\\s?;");
    private static final Pattern PATTERN_UPDATE = Pattern.compile("update\\s(\\w+)\\sset\\s(\\w+\\s?=\\s?[^,\\s]+(?:\\s?,\\s?\\w+\\s?=\\s?[^,\\s]+)*)(?:\\swhere\\s(\\w+\\s?[<=>]\\s?[^\\s\\;]+(?:\\sand\\s(?:\\w+)\\s?(?:[<=>])\\s?(?:[^\\s\\;]+))*))?\\s?;");
    private static final Pattern PATTERN_DROP_TABLE = Pattern.compile("drop\\stable\\s(\\w+);");
    private static final Pattern PATTERN_SELECT = Pattern.compile("select\\s(\\*|(?:(?:\\w+(?:\\.\\w+)?)+(?:\\s?,\\s?\\w+(?:\\.\\w+)?)*))\\sfrom\\s(\\w+(?:\\s?,\\s?\\w+)*)(?:\\swhere\\s([^\\;]+\\s?;))?");
    private static final Pattern PATTERN_DELETE_INDEX = Pattern.compile("delete\\sindex\\s(\\w+)\\s?;");
    private static final Pattern PATTERN_GRANT_ADMIN = Pattern.compile("grant\\sadmin\\sto\\s([^;\\s]+)\\s?;");
    private static final Pattern PATTERN_REVOKE_ADMIN = Pattern.compile("revoke\\sadmin\\sfrom\\s([^;\\s]+)\\s?;");


    void dbms() {
        //bean.User user = new bean.User("user1", "abc");
        User user = null;
        try {
            user = login();
        } catch (LoginFailException e) {
            e.printStackTrace();
            return;
        }
        //bean.User.grant(user.getName(), bean.User.READ_ONLY);
        //user.grant(bean.User.READ_ONLY);

        // 进入默认的表中
        user.intoDefaultTable();

        // 开始接收用户的输入，并处理.
        dealCommend(user);

    }

    private void dealCommend(User user) {
        Scanner sc = new Scanner(System.in);
        String cmd;
        while (!"exit".equals(cmd = sc.nextLine())) {
            // 匹配各个命令.
            matchCommend(user, cmd);
        }
    }

    private void matchCommend(User user, String cmd) {
        matchGrantAdmin(user, cmd);

        matcherRevokeAdmin(user, cmd);

        matcherInsert(user, cmd);

        matcherCreateTable(user, cmd);

        matcherAlterTable_add(user, cmd);

        matcherDelete(user, cmd);

        matcherUpdate(user, cmd);

        matcherDropTable(user, cmd);

        matcherSelect(cmd);

        matcherDeleteIndex(user, cmd);
    }

    private void matcherDeleteIndex(User user, String cmd) {
        Matcher matcherDeleteIndex = PATTERN_DELETE_INDEX.matcher(cmd);
        while (matcherDeleteIndex.find()) {
            if (user.getLevel() != User.ADMIN) {
                System.out.println("用户" + user.getName() + "权限不够，无法完成此操作！");
                break;
            }
            deleteIndex(matcherDeleteIndex);
        }
    }

    private void matcherSelect(String cmd) {
        Matcher matcherSelect = PATTERN_SELECT.matcher(cmd);
        while (matcherSelect.find()) {
            select(matcherSelect);
        }
    }

    private void matcherDropTable(User user, String cmd) {
        Matcher matcherDropTable = PATTERN_DROP_TABLE.matcher(cmd);
        while (matcherDropTable.find()) {
            if (user.getLevel() != User.ADMIN) {
                System.out.println("用户" + user.getName() + "权限不够，无法完成此操作！");
                break;
            }
            dropTable(matcherDropTable);
        }
    }

    private void matcherUpdate(User user, String cmd) {
        Matcher matcherUpdate = PATTERN_UPDATE.matcher(cmd);
        while (matcherUpdate.find()) {
            if (user.getLevel() != User.ADMIN) {
                System.out.println("用户" + user.getName() + "权限不够，无法完成此操作！");
                break;
            }
            update(matcherUpdate);
        }
    }

    private void matcherDelete(User user, String cmd) {
        Matcher matcherDelete = PATTERN_DELETE.matcher(cmd);
        while (matcherDelete.find()) {
            if (user.getLevel() != User.ADMIN) {
                System.out.println("用户" + user.getName() + "权限不够，无法完成此操作！");
                break;
            }
            delete(matcherDelete);
        }
    }

    private void matcherAlterTable_add(User user, String cmd) {
        Matcher matcherAlterTable_add = PATTERN_ALTER_TABLE_ADD.matcher(cmd);
        while (matcherAlterTable_add.find()) {
            if (user.getLevel() != User.ADMIN) {
                System.out.println("用户" + user.getName() + "权限不够，无法完成此操作！");
                break;
            }
            alterTableAdd(matcherAlterTable_add);
        }
    }

    private void matcherCreateTable(User user, String cmd) {
        Matcher matcherCreateTable = PATTERN_CREATE_TABLE.matcher(cmd);
        while (matcherCreateTable.find()) {
            if (user.getLevel() != User.ADMIN) {
                System.out.println("用户" + user.getName() + "权限不够，无法完成此操作！");
                break;
            }
            createTable(matcherCreateTable);
        }
    }

    private void matcherInsert(User user, String cmd) {
        Matcher matcherInsert = PATTERN_INSERT.matcher(cmd);
        while (matcherInsert.find()) {
            if (user.getLevel() != User.ADMIN) {
                System.out.println("用户" + user.getName() + "权限不够，无法完成此操作！");
                break;
            }
            insert(matcherInsert);
        }
    }

    private void matcherRevokeAdmin(User user, String cmd) {
        Matcher matcherRevokeAdmin = PATTERN_REVOKE_ADMIN.matcher(cmd);
        while (matcherRevokeAdmin.find()) {
            User revokeUser = User.getUser(matcherRevokeAdmin.group(1));
            if (null == revokeUser) {
                System.out.println("取消授权失败!");
            }
            if (user.getName().equals(revokeUser.getName())) {
                //如果是当前操作的用户，就直接更改当前用户权限
                user.grant(User.READ_ONLY);
                System.out.println("用户:" + user.getName() + "已取消授权！");
            } else {
                revokeUser.grant(User.READ_ONLY);
                System.out.println("用户:" + revokeUser.getName() + "已取消授权！");
            }
        }
    }

    private void matchGrantAdmin(User user, String cmd) {
        Matcher matcherGrantAdmin = PATTERN_GRANT_ADMIN.matcher(cmd);
        while (matcherGrantAdmin.find()) {
            User grantUser = User.getUser(matcherGrantAdmin.group(1));
            if (null == grantUser) {
                System.out.println("授权失败！");
            } else if (user.getName().equals(grantUser.getName())) {
                //如果是当前操作的用户，就直接更改当前用户权限
                user.grant(User.ADMIN);
                System.out.println("用户:" + user.getName() + "授权成功！");
            } else {
                grantUser.grant(User.ADMIN);
                System.out.println("用户:" + grantUser.getName() + "授权成功!");
            }
        }
    }

    private User login() throws LoginFailException {
        User user = User.getUser("user1", "abc");
        if (null == user) {
            throw new LoginFailException();
        } else {
            System.out.println(user.getName() + "登陆成功!");
        }
        return user;
    }

    private void deleteIndex(Matcher matcherDeleteIndex) {

        String tableName = MatherUtil.getTableName(matcherDeleteIndex);
        Table table = Table.getTable(tableName);
        if (table == null) {
            System.err.println(tableName + "不存在");
        } else {
            System.out.println(table.deleteIndex());
        }
    }

    private void select(Matcher matcherSelect) {
        // 将读到的所有数据放到tableDatasMap中
        Map<String, List<Map<String, String>>> tableDatasMap = new LinkedHashMap<>();

        // 将投影放在Map<String,List<String>> projectionMap中
        Map<String, List<String>> projectionMap = new LinkedHashMap<>();

        // 得到表名
        List<String> tableNames = StringUtil.parseFrom(matcherSelect.group(2));

        // where 子句的条件
        String whereStr = matcherSelect.group(3);

        //将tableName和table.fieldMap放入
        Map<String, Map<String, Field>> fieldMaps = new HashMap<>();

        if (getTablesDatas(matcherSelect, tableDatasMap, projectionMap, tableNames, whereStr, fieldMaps)) {
            return;
        }


        //解析连接条件，并创建连接对象jion
        List<Map<String, String>> joinConditionMapList = StringUtil.parseWhere_join(whereStr, fieldMaps);
        List<JoinCondition> joinConditionList = new LinkedList<>();

        for (Map<String, String> joinMap : joinConditionMapList) {
            joinTableWithField(projectionMap, fieldMaps, joinConditionList, joinMap);
        }
        List<Map<String, String>> resultDatas = Join.joinData(tableDatasMap, joinConditionList, projectionMap);
        //System.out.println(resultDatas);

        //将需要显示的字段名按table.filed的型式存入dataNameList
        List<String> dataNameList = new LinkedList<>();
        for (Map.Entry<String, List<String>> projectionEntry : projectionMap.entrySet()) {
            String projectionKey = projectionEntry.getKey();
            List<String> projectionValues = projectionEntry.getValue();
            for (String projectionValue : projectionValues) {
                dataNameList.add(projectionKey + "." + projectionValue);
            }

        }

        showResult(resultDatas, dataNameList);

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

    private boolean getTablesDatas(Matcher matcherSelect, Map<String, List<Map<String, String>>> tableDatasMap, Map<String, List<String>> projectionMap, List<String> tableNames, String whereStr, Map<String, Map<String, Field>> fieldMaps) {
        for (String tableName : tableNames) {
            Table table = Table.getTable(tableName);
            if (null == table) {
                printTableNotFound(tableName);
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

    private void printTableNotFound(String tableName) {
        System.err.println("未找到表：" + tableName);
    }

    private void insert(Matcher matcherInsert) {
        String tableName = MatherUtil.getTableName(matcherInsert);
        Table table = Table.getTable(tableName);
        if (null == table) {
            printTableNotFound(tableName);
            return;
        }
        Map dictMap = table.getFieldMap();
        Map<String, String> data = new HashMap<>();

        String[] fieldValues = matcherInsert.group(5).trim().split(",");
        //如果插入指定的字段
        if (null != matcherInsert.group(2)) {
            String[] fieldNames = matcherInsert.group(3).trim().split(",");
            //如果insert的名值数量不相等，错误
            if (fieldNames.length != fieldValues.length) {
                return;
            }
            for (int i = 0; i < fieldNames.length; i++) {
                String fieldName = fieldNames[i].trim();
                String fieldValue = fieldValues[i].trim();
                //如果在数据字典中未发现这个字段，返回错误
                if (!dictMap.containsKey(fieldName)) {
                    return;
                }
                data.put(fieldName, fieldValue);
            }
        } else {//否则插入全部字段
            Set<String> fieldNames = dictMap.keySet();
            int i = 0;
            for (String fieldName : fieldNames) {
                String fieldValue = fieldValues[i].trim();

                data.put(fieldName, fieldValue);

                i++;
            }
        }
        table.insert(data);
    }

    private void update(Matcher matcherUpdate) {
        String tableName = MatherUtil.getTableName(matcherUpdate);
        String setStr = matcherUpdate.group(2);
        String whereStr = matcherUpdate.group(3);

        Table table = Table.getTable(tableName);
        if (null == table) {
            printTableNotFound(tableName);
            return;
        }
        Map<String, Field> fieldMap = table.getFieldMap();
        Map<String, String> data = StringUtil.parseUpdateSet(setStr);


        List<SingleFilter> singleFilters = new ArrayList<>();
        if (null == whereStr) {
            table.update(data, singleFilters);
        } else {
            List<Map<String, String>> filtList = StringUtil.parseWhere(whereStr);
            for (Map<String, String> filtMap : filtList) {
                SingleFilter singleFilter = new SingleFilter(fieldMap.get(filtMap.get("fieldName"))
                        , filtMap.get("relationshipName"), filtMap.get("condition"));

                singleFilters.add(singleFilter);
            }
            table.update(data, singleFilters);
        }
    }

    private void delete(Matcher matcherDelete) {
        String tableName = MatherUtil.getTableName(matcherDelete);
        String whereStr = matcherDelete.group(2);
        Table table = Table.getTable(tableName);
        if (null == table) {
            printTableNotFound(tableName);
            return;
        }

        Map<String, Field> fieldMap = table.getFieldMap();

        List<SingleFilter> singleFilters = new ArrayList<>();
        if (null == whereStr) {
            table.delete(singleFilters);
        } else {
            List<Map<String, String>> filtList = StringUtil.parseWhere(whereStr);
            for (Map<String, String> filtMap : filtList) {
                SingleFilter singleFilter = new SingleFilter(fieldMap.get(filtMap.get("fieldName"))
                        , filtMap.get("relationshipName"), filtMap.get("condition"));

                singleFilters.add(singleFilter);
            }
            table.delete(singleFilters);
        }
    }

    private void createTable(Matcher matcherCreateTable) {
        String tableName = MatherUtil.getTableName(matcherCreateTable);
        String propertys = matcherCreateTable.group(2);
        Map<String, Field> fieldMap = StringUtil.parseCreateTable(propertys);
        System.out.println(Table.createTable(tableName, fieldMap));
    }

    private void dropTable(Matcher matcherDropTable) {
        String tableName = MatherUtil.getTableName(matcherDropTable);
        System.out.println(Table.dropTable(tableName));
    }

    private void alterTableAdd(Matcher matcherAlterTable_add) {
        String tableName = MatherUtil.getTableName(matcherAlterTable_add);
        String propertys = matcherAlterTable_add.group(2);
        Map<String, Field> fieldMap = StringUtil.parseCreateTable(propertys);
        Table table = Table.getTable(tableName);
        if (null == table) {
            printTableNotFound(tableName);
            return;
        }
        System.out.println(table.addDict(fieldMap));

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

}

