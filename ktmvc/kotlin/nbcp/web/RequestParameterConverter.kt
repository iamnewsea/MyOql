package nbcp.web


import org.springframework.core.MethodParameter
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import nbcp.comm.*
import nbcp.base.extend.*
import nbcp.base.utf8
import nbcp.base.utils.MyUtil
import org.slf4j.LoggerFactory
import java.lang.RuntimeException
import java.lang.reflect.Method
import javax.servlet.ServletRequest
import javax.servlet.ServletRequestWrapper
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession


/**
 * 自定义Mvc参数解析,有如下规则:
 * 1. 不支持默认值
 * 2. 基本类型, string , 不传递也会有默认值.
 * 3. 如果定义了可空参数,需要默认值, 重写参数 var  productIds = productIds ?: mutableListOf<String>()
 * 4. 只解析没有注解的参数,有任何注解,都不使用该方式.
 */
class RequestParameterConverter() : HandlerMethodArgumentResolver {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

//    private val packages by lazy {
//        return@lazy SpringUtil.context.environment.getProperty("shop.mvc-parameter-packages").AsString().split(",").filter { it.isNotEmpty() }
//    }


    override fun supportsParameter(parameter: MethodParameter): Boolean {
        if (ServletRequest::class.java.isAssignableFrom(parameter.parameterType)) return false
        if (ServletResponse::class.java.isAssignableFrom(parameter.parameterType)) return false
        if (HttpSession::class.java.isAssignableFrom(parameter.parameterType)) return false

        if (parameter.hasParameterAnnotation(PathVariable::class.java)) return false;
        if (parameter.hasParameterAnnotation(CookieValue::class.java)) return false;
        if (parameter.hasParameterAnnotation(RequestHeader::class.java)) return false;
        if (parameter.hasParameterAnnotation(RequestParam::class.java)) return false;
        if (parameter.hasParameterAnnotation(RequestBody::class.java)) return false;
        if (parameter.hasParameterAnnotation(SessionAttribute::class.java)) return false;
        if (parameter.hasParameterAnnotation(ModelAttribute::class.java)) return false;
        if (parameter.hasParameterAnnotation(RequestPart::class.java)) return false;


        if (parameter.hasParameterAnnotation(JsonModel::class.java)) return true;

//        var className = parameter.containingClass.name
//        if (packages.any()) {
//            return packages.any { return@any className.startsWith(it) }
//        }
        return true
    }

    private fun getMyRequest(request: HttpServletRequest): MyHttpRequestWrapper? {
        if (request is MyHttpRequestWrapper) {
            return request;
        }
        if ((request is ServletRequestWrapper) == false) {
            return null
        }

        var requestWrapper = request as ServletRequestWrapper
        if (requestWrapper.request == null) {
            return null;
        }

        return getMyRequest(requestWrapper.request as HttpServletRequest)
    }

    override fun resolveArgument(parameter: MethodParameter, mavContainer: ModelAndViewContainer?, nativeRequest: NativeWebRequest, binderFactory: WebDataBinderFactory?): Any? {
        if (mavContainer == null || nativeRequest == null || binderFactory == null) return null
        var webRequest = (nativeRequest as ServletWebRequest).request
        var myRequest = getMyRequest(webRequest);
        var value: Any? = null
        var key = parameter.parameterName


        if (webRequest.queryString != null) {
            var queryMap = webRequest.queryJson
            if (queryMap.containsKey(key)) {
                value = queryMap.get(key)

                if (value == null) {
                    if (parameter.parameterType.IsBooleanType()) {
                        value = true;
                    }
                } else {
                    //如果参数是 List.
                    if (parameter.parameterType.isArray || parameter.parameterType.IsListType()) {
                        if (value is String) {
                            value = listOf(value);
                        }
                    } else {
                        if (List::class.java.isAssignableFrom(value::class.java)) {
                            value = (value as List<String>).joinToString(",")
                        }
                    }
                }
            }
        }

        if (value == null && myRequest != null) {
            if (parameter.hasParameterAnnotation(JsonModel::class.java)) {
                return (myRequest.body ?: byteArrayOf()).toString(utf8).FromJson(parameter.parameterType);
            }

            value = myRequest.json.get(key)
        }


        if (value == null) {
            value = webRequest.getHeader(key)
        }

        if (value == null) {
            var require = parameter.getParameterAnnotation(Require::class.java)
            if (require != null) {
                var caller = ""
                if (parameter.executable is Method) {
                    var method = parameter.executable as Method
                    caller = "${method.name}(${method.parameters.map { it.toString() }.joinToString()}):${method.returnType.name}"
                } else {
                    var method = parameter.executable
                    caller = "${method.name}(${method.parameters.map { it.toString() }.joinToString()})"
                }

                var errorMsg = require.value.AsString("请求:${webRequest.fullUrl} --> 方法:${caller} 中，找不到参数${parameter.parameterName}")
                logger.error(errorMsg);
                throw RuntimeException("找不到参数${parameter.parameterName}")
            }

            if (parameter.parameterType == String::class.java) {
                return "";
            } else if (parameter.parameterType.IsNumberType()) {
                return MyUtil.getSimpleClassDefaultValue(parameter.parameterType);
            }
            return null;
        }

        var retValue = value.ConvertType(parameter.parameterType);

        if (parameter.parameterType == String::class.java) {
            return retValue.AsString().trim()
        }
        return retValue;
    }

}