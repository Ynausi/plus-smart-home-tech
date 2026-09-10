package ru.yandex.practicum.order.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.order.client.InventoryClient;
import ru.yandex.practicum.order.client.dto.InventoryOperationRequest;
import ru.yandex.practicum.order.client.dto.InventoryOperationResponse;
import ru.yandex.practicum.order.exception.InventoryServiceUnavailableException;
import ru.yandex.practicum.order.exception.OrderProcessingException;

@Slf4j
@Component
public class InventoryClientFallbackFactory implements FallbackFactory<InventoryClient> {

    @Override
    public InventoryClient create(Throwable cause) {
        OrderProcessingException businessFailure = findCause(cause, OrderProcessingException.class);

        return new InventoryClient() {
            @Override
            public InventoryOperationResponse reserve(InventoryOperationRequest request) {
                if (businessFailure != null) {
                    throw businessFailure;
                }

                log.warn(
                        "Fallback inventory-service на reserve: productId={}, quantity={}, причина={}",
                        request.productId(),
                        request.quantity(),
                        rootMessage(cause)
                );

                throw new InventoryServiceUnavailableException(
                        request.productId(),
                        "reserve",
                        cause
                );
            }

            @Override
            public InventoryOperationResponse release(InventoryOperationRequest request) {
                if (businessFailure != null) {
                    throw businessFailure;
                }

                log.warn(
                        "Fallback inventory-service на release: productId={}, quantity={}, причина={}",
                        request.productId(),
                        request.quantity(),
                        rootMessage(cause)
                );

                throw new InventoryServiceUnavailableException(
                        request.productId(),
                        "release",
                        cause
                );
            }
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
