package action;

import bean.Field;
import bean.Table;
import util.MatherUtil;
import util.PrintUtil;
import util.StringUtil;

import java.util.Map;
import java.util.regex.Matcher;

/**
 * @ClassName TableAction
 * @Description 对表进行操作
 * @Author 任耀
 * @Date 2019/9/14 21:23
 */
public class TableAction {
    /**
     * 创建一张表
     * @param matcherCreateTable
     */
    public static void createTable(Matcher matcherCreateTable) {
        String tableName = MatherUtil.getTableName(matcherCreateTable);
        String propertys = matcherCreateTable.group(2);
        Map<String, Field> fieldMap = StringUtil.parseCreateTable(propertys);
        System.out.println(Table.createTable(tableName, fieldMap));
    }

    // alter table
    public void alterTableAdd(Matcher matcherAlterTable_add) {
        String tableName = MatherUtil.getTableName(matcherAlterTable_add);
        String propertys = matcherAlterTable_add.group(2);
        Map<String, Field> fieldMap = StringUtil.parseCreateTable(propertys);
        Table table = Table.getTable(tableName);
        if (null == table) {
            PrintUtil.printTableNotFound(tableName);
            return;
        }
        System.out.println(table.addDict(fieldMap));

    }
    // drop table
    public void dropTable(Matcher matcherDropTable) {
        String tableName = MatherUtil.getTableName(matcherDropTable);
        System.out.println(Table.dropTable(tableName));
    }

    // create table

}
