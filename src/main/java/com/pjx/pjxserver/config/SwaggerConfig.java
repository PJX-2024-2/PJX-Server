package com.pjx.pjxserver.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@OpenAPIDefinition(info = @Info(title = "Ittory", description = "Ittory API 명세서", version = "v1"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@Server(url = "/")
public class SwaggerConfig {
    @Bean
    public OperationCustomizer operationCustomizer() {
        return (operation, handlerMethod) -> {
            this.addResponseBodyWrapper(operation);
            return operation;
        };
    }

    private void addResponseBodyWrapper(Operation operation) {
        Content content = operation.getResponses().get("200").getContent();
        if (content != null) {
            content.forEach((mediaTypeKey, mediaType) -> {
                Schema<?> originalSchema = mediaType.getSchema();
                Schema<?> wrappedSchema = wrapSchema(originalSchema);
                mediaType.setSchema(wrappedSchema);
            });
        }
    }

    private Schema<?> wrapSchema(Schema<?> originalSchema) {
        Schema<?> wrapperSchema = new Schema<>();

        wrapperSchema.addProperty("success", new Schema<>().type("boolean").example(true));
        wrapperSchema.addProperty("status", new Schema<>().type("integer").example(200));
        wrapperSchema.addProperty("data", originalSchema);

        return wrapperSchema;
    }
}

