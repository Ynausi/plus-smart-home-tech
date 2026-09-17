package ru.yandex.practicum.order.config;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.yandex.practicum.order.exception.OrderProcessingException;

import java.util.UUID;

@Configuration
public class FeignClientConfig {
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String REQUEST_ID_ATTRIBUTE = FeignClientConfig.class.getName() + ".requestId";

    @Bean
    public RequestInterceptor requestIdInterceptor() {
        return template -> template.header(REQUEST_ID_HEADER, resolveRequestId());
    }

    @Bean
    public ErrorDecoder orderFeignErrorDecoder() {
        ErrorDecoder defaultDecoder = new ErrorDecoder.Default();

        return (methodKey, response) -> {
            int status = response.status();

            if (status >= 400 && status < 500) {
                if (methodKey.contains("ProductClient")) {
                    if (status == 404) {
                        return new OrderProcessingException("Товар не найден");
                    }
                    return new OrderProcessingException(
                            "product-service отклонил запрос, HTTP " + status
                    );
                }

                if (methodKey.contains("InventoryClient")) {
                    if (status == 404) {
                        return new OrderProcessingException("Складская запись не найдена");
                    }
                    if (status == 409) {
                        return new OrderProcessingException("Недостаточно товара на складе");
                    }
                    if (status == 400) {
                        return new OrderProcessingException(
                                "inventory-service отклонил складскую операцию"
                        );
                    }
                    return new OrderProcessingException(
                            "inventory-service отклонил запрос, HTTP " + status
                    );
                }
            }

            return defaultDecoder.decode(methodKey, response);
        };
    }

    private String resolveRequestId() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();

            String incomingRequestId = request.getHeader(REQUEST_ID_HEADER);
            if (StringUtils.hasText(incomingRequestId)) {
                return incomingRequestId;
            }

            Object generatedRequestId = request.getAttribute(REQUEST_ID_ATTRIBUTE);
            if (generatedRequestId != null) {
                return generatedRequestId.toString();
            }

            String requestId = UUID.randomUUID().toString();
            request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
            return requestId;
        }

        return UUID.randomUUID().toString();
    }
}
