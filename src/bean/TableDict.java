package bean;

import java.io.*;
import java.util.Map;
import java.util.Set;

public class TableDict {
    private File dictFile;

    private Table table;

    public TableDict(File folder, String name, Table table) {
        this.dictFile = new File(folder, name + ".dict");
        this.table = table;
    }

    /**
     * 通过表名来读取表结构
     */
    public void readTableDictFile() {
        try (
                BufferedReader br = new BufferedReader(new FileReader(getDictFile()))
        ) {
            String line;
            // 读到末尾是NULL
            while (null != (line = br.readLine())) {
                putFieldIntoTable(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将文件中的属性放入到Table中
     *
     * @param line 文件中读取的数据
     */
    private void putFieldIntoTable(String line) {
        // 用空格产拆分字段
        String[] fieldValues = line.split(" ");
        Field field = new Field();
        field.setName(fieldValues[0]).setType(fieldValues[1]);
        // 如果长度为3说明此字段是主键
        if (fieldValues.length >= 3 && "*".equals(fieldValues[2])) {
            field.setPrimaryKey(true);
        } else {
            field.setPrimaryKey(false);
        }
        //将字段的名字作为key
        table.getFieldMap().put(field.getName(), field);
    }

    /**
     * 在字典文件中写入创建的字段信息,然后将新增的字段map追加到this.fieldMap
     *
     * @param fields 字段列表，其中map的name为列名，type为数据类型，primaryKey为是否作为主键
     * @return
     */
    public String addDict(Map<String, Field> fields) {
        Set<String> keys = fields.keySet();
        for (String key : keys) {
            if (table.getFieldMap().containsKey(key)) {
                return "错误：存在重复添加的字段:" + key;
            }
        }
        writeDict(fields, true);

        table.getFieldMap().putAll(fields);

        return "success";
    }

    /**
     * 在数据文件没有此字段的数据的前提下，可以删除此字段
     *
     * @param fieldName 字段名
     * @return
     */
    public String deleteDict(String fieldName) {
        Map<String, Field> filedMap = table.getFieldMap();
        if (!filedMap.containsKey(fieldName)) {
            return "错误：不存字段：" + fieldName;
        }
        filedMap.remove(fieldName);
        writeDict(filedMap, false);

        return "success";
    }

    /**
     * 提供一组字段写入文件
     *
     * @param fields 字段映射集
     * @param append 是否在文件结尾追加
     */
    private void writeDict(Map<String, Field> fields, boolean append) {
        try (
                FileWriter fw = new FileWriter(dictFile, append);
                PrintWriter pw = new PrintWriter(fw)
        ) {
            for (Field field : fields.values()) {
                String name = field.getName();
                String type = field.getType();
                //如果是主键字段后面加*
                if (field.isPrimaryKey()) {
                    pw.println(name + " " + type + " " + "*");
                } else {
                    //非主键
                    pw.println(name + " " + type + " " + "^");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getDictFile() {
        return dictFile;
    }

    public void setDictFile(File dictFile) {
        this.dictFile = dictFile;
    }
}
