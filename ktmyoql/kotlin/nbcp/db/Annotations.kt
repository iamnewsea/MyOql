package nbcp.db

import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Component
import java.lang.annotation.Documented
import java.lang.annotation.Inherited
import kotlin.reflect.KClass


/**
 * 生成元数据的组
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class MetaDataGroup(val group: String)

/**
 * 标记实体删除之前是否保存到垃圾箱
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RemoveToSysDustbin()


/**
 * 记录当更新某些字段时,记录到日志表
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DbEntityLogHistory(vararg val fields: String)

///**
// * 标注 实体更新的 Bean，需要实现  IDbEntityUpdate
// */
//@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
//@Retention(AnnotationRetention.RUNTIME)
//@Component
//annotation class DbEntityUpdate(@get:AliasFor(annotation = Component::class) val value: String = "")
//
//@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
//@Retention(AnnotationRetention.RUNTIME)
//@Component
//annotation class DbEntityInsert(@get:AliasFor(annotation = Component::class) val value: String = "")
//
///**
// * 标注 实体删除的 Bean，需要实现  IDbEntityDelete
// */
//@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
//@Retention(AnnotationRetention.RUNTIME)
//@Component
//annotation class DbEntityDelete(@get:AliasFor(annotation = Component::class) val value: String = "")


/**
 * 重复性
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
annotation class DbEntityFieldRefs(val values: Array<DbEntityFieldRef>)

/**
 * 标记Mongo字段是另一个表字段的引用， 当另一个表字段更新后，更新该字段
 * 例子： 对user表标注：
 * DbEntityFieldRef("SysCorporation","corp.id:id","corp.name:name")
 * 或，如果fieldName相同，可省略后面的name
 * DbEntityFieldRef("SysCorporation","corp.id","corp.name")
 * 当 SysCorporation.name 发生变化后， 该表自动更新。
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@java.lang.annotation.Repeatable(DbEntityFieldRefs::class)
@Repeatable
@Inherited
annotation class DbEntityFieldRef(val masterEntityClass: KClass<*>,val idFieldMap: String, val nameFieldMap: String)



