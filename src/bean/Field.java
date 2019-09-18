package bean;

/**
 * 属性
 *
 * @author 任耀
 * @date 2019年9月16日
 */
public class Field {
    /**
     * 属性名
     */
    private String name;
    /**
     * 属性的数据类型
     */
    private String type;
    /**
     * 是否是主键
     */
    private boolean primaryKey;

    public String getName() {
        return name;
    }

    public Field setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public Field setType(String type) {
        this.type = type;
        return this;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public Field setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }
}
