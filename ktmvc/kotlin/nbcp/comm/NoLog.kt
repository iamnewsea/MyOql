package nbcp.comm

/**
 * Created by udi on 17-3-30.
 */

/**
 * 不需要记录日志的注解
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class NoLog