package nbcp.db.mongo.component

import nbcp.base.extend.InfoError
import nbcp.base.extend.IsSimpleType
import nbcp.base.extend.ToJson
import nbcp.db.db
import nbcp.db.mongo.IMongoWhereable
import nbcp.db.mongo.MongoClipBase
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import java.time.LocalDateTime

open class MongoBaseUpdateClip(tableName: String) : MongoClipBase(tableName), IMongoWhereable {
    companion object {
        private val logger by lazy {
            return@lazy LoggerFactory.getLogger(this::class.java)
        }
    }

    var whereData = mutableListOf<Criteria>()
        private set

    protected var setData = LinkedHashMap<String, Any?>()
    protected var unsetData = mutableListOf<String>()
    protected val pushData = LinkedHashMap<String, Any>() //加
    protected val pullData = LinkedHashMap<String, Any>() //删
    protected val incData = LinkedHashMap<String, Int>() //

    fun setValue(column: String, value: Any?) {
        this.setData.put(column, value);
    }

    fun getChangedFieldData(): Map<String, Any?> {
        var ret = mutableMapOf<String, Any?>()
        ret.putAll(this.setData);
        ret.putAll(this.unsetData.map { it to null })
        return ret;
    }


    /**
     * 更新条件不能为空。
     */
    fun exec(): Int {
        if (whereData.size == 0) {
            throw RuntimeException("更新条件为空，不允许更新")
            return 0;
        }

        if (this.setData.containsKey("updateAt") == false) {
            this.setData.put("updateAt", LocalDateTime.now())
        }
        return execAll();
    }


    /**
     * 更新条件可以为空。
     */
    fun execAll(): Int {
        db.affectRowCount = -1;
//        procMongo_IdColumn(whereData);

        var criteria = this.getMongoCriteria(*whereData.toTypedArray());


//        for (wkv in whereData) {
//            criteria = criteria.and(wkv.first).`is`(wkv.second);
//        }

        var update = org.springframework.data.mongodb.core.query.Update();

        for (kv in setData) {
            if (kv.value != null) {
                update = update.set(kv.key, kv.value!!);
            } else {
                update = update.unset(kv.key);
            }
        }

        for (it in unsetData) {
            update = update.unset(it);
        }

        for (kv in pushData) {
            update = update.push(kv.key, kv.value);
        }


        for (kv in pullData) {
//            procMongo_IdColumn(kv.value)
            var value = kv.value;
            if (value is Criteria) {
                update = update.pull(kv.key, value.criteriaObject);
            } else {
                var type = value::class.java;
                if (type.IsSimpleType() == false) {
                    throw RuntimeException("pull 必须是简单类型")
                }

                update = update.pull(kv.key, value);
            }
        }

        incData.forEach {
            if (it.value != 0) {
                update = update.inc(it.key, it.value);
            }
        }

        //如果没有要更新的列.
        if (update.updateObject.keys.size == 0) {
            return 0;
        }


//        var eventObject: MongoUpdateEventObject? = null;
//        if (whereCriteriaObject.keys.contains("_id")) {
//            var _id_value = whereCriteriaObject["_id"].toString();
//
//            if (_id_value.HasValue) {
//                eventObject = MongoUpdateEventObject(collectionClazz, _id_value, update.updateObject)
//            }
//        }


        var settingResult = db.mongoEvents.onUpdating(this)
        if (settingResult.any { (it.second?.result ?: true) == false }) {
            return 0;
        }

        var ret = 0;
        try {
            var result = mongoTemplate.updateMulti(
                    Query.query(criteria),
                    update,
                    collectionName);

            if (result.modifiedCount > 0) {
                settingResult.forEach {
                    it.first.update(this, it.second)
                }
            }

            ret = result.matchedCount.toInt();
            db.affectRowCount = ret
        } catch (e: Exception) {
            ret = -1;
            throw e;
        } finally {
            logger.InfoError(ret < 0) {
                "update:[" + this.collectionName + "],where:" + criteria.criteriaObject.ToJson() + ",set:" + update.updateObject.ToJson() + " ,result:" + ret
            }
        }


        return ret;
    }
}