package ${ftl_package}.exception;
/**
* my exception
*
* @author ${ftl_author}
* @date ${ftl_now}
* @version ${ftl_version}
*/
public class ${ftl_exception_class} extends Exception {
    public ${ftl_exception_class}(){
    super("未知错误");
    }

    public ${ftl_exception_class}(String errorMsg){
    super(errorMsg);
    }

    public ${ftl_exception_class}(String errorMsg, Throwable cause){
    super(errorMsg, cause);
    }

}
