package bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class TableFile {

    private Table table;
    /**
     * 表所在的文件夹
     */
    private File folder;


    /**
     * 读取指定文件的所有数据加行号
     *
     * @param dataFile 数据文件
     * @return 数据列表
     */
    public List<Map<String, String>> readDatasAndLineNum(File dataFile) {
        List<Map<String, String>> dataMapList = new ArrayList<>();

        try (
                BufferedReader br = new BufferedReader(new FileReader(dataFile))
        ) {

            String line = null;
            long lineNum = 1;
            while (null != (line = br.readLine())) {
                Map<String, String> dataMap = new LinkedHashMap<>();
                putDataMapToList(dataMapList, line, dataMap);
                dataMap.put(Table.LINE_NUM, String.valueOf(lineNum));
                lineNum++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataMapList;
    }

    private void putDataMapToList(List<Map<String, String>> dataMapList, String line, Map<String, String> dataMap) {
        String[] datas = line.split(" ");
        Iterator<String> fieldNames = table.getFieldMap().keySet().iterator();
        for (String data : datas) {
            String dataName = fieldNames.next();
            dataMap.put(dataName, data);
        }
        dataMapList.add(dataMap);
    }

    /**
     * 读取指定文件的所有数据
     *
     * @param dataFile 数据文件
     * @return 数据列表
     */
    public List<Map<String, String>> readDatas(File dataFile) {
        List<Map<String, String>> dataMapList = new ArrayList<>();

        try (
                FileReader fr = new FileReader(dataFile);
                BufferedReader br = new BufferedReader(fr)
        ) {

            String line;
            while (null != (line = br.readLine())) {
                putDataMapToList(dataMapList, line, new LinkedHashMap<>());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataMapList;
    }


    public File tableFileInsert(Map<String, String> srcData) {
        File lastFile = null;
        int lineNum = 0;
        int fileNum = 0;
        for (File file : table.getDataFileSet()) {
            fileNum++;
            lastFile = file;
            lineNum = fileLineNum(lastFile);
        }
        //如果没有一个文件或者文件已满，新建1.data
        if (null == lastFile || 0 == fileNum || Table.LINE_NUM_CONFINE <= fileLineNum(lastFile)) {
            //如果最后一个文件大于行数限制，新建数据文件
            if (Table.LINE_NUM_CONFINE <= fileLineNum(lastFile)) {
                lastFile = new File(folder + Table.DATA_FILE_PREFIX, fileNum + 1 + Table.DATA_FILE_SUFFIX);
            } else {
                lastFile = new File(folder + Table.DATA_FILE_PREFIX, 1 + Table.DATA_FILE_SUFFIX);
            }
            table.getDataFileSet().add(lastFile);
            lineNum = 0;
        }
        //添加索引
        for (Map.Entry<String, Field> fieldEntry : table.getFieldMap().entrySet()) {
            addIdxToField(srcData, lastFile, lineNum, fieldEntry);
        }
        return lastFile;
    }

    private int fileLineNum(File file) {
        int num = 0;
        try (
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr)
        ) {
            while (null != br.readLine()) {
                num++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return num;
    }

    private void addIdxToField(Map<String, String> srcData, File lastFile, int lineNum, Map.Entry<String, Field> fieldEntry) {
        String dataName = fieldEntry.getKey();
        String dataValue = srcData.get(dataName);
        //如果发现此数据为空，不添加到索引树中
        if (null == dataValue || Table.NULL_DB.equals(dataValue)) {
            return;
        }
        String dataType = fieldEntry.getValue().getType();

        IndexTree indexTree = table.getIndexMap().get(dataName);
        if (null == indexTree) {
            table.getIndexMap().put(dataName, new IndexTree());
            indexTree = table.getIndexMap().get(dataName);
        }
        IndexKey indexKey = new IndexKey(dataValue, dataType);
        indexTree.putIndex(indexKey, lastFile.getAbsolutePath(), lineNum);
    }

    /**
     * 删除一个文件
     *
     * @param file
     */
    public static void deleteTableFile(File file) {
        //判断是否是文件
        if (file.isDirectory()) {
            //否则如果它是一个目录,递归删除目录下所有目录和文件
            File[] files = file.listFiles();
            for (File value : files) {
                deleteTableFile(value);
            }
        }
        // 删除自身
        file.delete();
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public File getFolder() {
        return folder;
    }

    public void setFolder(File folder) {
        this.folder = folder;
    }

    public TableFile(Table table, File folder) {
        this.table = table;
        this.folder = folder;
    }
}
