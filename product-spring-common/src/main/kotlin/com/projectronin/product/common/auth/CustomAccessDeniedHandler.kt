import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.access.ExceptionTranslationFilter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class CustomAccessDeniedHandler : ExceptionTranslationFilter() {

    override fun handle(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        accessDeniedException: org.springframework.security.access.AccessDeniedException?
    ) {
        TODO("Not yet implemented")
    }
}
