package com.example.wallet.controller;

import com.example.wallet.entity.Account;
import com.example.wallet.entity.AccountDetails;
import com.example.wallet.repository.AccountDetailsRepository;
import com.example.wallet.entity.Gender;
import com.example.wallet.entity.MilitaryStatus;
import com.example.wallet.security.JwtService;
import com.example.wallet.dto.WalletDto;
import com.example.wallet.dto.WalletUpdateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class WalletControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountDetailsRepository accountDetailsRepository;
    @Autowired
    JwtService jwtService;

    @Autowired
    ObjectMapper objectMapper;

    Account account;
    AccountDetails accountDetails;
    String token;


    private void setNewValueToAccountToken(
            String nationalId, String phoneNumber,
            String email) {
        account = new Account(
                nationalId, phoneNumber, email, "goodPassword34",
                "firstName", "lastName", LocalDate.parse("2010-10-10"), Gender.MALE, MilitaryStatus.INCLUDED,
                Date.from(Instant.now())
        );
        account.setActive(true);
        accountDetails = new AccountDetails(account);
        String jwt = jwtService.generateToken(
                accountDetailsRepository.save(accountDetails));
        token = "Bearer " + jwt;
    }

    @Test
    public void testGetAllWallets_Valid_Empty() throws Exception{
        // Arrange
        setNewValueToAccountToken("0121236789", "09987456789", "emailtest@g.com");


        // Act
        ResultActions result = mockMvc.perform(
                get("/api/wallet")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", token)
        );

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty())
                .andExpect(jsonPath("$").isArray());

    }

    @Test
    public void testGetAllWallets_Invalid_Authorization() throws Exception{

        // Act
        ResultActions result = mockMvc.perform(
                get("/api/wallet")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
        );

        // Assert
        result.andExpect(status().is4xxClientError());
    }


    @Test
    public void testCreateWallet_GetById_UpdateTitle_Valid() throws Exception{

        // Arrange
        setNewValueToAccountToken("0121231234", "09987451234", "emailtest34@g.com");

        WalletDto walletDto = new WalletDto();
        walletDto.setTitle("title");
        walletDto.setInitialBalance(100_000_000L);

        // Act
        ResultActions result = mockMvc.perform(
                post("/api/wallet")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", token)
                        .content(objectMapper.writeValueAsString(walletDto))

        );
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.balance", is(1.0E8)))
                .andExpect(jsonPath("$.number").exists())
                .andExpect(jsonPath("$.sheba").exists())
                .andExpect(jsonPath("$.title").exists());

        // GET TEST
        // Act
        ResultActions result1 = mockMvc.perform(
                get("/api/wallet/1")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", token)
        );
        result1.andExpect(status().isOk());
        // PUT TEST
        WalletUpdateDto walletUpdateDto = new WalletUpdateDto();
        walletUpdateDto.setTitle("newTitle");

        ResultActions result2 = mockMvc.perform(
                put("/api/wallet/1")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(walletUpdateDto))

        );
        result2.andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(walletUpdateDto.getTitle())));
    }


    @Test
    public void testCreateWallet_InvalidBalance() throws Exception{
        setNewValueToAccountToken("0121234321", "09987454321", "emailtest3498@g.com");


        WalletDto walletDto = new WalletDto();
        walletDto.setTitle("title");
        walletDto.setInitialBalance(100L);

        // Act
        ResultActions result = mockMvc.perform(
                post("/api/wallet")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", token)
                        .content(objectMapper.writeValueAsString(walletDto))

        );

        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    public void testGetWallet_Invalid_Ownership() throws Exception{
        // Arrange
        setNewValueToAccountToken("0121456334", "09900451234", "emailtes99t34@g.com");

        WalletDto walletDto = new WalletDto();
        walletDto.setTitle("title");
        walletDto.setInitialBalance(100_000_000L);
        mockMvc.perform(
                post("/api/wallet")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", token)
                        .content(objectMapper.writeValueAsString(walletDto))

        );
        setNewValueToAccountToken("0989456334", "09990451234", "ems99t34@g.com");

        ResultActions result1 = mockMvc.perform(
                get("/api/wallet/1")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", token)

        );
        result1.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    public void testGetWallet_Invalid_NotExists() throws Exception{
        // Arrange
        setNewValueToAccountToken("0121456884", "09900455464", "emailtegvujbt34@g.com");

        ResultActions result = mockMvc.perform(
                get("/api/wallet/12")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", token)

        );
        result.andExpect(status().is4xxClientError());
    }

    @Test
    public void testUpdateWallet_Valid() throws Exception {
        setNewValueToAccountToken("0129170984", "09993931464", "emailtek878nwsvujbt34@g.com");
        WalletDto walletDto = new WalletDto();
        walletDto.setTitle("title");
        walletDto.setInitialBalance(100_000_000L);
        mockMvc.perform(
                post("/api/wallet")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", token)
                        .content(objectMapper.writeValueAsString(walletDto))

        );

        WalletUpdateDto updateDto = new WalletUpdateDto();
        updateDto.setTitle("newTitle");
        ResultActions result = mockMvc.perform(
                put("/api/wallet/2")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", token)
                        .content(objectMapper.writeValueAsString(updateDto))
        );
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)));
    }

    @Test
    public void testUpdateWallet_InvalidOwnerShip() throws Exception {
        setNewValueToAccountToken("0129002984", "09118931464", "emaiaw8nwsvujbt34@g.com");
        WalletDto walletDto = new WalletDto();
        walletDto.setTitle("title");
        walletDto.setInitialBalance(100_000_000L);
        mockMvc.perform(
                post("/api/wallet")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", token)
                        .content(objectMapper.writeValueAsString(walletDto))

        );

        setNewValueToAccountToken("0129919884", "09915131464", "emaipwc8nwsvujbt34@g.com");
        WalletUpdateDto updateDto = new WalletUpdateDto();
        updateDto.setTitle("newTitle");
        ResultActions result = mockMvc.perform(
                put("/api/wallet/1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", token)
                        .content(objectMapper.writeValueAsString(updateDto))
        );
        result.andExpect(status().isForbidden());
    }

}
