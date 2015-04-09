package ${ftl_service_package};

import com.google.common.base.Preconditions;
import com.tc.common.dao.ISingleTableDao;
import ${ftl_package}.exception.${ftl_exception_class};
import ${ftl_model_package}.pagination.Page;
import ${ftl_model_package}.pagination.PageAttribute;
import ${ftl_model_package}.pagination.PageList;
import org.springframework.beans.BeanUtils;


import java.util.List;

/**
* 通用的业务接口的参考实现
*
* @author ${ftl_author}
* @date ${ftl_now}
* @version ${ftl_version}
*/
public abstract class AbstractBaseService<T> implements IBaseService<T> {


    /** 子类需要注入特定的DAO实现 */
    protected abstract ISingleTableDao<T> getMyBatisRepository();

    /** 构建只包含主键ID的实体对象*/
    protected abstract T constructPkEntity(final Integer id);

    /**
    * 注意：只能做一些对于参数的预操作，不能涉及事务。涉及事务请覆盖create
    * 子类可根据情况覆盖此方法
    * 用于在执行新增操作之前进行预处理
    * @param entity 需新增的实体
    */
    protected void preCreate(T entity) throws ${ftl_exception_class} {
        return;
    }

    @Override
    public void create(T entity) throws ${ftl_exception_class} {
        Preconditions.checkNotNull(entity, "新增对象不能为空");
        preCreate(entity);
        int successCount = getMyBatisRepository().insert(entity);
        if (1 != successCount) {
            throw new ${ftl_exception_class}("新增对象-数据库失败");
    }
    }
    /**
    * 注意：只能做一些对于参数的预操作，不能涉及事务。涉及事务请覆盖modifyEntityById
    * 子类可根据情况覆盖此方法
    * 用于在执行修改操作之前进行预处理
    * @param entity 需修改的实体
    */
    protected void preModify(T entity) throws ${ftl_exception_class} {
        return;
    }

    @Override
    public void modifyEntityById(Integer id,T entity) throws ${ftl_exception_class} {
        Preconditions.checkNotNull(id, "修改对象主键为空");
        Preconditions.checkNotNull(entity, "修改对象不能为空");

        this.getEntityById(id);//权限校验
        preModify(entity);

        BeanUtils.copyProperties(constructPkEntity(id),entity);
        int successCount = getMyBatisRepository().updateByPk(entity);
        if (1 != successCount) {
            throw new ${ftl_exception_class}("修改对象-数据库失败");
        }
    }

    @Override
    public void deleteEntityById(Integer id) throws ${ftl_exception_class} {
        Preconditions.checkNotNull(id, "修改对象不能对应主键不能为空");

        this.getEntityById(id);//权限校验
        int successCount = getMyBatisRepository().deleteByPrimarykey(constructPkEntity(id));
        if (1 != successCount) {
            throw new ${ftl_exception_class}("删除对象-数据库失败");
    }
    }

    @Override
    public T getEntityById(Integer id) throws ${ftl_exception_class} {
        Preconditions.checkNotNull(id, "修改对象不能对应主键不能为空");
        return getMyBatisRepository().getOneByPrimaryKey(constructPkEntity(id));
    }

    @Override
    public int countByByCriteria(T queryObject){
        return getMyBatisRepository().countByQueryObject(queryObject);
    }

    /**
    * 注意：只能做一些对于参数的预操作，不能涉及事务。涉及事务请覆盖queryEntityList
    * 子类可根据情况覆盖此方法
    * 用于在执行查询列表操作之前进行预处理
    * @param queryObject 查询参数对象
    */
    protected void preQuery(T queryObject) throws ${ftl_exception_class} {
        return;
    }

    @Override
    public List<T> queryEntityList(T queryObject) throws ${ftl_exception_class} {
        Preconditions.checkNotNull(queryObject, "查询参数对象不能为空");
        preQuery(queryObject);
        return getMyBatisRepository().getAllItemsByQueryObject(queryObject);
    }


    @Override
    public PageList<T> queryEntityPageList(PageAttribute pageAttr, T queryObject, Map<String, Object> otherParam) {

        Preconditions.checkNotNull(queryObject, "查询参数对象不能为空");
        if(null == pageAttr)
            pageAttr =PageAttribute.getInstance();

        //count记录数
        int count = getMyBatisRepository().countPage(queryObject, otherParam);
        if (0 == count)
            return PageList.<T>getEmptyInstance();
        List<T> datas = getMyBatisRepository().queryPageList(pageAttr.getPageSize(), pageAttr.getBegin(), queryObject, otherParam);
        return PageList.getInstance(datas, Page.getInstance(pageAttr,count));
     }
}