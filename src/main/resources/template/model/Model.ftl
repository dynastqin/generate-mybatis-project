package ${ftl_model_package};

import java.io.Serializable;
import java.util.Date;

/**
* model ${className}
*
* @author ${ftl_author}
* @date ${ftl_now}
* @version ${ftl_version}
*/
public class ${className} implements Serializable {
<#list classFields as v>
    private ${v.type} ${v.field};
</#list>

<#list classFields as v>
    public void set${v.field?cap_first}(${v.type} ${v.field}){
        this.${v.field} = ${v.field};
    }
    public ${v.type} get${v.field?cap_first}(){
        return this.${v.field};
    }

</#list>
}