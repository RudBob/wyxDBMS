package bean;

import java.io.*;
import java.util.*;

/**
 *
 */
public class TableIndex {
    /**
     * 索引文件
     */
    private File indexFile;
    /**
     * 存放对所有字段的索引树
     */
    private Map<String, IndexTree> indexMap;

    private File folder;

    private Table table;

    public static final String IDEX_FILE_SUFFIX = ".index";

    TableIndex(File folder, String name, Table table) {
        this.indexMap = new HashMap<>();
        this.indexFile = new File(folder, name + IDEX_FILE_SUFFIX);
        this.folder = folder;
        this.table = table;
    }


    void readTableIdx() {
        if (getIndexFile().exists()) {
            readIndex();
        }
    }

    /**
     * 为每个属性建立索引树，如果此属性值为[NULL]索引树将排除此条字段
     */
    public void buildIndex() {
        setIndexMap(new HashMap<>());
        File[] dataFiles = new File(folder, "data").listFiles();
        //每个文件
        for (File dataFile : dataFiles) {
            List<Map<String, String>> datas = table.getTableFile().readDatasAndLineNum(dataFile);
            //每个元组
            for (Map<String, String> data : datas) {
                //每个数据字段
                for (Map.Entry<String, Field> fieldEntry : table.getFieldMap().entrySet()) {
                    buildeFieldIndex(dataFile, data, fieldEntry);
                }
            }
        }

        //重新填充 dataFileSet
        if (0 != dataFiles.length) {
            for (int i = 1; i <= dataFiles.length; i++) {
                File dataFile = new File(folder + Table.DATA_FILE_PREFIX, i + Table.DATA_FILE_SUFFIX);
                table.getDataFileSet().add(dataFile);
            }
        }
    }



    /**
     * @param dataFile
     * @param data
     * @param fieldEntry
     */
    private void buildeFieldIndex(File dataFile, Map<String, String> data, Map.Entry<String, Field> fieldEntry) {
        String dataName = fieldEntry.getKey();
        String dataValue = data.get(dataName);
        //如果发现此数据为空，不添加到索引树中
        if (Table.NULL_DB.equals(dataValue)) {
            return;
        }
        String dataType = fieldEntry.getValue().getType();
        int lineNum = Integer.valueOf(data.get(Table.LINE_NUM));


        IndexTree indexTree = getIndexMap().get(dataName);
        if (null == indexTree) {
            getIndexMap().put(dataName, new IndexTree());
            indexTree = getIndexMap().get(dataName);
        }
        IndexKey indexKey = new IndexKey(dataValue, dataType);
        indexTree.putIndex(indexKey, dataFile.getAbsolutePath(), lineNum);
    }

    /**
     * 将索引对象从索引文件读取
     */
    private void readIndex() {
        if (!getIndexFile().exists()) {
            return;
        }
        try (
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getIndexFile()))
        ) {
            setIndexMap((Map<String, IndexTree>) ois.readObject());
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将索引对象写入索引文件
     */
    void writeIndex() {
        try (
                FileOutputStream fos = new FileOutputStream(getIndexFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(getIndexMap());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除建立的索引
     */
    public String deleteIndex() {
        if (getIndexFile().exists()) {
            getIndexFile().delete();
            return "success";
        }
        return "失败";
    }

    File getIndexFile() {
        return indexFile;
    }

    void setIndexFile(File indexFile) {
        this.indexFile = indexFile;
    }

    Map<String, IndexTree> getIndexMap() {
        return indexMap;
    }

    void setIndexMap(Map<String, IndexTree> indexMap) {
        this.indexMap = indexMap;
    }
}
