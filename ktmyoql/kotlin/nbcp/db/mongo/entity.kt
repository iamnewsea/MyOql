package nbcp.db.mongo.entity

import nbcp.db.DbEntityGroup
import nbcp.db.IdName
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import nbcp.db.mongo.*

//系统附件表
@Document
@DbEntityGroup("base")
open class SysAnnex(
        var name: String = "",          //显示的名字,友好的名称
        var tags: List<String> = listOf(),
        var ext: String = "",           //后缀名。
        var size: Int = 0,              //大小
        var checkCode: String = "",     //Md5,Sha1
        var imgWidth: Int = 0,          // 图像宽度值。
        var imgHeight: Int = 0,         // 图像宽度值。
        var url: String = "",           //下载的路径。没有 host

        var createBy: IdName = IdName(), //创建者
        var corpId: String = "",    //创建所属企业,如果是 admin,则 id = "admin" , name = "admin 即可
        var errorMsg: String = "",      //文件处理时的错误消息
        var createAt: LocalDateTime = LocalDateTime.now()
) : IMongoDocument() {
}


@Document
@DbEntityGroup("base")
open class SysLog(
        var msg: String = "",
        var type: String = "",
        var clientIp: String = "",
        var module: String = "",
        var remark: String = "",
        var data: Any? = null,
        var creatAt: LocalDateTime = LocalDateTime.now(),
        var createBy: String = ""
) : IMongoDocument() {
}

//存放删除的数据。
@Document
@DbEntityGroup("base")
open class SysDustbin(
        var table: String = "",
        var remark: String = "",
        var creator: IdName = IdName(),
        var data: Any = Object(),
        var createAt: LocalDateTime = LocalDateTime.now()
) : IMongoDocument()

