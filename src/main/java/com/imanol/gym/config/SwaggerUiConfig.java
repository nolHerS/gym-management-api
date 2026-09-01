package com.imanol.gym.config;

import org.springdoc.webmvc.ui.SwaggerIndexTransformer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;

import java.nio.charset.StandardCharsets;

@Configuration
public class SwaggerUiConfig implements WebMvcConfigurer {

    private static final String CUSTOM_CSS_PATH = "/swagger-ui-custom.css";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(CUSTOM_CSS_PATH)
                .addResourceLocations("classpath:/static/");
    }

    @Bean
    public SwaggerIndexTransformer swaggerIndexTransformer() {
        return (request, resource, transformerChain) -> {
            Resource transformed = transformerChain.transform(request, resource);

            if (!request.getRequestURI().endsWith("/index.html")) {
                return transformed;
            }

            String html;
            try (var inputStream = transformed.getInputStream()) {
                html = new String(
                        inputStream.readAllBytes(),
                        StandardCharsets.UTF_8
                );
            }

            String stylesheet = "<link rel=\"stylesheet\" href=\""
                    + CUSTOM_CSS_PATH
                    + "\">";

            if (!html.contains(CUSTOM_CSS_PATH)) {
                html = html.replace("</head>", stylesheet + "</head>");
            }

            return new TransformedResource(
                    transformed,
                    html.getBytes(StandardCharsets.UTF_8)
            );
        };
    }
}
