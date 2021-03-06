package nbcp.db.mongo.event;

import nbcp.db.mongo.*;
import nbcp.db.EventResult

interface IMongoEntityInsert {
    fun beforeInsert(insert: MongoBaseInsertClip): EventResult

    fun insert(insert: MongoBaseInsertClip, eventData: EventResult)
}

/**
 * 实体Update接口，标记 DbEntityUpdate 注解的类使用。
 */
interface IMongoEntityUpdate {
    fun beforeUpdate(update: MongoBaseUpdateClip): EventResult

    fun update(update: MongoBaseUpdateClip, eventData: EventResult)
}

/**
 * 实体 Delete 接口，标记 DbEntityDelete 注解的类使用。
 */
interface IMongoEntityDelete {
    fun beforeDelete(delete: MongoDeleteClip<*>): EventResult

    fun delete(delete: MongoDeleteClip<*>, eventData: EventResult)
}