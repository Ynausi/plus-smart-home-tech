package ru.yandex.practicum.order.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Configuration
public class FeignClientConfig {
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String REQUEST_ID_ATTRIBUTE = FeignClientConfig.class.getName() + ".requestId";

    @Bean
    public RequestInterceptor requestIdInterceptor() {
        return template -> template.header(REQUEST_ID_HEADER, resolveRequestId());
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
