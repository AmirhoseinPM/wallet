package com.example.wallet.transaction;

import com.example.wallet.account.Account;
import com.example.wallet.account.AccountDetails;
import com.example.wallet.account.AccountDetailsRepository;
import com.example.wallet.security.JwtService;
import com.example.wallet.wallet.*;
import com.example.wallet.wallet.helper.WalletDto;
import com.example.wallet.wallet.helper.WalletGenerator;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    JwtService jwtService;
    @Autowired
    AccountDetailsRepository accountDetailsRepository;
    @Autowired
    WalletRepository walletRepository;
    @Autowired
    WalletGenerator walletBuilderFactory;

    final Long MAXIMUM_DAILY_WITHDRAWAL = 15_000_000L;
    final Long MINIMUM_TRANSACTION_AMOUNT = 1000L;

    Account account;
    AccountDetails accountDetails;
    String token;
    Wallet wallet;
    @Autowired
    private TransactionRepository transactionRepository;


    private void setNewValueToAccountJwtWallet(
            String nationalId, String phoneNumber,
            String email, Long initialWalletBalance) {
        account = new Account(
                nationalId, phoneNumber, email, "goodPassword34",
                "firstName", "lastName", LocalDate.parse("2010-10-10"), "m", "i",
                Date.from(Instant.now())
        );
        account.setActive(true);
        accountDetails = new AccountDetails(account);
        String jwt = jwtService.generateToken(
                accountDetailsRepository.save(accountDetails));
        token = "Bearer " + jwt;


        WalletDto walletDto = new WalletDto();
        walletDto.setTitle("title");
        walletDto.setInitialBalance(initialWalletBalance);
        wallet = walletBuilderFactory.getNewWallet(walletDto, account.getNationalId());
        wallet.setActive(true);
        wallet = walletRepository.save(wallet);
        wallet.setAccount(account);
        wallet = walletRepository.save(wallet);
    }

    @Test
    public void testAll_Invalid_Auth() throws Exception {
        setNewValueToAccountJwtWallet(
                "0121231444", "09999451234", "emailtet@g.com", 100_000_000L
        );

        ResultActions result = mockMvc.perform(
                get("/api/transaction")
                        .accept(MediaType.APPLICATION_JSON_VALUE));
        result.andExpect(status().isForbidden());

        ResultActions result2 = mockMvc.perform(
                get("/api/transaction/1")
                        .accept(MediaType.APPLICATION_JSON_VALUE));
        result2.andExpect(status().isForbidden());


        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setWithdrawal(true);
        transactionDto.setAmount(100L);
        transactionDto.setWalletId(wallet.getId());
        transactionDto.setDescription("sample");
        // Arrange
        // Act
        ResultActions result3 = mockMvc.perform(
                post("/api/transaction/wallet/" + wallet.getId())
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(transactionDto))

        );
        result3.andExpect(status().isForbidden());
    }

    @Test
    public void testWithdraw_Valid() throws Exception{

        setNewValueToAccountJwtWallet(
                "0121231224", "09999452234", "emaisdfgvltet@g.com", 100_000_000L
        );

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setWithdrawal(true);
        transactionDto.setAmount(1000L);
        transactionDto.setWalletId(wallet.getId());
        transactionDto.setDescription("Withdrawal sample");

        ResultActions result = mockMvc.perform(
                post("/api/transaction/wallet/" + transactionDto.getWalletId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(transactionDto))

        );

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.withdrawal").value(true))
                .andExpect(jsonPath("$.amount").value(transactionDto.getAmount()))
                .andExpect(jsonPath("$.wallet.id").value(wallet.getId()))
                .andExpect(jsonPath("$.description").value(transactionDto.getDescription()));

    }

    @Test
    public void testReceive_Valid() throws Exception{
        setNewValueToAccountJwtWallet(
                "0121231123",
                "09999452987",
                "emavltet@g.com",
                100_000_000L
        );

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setWithdrawal(false);
        transactionDto.setAmount(1000L);
        transactionDto.setWalletId(wallet.getId());
        transactionDto.setDescription("Receive sample");

        ResultActions result = mockMvc.perform(
                post("/api/transaction/wallet/" + transactionDto.getWalletId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(transactionDto))

        );

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.withdrawal").value(false))
                .andExpect(jsonPath("$.amount").value(transactionDto.getAmount()))
                .andExpect(jsonPath("$.wallet.id").value(wallet.getId()))
                .andExpect(jsonPath("$.description").value(transactionDto.getDescription()));

    }


    @Test
    public void testTransaction_InvalidAmount_TooLow() throws Exception{
        setNewValueToAccountJwtWallet(
                "0918523123",
                "09185231231",
                "sampleEmail@mail.com",
                100_000_000L
        );
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setWithdrawal(false);
        transactionDto.setAmount(MINIMUM_TRANSACTION_AMOUNT - 1);
        transactionDto.setWalletId(wallet.getId());
        transactionDto.setDescription("Receive sample");

        ResultActions result = mockMvc.perform(
                post("/api/transaction/wallet/" + wallet.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(transactionDto))

        );
        result.andExpect(status().is4xxClientError());
    }

    @Test
    public void testTransaction_InvalidWalletOwnership() throws Exception{
        setNewValueToAccountJwtWallet(
                "0918523120",
                "09185201231",
                "sampleEmail0@mail.com",
                100_000_000L
        );
        long olderWalletId = wallet.getId();
        setNewValueToAccountJwtWallet(
                "0918523121",
                "09185261231",
                "sampleEmail@gmail.com",
                100_000_000L
        );
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setWithdrawal(false);
        transactionDto.setAmount(MINIMUM_TRANSACTION_AMOUNT - 1);
        transactionDto.setWalletId(wallet.getId());
        transactionDto.setDescription("Receive sample");

        ResultActions result = mockMvc.perform(
                post("/api/transaction/wallet/" + olderWalletId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(transactionDto))

        );

        result.andExpect(status().isForbidden());
    }


    @Test
    public void testWithdrawal_InvalidAmount_NotAvailableBalance() throws Exception{
        setNewValueToAccountJwtWallet(
                "0123456789",
                "09123456789",
                "sample2Email@mail.com",
                100_000L
        );
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setWithdrawal(true);
        transactionDto.setAmount(1_000_000L);
        transactionDto.setWalletId(wallet.getId());
        transactionDto.setDescription("Receive sample");

        ResultActions result = mockMvc.perform(
                post("/api/transaction/wallet/" + wallet.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(transactionDto))

        );
        result.andExpect(status().is4xxClientError());
    }


    @Test
    public void testWithdrawal_InvalidAmount_MaximumDailyWithdrawal() throws Exception{
        setNewValueToAccountJwtWallet(
                "0123459876",
                "09123459876",
                "sample2Email2@mail.com",
                100_000_000L
        );
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setWithdrawal(true);
        transactionDto.setAmount(MAXIMUM_DAILY_WITHDRAWAL / 10);
        transactionDto.setWalletId(wallet.getId());
        transactionDto.setDescription("Receive sample");
        for (int i = 0; i < 10; i++)
            mockMvc.perform(
                    post("/api/transaction/wallet/" + transactionDto.getWalletId())
                            .header("Authorization", token)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(transactionDto))

            );

        ResultActions result = mockMvc.perform(
                post("/api/transaction/wallet/" + transactionDto.getWalletId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(transactionDto))

        );
        result.andExpect(status().is4xxClientError());
    }

    @Test
    public void testGetAll_Valid() throws Exception{
        setNewValueToAccountJwtWallet(
                "0178459876",
                "09175459876",
                "samplyye2Email2@mail.com",
                100_000_000L
        );
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setWithdrawal(true);
        transactionDto.setAmount(MAXIMUM_DAILY_WITHDRAWAL / 100);
        transactionDto.setWalletId(wallet.getId());
        transactionDto.setDescription("Receive sample");
        for (int i = 0 ; i < 10; i++) {
            ResultActions result = mockMvc.perform(
                    post("/api/transaction/wallet/" + transactionDto.getWalletId())
                            .header("Authorization", token)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(transactionDto))

            );
            result.andExpect(status().isOk());
        }
        ResultActions result = mockMvc.perform(
                get("/api/transaction")
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON_VALUE)

        );
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$." + LocalDate.now()).exists());
    }

    @Test
    public void testGetByWalletId_Valid() throws Exception{
        setNewValueToAccountJwtWallet(
                "0174199876",
                "09191659876",
                "samplyye2il2@mail.com",
                100_000_000L
        );
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setWithdrawal(true);
        transactionDto.setAmount(MAXIMUM_DAILY_WITHDRAWAL / 100);
        transactionDto.setWalletId(wallet.getId());
        transactionDto.setDescription("Receive sample");
        for (int i = 0 ; i < 10; i++) {
            ResultActions result = mockMvc.perform(
                    post("/api/transaction/wallet/" + transactionDto.getWalletId())
                            .header("Authorization", token)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(transactionDto))

            );
            result.andExpect(status().isOk());
        }
        ResultActions result = mockMvc.perform(
                get("/api/transaction/wallet/" + wallet.getId())
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON_VALUE)


        );
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$." + LocalDate.now()).exists());
    }

    @Test
    public void testGetByWalletId_Invalid_Ownership() throws Exception{
        setNewValueToAccountJwtWallet(
                "0174199996",
                "09197259876",
                "samplyye2il20099@mail.com",
                100_000_000L
        );
        Long currentWalletId = wallet.getId();
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setWithdrawal(true);
        transactionDto.setAmount(MAXIMUM_DAILY_WITHDRAWAL / 100);
        transactionDto.setWalletId(wallet.getId());
        transactionDto.setDescription("Receive sample");
        ResultActions result = mockMvc.perform(
                post("/api/transaction/wallet/" + transactionDto.getWalletId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(transactionDto))

        );
        result.andExpect(status().isOk());
        setNewValueToAccountJwtWallet(
                "0174777776",
                "09199999876",
                "samplyye2il2jtyc@mail.com",
                100_000_000L
        );
        ResultActions result2 = mockMvc.perform(
                get("/api/transaction/wallet/" + currentWalletId)
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON_VALUE)


        );
        result2.andExpect(status().isForbidden());
    }

    @Test
    public void testGetByWalletId_Invalid_NotFound() throws Exception{
        setNewValueToAccountJwtWallet(
                "0174188876",
                "09888659876",
                "samplyye2il8882@mail.com",
                100_000_000L
        );

        ResultActions result2 = mockMvc.perform(
                get("/api/transaction/wallet/13")
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
        );
        result2.andExpect(status().isNotFound());
    }

    @Test
    public void testGetById_Valid() throws Exception{
        setNewValueToAccountJwtWallet(
                "0174154326",
                "09191676546",
                "samplyye2ilub2@mail.com",
                100_000_000L
        );
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setWithdrawal(true);
        transactionDto.setAmount(MAXIMUM_DAILY_WITHDRAWAL / 100);
        transactionDto.setWalletId(wallet.getId());
        transactionDto.setDescription("Receive sample");
        ResultActions result = mockMvc.perform(
                post("/api/transaction/wallet/" + transactionDto.getWalletId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(transactionDto))

        );
        result.andExpect(status().isOk());

        ResultActions result22 = mockMvc.perform(
                get("/api/transaction/1")
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON_VALUE)


        );
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(transactionDto.getAmount()))
                .andExpect(jsonPath("$.withdrawal").value(transactionDto.isWithdrawal()));
    }

    @Test
    public void testGetById_Invalid_NotFound() throws Exception{
        setNewValueToAccountJwtWallet(
                "0174888326",
                "09888676546",
                "samplyykhjbe2ilub2@mail.com",
                100_000_000L
        );

        ResultActions result22 = mockMvc.perform(
                get("/api/transaction/1222")
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON_VALUE)


        );
        result22.andExpect(status().isNotFound());
    }

    @Test
    public void testGetById_Invalid_Ownership() throws Exception{
        setNewValueToAccountJwtWallet(
                "0171111126",
                "09191611111",
                "samply2enaob111112@mail.com",
                100_000_000L
        );
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setWithdrawal(true);
        transactionDto.setAmount(MAXIMUM_DAILY_WITHDRAWAL / 100);
        transactionDto.setWalletId(wallet.getId());
        transactionDto.setDescription("Receive sample");
        ResultActions result = mockMvc.perform(
                post("/api/transaction/wallet/" + transactionDto.getWalletId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(transactionDto))

        );
        result.andExpect(status().isOk());

        setNewValueToAccountJwtWallet(
                "0122222926",
                "09192222946",
                "sampkjbyye22222ilub2@mail.com",
                100_000_000L
        );

        ResultActions result22 = mockMvc.perform(
                get("/api/transaction/1")
                        .header("Authorization", token)
                        .accept(MediaType.APPLICATION_JSON_VALUE)


        );
        result22.andExpect(status().isForbidden());
    }

}
