package com.zerobase.myaccount.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.myaccount.dto.CancelBalanceRequest;
import com.zerobase.myaccount.dto.TransactionDto;
import com.zerobase.myaccount.dto.UseBalanceRequest;
import com.zerobase.myaccount.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static com.zerobase.myaccount.type.TransactionResultType.S;
import static com.zerobase.myaccount.type.TransactionType.USE;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {
    @MockBean
    TransactionService transactionService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void successUseBalnce() throws Exception {
        // given
        given(transactionService.useBalance(anyLong(), anyString(), anyLong()))
                .willReturn(TransactionDto.builder()
                        .transactionId("transactionId")
                        .accountNumber("1234567890")
                        .transactedAt(LocalDateTime.now())
                        .amount(100L)
                        .transactionResultType(S)
                        .build());

        // when
        // then
        mockMvc.perform(post("/transaction/use")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UseBalanceRequest(1L, "1000000000", 300L)
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.transactionResult").value("S"))
                .andExpect(jsonPath("$.transactionId").value("transactionId"))
                .andExpect(jsonPath("$.amount").value(100))
                .andDo(print());
    }

    @Test
    void successCancelBalance() throws Exception {
        // given
        given(transactionService.cancelBalance(anyString(), anyString(), anyLong()))
                .willReturn(TransactionDto.builder()
                        .accountNumber("1000000000")
                        .transactionResultType(S)
                        .transactionId("transactionId")
                        .amount(1000L)
                        .transactedAt(LocalDateTime.now())
                        .build());

        // when
        // then
        mockMvc.perform(post("/transaction/cancel")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CancelBalanceRequest("transactionId", "1000000000", 100L)
                        )))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1000000000"))
                .andExpect(jsonPath("$.transactionResult").value("S"))
                .andExpect(jsonPath("$.transactionId").value("transactionId"))
                .andExpect(jsonPath("$.amount").value(1000));
    }

    @Test
    void successGetTransaction() throws Exception {
        // given
        given(transactionService.getTransaction(anyString()))
                .willReturn(TransactionDto.builder()
                        .accountNumber("1000000000")
                        .transactionResultType(S)
                        .transactionType(USE)
                        .transactionId("transactionId")
                        .amount(10000L)
                        .transactedAt(LocalDateTime.now())
                        .build());

        // when
        // then
        mockMvc.perform(get("/transaction/12345"))
                .andExpect(jsonPath("$.accountNumber").value("1000000000"))
                .andExpect(jsonPath("$.transactionResult").value("S"))
                .andExpect(jsonPath("$.transactionType").value("USE"))
                .andExpect(jsonPath("$.amount").value(10000))
                .andExpect(jsonPath("$.transactionId").value("transactionId"))
                .andDo(print());
    }
}