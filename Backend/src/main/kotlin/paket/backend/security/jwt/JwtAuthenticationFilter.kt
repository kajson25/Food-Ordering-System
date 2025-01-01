package paket.backend.security.jwt

import io.jsonwebtoken.Claims
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenUtil: JwtTokenUtil,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = request.getHeader("Authorization")?.substringAfter("Bearer ")

        if (token != null && jwtTokenUtil.validateToken(token)) {
            val claims: Claims = jwtTokenUtil.extractClaims(token)
            val email = claims.subject
            val permissions = claims["permissions"] as List<*>

            // Add email and permissions to request attributes
            request.setAttribute("email", email)
            request.setAttribute("permissions", permissions)
        }

        filterChain.doFilter(request, response)
    }
}
