package com.shangluhua.app.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shangluhua.app.product.ProductResponse;
import com.shangluhua.app.product.ProductService;
import com.shangluhua.app.product.ProductSpu;
import com.shangluhua.app.product.ProductStatus;
import com.shangluhua.app.sales.CreateSalesOrderRequest;
import com.shangluhua.app.sales.SalesOrder;
import com.shangluhua.app.sales.SalesOrderResponse;
import com.shangluhua.app.sales.SalesOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShareController.class)
@AutoConfigureMockMvc(addFilters = false)
class ShareControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private SalesOrderService salesOrderService;

    @MockBean
    private com.shangluhua.app.auth.JwtUtil jwtUtil;

    @MockBean
    private com.shangluhua.app.auth.AdminUserRepository adminUserRepository;

    @Test
    void shouldReturnProductList() throws Exception {
        ProductSpu spu = new ProductSpu();
        ReflectionTestUtils.setField(spu, "id", 1L);
        spu.setCode("P001");
        spu.setName("测试商品");
        spu.setStatus(ProductStatus.ON_SALE);

        when(productService.search(any(), anyInt(), anyInt())).thenReturn(List.of(spu));

        mockMvc.perform(get("/api/share/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("P001"))
                .andExpect(jsonPath("$[0].name").value("测试商品"));
    }

    @Test
    void shouldCreateOrder() throws Exception {
        SalesOrder order = new SalesOrder();
        ReflectionTestUtils.setField(order, "id", 1L);
        order.setOrderNo("SO2026051900011234");
        order.setTotalAmount(new BigDecimal("200.00"));
        order.setDebtAmount(new BigDecimal("150.00"));

        when(salesOrderService.create(any())).thenReturn(order);

        String body = """
                {
                    "items": [{"skuId": 1, "quantity": 2, "unitPrice": 100.00}],
                    "paidAmount": 50.00,
                    "contactName": "张三",
                    "contactPhone": "13800138000"
                }
                """;

        mockMvc.perform(post("/api/share/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNo").value("SO2026051900011234"));
    }
}
