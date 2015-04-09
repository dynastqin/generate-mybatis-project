package ${ftl_service_package};

import ${ftl_package}.exception.${ftl_exception_class};
import ${ftl_model_package}.pagination.PageAttribute;
import ${ftl_model_package}.pagination.PageList;

import java.util.List;
import java.util.Map;

/**
* 定义常用业务功能接口
*
* @author ${ftl_author}
* @date ${ftl_now}
* @version ${ftl_version}
*/
public interface IBaseService<T> {
    void create(T entity) throws ${ftl_exception_class};
    void modifyEntityById(Integer id,T entity) throws ${ftl_exception_class};
    void deleteEntityById(Integer id) throws ${ftl_exception_class};
    T getEntityById(Integer id) throws ${ftl_exception_class};
    int countByByCriteria(T entity);
    List<T> queryEntityList(T queryObject) throws ${ftl_exception_class};

    //pageAttr允许空，若空将按照pageIndex=1，pageSize=9999获取数据，避免大数据查询
    PageList<T> queryEntityPageList(PageAttribute pageAttr, T queryObject, Map<String, Object> otherParam);
 }
