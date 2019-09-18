package bean;

import java.io.*;
import java.util.*;

/**
 * 数据库中对应的表
 */
public class Table {
    /**
     * 表名
     */
    private String name;

    /**
     * 数据字典
     */
    private TableDict tableDict;

    /**
     * 表中存放的数据
     */
    private TableData tableData;
    /**
     * 表中存放的索引
     */
    private TableIndex tableIndex;
    /**
     *
     */
    private TableFile tableFile;
    /**
     * 用户姓名，切换或修改用户时修改
     */
    private static String userName;
    /**
     * 数据库dataBase名，切换时修改
     */
    private static String dbName;

    /**
     * 控制文件行数
     */
    public static final long LINE_NUM_CONFINE = 10;

    public static final String NULL_DB = "[NULL]";
    public static final String LINE_NUM = "[lineNum]";
    public static final String DATA_FILE_SUFFIX = ".data";
    public static final String DATA_FILE_PREFIX = "/data";


    /**
     * 只能静态创建，所以构造函数私有
     */
    private Table(String name) {
        this.name = name;
        tableFile = new TableFile(this,
                new File(ROOT_FILE + "/" + userName + "/" + dbName + "/" + name));
        tableDict = new TableDict(tableFile.getFolder(), name, this);

        tableData = new TableData(tableFile.getFolder(), this);
        tableIndex = new TableIndex(tableFile.getFolder(), name, this);
    }


    /**
     * 初始化表信息，包括用户和数据库
     *
     * @param userName 用户名
     * @param dbName   数据库名
     */
    static void init(String userName, String dbName) {
        Table.userName = userName;
        Table.dbName = dbName;
    }

    /**
     * 创建一个新的表文件
     *
     * @param name 表名
     * @return 如果表存在返回失败的信息，否则返回success
     */
    public static String createTable(String name, Map<String, Field> fields) {
        if (existTable(name)) {
            return "创建表失败，因为已经存在表:" + name;
        }

        Table table = new Table(name);
        //创建真实目录
        if (table.getDictFile().getParentFile().mkdirs()) {
            table.addDict(fields);
            return "success";
        }
        return "创建表" + name + "失败：未知错误404";
    }

    public String addDict(Map<String, Field> fields) {
        return getTableDict().addDict(fields);
    }

    /**
     * 根据表名获取表
     *
     * @param name 表名
     * @return 如果不存在此表返回null, 否则返回对应Table对象
     */
    public static Table getTable(String name) {
        if (!existTable(name)) {
            return null;
        }
        Table table = new Table(name);
        // 读取表结构
        table.tableDict.readTableDictFile();
        // 读取表内数据
        table.readTableData();
        // 读取索引对象
        table.readTableIdx();
        return table;
    }


    /**
     * 通过表名，读取表索引
     */
    private void readTableIdx() {
        // 如果表索引存在才读取
        tableIndex.readTableIdx();
    }

    /**
     * 通过表名，读取表中数据
     */
    private void readTableData() {
        // 读取多个数据文件，并将其加入到结果集中.
        File[] dataFiles = new File(getFolder(), "data").listFiles();
        if (null != dataFiles && 0 != dataFiles.length) {
            for (int i = 1; i <= dataFiles.length; i++) {
                getDataFileSet().add(new File(getFolder() + DATA_FILE_PREFIX,
                        i + DATA_FILE_SUFFIX));
            }
        }
    }


    public static final String ROOT_FILE = "dir";

    public static String dropTable(String name) {
        if (!existTable(name)) {
            return "错误：不存在表:" + name;
        }
        File folder = folderFactory(userName, dbName, name);
        TableFile.deleteTableFile(folder);
        return "success";

    }

    /**
     * 判断表是否存在
     *
     * @param name 表名
     * @return 存在与否
     */
    private static boolean existTable(String name) {
        File folder = folderFactory(userName, dbName, name);
        return folder.exists();
    }

    public static File folderFactory(String userName, String dbName, String name) {
        return new File(ROOT_FILE + "/" + userName + "/" + dbName + "/" + name);
    }

    /**
     * 插入数据到最后一个数据文件，如果数据行数超过限定值，写入下一个文件中
     *
     * @param srcData
     * @return
     */
    public String insert(Map<String, String> srcData) {
        File lastFile = tableFile.tableFileInsert(srcData);
        tableIndex.writeIndex();
        return tableData.insertData(lastFile, srcData);
    }


    /**
     * 读取对应索引文件的数据
     *
     * @return
     */
    private List<Map<String, String>> read() {
        //索引文件***
        List<Map<String, String>> datas = new ArrayList<>();
        for (File file : getDataFileSet()) {
            datas.addAll(tableFile.readDatas(file));
        }
        return datas;
    }


    /**
     * 读取对应索引文件的数据并过滤
     *
     * @param singleFilters 过滤器
     * @return
     */
    public List<Map<String, String>> read(List<SingleFilter> singleFilters) {
        //索引文件***
        List<Map<String, String>> datas = new ArrayList<>();
        if (null != singleFilters && 0 != singleFilters.size()) {
            Set<File> fileSet = findFileSet(singleFilters);
            for (File file : fileSet) {
                datas.addAll(readFilter(file, singleFilters));
            }
        } else {
            datas = read();
        }

        return datas;
    }


    /**
     * 读取指定文件的数据并用where规则过滤
     *
     * @param file
     * @param singleFilters
     * @return
     */
    private List<Map<String, String>> readFilter(File file, List<SingleFilter> singleFilters) {
        //读取数据文件
        List<Map<String, String>> srcDatas = getTableFile().readDatas(file);
        List<Map<String, String>> filtDatas = new ArrayList<>(srcDatas);
        //循环过滤
        for (SingleFilter singleFilter : singleFilters) {
            filtDatas = singleFilter.singleFiltData(filtDatas);
        }
        //将过滤的数据返回
        return filtDatas;
    }

    /**
     * 将数据写入对应的文件
     *
     * @param dataFile
     * @param datas
     */
    private void writeDatas(File dataFile, List<Map<String, String>> datas) {
        if (dataFile.exists()) {
            dataFile.delete();
        }
        for (Map<String, String> data : datas) {
            tableData.insertData(dataFile, data);
        }
    }

    /**
     * 根据给定的过滤器组，查找索引，将指定的文件数据删除
     *
     * @param singleFilters 过滤器组
     */
    public void delete(List<SingleFilter> singleFilters) {
        //此处查找索引
        Set<File> fileSet = findFileSet(singleFilters);
        for (File file : fileSet) {
            deleteData(file, singleFilters);
        }
        tableIndex.buildIndex();
        tableIndex.writeIndex();
    }

    /**
     * 读取给定文件，读取数据并使用过滤器组过滤，将过滤后的写入文件
     *
     * @param file          数据文件
     * @param singleFilters 过滤器组
     */
    private void deleteData(File file, List<SingleFilter> singleFilters) {
        //读取数据文件
        List<Map<String, String>> srcDatas = getTableFile().readDatas(file);
        List<Map<String, String>> filtDatas = new ArrayList<>(srcDatas);
        for (SingleFilter singleFilter : singleFilters) {
            filtDatas = singleFilter.singleFiltData(filtDatas);
        }
        srcDatas.removeAll(filtDatas);
        writeDatas(file, srcDatas);
    }

    /**
     * 根据给定的过滤器组，查找索引，将指定的文件数据更新
     *
     * @param updateDatas   更新的数据
     * @param singleFilters 过滤器组
     */
    public void update(Map<String, String> updateDatas, List<SingleFilter> singleFilters) {
        Set<File> fileSet = findFileSet(singleFilters);
        for (File file : fileSet) {
            updateFileData(file, updateDatas, singleFilters);
        }
        tableIndex.buildIndex();
        tableIndex.writeIndex();
    }

    /**
     * 读取给定文件，读取数据并使用过滤器组过滤，将过滤出的数据更新并写入文件
     *
     * @param file          数据文件
     * @param updateDatas   更新的数据
     * @param singleFilters 过滤器组
     */
    private void updateFileData(File file, Map<String, String> updateDatas, List<SingleFilter> singleFilters) {
        //读取数据文件
        List<Map<String, String>> filterDatas = new ArrayList<>(getTableFile().readDatas(file));
        //循环过滤
        for (SingleFilter singleFilter : singleFilters) {
            filterDatas = singleFilter.singleFiltData(filterDatas);
        }
        //将过滤的数据遍历，将数据的值更新为updateDatas对应的数据
        for (Map<String, String> filtData : filterDatas) {
            for (Map.Entry<String, String> setData : updateDatas.entrySet()) {
                filtData.put(setData.getKey(), setData.getValue());
            }
        }
        writeDatas(file, getTableFile().readDatas(file));
    }

    private Set<File> findFileSet(List<SingleFilter> singleFilters) {
        Set<File> fileSet = new HashSet<>();
        //此处查找索引
        for (SingleFilter singleFilter : singleFilters) {
            String fieldName = singleFilter.getField().getName();
            String fieldType = singleFilter.getField().getType();
            Relationship relationship = singleFilter.getRelationship();
            String condition = singleFilter.getCondition();

            IndexKey indexKey = new IndexKey(condition, fieldType);
            IndexTree indexTree = getIndexMap().get(fieldName);
            fileSet.addAll(indexTree.getFiles(relationship, indexKey));
        }
        return fileSet;
    }

    public String deleteIndex() {
        return tableIndex.deleteIndex();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getFolder() {
        return tableFile.getFolder();
    }

    public void setFolder(File folder) {
        tableFile.setFolder(folder);
    }

    public TableData getTableData() {
        return tableData;
    }

    public void setTableData(TableData tableData) {
        this.tableData = tableData;
    }

    public TableIndex getTableIndex() {
        return tableIndex;
    }

    public void setTableIndex(TableIndex tableIndex) {
        this.tableIndex = tableIndex;
    }

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String userName) {
        Table.userName = userName;
    }

    public static String getDbName() {
        return dbName;
    }

    public static void setDbName(String dbName) {
        Table.dbName = dbName;
    }

    public File getIndexFile() {
        return tableIndex.getIndexFile();
    }

    public void setIndexFile(File indexFile) {
        tableIndex.setIndexFile(indexFile);
    }

    public Map<String, IndexTree> getIndexMap() {
        return tableIndex.getIndexMap();
    }

    public void setIndexMap(Map<String, IndexTree> indexMap) {
        tableIndex.setIndexMap(indexMap);
    }

    public LinkedHashSet<File> getDataFileSet() {
        return tableData.getDataFileSet();
    }

    public void setDataFileSet(LinkedHashSet<File> dataFileSet) {
        tableData.setDataFileSet(dataFileSet);
    }

    public Map<String, Field> getFieldMap() {
        return tableData.getFieldMap();
    }

    public void setFieldMap(Map<String, Field> fieldMap) {
        tableData.setFieldMap(fieldMap);
    }

    public File getDictFile() {
        return tableDict.getDictFile();
    }

    public void setDictFile(File dictFile) {
        tableDict.setDictFile(dictFile);
    }

    private TableDict getTableDict() {
        return this.tableDict;
    }

    public void setTableDict(TableDict tableDict) {
        this.tableDict = tableDict;
    }

    public TableFile getTableFile() {
        return tableFile;
    }

    public void setTableFile(TableFile tableFile) {
        this.tableFile = tableFile;
    }

}
