package nbcp.db.mongo

import nbcp.base.extend.*
import nbcp.base.line_break
import nbcp.base.utils.Md5Util
import nbcp.comm.JsonMap
import nbcp.comm.ListResult
import nbcp.db.db
import nbcp.db.mongo.IMongoWhereable
import nbcp.db.mongo.MongoClipBase
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.query.BasicQuery
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import java.lang.Exception

open class MongoBaseQueryClip (tableName:String ): MongoClipBase(tableName), IMongoWhereable {
    var whereData = mutableListOf<Criteria>()
    protected var skip: Int = 0;
    protected var take: Int = -1;
    protected var sort: Document = Document()
    //    private var whereJs: String = "";
    protected var selectColumns = mutableSetOf<String>();
    //    private var selectDbObjects = mutableSetOf<String>();
    protected var unSelectColumns = mutableSetOf<String>()

    fun selectField(column:String){
        this.selectColumns.add(column);
    }

    companion object {
        private val logger by lazy {
            return@lazy LoggerFactory.getLogger(this::class.java)
        }
    }

    /**
     * 返回该对象的 Md5。
     */
    private fun getCacheKey(): String {
        var unKeys = mutableListOf<String>()

        unKeys.add(whereData.map { it.criteriaObject.ToJson() }.joinToString("&"))
        unKeys.add(skip.toString())
        unKeys.add(take.toString())
        unKeys.add(sort.ToJson())
        unKeys.add(selectColumns.joinToString(","))
        unKeys.add(unSelectColumns.joinToString(","))


        return Md5Util.getBase64Md5(unKeys.joinToString("\n"));
    }

    fun <R> toList(clazz: Class<R>, mapFunc: ((Document) -> Unit)? = null): MutableList<R> {
        db.affectRowCount = -1;
        var isString = false;
        if (clazz.IsSimpleType()) {
            isString = clazz.name == "java.lang.String";
        }
//        else if (selectColumns.any() == false) {
//            if (Map::class.java.isAssignableFrom(clazz) == false) {
//                select(*clazz.fields.map { it.name }.toTypedArray())
//            }
//        }

        var criteria = this.getMongoCriteria(*whereData.toTypedArray());
        var projection = Document();
        selectColumns.forEach {
            projection.put(it, 1)
        }

//        selectDbObjects.forEach {
////            it.keys.forEach { key ->
////                projection.put(key, it.get(key))
////            }
////        }

        unSelectColumns.forEach {
            projection.put(it, 0)
        }

        var query = BasicQuery(criteria.toDocument(), projection);

        if (this.skip > 0) {
            query.skip(this.skip.AsLong())
        }

        if (this.take > 0) {
            query.limit(this.take)
        }

        if (sort.any()) {
            query.sortObject = sort
        }

        var cursor = mongoTemplate.find(query, Document::class.java, this.collectionName)

//        var cacheValue = MyCache.find(this.collectionName, this.getCacheKey())
//        if (cacheValue.HasValue) {
//            return cacheValue.FromJson<MutableList<R>>()
//        }


//        var mapper = mor.util.getMongoMapper(collectionName, clazz);

        var ret = mutableListOf<R>();
        var lastKey = selectColumns.lastOrNull() ?: ""
        var error = false;
        try {
            db.change_id2Id(cursor);
            cursor.forEach {
                //            if( it.containsField("_id")){
//                it.put("id",it.get("_id").toString())
//            }

                if (isString) {
                    if (lastKey.isEmpty()) {
                        lastKey = it.keys.last()
                    }

                    ret.add(it.GetComplexPropertyValue(lastKey) as R)
                } else if (clazz.IsSimpleType()) {
                    if (lastKey.isEmpty()) {
                        lastKey = it.keys.last()
                    }

                    ret.add(it.GetComplexPropertyValue(*lastKey.split(".").toTypedArray()) as R);
                } else {
                    if (Document::class.java.isAssignableFrom(clazz)) {
                        ret.add(it as R);
                    } else {
//                    var ent = mapper.toEntity(it)
                        var ent = it.ConvertJson(clazz)
                        ret.add(ent);
                    }
                }
            }

            db.affectRowCount = cursor.size;
        } catch (e: Exception) {
            error = true;
            throw e;
        } finally {
            fun getMsgs(): String {
                var msgs = mutableListOf<String>()
                msgs.add("query:[" + this.collectionName + "] ");
                msgs.add(" where:" + criteria.criteriaObject.ToJson())
                if (selectColumns.any()) {
                    msgs.add(" select:" + selectColumns.joinToString(","))
                }
                if (unSelectColumns.any()) {
                    msgs.add(" unselect:" + unSelectColumns.joinToString(","))
                }
                if (sort.any()) {
                    msgs.add(" sort:" + sort.ToJson())
                }
                if (skip > 0 || take > 0) {
                    msgs.add(" limit:${skip},${take}")
                }

                msgs.add(" result_count:" + cursor.size.toString())
                return msgs.joinToString(line_break);
            }

             logger.InfoError(error) { getMsgs() }
        }


//        if (total != null && skip == 0) {
//            if (ret.size < take) {
//                total(ret.size);
//            } else {
//                total(this.count())
//            }
//        }

        return ret
    }


    /**
     * 将忽略 skip , take
     */
    fun count(): Int {
        return mongoTemplate.count(Query.query(this.getMongoCriteria(*whereData.toTypedArray())), collectionName).toInt()
    }

    fun exists(): Boolean {
        return mongoTemplate.exists(Query.query(this.getMongoCriteria(*whereData.toTypedArray())), collectionName);
    }


    fun <R> toListResult(clazz: Class<R>, mapFunc: ((Document) -> Unit)? = null): ListResult<R> {
        var ret = ListResult<R>();
        ret.data = toList(clazz, mapFunc);

        if (this.skip == 0 && this.take > 0) {
            if (ret.data.size < this.take) {
                ret.total = ret.data.size;
            } else {
                ret.total = count()
            }
        }
        return ret;
    }


    fun toMapListResult(): ListResult<JsonMap> {
        return toListResult(JsonMap::class.java);
    }


}