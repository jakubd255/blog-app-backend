package pl.jakubdudek.blogappbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;


@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class PaginationConfig {
    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer customize() {
        return pageableResolver -> pageableResolver.setFallbackPageable(PageRequest.of(0, 20));
    }
}
