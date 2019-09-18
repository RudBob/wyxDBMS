package bean;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

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
    TableIndex(File folder, String name) {
        this.indexMap = new HashMap<>();
        this.indexFile = new File(folder, name + ".index");
        this.folder = folder;
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

    void readTableIdx() {
        if (getIndexFile().exists()) {
            readIndex();
        }
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
}
