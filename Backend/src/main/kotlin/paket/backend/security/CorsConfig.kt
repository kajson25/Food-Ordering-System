package paket.backend.security

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/**") // Allow all paths
            .allowedOrigins("http://localhost:4200") // Allow frontend origin (Do NOT use "*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allow specific methods
            .allowedHeaders("*") // Allow all headers
            .allowCredentials(true) // Allow credentials (important for JWT auth)
    }
}
