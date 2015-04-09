package com.tc.dynastqin;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tc.dynastqin.pojo.DBFieldInfo;
import com.tc.dynastqin.utils.GenerateUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Generate {

    private static final String MAVEN_JAVA_PATH = "src/main/java/";
    private static final String MAVEN_RESOURCES_PATH = "src/main/resources/";
    //目标项目路径
    private static String targetProjectPath;
    private static Properties config;

    private static Configuration cfg;

    private DataSource ds = null;
    private NamedParameterJdbcTemplate jdbcTemplate = null;

    //db info enum
    enum DbType {
        MYSQL("com.mysql.jdbc.Driver",
                "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA='%s'",
                "SELECT column_name AS columnName,data_type AS dataType," +
                        "character_maximum_length AS charMaxLength,numeric_precision AS numericMaxLength," +
                        "numeric_scale AS numericScaleMaxLength,is_nullable AS isNullable," +
                        "(CASE WHEN extra = 'auto_increment' THEN TRUE ELSE FALSE END) AS isPrimaryKey" +
                        " FROM Information_schema.COLUMNS" +
                        " WHERE table_Name = '%s'"),

        POSTGRESQL("org.postgresql.Driver",
                "select tablename from pg_tables where schemaname='public'",
                "select column_name AS columnName,data_type AS dataType," +
                        "(CASE WHEN is_nullable='YES' THEN true ELSE false END) AS isNullable,character_maximum_length AS charMaxLength," +
                        "(CASE WHEN position('nextval' in column_default)>0 THEN true ELSE false END) AS isPrimaryKey" +
                        " from information_schema.columns" +
                        " where table_schema = 'public' and table_name = '%s'");

        private String jdbcDriver, allTableSQL, tableInfoSQL;

        DbType(String jdbcDriver, String allTableSQL, String tableInfoSQL) {
            this.jdbcDriver = jdbcDriver;
            this.allTableSQL = allTableSQL;
            this.tableInfoSQL = tableInfoSQL;
        }

        public String getJdbcDriver() {
            return jdbcDriver;
        }

        public String getAllTableSQL() {
            return allTableSQL;
        }

        public String getTableInfoSQL() {
            return tableInfoSQL;
        }
    }

    private static DbType dbType;
    private String dbName;

    /**
     * 初始化
     *
     * @throws Exception
     */
    private void init() throws Exception {
        config = new Properties();
        config.load(Generate.class.getClassLoader().getResourceAsStream("sys-config.properties"));
        config.load(Generate.class.getClassLoader().getResourceAsStream("custom-config.properties"));

        // 初始化FreeMarker配置 创建一个Configuration实例,并设置FreeMarker的模版文件位置
        cfg = new Configuration();
        cfg.setDirectoryForTemplateLoading(this.loadTemplateDirectory());
        targetProjectPath = config.get("target.project.path").toString();

        String jdbcDriver = config.getProperty("jdbc.driver");
        String jdbcUrl = config.getProperty("jdbc.url");
        // 创建数据库连接
        Class.forName(jdbcDriver);
        ds = new DriverManagerDataSource(jdbcUrl, config.getProperty("jdbc.username"), config.getProperty("jdbc.password"));
        jdbcTemplate = new NamedParameterJdbcTemplate(ds);

        if (DbType.POSTGRESQL.getJdbcDriver().equals(jdbcDriver)) {
            dbType = DbType.POSTGRESQL;

        } else if (DbType.MYSQL.getJdbcDriver().equals(jdbcDriver)) {
            dbType = DbType.MYSQL;
            //获取mysql db
            Pattern p = Pattern.compile("/[a-z|_|1-9]+\\?");
            Matcher m = p.matcher(jdbcUrl);
            if (m.find()) {
                String target = m.group();
                dbName = m.group().substring(1, target.length() - 1);
            }
        }
    }

    /**
     * 断开db连接
     */
    private void destory() {
        try {
            if (ds != null)
                ds.getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取模板所在文件夹
     *
     * @return
     */
    private File loadTemplateDirectory() {
        String tempPath = config.getProperty("template.path");
        File tempDirectory = new File(tempPath);
        if (StringUtils.isBlank(tempPath) || !tempDirectory.isDirectory()) {
            tempDirectory = new File(Generate.class.getResource("/").getPath() + "template");
        }
        return tempDirectory;
    }

    /**
     * 获取表字段信息
     *
     * @param table 表名
     * @return
     */
    private List<DBFieldInfo> getFieldInfo(String table) {

        String sql = String.format(dbType.getTableInfoSQL(), table);
        RowMapper mapper = new BeanPropertyRowMapper(DBFieldInfo.class);
        return jdbcTemplate.query(sql, new HashMap<String, Object>(), mapper);
    }

    /**
     * 获取所有表名
     *
     * @return
     */
    private List<String> getAllTable() {
        String sql = String.format(dbType.getAllTableSQL(), dbName);

        List<String> tables = jdbcTemplate.queryForList(sql, new HashMap<String, Object>(), String.class);
        String selectedTables = Objects.firstNonNull(config.get("table.generate"), "").toString();
//        String[] selectedTables = Objects.firstNonNull(config.get("table.generate"), "").toString().split(",");//用户指定的表
        if ("".equals(selectedTables)) return tables;

        List<String> retTables = Lists.newArrayList();
        for (String v : selectedTables.split(",")) {
            if (tables.contains(v)) {
                retTables.add(v.trim());
            }
        }
        return retTables;
    }

    /**
     * 根据模板生成文件
     *
     * @param root 模板编码Map
     * @param projectPath 目标项目路径
     * @param savePath 各模板生成后存放到目标项目的相对路径
     * @param fileName 模板对应文件名
     * @param template 模板
     */
    private void buildTemplate(Map root, String projectPath, String savePath,
                               String fileName, Template template) {

        String realSavePath = GenerateUtils.appendBias(projectPath) + savePath;
        String realFileName = GenerateUtils.appendBias(realSavePath) + fileName;


        File newsDir = new File(realSavePath);
        if (!newsDir.exists()) {
            newsDir.mkdirs();
        }

        try {
            Writer out = new OutputStreamWriter(new FileOutputStream(
                    realFileName), "UTF-8");

            template.process(root, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成基础model、dao、service
     *
     * @param gc
     * @param ftlParams 模板变量map
     * @throws IOException
     */
    private static void generateBase(Generate gc, Map<String, Object> ftlParams) throws IOException {
        //1 pagination
        String savePath = MAVEN_JAVA_PATH +
                GenerateUtils.appendBias(Objects.firstNonNull(config.getProperty("ftl_model_package"), "").replace(".", "/")) + "pagination";
        Template template = cfg.getTemplate(Objects.firstNonNull(config.getProperty("template.Page.file"), ""));
        gc.buildTemplate(ftlParams, targetProjectPath, savePath, "Page.java", template);

        savePath = MAVEN_JAVA_PATH +
                GenerateUtils.appendBias(Objects.firstNonNull(config.getProperty("ftl_model_package"), "").replace(".", "/")) + "pagination";
        template = cfg.getTemplate(Objects.firstNonNull(config.getProperty("template.PageAttribute.file"), ""));
        gc.buildTemplate(ftlParams, targetProjectPath, savePath, "PageAttribute.java", template);

        savePath = MAVEN_JAVA_PATH +
                GenerateUtils.appendBias(Objects.firstNonNull(config.getProperty("ftl_model_package"), "").replace(".", "/")) + "pagination";
        template = cfg.getTemplate(Objects.firstNonNull(config.getProperty("template.PageList.file"), ""));
        gc.buildTemplate(ftlParams, targetProjectPath, savePath, "PageList.java", template);

        //2 my exception
        String fileName = config.get("ftl_exception_class") + ".java";
        savePath = MAVEN_JAVA_PATH +
                GenerateUtils.appendBias(Objects.firstNonNull(config.getProperty("ftl_package"), "").replace(".", "/")) + "exception";
        template = cfg.getTemplate(Objects.firstNonNull(config.getProperty("template.MyException.file"), ""));
        gc.buildTemplate(ftlParams, targetProjectPath, savePath, fileName, template);

        //3 IBaseService&AbstractBaseService&IPaginationService
        savePath = MAVEN_JAVA_PATH +
                Objects.firstNonNull(config.getProperty("ftl_service_package"), "").replace(".", "/");
        template = cfg.getTemplate(Objects.firstNonNull(config.getProperty("template.IBaseService.file"), ""));
        gc.buildTemplate(ftlParams, targetProjectPath, savePath, "IBaseService.java", template);

        savePath = MAVEN_JAVA_PATH +
                Objects.firstNonNull(config.getProperty("ftl_service_package"), "").replace(".", "/");
        template = cfg.getTemplate(Objects.firstNonNull(config.getProperty("template.AbstractBaseService.file"), ""));
        gc.buildTemplate(ftlParams, targetProjectPath, savePath, "AbstractBaseService.java", template);
    }

    /**
     * 生成对应table的model，dao，service和mybatis配置文件
     *
     * @param gc
     * @param ftlParams 模板变量map
     * @param isFirstGenerate 是否第一次生成
     * @throws IOException
     */
    private static void generateOther(Generate gc, Map<String, Object> ftlParams, Boolean isFirstGenerate) throws IOException {

        int tableSubStrIndex = Integer.valueOf(Objects.firstNonNull(config.get("table.substring.index"), 0).toString());
        List<String> tables = gc.getAllTable();
        for (String v : tables) {
            List<DBFieldInfo> dbFieldInfos = gc.getFieldInfo(v);
            String className = StringUtils.capitalize(GenerateUtils.convertCamel(v.substring(tableSubStrIndex)));
            ftlParams.put("tableName", v);
            ftlParams.put("className", className);
            ftlParams.put("classFields", dbFieldInfos);

            //model
            String fileName = className + ".java";
            String savePath = MAVEN_JAVA_PATH +
                    Objects.firstNonNull(config.getProperty("ftl_model_package"), "").replace(".", "/");
            Template template = cfg.getTemplate(Objects.firstNonNull(config.getProperty("template.Model.file"), ""));
            gc.buildTemplate(ftlParams, targetProjectPath, savePath, fileName, template);

            // 第一次会生成，以后不再生成，避免覆盖用户脚本
            if (isFirstGenerate) {
                //dao
                fileName = "I" + className + "Dao.java";
                savePath = MAVEN_JAVA_PATH +
                        Objects.firstNonNull(config.getProperty("ftl_dao_package"), "").replace(".", "/");
                template = cfg.getTemplate(Objects.firstNonNull(config.getProperty("template.IDao.file"), ""));
                gc.buildTemplate(ftlParams, targetProjectPath, savePath, fileName, template);

                //service
                fileName = "I" + className + "Service.java";
                savePath = MAVEN_JAVA_PATH +
                        Objects.firstNonNull(config.getProperty("ftl_service_package"), "").replace(".", "/");
                template = cfg.getTemplate(Objects.firstNonNull(config.getProperty("template.IService.file"), ""));
                gc.buildTemplate(ftlParams, targetProjectPath, savePath, fileName, template);

                //service impl
                fileName = className + "Service.java";
                savePath = MAVEN_JAVA_PATH +
                        Objects.firstNonNull(config.getProperty("ftl_service_package"), "").replace(".", "/") + "/impl";
                template = cfg.getTemplate(Objects.firstNonNull(config.getProperty("template.Service.file"), ""));
                gc.buildTemplate(ftlParams, targetProjectPath, savePath, fileName, template);
            }

            //sql
            fileName = className + "_sql.xml";
            savePath = MAVEN_RESOURCES_PATH + "mybatis/";
            template = cfg.getTemplate(Objects.firstNonNull(config.getProperty("template.sql.file"), ""));
            gc.buildTemplate(ftlParams, targetProjectPath, savePath, fileName, template);


            if (isFirstGenerate) {
                fileName = className + "_sqlex.xml";
                savePath = MAVEN_RESOURCES_PATH + "mybatis/ex/";
                template = cfg.getTemplate(Objects.firstNonNull(config.getProperty("template.sqlex.file"), ""));
                gc.buildTemplate(ftlParams, targetProjectPath, savePath, fileName, template);
            }
        }
    }


    public static void main(String[] args) throws Exception {

        Generate gc = new Generate();
        gc.init();

        Map<String, Object> ftlParams = Maps.newHashMap();
        //装填模板中的变量
        for (Object k : config.keySet()) {
            if (k.toString().startsWith("ftl")) {
                ftlParams.put(k.toString(), config.get(k).toString());
            }
        }

        ftlParams.put("ftl_now",
                new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date()));
        ftlParams.put("ftl_dbType", dbType.toString());

        Object isFirstTmp = config.get("is.first.generate");
        Object isBaseTmp = config.get("is.base.generate");

        if (isBaseTmp == null || "true".equals(isBaseTmp.toString()))
            generateBase(gc, ftlParams);

        generateOther(gc, ftlParams, isFirstTmp == null || "true".equals(isFirstTmp.toString()));

        gc.destory();
        System.out.println("----------生成成功!");
    }

    //freemarker页面引擎生成页面核心代码示例：
    private void freemarkerGenerator() throws IOException, TemplateException {
        Configuration _config = new Configuration();
        _config.setDirectoryForTemplateLoading(new File("e:/template"));
        Template template = _config.getTemplate("src/test.ftl");
        String realFile = "e:/test.java";

        Map<String, Object> params = Maps.newHashMap();
        params.put("ftl_date", new Date());

        Writer out = new OutputStreamWriter(new FileOutputStream(realFile), "UTF-8");
        template.process(params, out);

    }
}
