package ru.yandex.practicum.order;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.order.client.InventoryClient;
import ru.yandex.practicum.order.client.ProductClient;
import ru.yandex.practicum.order.client.dto.InventoryOperationResponse;
import ru.yandex.practicum.order.client.dto.ProductClientResponse;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderItemRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("unchecked")
class OrderServiceAcceptanceTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @MockBean
    private ProductClient productClient;

    @MockBean
    private InventoryClient inventoryClient;

    @BeforeEach
    void setUpRemoteServices() {
        when(productClient.findById(1L))
                .thenReturn(new ProductClientResponse(
                        1L,
                        "Acceptance Smart Lamp",
                        new BigDecimal("3490.00"),
                        true
                ));
        when(productClient.findById(2L))
                .thenReturn(new ProductClientResponse(
                        2L,
                        "Acceptance Smart Plug",
                        new BigDecimal("1290.00"),
                        true
                ));
        when(inventoryClient.reserve(any()))
                .thenReturn(new InventoryOperationResponse(true, 10, "OK"));
        when(inventoryClient.release(any()))
                .thenReturn(new InventoryOperationResponse(true, 10, "OK"));
    }

    @Test
    void shouldCreateConfirmedOrderUsingProductAndInventoryClients() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(
                "Acceptance Buyer",
                "acceptance-buyer@example.com",
                List.of(
                        new OrderItemRequest(1L, 2),
                        new OrderItemRequest(2L, 1)
                )
        );

        MvcResult createResponse = postJson("/api/orders", request);

        assertThat(status(createResponse))
                .as("POST /api/orders должен создавать заказ и возвращать HTTP 201 Created")
                .isEqualTo(201);

        Map<String, Object> created = readMap(createResponse);
        Long orderId = asLong(created.get("id"));

        assertThat(orderId)
                .as("Созданный заказ должен содержать поле id")
                .isNotNull();
        assertThat(created.get("status"))
                .as("При успешных ответах каталога и склада заказ подтверждается")
                .isEqualTo("CONFIRMED");
        assertThat(asDecimal(created.get("totalPrice")))
                .as("order-service рассчитывает totalPrice по данным product-service")
                .isEqualByComparingTo("8270.00");
        assertThat((List<?>) created.get("items"))
                .as("Заказ должен хранить снимок товарных данных")
                .hasSize(2)
                .anySatisfy(item -> assertThat((Map<String, Object>) item)
                        .containsEntry("productName", "Acceptance Smart Lamp"));

        MvcResult byIdResponse = mvc.perform(get("/api/orders/{id}", orderId)).andReturn();
        assertThat(status(byIdResponse)).isEqualTo(200);
        assertThat(readMap(byIdResponse).get("customerEmail"))
                .isEqualTo("acceptance-buyer@example.com");

        MvcResult byEmailResponse = mvc.perform(get("/api/orders/by-email")
                .param("email", "acceptance-buyer@example.com"))
                .andReturn();
        assertThat(status(byEmailResponse)).isEqualTo(200);
        assertThat(readList(byEmailResponse))
                .anySatisfy(item -> assertThat(item)
                        .containsEntry("customerEmail", "acceptance-buyer@example.com"));
    }

    @Test
    void shouldReturnBadRequestForInvalidOrderPayload() throws Exception {
        CreateOrderRequest invalidRequest = new CreateOrderRequest(
                "",
                "not-an-email",
                List.of()
        );

        MvcResult response = postJson("/api/orders", invalidRequest);

        assertThat(status(response)).isEqualTo(400);
        assertThat(readMap(response)).containsKeys("message", "validationErrors");
    }

    private MvcResult postJson(String url, Object body) throws Exception {
        return mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(body)))
                .andReturn();
    }

    private static int status(MvcResult result) {
        return result.getResponse().getStatus();
    }

    private Map<String, Object> readMap(MvcResult result) throws Exception {
        return json.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
        });
    }

    private List<Map<String, Object>> readList(MvcResult result) throws Exception {
        return json.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
        });
    }

    private static Long asLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    private static BigDecimal asDecimal(Object value) {
        return new BigDecimal(value.toString());
    }
}
