package nbcp.db.sql

import nbcp.base.extend.ToJson
import nbcp.base.utils.CodeUtil
import nbcp.db.*
import nbcp.db.sql.entity.s_dustbin
import org.springframework.stereotype.Component

@Component
class SqlDustbinEvent : ISqlEntityDelete {

    override fun beforeDelete(delete: SqlDeleteClip<*, *>): DbEntityEventResult? {
        var dust = delete.mainEntity.tableClass.getAnnotation(RemoveToSysDustbin::class.java)
        if (dust != null) {
            //找出数据
            var where = delete.whereDatas.toSingleData()
            where.expression = "select * from " + delete.mainEntity.fromTableName + " where " + where.expression
            var cursor = RawQuerySqlClip(where, delete.mainEntity).toMapList()
            return DbEntityEventResult(true, cursor)
        }

        return null;
    }

    override fun delete(delete: SqlDeleteClip<*, *>, eventData: DbEntityEventResult?) {
        var data = eventData?.extData
        if (data == null) return

        var dustbin = s_dustbin()
        dustbin.id = CodeUtil.getCode()
        dustbin.table = delete.mainEntity.tableName
        dustbin.data = data.ToJson()

        db.sql_base.s_dustbin.doInsert(dustbin)
    }
}