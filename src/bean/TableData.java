package bean;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class TableData {

    private LinkedHashSet<File> dataFileSet;

    /**
     * 字段映射集
     */
    private Map<String, Field> fieldMap;

    private File folder;

    private Table table;

    public TableData(File folder, Table table) {
        this.fieldMap = new LinkedHashMap<>();
        this.dataFileSet = new LinkedHashSet<>();
        this.folder = folder;
        this.table = table;
    }

    /**
     * 在插入时，对语法进行检查，并对空位填充[NULL]
     *
     * @param srcData 未处理的原始数据
     * @return
     */
    public String insertData(File file, Map<String, String> srcData) {
        if (srcData.size() > getFieldMap().size() || 0 == srcData.size()) {
            return "错误：插入数据失败，请检查语法";
        }

        //遍历数据字典,查看主键是否为空
        String fieldKey = queryPriIdxIsNull(srcData);
        if (fieldKey != null) {
            return fieldKey;
        }
        Map<String, String> insertData = fillData(srcData, Table.NULL_DB);
        if (!checkType(insertData)) {
            return "错误：检查插入的类型";
        }

        file.getParentFile().mkdirs();
        try (
                FileWriter fw = new FileWriter(file, true);
                PrintWriter pw = new PrintWriter(fw)
        ) {
            StringBuilder line = new StringBuilder();
            for (String value : insertData.values()) {
                line.append(value).append(" ");
            }
            pw.println(line.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return "写入异常";
        }

        table.getTableIndex().buildIndex();
        table.getTableIndex().writeIndex();
        return "success";
    }

    /**
     * 利用正则表达式判断data类型是否与数据字典相符
     *
     * @param data
     * @return
     */
    private boolean checkType(Map<String, String> data) {
        //如果长度不一致，返回false
        if (data.size() != getFieldMap().size()) {
            return false;
        }

        //遍历data.value和field.type,逐个对比类型

        for (Field field : getFieldMap().values()) {
            String dataValue = data.get(field.getName());
            //如果是[NULL]则跳过类型检查
            if (Table.NULL_DB.equals(dataValue)) {
                continue;
            }

            switch (field.getType()) {
                case "int":
                    if (!dataValue.matches("^(-|\\+)?\\d+$")) {
                        return false;
                    }
                    break;
                case "double":
                    if (!dataValue.matches("^(-|\\+)?\\d*\\.?\\d+$")) {
                        return false;
                    }
                    break;
                case "varchar":
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    /**
     * 对空位填充fillStr,填充后的字段按照数据字段顺序排序
     *
     * @param fillStr 要填充的字符串
     * @param data    原始数据
     * @return 填充后的数据
     */
    private Map<String, String> fillData(Map<String, String> data, String fillStr) {
        //fillData是真正写入文件的集合，空位补fillStr;
        Map<String, String> fillData = new LinkedHashMap<>();
        //遍历数据字典
        for (Map.Entry<String, Field> fieldEntry : getFieldMap().entrySet()) {
            String fieldKey = fieldEntry.getKey();
            if (null == data.get(fieldKey)) {
                fillData.put(fieldKey, fillStr);
            } else {
                fillData.put(fieldKey, data.get(fieldKey));
            }
        }
        return fillData;
    }

    private String queryPriIdxIsNull(Map<String, String> srcData) {
        for (Map.Entry<String, Field> fieldEntry : getFieldMap().entrySet()) {
            String fieldKey = fieldEntry.getKey();
            Field field = fieldEntry.getValue();
            //如果此字段是主键,不可以为null
            if (field.isPrimaryKey()) {
                if (null == srcData.get(fieldKey) || Table.NULL_DB.equals(srcData.get(fieldKey))) {
                    return "错误：字段:" + fieldKey + "是主键，不能为空";
                }
            }
        }
        return null;
    }

    public LinkedHashSet<File> getDataFileSet() {
        return dataFileSet;
    }

    public void setDataFileSet(LinkedHashSet<File> dataFileSet) {
        this.dataFileSet = dataFileSet;
    }

    public Map<String, Field> getFieldMap() {
        return fieldMap;
    }

    public void setFieldMap(Map<String, Field> fieldMap) {
        this.fieldMap = fieldMap;
    }
}
