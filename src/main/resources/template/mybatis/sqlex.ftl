<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<!--
I${className}Dao mybatis ex

@author ${ftl_author}
@date ${ftl_now}
@version ${ftl_version}
-->
<#list classFields as v>
    <#if v.isPrimaryKey=true>
        <#assign tableKeyName=v.columnName>
    </#if>
</#list>
<mapper namespace="${ftl_dao_package}.I${className}Dao" >
    <select id="queryPageList" resultMap="${className}Map">
        select * from ${tableName}
        <include refid="page_where_clause"/>
        order by ${tableKeyName} desc
        LIMIT ${"#"}{pageSize} OFFSET ${"#"}{offSet}
    </select>

    <select id="countPage" resultType="int">
        select count(*) from ${tableName}
        <include refid="page_where_clause"/>
    </select>

    <sql id="page_where_clause">
        <trim prefix="where" prefixOverrides="AND|OR">
        <#list classFields as v>
            <if test="queryObject.${v.field} != null">AND ${v.columnName} = ${"#"}{queryObject.${v.field}}</if>
        </#list>

            <!-- otherParameters -->
            <if test="otherParameters != null">
                <!--your script-->
            </if>
        </trim>
    </sql>

    <!--extension script-->

</mapper>
