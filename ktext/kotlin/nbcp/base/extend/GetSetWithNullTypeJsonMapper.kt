package nbcp.base.extend

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.core.util.VersionUtil
import com.fasterxml.jackson.databind.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import nbcp.base.utils.SpringUtil
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.cfg.MapperConfig
import com.fasterxml.jackson.databind.introspect.AnnotatedClass
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

/**
 * 使用 GET，SET 方式序列化JSON，应用在Web返回值的场景中。
 */
open class GetSetWithNullTypeJsonMapper : JsonBaseObjectMapper() {

    init {
        setDefaultConfig()

        this.registerModule(SpringUtil.getBean<JavascriptDateModule>());
    }

    companion object {
        /**
         * 创建只输出非Null且非Empty(如List.isEmpty)的属性到Json字符串的Mapper,建议在外部接口中使用.
         */
        val instance: GetSetWithNullTypeJsonMapper by lazy { return@lazy GetSetWithNullTypeJsonMapper() }
    }
}