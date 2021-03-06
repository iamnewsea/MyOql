package nbcp

import ch.qos.logback.classic.Level
import nbcp.comm.*
import nbcp.utils.*
import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.IdName
import nbcp.db.IdUrl
import org.junit.Test
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class TestKtExt_Json : TestBase() {

    class bc {
        var isDeleted: Boolean? = false;
        var list = mutableListOf<IdName>();
        var ary = arrayOf<IdName>()
    }

    @Test
    fun test_ToJson() {
        var b = bc();
        b.list.add(IdName("1", "abc"))
        b.ary = arrayOf(IdName("2", "def"))
        b.isDeleted = true;

        usingScope(JsonStyleEnumScope.GetSetStyle) {
            println(b.ToJson())
        }

        usingScope(JsonStyleEnumScope.FieldStyle) {
            println(b.ToJson())
        }
    }

    @Test
    fun test_FromJson() {
        var b = bc();
        b.list.add(IdName("1", "abc"))
        b.ary = arrayOf(IdName("2", "def"))
        b.isDeleted = true;


        var result   = ApiResult<Any>();
        result.data = b;

        var str = result.ToJson();
        usingScope(JsonStyleEnumScope.GetSetStyle) {
            result = str.FromJson<ApiResult<Any>>()!!;
            println(result.data!!.ConvertJson(bc::class.java).list.first().ToJson())
        }
    }
}