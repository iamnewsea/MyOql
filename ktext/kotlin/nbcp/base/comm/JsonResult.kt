package nbcp.comm

import org.slf4j.LoggerFactory
import nbcp.base.extend.*
import java.io.Serializable
import java.lang.reflect.ParameterizedType
import java.util.ArrayList


/**
 * Created by jin on 2017/3/16.
 */

/**
 * 普通的返回对象。
 */

open class JsonResult(var msg: String = "", var cause: String = "") {}

open class ApiResult<T>(msg: String = "", cause: String = "") : JsonResult(msg, cause) {
    var data: T? = null
    var value: Any? = null
    var valueRemark: String = ""

    companion object {
        fun <T> of(data: T?): ApiResult<T> {
            var ret = ApiResult<T>();
            ret.data = data;
            return ret;
        }
    }

    /** 设置额外value的值。
     * @param valueRemark value值的含义
     * @param value value的值
     */
    fun setValue(valueRemark: String, value: Any): ApiResult<T> {
        this.valueRemark = valueRemark;
        this.value = value;
        return this;
    }
}


class NoDataResult<T>() : ApiResult<T>("找不到数据") {
    constructor(cause: String) : this() {
        super.msg = "找不到" + cause + "的数据";
    }
}


/**
 * 查询对象
 */

open class ListQueryModel {
    var skip: Int = 0;
    var take: Int = -1;
}

/**
 * 列表返回对象
 */
class ListResult<T>(msg: String = "",
                    var total: Int = -1,
                    var data: List<T> = listOf()
) : JsonResult(msg) {
    var value: Any? = null
    var valueRemark: String = ""

    companion object {
        fun <T> of(data: List<T>): ListResult<T> {
            var ret = ListResult<T>();
            ret.data = data;
            ret.total = data.size;
            return ret;
        }
    }

    /** 设置额外value的值。
     * @param valueRemark value值的含义
     * @param value value的值
     */
    fun setValue(valueRemark: String, value: Any): ListResult<T> {
        this.valueRemark = valueRemark;
        this.value = value;
        return this;
    }
}

//class AppListResult<T>(msg: String = "",
//                       var over: Boolean = false, //表示数据已结束。
//                       var data: MutableList<T> = mutableListOf(),
//                       var value: String = ""//返回除 data 额外的信息
//) : JsonResult(msg) {
//}