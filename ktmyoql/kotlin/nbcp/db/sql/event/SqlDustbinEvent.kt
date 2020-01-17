package nbcp.db.sql

import nbcp.base.extend.ToJson
import nbcp.base.utils.CodeUtil
import nbcp.db.*
import nbcp.db.sql.entity.s_dustbin
import nbcp.db.sql.table.BaseGroup
import org.springframework.stereotype.Component

@DbEntityDelete( )
class SqlDustbinEvent : ISqlEntityDelete {

    override fun beforeDelete(delete: SqlDeleteClip<*, *>): DbEntityEventResult? {
        var dust = delete.mainEntity.tableClass.getAnnotation(MongoEntitySysDustbin::class.java)
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

        BaseGroup.s_dustbin_table().insert(s_dustbin())
//        SqlInsertClip(delete.mainEntity).insert(dustbin)
//        delete.mongoTemplate.insert(dustbin)
    }
}