package com.zerobase.myaccount.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.myaccount.dto.AccountDto;
import com.zerobase.myaccount.dto.CreateAccountRequest;
import com.zerobase.myaccount.dto.DeleteAccountRequest;
import com.zerobase.myaccount.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @MockBean
    private AccountService accountService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void successCreateAccount() throws Exception {
        // given
        given(accountService.createAccount(anyLong(), anyLong()))
                .willReturn(AccountDto.builder()
                        .userId(1L)
                        .accountNumber("1000000000")
                        .registeredAt(LocalDateTime.now())
                        .unRegisteredAt(null)
                        .build());

        // when
        // then
        mockMvc.perform(post("/account")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateAccountRequest(1L, 10000L)
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.accountNumber").value("1000000000"))
                .andDo(print());
    }

    @Test
    void successDeleteAccount() throws Exception {
        // given
        given(accountService.deleteAccount(anyLong(), anyString()))
                .willReturn(AccountDto.builder()
                        .userId(1L)
                        .accountNumber("1000000000")
                        .balance(1000L)
                        .registeredAt(LocalDateTime.now())
                        .unRegisteredAt(LocalDateTime.now())
                        .build());

        // when
        // then
        mockMvc.perform(delete("/account")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DeleteAccountRequest(1L, "1000000000")
                        )))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.accountNumber").value("1000000000"))
                .andDo(print());
    }

    @Test
    void successGetAccountListByUserId() throws Exception {
        // given
        given(accountService.getAccountListByUserId(anyLong()))
                .willReturn(Arrays.asList(AccountDto.builder()
                        .accountNumber("1000000000")
                        .balance(1000L)
                        .build(), AccountDto.builder()
                        .accountNumber("2000000000")
                        .balance(2000L)
                        .build(), AccountDto.builder()
                        .accountNumber("3000000000")
                        .balance(3000L)
                        .build()));

        // when
        // then
        mockMvc.perform(get("/account?user_id=1"))
                .andExpect(jsonPath("$[0].accountNumber").value("1000000000"))
                .andExpect(jsonPath("$[0].balance").value(1000))
                .andExpect(jsonPath("$[1].accountNumber").value("2000000000"))
                .andExpect(jsonPath("$[1].balance").value(2000))
                .andExpect(jsonPath("$[2].accountNumber").value("3000000000"))
                .andExpect(jsonPath("$[2].balance").value(3000))
                .andDo(print());
    }

}