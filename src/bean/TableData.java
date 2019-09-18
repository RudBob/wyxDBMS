package bean;

import java.io.File;
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

    public TableData(File folder, String name) {
        this.fieldMap = new LinkedHashMap<>();
        this.dataFileSet = new LinkedHashSet<>();
        this.folder = folder;
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
