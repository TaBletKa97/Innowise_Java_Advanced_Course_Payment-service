package com.innowise.paymentservice.controller;

import com.innowise.paymentservice.external.RandomHttpClient;
import com.innowise.paymentservice.messagebrokers.MessageBroker;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.repository.entity.Payment;
import com.innowise.paymentservice.repository.entity.PaymentStatus;
import com.innowise.paymentservice.service.dto.PaymentCreateRequestDto;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.innowise.paymentservice.repository.entity.PaymentStatus.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PaymentControllerTest {

    private static final String HEADER_USER_ID = "user_id";
    private static final String HEADER_ROLE = "role";
    private static final String HEADER_VALUE_USER = "USER";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RandomHttpClient client;

    @MockitoBean
    private MessageBroker broker;

    private static MongoDBContainer mongo;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @BeforeAll
    static void init() {
        DockerImageName myImage =
                DockerImageName.parse("amd64/mongo:8.0-noble")
                        .asCompatibleSubstituteFor("mongo");
        mongo = new MongoDBContainer(myImage);
        mongo.start();
    }

    @BeforeEach
    void setUp() {
        var pay1 = new Payment();
        var pay2 = new Payment();
        var pay3 = new Payment();
        var pay4 = new Payment();
        var pay5 = new Payment();
        var pay6 = new Payment();

        pay1.setId("69e3aacf8951b6a63b24edab");
        pay1.setUserId(1L);
        pay1.setOrderId(1L);
        pay1.setStatus(SUCCESS);
        pay1.setTimestamp(LocalDateTime.now().minusDays(2));
        pay1.setPaymentAmount(BigDecimal.TEN);

        pay2.setId("69e3aacf8951b6a63b24eda3");
        pay2.setUserId(1L);
        pay2.setOrderId(2L);
        pay2.setStatus(SUCCESS);
        pay2.setTimestamp(LocalDateTime.now().minusDays(1));
        pay2.setPaymentAmount(BigDecimal.ONE);

        pay3.setUserId(2L);
        pay3.setOrderId(3L);
        pay3.setPaymentAmount(BigDecimal.TEN);
        pay3.setStatus(PENDING);

        pay4.setUserId(1L);
        pay4.setOrderId(4L);
        pay4.setStatus(FAILED);
        pay4.setTimestamp(LocalDateTime.now().minusDays(2));
        pay4.setPaymentAmount(BigDecimal.TEN);

        pay5.setUserId(5L);
        pay5.setOrderId(5L);
        pay5.setStatus(SUCCESS);
        pay5.setTimestamp(LocalDateTime.now());
        pay5.setPaymentAmount(BigDecimal.TEN);

        pay6.setUserId(5L);
        pay6.setOrderId(6L);
        pay6.setStatus(FAILED);
        pay6.setTimestamp(LocalDateTime.now());
        pay6.setPaymentAmount(BigDecimal.TEN);

        paymentRepository.saveAll(List.of(pay1, pay2, pay3, pay4, pay5, pay6));
    }

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAll();
    }

    @Test
    void containerIsRunning() {
        assertTrue(mongo.isRunning());
    }


    @Test
    @WithMockUser(username = "1", authorities = "ADMIN")
    void createPayment_ShouldCreatePayment() throws Exception {
        // Arrange
        Long orderId = 100L;
        Long userId = 2L;
        var amount = BigDecimal.TEN;
        var req = new PaymentCreateRequestDto(orderId, userId, amount);

        // Act and Assert
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(Matchers.notNullValue()))
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.status").value(PaymentStatus.PENDING.toString()))
                .andExpect(jsonPath("$.timestamp").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.paymentAmount").value(amount));
    }

    @Test
    @WithMockUser(username = "1", authorities = "ADMIN")
    void createPayment_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Long orderId = -100L;
        Long userId = 2L;
        var amount = BigDecimal.TEN;
        var req = new PaymentCreateRequestDto(orderId, userId, amount);

        // Act and Assert
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "1", authorities = "ADMIN")
    void getPaymentsByOrderId_ShouldReturnPayment() throws Exception {
        // Arrange
        var id = 1L;

        // Act and Assert
        mockMvc.perform(get("/orders/" + id + "/payment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(Matchers.notNullValue()))
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.status").value(SUCCESS.toString()))
                .andExpect(jsonPath("$.timestamp").value(Matchers.notNullValue()))
                .andExpect(jsonPath("$.paymentAmount").value(BigDecimal.TEN));
    }

    @Test
    @WithMockUser(username = "1", authorities = "ADMIN")
    void getPaymentsByOrderId_ShouldReturn404() throws Exception {
        // Arrange
        var id = 600L;

        // Act and Assert
        mockMvc.perform(get("/orders/" + id + "/payment"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "1", authorities = "ADMIN")
    void getPaymentsByStatus_ShouldReturnListOfPayments() throws Exception {

        // Arrange & Act & Assert
        mockMvc.perform(get("/payments?status=SUCCESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        mockMvc.perform(get("/payments?status=FAILED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(username = "1", authorities = "ADMIN")
    void getPaymentsByUserId_ShouldReturnListOfPayments() throws Exception {
        // Arrange
        long userId1 = 1L;
        long userId2 = 2L;
        long userId3 = 3L;

        // Act & Assert
        mockMvc.perform(get("/users/" + userId1 + "/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        mockMvc.perform(get("/users/" + userId2 + "/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/users/" + userId3 + "/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @ParameterizedTest
    @WithMockUser(username = "1", authorities = "ADMIN")
    @CsvSource({
            "2, SUCCESS",
            "1, FAILED"
    })
    void processPaymentsById_ShouldProcessPayment_Success(
            String random, String status
    ) throws Exception {
        // Arrange
        Optional<Payment> byOrderId = paymentRepository.getByOrderId(3L);
        String id = byOrderId.get().getId();

        when(client.getRandom()).thenReturn(random);
        // Act & Assert
        mockMvc.perform(patch("/payments/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.orderId").value(3))
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.timestamp").value(Matchers.notNullValue()))
                .andExpect(jsonPath("$.paymentAmount").value(BigDecimal.TEN));
    }

    @Test
    @WithMockUser(username = "1", authorities = "ADMIN")
    void processPaymentsById_ShouldReturn404() throws Exception {
        // Arrange
        String id = "something";

        // Act & Assert
        mockMvc.perform(patch("/payments/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "1", authorities = "ADMIN")
    void processPaymentsById_ShouldReturnBadRequest() throws Exception {
        // Arrange
        String id = "69e3aacf8951b6a63b24edab";

        // Act & Assert
        mockMvc.perform(patch("/payments/" + id))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "1", authorities = "ADMIN")
    void getTotalByUserId_ShouldReturnAmount() throws Exception {
        // Arrange
        String startDate = LocalDate.now().minusDays(2).toString();
        String endDate1 = LocalDate.now().minusDays(1).toString();

        // Act & Assert
        mockMvc.perform(get("/users/1/payments/total?startDate="
                        + startDate + "&endDate=" + endDate1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(BigDecimal.valueOf(11)));

        mockMvc.perform(get("/users/1/payments/total?startDate="
                        + startDate + "&endDate=" + startDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(BigDecimal.valueOf(10)));
    }

    @Test
    @WithMockUser(username = "1", authorities = "ADMIN")
    void getTotalByUserId_ShouldReturnZero() throws Exception {
        // Arrange
        String weekAgo = LocalDate.now().minusWeeks(1).toString();

        // Act & Assert
        mockMvc.perform(get("/users/1/payments/total?startDate="
                        + weekAgo + "&endDate=" + weekAgo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(BigDecimal.ZERO));
    }

    @Test
    @WithMockUser(username = "1", authorities = "ADMIN")
    void getTotal_ShouldReturnAmount() throws Exception {
        // Arrange
        String weeksAgo = LocalDate.now().minusWeeks(2).toString();
        String today = LocalDate.now().toString();

        // Act & Assert
        mockMvc.perform(get("/payments/total?startDate="
                        + weeksAgo + "&endDate=" + today))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(BigDecimal.valueOf(21)));

        mockMvc.perform(get("/payments/total?startDate="
                        + today + "&endDate=" + today))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(BigDecimal.valueOf(10)));
    }

    @Test
    @WithMockUser(username = "1", authorities = "ADMIN")
    void getTotal_ShouldReturnZero() throws Exception {
        // Arrange
        String weeksAgo = LocalDate.now().minusWeeks(2).toString();

        // Act & Assert
        mockMvc.perform(get("/payments/total?startDate="
                        + weeksAgo + "&endDate=" + weeksAgo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(BigDecimal.valueOf(0)));

    }

    @Test
    void getTotal_ShouldReturnForbidden() throws Exception {
        // Arrange
        String weeksAgo = LocalDate.now().minusWeeks(2).toString();

        // Act & Assert
        mockMvc.perform(get("/payments/total?startDate="
                        + weeksAgo + "&endDate=" + weeksAgo)
                        .header(HEADER_USER_ID, 1)
                        .header(HEADER_ROLE, HEADER_VALUE_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "1", authorities = "ADMIN")
    void getTotalByUserId_ShouldReturnForbiddenForWrongUser() throws Exception {
        // Arrange
        String weeksAgo = LocalDate.now().minusWeeks(2).toString();

        // Act & Assert
        mockMvc.perform(get("/users/2/payments/total?startDate="
                        + weeksAgo + "&endDate=" + weeksAgo)
                        .header(HEADER_USER_ID, 1)
                        .header(HEADER_ROLE, HEADER_VALUE_USER))
                .andExpect(status().isOk());
    }

    @Test
    void getTotalByUserId_ShouldReturnOkForValidUser() throws Exception {
        // Arrange
        String weeksAgo = LocalDate.now().minusWeeks(2).toString();

        // Act & Assert
        mockMvc.perform(get("/users/2/payments/total?startDate="
                        + weeksAgo + "&endDate=" + weeksAgo)
                        .header(HEADER_USER_ID, 1)
                        .header(HEADER_ROLE, HEADER_VALUE_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    void processPaymentsById_ShouldReturnOkForValidUser() throws Exception {
        // Arrange
        Optional<Payment> byOrderId = paymentRepository.getByOrderId(3L);
        String id = byOrderId.get().getId();

        when(client.getRandom()).thenReturn("1");
        // Act & Assert
        mockMvc.perform(patch("/payments/" + id)
                        .header(HEADER_USER_ID, 2)
                        .header(HEADER_ROLE, HEADER_VALUE_USER))
                .andExpect(status().isOk());
    }

    @Test
    void processPaymentsById_ShouldReturnForbiddenForWrongUser() throws Exception {
        // Arrange
        Optional<Payment> byOrderId = paymentRepository.getByOrderId(3L);
        String id = byOrderId.get().getId();

        // Act & Assert
        mockMvc.perform(patch("/payments/" + id)
                        .header(HEADER_USER_ID, 500)
                        .header(HEADER_ROLE, HEADER_VALUE_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPaymentsByOrderId_ShouldReturnOkForValidUser() throws Exception {
        // Arrange
        var id = 1L;

        // Act and Assert
        mockMvc.perform(get("/orders/" + id + "/payment")
                        .header(HEADER_USER_ID, 1)
                        .header(HEADER_ROLE, HEADER_VALUE_USER))
                .andExpect(status().isOk());
    }

    @Test
    void getPaymentsByOrderId_ShouldReturnForbiddenForWrongUser() throws Exception {
        // Arrange
        var id = 1L;

        // Act and Assert
        mockMvc.perform(get("/orders/" + id + "/payment")
                        .header(HEADER_USER_ID, 500)
                        .header(HEADER_ROLE, HEADER_VALUE_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPaymentsByUserId_ShouldReturnOkForValidUser() throws Exception {
        // Arrange
        long userId1 = 1L;

        // Act & Assert
        mockMvc.perform(get("/users/" + userId1 + "/payments")
                        .header(HEADER_USER_ID, 1)
                        .header(HEADER_ROLE, HEADER_VALUE_USER))
                .andExpect(status().isOk());
    }

    @Test
    void getPaymentsByUserId_ShouldReturnForbiddenForWrongUser() throws Exception {
        // Arrange
        long userId1 = 1L;

        // Act & Assert
        mockMvc.perform(get("/users/" + userId1 + "/payments")
                        .header(HEADER_USER_ID, 2)
                        .header(HEADER_ROLE, HEADER_VALUE_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPaymentsByStatus_ShouldReturnForbiddenForNonAdmin() throws Exception {
        // Arrange & Act & Assert
        mockMvc.perform(get("/payments?status=SUCCESS")
                        .header(HEADER_USER_ID, 1)
                        .header(HEADER_ROLE, HEADER_VALUE_USER))
                .andExpect(status().isForbidden());
    }
}