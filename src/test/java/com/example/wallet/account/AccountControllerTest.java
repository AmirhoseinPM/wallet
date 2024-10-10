package com.example.wallet.account;


import com.example.wallet.jwt.JwtService;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;


import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    AccountDetailsRepository accountDetailsRepository;
    @Autowired
    JwtService jwtService;

    private final Logger log = LoggerFactory.getLogger(AccountRepositoryTest.class);

    Account account;
    AccountDetails accountDetails;

    @BeforeEach
    public void setup(){
        account = new Account(
                "0123456789", "09123456789", "email@g.com", "goodpassword34",
                "firstName", "lastName", LocalDate.parse("2010-10-10"), "m", "i",
                Date.from(Instant.now())
        );
        account.setId(1L);
        log.info(account.toString());
        accountDetails = new AccountDetails(account);
    }

    @Test
    @DisplayName("test get account with token")
    @Order(1)
    public void testGetMe_ValidAccount_ValidToken() throws Exception {
        // Arrange
        String token = "Bearer " +
                jwtService.generateToken(accountDetailsRepository.save(accountDetails));

        // Act
        ResultActions result = mockMvc.perform(
                get("/api/account/me")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", token)
        );

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName",is(account.getFirstName())))
                .andExpect(jsonPath("$.lastName",is(account.getLastName())))
                .andExpect(jsonPath("$.email",is(account.getEmail())))
                .andExpect(jsonPath("$.phoneNumber",is(account.getPhoneNumber())))
                .andExpect(jsonPath("$.nationalId",is(account.getNationalId())))
                .andExpect(jsonPath("$.birthDate[0]",is(account.getBirthDate().getYear())))
                .andExpect(jsonPath("$.birthDate[1]",is(account.getBirthDate().getMonthValue())))
                .andExpect(jsonPath("$.birthDate[2]",is(account.getBirthDate().getDayOfMonth())))
                .andExpect(jsonPath("$.militaryStatus",is(account.getMilitaryStatus())))
                .andExpect(jsonPath("$.gender",is(account.getGender())));
    }


    @Test
    @DisplayName("test get account with token")
    @Order(1)
    public void testGetMe_ValidAccount_InvalidToken() throws Exception {
        // Arrange
        accountDetailsRepository.save(accountDetails);


        // Act
        ResultActions result = mockMvc.perform(
                get("/api/account/me")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
        );

        // Assert
        result.andExpect(status().is4xxClientError());
    }

    
}
