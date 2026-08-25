package io.github.alexisTrejo11.construction.company.config.rateLimiter;

import io.github.bucket4j.Bucket;
import io.github.alexisTrejo11.construction.company.config.security.CurrentUserArgumentResolver;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final Bucket bucket;
    private final CurrentUserArgumentResolver currentUserArgumentResolver;

    public WebMvcConfig(
        Bucket bucket,
        CurrentUserArgumentResolver currentUserArgumentResolver
    ) {
        this.bucket = bucket;
        this.currentUserArgumentResolver = currentUserArgumentResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor(bucket))
                .addPathPatterns("/**");
    }

    @Override
    public void addArgumentResolvers(List<org.springframework.web.method.support.HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }
}
