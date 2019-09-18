package action;

import bean.Table;
import util.MatherUtil;

import java.util.regex.Matcher;

public class TableIndexAction {
    /**
     * 删除索引
     *
     * @param matcherDeleteIndex 已经匹配过deleteIndex的Matcher
     */
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
