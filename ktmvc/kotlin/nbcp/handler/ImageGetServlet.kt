package nbcp.handler

import nbcp.db.IdUrl
import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.DatabaseEnum
import nbcp.db.mongo.*
import nbcp.db.mongo.service.UploadFileMongoService
import nbcp.db.mysql.service.UploadFileMysqlService
import nbcp.web.MyHttpRequestWrapper
import nbcp.web.WriteTextValue
import nbcp.web.findParameterValue
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
    @Value("\${app.upload.path:}")
    private var uploadPath = ""

    @Value("\${app.upload.dbType:Mongo}")
    private var dbType = "Mongo"

    private val dbService by lazy {
        if (this.dbType VbSame DatabaseEnum.Mongo.toString()) {
            return@lazy SpringUtil.context.getBean(UploadFileMongoService::class.java)
        } else {
            return@lazy SpringUtil.context.getBean(UploadFileMysqlService::class.java)
        }
    }

    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {

        var id = request.findParameterValue("id").AsString();
        var url = request.findParameterValue("url").AsString();
        var width = request.findParameterValue("width").AsInt()
        var height = request.findParameterValue("height").AsInt()

        if (url.HasValue) {
            url = JsUtil.decodeURIComponent(url);
        }

        if (width <= 0 || height <= 0 || (id.isEmpty() && url.isEmpty())) {
            response.status = 500;
            response.WriteTextValue("参数不正确")
            return;
        }

        if (url.isEmpty()) {
            url = dbService.queryById(id)?.url ?: throw RuntimeException("找不到数据")
        }

        var ret = ImageUtil.zoomImageScale(File(uploadPath + url).inputStream().buffered(), response.outputStream, width, height)
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