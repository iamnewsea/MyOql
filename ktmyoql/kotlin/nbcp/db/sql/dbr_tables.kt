
package nbcp.db.sql.table

import nbcp.db.*
import nbcp.db.sql.*
import nbcp.db.sql.entity.*
import nbcp.db.mysql.*
import nbcp.db.mysql.entity.*
import nbcp.base.extend.*
import nbcp.base.utils.*
import org.springframework.stereotype.Component

//generate auto @2020-01-11 18:44:09


@Component("sql.base")
@DataGroup("base")
class BaseGroup : IDataGroup{
    override fun getEntities():Set<SqlBaseTable<*>> = setOf(s_annex,s_log)

    val s_annex by lazy{ return@lazy s_annex_table(); }
    val s_log by lazy{ return@lazy s_log_table(); }


    
    class s_annex_table(datasource:String=""):SqlBaseTable<s_annex>(s_annex::class.java,"s_annex",datasource) {
        val id=SqlColumnName(DbType.String,this.getAliaTableName(),"id")
        val name=SqlColumnName(DbType.String,this.getAliaTableName(),"name")
        val ext=SqlColumnName(DbType.String,this.getAliaTableName(),"ext")
        val size=SqlColumnName(DbType.Int,this.getAliaTableName(),"size")
        val checkCode=SqlColumnName(DbType.String,this.getAliaTableName(),"checkCode")
        val imgWidth=SqlColumnName(DbType.Int,this.getAliaTableName(),"imgWidth")
        val imgHeight=SqlColumnName(DbType.Int,this.getAliaTableName(),"imgHeight")
        val url=SqlColumnName(DbType.String,this.getAliaTableName(),"url")
        val createby_id=SqlColumnName(DbType.Int,this.getAliaTableName(),"createby_id")
        val createby_name=SqlColumnName(DbType.String,this.getAliaTableName(),"createby_name")
        val corp_id=SqlColumnName(DbType.String,this.getAliaTableName(),"corp_id")
        val errorMsg=SqlColumnName(DbType.String,this.getAliaTableName(),"errorMsg")
        val createAt=SqlColumnName(DbType.DateTime,this.getAliaTableName(),"createAt")
    
        override fun getAutoIncrementKey(): String { return ""}
        override fun getUks(): Array<Array<String>>{ return arrayOf( arrayOf("id")  )}
        override fun getRks(): Array<Array<String>>{ return arrayOf( arrayOf("corp_id")  )}
        override fun getFks(): Array<FkDefine>{ return arrayOf()}
    
    
        fun queryById (id: String ): SqlQueryClip<s_annex_table, s_annex> {
            return this.query().where{ (it.id match_equal id) }
        }
    
    
        fun findById (id: String ): s_annex? {
            return this.query().where{ (it.id match_equal id) }.limit(0,1).toEntity()
        }
    
        fun deleteById (id: String ): SqlDeleteClip<s_annex_table,s_annex> {
            return this.delete().where{ (it.id match_equal id) }
        }
    
        fun updateById (id: String ): SqlUpdateClip<s_annex_table,s_annex> {
            return this.update().where{ (it.id match_equal id) }
        }
    
    }
    
    class s_log_table(datasource:String=""):SqlBaseTable<s_log>(s_log::class.java,"s_log",datasource) {
        val id=SqlColumnName(DbType.String,this.getAliaTableName(),"id")
        val msg=SqlColumnName(DbType.String,this.getAliaTableName(),"msg")
        val creatAt=SqlColumnName(DbType.DateTime,this.getAliaTableName(),"creatAt")
        val createBy_id=SqlColumnName(DbType.String,this.getAliaTableName(),"createBy_id")
        val createBy_name=SqlColumnName(DbType.String,this.getAliaTableName(),"createBy_name")
        val corp_id=SqlColumnName(DbType.String,this.getAliaTableName(),"corp_id")
        val type=SqlColumnName(DbType.String,this.getAliaTableName(),"type")
        val clientIp=SqlColumnName(DbType.String,this.getAliaTableName(),"clientIp")
        val module=SqlColumnName(DbType.String,this.getAliaTableName(),"module")
        val remark=SqlColumnName(DbType.String,this.getAliaTableName(),"remark")
    
        override fun getAutoIncrementKey(): String { return ""}
        override fun getUks(): Array<Array<String>>{ return arrayOf( arrayOf("id")  )}
        override fun getRks(): Array<Array<String>>{ return arrayOf( )}
        override fun getFks(): Array<FkDefine>{ return arrayOf()}
    
    
        fun queryById (id: String ): SqlQueryClip<s_log_table, s_log> {
            return this.query().where{ (it.id match_equal id) }
        }
    
    
        fun findById (id: String ): s_log? {
            return this.query().where{ (it.id match_equal id) }.limit(0,1).toEntity()
        }
    
        fun deleteById (id: String ): SqlDeleteClip<s_log_table,s_log> {
            return this.delete().where{ (it.id match_equal id) }
        }
    
        fun updateById (id: String ): SqlUpdateClip<s_log_table,s_log> {
            return this.update().where{ (it.id match_equal id) }
        }
    
    }
}