package nbcp.db.mysql.tool

import nbcp.base.extend.*

import nbcp.comm.*
import nbcp.db.sql.*
import java.time.LocalDateTime
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

class MysqlEntityGenerator {
    fun db2Entitys(db: String, group: String, tableLike: String, vararg tables: String): List<String> {

        var tables_map = RawQuerySqlClip(SingleSqlData("""
SELECT table_name,table_comment
FROM INFORMATION_SCHEMA.TABLES
where table_schema = {db} ${if (tableLike.HasValue) " and table_name like '${tableLike}'" else ""}  ${if (tables.any()) " and table_name in (${tables.map { "'" + it + "'" }.joinToString(",")})" else ""}
order by table_name
""", JsonMap("db" to db)), "TABLES").toMapList()

        var columns_map = RawQuerySqlClip(SingleSqlData("""
SELECT table_name , column_name , data_type , column_comment, column_key,extra
FROM INFORMATION_SCHEMA.COLUMNS
where table_schema = {db}  ${if (tableLike.HasValue) " and table_name like '${tableLike}'" else ""}  ${if (tables.any()) " and table_name in (${tables.map { "'" + it + "'" }.joinToString(",")})" else ""}
order by table_name , ordinal_position
""", JsonMap("db" to db)), "COLUMNS").toMapList()

        var indexes_map = RawQuerySqlClip(SingleSqlData("""
SELECT TABLE_NAME ,index_name,seq_in_index,column_name 
FROM INFORMATION_SCHEMA.STATISTICS
where table_schema = {db} AND non_unique = 0 AND INDEX_name != 'PRIMARY' ${if (tableLike.HasValue) " and table_name like '${tableLike}'" else ""}  ${if (tables.any()) " and table_name in (${tables.map { "'" + it + "'" }.joinToString(",")})" else ""}
ORDER BY TABLE_NAME , index_name , seq_in_index
""", JsonMap("db" to db)), "COLUMNS").toMapList()

        return tables_map.map {
            var tableName = it.getStringValue("table_name");
            var tableComment = it.getStringValue("table_comment")

            var columns = columns_map.filter { it.getStringValue("table_name") == tableName }
                    .map colMap@{
                        var columnName = it.getStringValue("column_name")
                        var dataType = it.getStringValue("data_type")
                        var columnComment = it.getStringValue("column_comment")

                        var kotlinType = dataType
                        var defaultValue = "";

                        if (dataType VbSame "varchar" || dataType VbSame "char" || dataType VbSame "text" || dataType VbSame "mediumtext" || dataType VbSame "longtext" || dataType VbSame "enum") {
                            kotlinType = "String";
                            defaultValue = "\"\"";
                        } else if (dataType VbSame "int") {
                            kotlinType = "Int";
                            defaultValue = "0";
                        } else if (dataType VbSame "bit") {
                            kotlinType = "Boolean";
                            defaultValue = "false";
                        } else if (dataType VbSame "datetime") {
                            kotlinType = "LocalDateTime?"
                            defaultValue = "null"
                        } else if (dataType VbSame "date") {
                            kotlinType = "LocalDate?"
                            defaultValue = "null"
                        } else if (dataType VbSame "float") {
                            kotlinType = "Float"
                            defaultValue = "0F"
                        } else if (dataType VbSame "double") {
                            kotlinType = "Double"
                            defaultValue = "0.0"
                        } else if (dataType VbSame "long") {
                            kotlinType = "Long"
                            defaultValue = "0L"
                        } else if (dataType VbSame "tinyint") {
                            kotlinType = "Byte"
                            defaultValue = "0"
                        } else if (dataType VbSame "bigint") {
                            kotlinType = "Long"
                            defaultValue = "0L"
                        } else if (dataType VbSame "decimal") {
                            kotlinType = "BigDecimal"
                            defaultValue = "BigDecimal.ZERO"
                        }

                        var defs = mutableListOf<String>()
                        if (columnComment.HasValue) {
                            defs.add(
                                    """
/**
* ${columnComment}
*/""");
                        }

                        if (it.getStringValue("extra") == "auto_increment") {
                            defs.add("@SqlAutoIncrementKey")
                        }

                        defs.add("""var ${columnName}: ${kotlinType} = ${defaultValue}""")

                        return@colMap defs.joinToString(line_break)
                    }

            var uks = mutableListOf<String>();

            uks.add(columns_map.filter { it.getStringValue("table_name") == tableName && it.getStringValue("column_key") == "PRI" }
                    .map { it.getStringValue("column_name") }
                    .map { """"${it}"""" }
                    .joinToString(",")
            )

            indexes_map.filter { it.getStringValue("table_name") == tableName }
                    .groupBy { it.getStringValue("index_name") }
                    .forEach {
                        uks.add(it.value.map { it.getStringValue("column_name") }
                                .map { """"${it}"""" }
                                .joinToString(",")
                        )
                    }


            return@map """
/**
* ${tableComment}
*/
@DbEntityGroup("${group}")
@SqlUks(${uks.joinToString(",")})
data class ${tableName}(
    ${columns.joinToString(",\n").replace("\n", "\n\t")}
): IBaseDbEntity()
"""
        }
    }

    fun entity2Sql(entity: KClass<*>): String {
        var list = entity.memberProperties.map { property ->
            var propertyType = property.javaField!!.type as Class<*>
            var type = propertyType.name

            if (property.javaField!!.type == String::class.java) {
                type = "varchar(50)"
            }
            if (propertyType == Int::class.java || propertyType.name == "java.lang.Integer") {
                type = "int"
            }

            if (propertyType == LocalDateTime::class.java || propertyType == Date::class.java) {
                type = "DateTime"
            }

            if (propertyType == Boolean::class.java || propertyType.name == "java.lang.Boolean") {
                type = "bit"
            }

            return@map """`${propertyType.name}` ${type} not null ${if (propertyType.IsNumberType()) "default '0'" else if (propertyType.IsStringType()) "default ''" else ""} comment ''"""
        }

        return """
DROP TABLE IF EXISTS `${entity.simpleName}`;
CREATE TABLE IF NOT EXISTS `${entity.simpleName}` (
  ${list.map { it + line_break + "," }.joinToString("")}
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT COMMENT='';
"""
    }
}