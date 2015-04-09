<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<!--
I${className}Dao mybatis

@author ${ftl_author}
@date ${ftl_now}
@version ${ftl_version}
-->
<mapper namespace="${ftl_dao_package}.I${className}Dao" >
    <resultMap id="${className}Map" type="${ftl_model_package}.${className}">
        <#list classFields as v>
        <result property="${v.field}" column="${v.columnName}"/>
            <#if v.isPrimaryKey=true>
                <#assign tableKeyName=v.columnName>
                <#assign beanKeyName=v.field>
                <#assign beanKeyType=v.type>
            </#if>
        </#list>
    </resultMap>

    <insert id="insert" parameterType="${ftl_model_package}.${className}">
        <#if ftl_dbType="MYSQL">
            <selectKey keyProperty="${beanKeyName}" resultType="${beanKeyType}" order="AFTER">
                SELECT LAST_INSERT_ID() AS ${beanKeyName}
            </selectKey>
        <#elseif ftl_dbType="POSTGRESQL">
            <selectKey keyProperty="${beanKeyName}" resultType="${beanKeyType}" order="BEFORE">
                SELECT nextval('${tableName}_${tableKeyName}_seq')
            </selectKey>
        </#if>
        INSERT INTO ${tableName} (
        <trim suffixOverrides=",">
            <#list classFields as v>
                <#if v.isNullable=true>
                <if test="${v.field} != null">${v.columnName},</if>
                <#else>
                ${v.columnName},
                </#if>
            </#list>
        </trim>
        )VALUES(
        <trim suffixOverrides=",">
            <#list classFields as v>
                <#if v.isNullable=true>
                    <if test="${v.field} != null"> ${"#{"+v.field+"}"},</if>
                <#else>
                    ${"#{"+v.field+"}"},
                </#if>
            </#list>
        </trim>
        )
    </insert>

    <update id="updateByPk" parameterType="${ftl_model_package}.${className}">
        update ${tableName}
        <trim prefix="SET" suffixOverrides=",">
        <#list classFields as v>
            <if test="${v.field} != null">${v.columnName} = ${"#{"+v.field+"}"},</if>
        </#list>
        </trim>
        where ${tableKeyName} = ${"#{"+beanKeyName+"}"}
    </update>

    <delete id="deleteByPrimarykey" parameterType="int">
        delete from ${tableName} where ${tableKeyName} = ${"#{"+beanKeyName+"}"}
    </delete>

    <select id="getOneByPrimaryKey" parameterType="int" resultMap="${className}Map">
        select * from ${tableName} where ${tableKeyName} = ${"#{"+beanKeyName+"}"}
    </select>

    <select id="getAllItemsByQueryObject" resultMap="${className}Map" parameterType="${ftl_model_package}.${className}">
        select * from ${tableName}
        <include refid="select_where_clause"/>
        order by ${tableKeyName} desc
    </select>

    <select id="countByQueryObject" resultType="Integer" parameterType="${ftl_model_package}.${className}" >
        select count(${tableKeyName}) from ${tableName}
        <include refid="select_where_clause"/>
    </select>

    <sql id="select_where_clause">
        <trim prefix="where" prefixOverrides="AND|OR">
        <#list classFields as v>
            <if test="${v.field} != null">AND ${v.columnName} = ${"#{"+v.field+"}"}</if>
        </#list>
        </trim>
    </sql>
</mapper>