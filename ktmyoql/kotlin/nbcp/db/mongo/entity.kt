package nbcp.db.mongo.entity

import nbcp.db.*
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import nbcp.db.mongo.*

//--------------------------------------------------------
/**
 * 用户信息
 */
@Document
@DbEntityGroup("MongoBase")
@RemoveToSysDustbin
open class SsoUser(
        var loginName: String = "",
        var mobile: String = "",
        var email: String = "",

        var name: String = "",
        var logo: IdUrl = IdUrl(), //头像.

        var idCard: UserIdCardData = UserIdCardData(),

        var liveCity: IntCodeName = IntCodeName(),
        var liveLocation: String = "",  //常住地
        var corpName: String = "",
        var job: String = "",
        var workCity: IntCodeName = IntCodeName(),
        var workLocation: String = ""  //工作地
) : IMongoDocument()

/**
 * 登录信息
 */
@Document
@DbEntityGroup("MongoBase")
data class SsoLoginUser(
        var userId: String = "",   //用户Id
        var loginName: String = "",
        var mobile: String = "",    //认证后更新
        var email: String = "",     //认证后更新

        var password: String = "",  // Md5Util.getBase64Md5(pwd)
        var lastLoginAt: LocalDateTime = LocalDateTime.now(),

        var authorizeCode: String = "", //授权码
        var token: String = "",     //常用数据，也会放到主表。
        var freshToken: String = "",
        var authorizeCodeCreateAt: LocalDateTime = LocalDateTime.now(),

        var grantApps:MutableList<IdName> = mutableListOf(),  //授权的App
        var isLocked: Boolean = false,
        var lockedRemark: String = ""
) : IMongoDocument()


/**
 * 应用中心
 */
@Document
@DbEntityGroup("MongoBase")
data class SysApplication(
        var key: String = "",           // 应用Id，CodeUtil.getCode()
        var name: String = "",
        var slogan: String = "",                    // 广告语， 每次登录的时候显示
        var logo: IdUrl = IdUrl(),      //应用Logo
        var siteUrl: String = "",         //展示信息，应用主站
        var remark: String = "",
        var hostDomainName: String = "",            // 安全域名，http 或 https 开头。
        var secret: String = "",        //应用密钥，Api用。
        var userUpdateHookCallbackUrl: String = "",    //用户更新后，通知App的回调。
        var authorizeRange: MutableList<String> = mutableListOf(),  //需要授权的信息
        var isLocked: Boolean = false,
        var loadRemark: String = ""
) : IMongoDocument()



//系统附件表
@Document
@DbEntityGroup("MongoBase")
open class SysAnnex(
        var name: String = "",          //显示的名字,友好的名称
        var tags: List<String> = listOf(),
        var ext: String = "",           //后缀名。
        var size: Int = 0,              //大小
        var checkCode: String = "",     //Md5,Sha1
        var imgWidth: Int = 0,          // 图像宽度值。
        var imgHeight: Int = 0,         // 图像宽度值。
        var url: String = "",           //下载的路径。没有 host

        var creator: IdName = IdName(), //创建者
        var corpId: String = "",    //创建所属企业,如果是 admin,则 id = "admin" , name = "admin 即可
        var errorMsg: String = ""      //文件处理时的错误消息
) : IMongoDocument() {
}


@Document
@DbEntityGroup("MongoBase")
data class SysCity(
        var code: Int = 0,
        var name: String = "",
        var fullName: String = "",
        var level: Int = 0,
        var lng: Float = 0F, //经度
        var lat: Float = 0F, //纬度
        var pinyin: String = "",
        var telCode: String = "",
        var postCode: String = "",
        var pcode: Int = 0
) : IMongoDocument()

@Document
@DbEntityGroup("MongoBase")
open class SysLog(
        var module: String = "", //模块
        var type: String = "",  //类型
        var key: String = "",   //实体标志, 查询用： module + key
        var msg: String = "",   //消息
        var data: Any? = null,
        var remark: String = "",
        var clientIp: String = "",
        var creatAt: LocalDateTime = LocalDateTime.now(),
        var creatorId: String = ""
) : IMongoDocument() {
}

//存放删除的数据。
@Document
@DbEntityGroup("MongoBase")
open class SysDustbin(
        var table: String = "",
        var remark: String = "",
        var creator: IdName = IdName(),
        var data: Any = Object()
) : IMongoDocument()



