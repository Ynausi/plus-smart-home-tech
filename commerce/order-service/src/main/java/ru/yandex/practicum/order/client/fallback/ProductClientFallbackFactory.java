package ru.yandex.practicum.order.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.order.client.ProductClient;
import ru.yandex.practicum.order.exception.OrderProcessingException;
import ru.yandex.practicum.order.exception.ProductServiceUnavailableException;

@Slf4j
@Component
public class ProductClientFallbackFactory implements FallbackFactory<ProductClient> {

    @Override
    public ProductClient create(Throwable cause) {
        OrderProcessingException businessFailure = findCause(cause, OrderProcessingException.class);

        return productId -> {
            if (businessFailure != null) {
                throw businessFailure;
            }

            log.warn(
                    "Fallback product-service: productId={}, причина={}",
                    productId,
                    rootMessage(cause)
            );

            throw new ProductServiceUnavailableException(productId, cause);
        };
    }

    private static <T extends Throwable> T findCause(Throwable source, Class<T> type) {
        Throwable current = source;
        while (current != null) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }
            current = current.getCause();
        }
        return null;
    }

    private static String rootMessage(Throwable source) {
        Throwable current = source;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getClass().getSimpleName() + ": " + current.getMessage();
    }
}
