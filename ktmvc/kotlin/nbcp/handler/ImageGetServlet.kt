package nbcp.handler

import nbcp.db.IdUrl
import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.DatabaseEnum
import nbcp.db.mongo.*
import nbcp.db.mongo.service.UploadFileMongoService
import nbcp.db.mysql.service.UploadFileMysqlService
import nbcp.web.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import java.io.File
import java.lang.RuntimeException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 动态获取图片，参数：
 * id,url: 两者必传其一
 * width,height
 */
@OpenAction
@WebServlet(urlPatterns = arrayOf("/image/dynamic"))
open class ImageGetServlet : HttpServlet() {

    private val dbService by lazy {
        if (config.databaseType VbSame DatabaseEnum.Mongo.toString()) {
            return@lazy SpringUtil.context.getBean(UploadFileMongoService::class.java)
        } else {
            return@lazy SpringUtil.context.getBean(UploadFileMysqlService::class.java)
        }
    }

    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        //附件数据库表中的Id
        var id = request.findParameterStringValue("id") ;

        //不带 host 头部的地址
        var url = request.findParameterStringValue("url") ;
        if (id.isEmpty() && url.isEmpty()) {
            throw ParameterInvalidException("参数非法", "id")
        }

        var width = request.findParameterIntValue("width")
        var height = request.findParameterIntValue("height")

        if (width <= 0 && height <= 0) {
            throw ParameterInvalidException("参数不正确", "width")
//            response.status = 500;
//            response.WriteTextValue("参数不正确")
//            return;
        }

        if (url.isEmpty()) {
            url = dbService.queryById(id)?.url ?: throw NoDataException("找不到数据")
        } else {
            url = JsUtil.decodeURIComponent(url);
        }

        var ret = ImageUtil.zoomImageScale(File(config.uploadPath + url).inputStream().buffered(), response.outputStream, width, height)
        if (ret.msg.HasValue) {
            response.WriteTextValue(ret.msg)
            return;
        }


        var contentType = MyUtil.getMimeType(ret.data!!)
        if (contentType.HasValue) {
            response.contentType = contentType
        }
        response.outputStream.write(ret.ToJson().toByteArray(utf8));
    }
}