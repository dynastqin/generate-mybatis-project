package com.tc.dynastqin.pojo;

import com.tc.dynastqin.utils.GenerateUtils;

/**
 * DB字段信息对应bean
 * Created by tantao on 14-9-11.
 */
public class DBFieldInfo {
    private String columnName;//db对应列
    private String dataType;//类型
    private Boolean isNullable;//是否允许空
    private Integer charMaxLength;//若为字符类型的最大长度
    private Boolean isPrimaryKey;//是否是主键

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Boolean getIsNullable() {
        return isNullable;
    }

    public void setIsNullable(Boolean isNullable) {
        this.isNullable = isNullable;
    }

    public Integer getCharMaxLength() {
        return charMaxLength;
    }

    public void setCharMaxLength(Integer charMaxLength) {
        this.charMaxLength = charMaxLength;
    }

    public Boolean getIsPrimaryKey() {
        return isPrimaryKey;
    }

    public void setIsPrimaryKey(Boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }

    public String getField(){//bean 对应列
        return GenerateUtils.convertCamel(columnName);
    }

    public String getType(){//bean 对应列类型(根据db类型转换)
        if ("serial".equals(dataType)
                || dataType.startsWith("int")
                || "smallint".equals(dataType)) {
            return "Integer";

        } else if (dataType.startsWith("char")
                || dataType.startsWith("varchar")
                || "text".equals(dataType)) {
            return "String";

        } else if (dataType.startsWith("timestamp")
                ||dataType.startsWith("time")
                ||dataType.startsWith("date")
                ||dataType.startsWith("datetime")) {
            return "Date";

        } else if ("boolean".equals(dataType)) {
            return "Boolean";

        }else if(dataType.startsWith("numeric")){
            return "BigDecimal";
        }

        return "__UNKNOWN__";
    }


}
