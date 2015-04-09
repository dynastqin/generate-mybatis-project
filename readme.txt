本项目用于生成Maven项目的model，dao，service和mybatis配置文件，目前支持MySQL和postgreSQL数据库

注意：
    1.不要修改sys-config.properties文件
    2.需要修改custom-config.properties属性值以适应自己的项目，允许修改和增加属性，但不要删除
    3.模板可自定义，也可修改resources/template中的模板，增加的模板变量请在custom-config.properties定义
    4.请不要删除模板路径下的任何文件，也不要重命名，若要增加，请修改Generate.java源码
    5.若表有主键，主键必须是一个字段且自增
    6.dao的基类使用tcmc-common-1.0.4 的com.tc.common.dao.ISingleTableDao