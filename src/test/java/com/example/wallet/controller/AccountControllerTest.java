package com.example.wallet.controller;


import com.example.wallet.dto.ChangePasswordDto;
import com.example.wallet.enums.Gender;
import com.example.wallet.dto.LoginDto;
import com.example.wallet.enums.MilitaryStatus;
import com.example.wallet.domain.entity.Account;
import com.example.wallet.domain.entity.AccountDetails;
import com.example.wallet.repository.AccountDetailsRepository;
import com.example.wallet.repository.AccountRepository;
import com.example.wallet.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
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
    AccountRepository accountRepository;
    @Autowired
    JwtService jwtService;
    @Autowired
    ObjectMapper objectMapper;

    Account account;
    AccountDetails accountDetails;
    String jwt;
    final String accountJsonString = "{" +
            "\"phoneNumber\": %1$s," +
            "\"firstName\": %2$s," +
            "\"lastName\": %3$s," +
            "\"birthDate\": %4$s," +
            "\"gender\": %5$s," +
            "\"militaryStatus\": %6$s," +
            "\"email\": %7$s," +
            "\"nationalId\": %8$s," +
            "\"password\": %9$s" +
            "}";

    private String getNewAccountString(String nationalId, String phoneNumber, String email) {
        String account = "{" +
                "\"phoneNumber\": \"%1$s\"," +
                "\"firstName\": \"amir\"," +
                "\"lastName\": \"pooyan\"," +
                "\"birthDate\": \"2000-12-20\"," +
                "\"gender\": \"male\"," +
                "\"militaryStatus\": \"included\"," +
                "\"email\": \"%2$s\"," +
                "\"nationalId\": \"%3$s\"," +
                "\"password\": \"abcdefghijklmn\"" +
                "}";

        return String.format(account, phoneNumber, email, nationalId);
    }
    private void setNewAccount(String nationalId, String phoneNumber, String email) {
        account = new Account(
                nationalId, phoneNumber, email, "goodpassword34",
                "firstName", "lastName", LocalDate.parse("2000-10-10"), Gender.MALE, MilitaryStatus.INCLUDED,
                Date.from(Instant.now())
        );
    }
    @Test
    public void testGetMe_Valid() throws Exception {

        // Arrange
        setNewAccount("0121236789", "09987456789", "emailtest@g.com");
        account.setActive(true);
        accountDetails = new AccountDetails(account);
        jwt = jwtService.generateToken(
                accountDetailsRepository.save(accountDetails));
        String token = "Bearer " + jwt;

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
                .andExpect(jsonPath("$.birthDate",is(account.getBirthDate().toString())))
                .andExpect(jsonPath("$.militaryStatus",is(account.getMilitaryStatus().toString())))
                .andExpect(jsonPath("$.gender",is(account.getGender().toString())));
    }


    @Test
    public void testGetMe_InvalidToken() throws Exception {

        // Arrange
        setNewAccount("0123456788", "09123456788", "email1@g.com");
        account.setActive(true);
        accountDetails = new AccountDetails(account);
        jwt = jwtService.generateToken(
                accountDetailsRepository.save(accountDetails));
        String invalidToken = "Bearer " +
                jwt.replace("a", "w").replace("b", "r");
        // Act1
        ResultActions result1 = mockMvc.perform(
                get("/api/account/me")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .header("Authorization", invalidToken)

        );
        // Act2
        ResultActions result2 = mockMvc.perform(
                get("/api/account/me")
                        .accept(MediaType.APPLICATION_JSON_VALUE)

        );
        // Assert
        result1.andExpect(status().is4xxClientError());
        result2.andExpect(status().is4xxClientError());
    }

    @Test
    public void testSignup_ValidAccount() throws Exception {
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                        "\"09123455432\"",
                                        "\"amir\"",
                                        "\"pooyan\"",
                                        "\"2000-10-10\"",
                                        "\"male\"",
                                        "\"included\"",
                                        "\"emailkbfvbd@gmail.com\"",
                                        "\"0123454312\"",
                                        "\"amojcuribe38v\"")
                        )
        );

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName",is("amir")))
                .andExpect(jsonPath("$.lastName",is("pooyan")))
                .andExpect(jsonPath("$.email",is("emailkbfvbd@gmail.com")))
                .andExpect(jsonPath("$.phoneNumber",is("+989123455432")))
                .andExpect(jsonPath("$.nationalId",is("0123454312")))
                .andExpect(jsonPath("$.birthDate",is("2000-10-10")))
                .andExpect(jsonPath("$.militaryStatus",is("INCLUDED")))
                .andExpect(jsonPath("$.gender",is("MALE")));

    }

    @Test
    public void testSignup_Invalid_NationalId_TooShort() throws Exception {

        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                        "\"09123456787\"",
                                        "\"amir\"",
                                        "\"pooyan\"",
                                        "\"2000-10-10\"",
                                        "\"male\"",
                                        "\"included\"",
                                        "\"email2@gmail.com\"",
                                        "\"0123456\"",
                                        "\"amojcuribe38v\"")
                        )
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());

    }

    @Test
    public void testSignup_Invalid_NationalId_TooLong() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                        "\"09123456787\"",
                                        "\"amir\"",
                                        "\"pooyan\"",
                                        "\"2000-10-10\"",
                                        "\"male\"",
                                        "\"included\"",
                                        "\"email2@gmail.com\"",
                                        "\"012345611111222\"",
                                        "\"amojcuribe38v\"")
                        )
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());
    }
    @Test
    public void testSignup_Invalid_NationalId_BadChar() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                        "\"09123456787\"",
                                        "\"amir\"",
                                        "\"pooyan\"",
                                        "\"2000-10-10\"",
                                        "\"male\"",
                                        "\"included\"",
                                        "\"email2@gmail.com\"",
                                        "\"012345678o\"",
                                        "\"amojcuribe38v\"")
                        )
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());
    }


    @Test
    public void testSignup_Invalid_NationalId_NotUnique() throws Exception {
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                        "\"09123456787\"",
                                        "\"amir\"",
                                        "\"pooyan\"",
                                        "\"2000-10-10\"",
                                        "\"male\"",
                                        "\"included\"",
                                        "\"email2@gmail.com\"",
                                        "\"0123456787\"",
                                        "\"amojcuribe38v\""
                                ))
        );
        result.andExpect(status().isOk());

        ResultActions result2 = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                        "\"09123456787\"",
                                        "\"amir\"",
                                        "\"pooyan\"",
                                        "\"2000-10-10\"",
                                        "\"male\"",
                                        "\"included\"",
                                        "\"email2@gmail.com\"",
                                        "\"0123456787\"",
                                        "\"amojcuribe38v\""
                                )
                        )
        );
        // Assert
        result2.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("Account creation failed, national id or phone number or email already exist")))
                .andExpect(jsonPath("$.status", is("400")));
    }
    @Test
    public void testSignup_Invalid_PhoneNumber_TooLong() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                        "\"0912345678798\"",
                                        "\"amir\"",
                                        "\"pooyan\"",
                                        "\"2000-10-10\"",
                                        "\"male\"",
                                        "\"included\"",
                                        "\"email2@gmail.com\"",
                                        "\"0123456787\"",
                                        "\"amojcuribe38v\""
                                )
                        )
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());
    }
    @Test
    public void testSignup_Invalid_PhoneNumber_TooShort() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                        "\"091234567\"",
                                        "\"amir\"",
                                        "\"pooyan\"",
                                        "\"2000-10-10\"",
                                        "\"male\"",
                                        "\"included\"",
                                        "\"email2@gmail.com\"",
                                        "\"0123456787\"",
                                        "\"amojcuribe38v\""
                                )
                        )
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());
    }
    @Test
    public void testSignup_Invalid_PhoneNumber_BadChar() throws Exception {
        // Arrange
        setNewAccount("0123456789", "0912345678o", "email2@g.com");
        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                        "\"091234567o7\"",
                                        "\"amir\"",
                                        "\"pooyan\"",
                                        "\"2000-10-10\"",
                                        "\"male\"",
                                        "\"included\"",
                                        "\"email2@gmail.com\"",
                                        "\"0123456787\"",
                                        "\"amojcuribe38v\""
                                )
                        )
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());
    }
    @Test
    public void testSignup_Invalid_PhoneNumber_NotUnique() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                        "\"09123456321\"",
                                        "\"amir\"",
                                        "\"pooyan\"",
                                        "\"2000-10-10\"",
                                        "\"male\"",
                                        "\"included\"",
                                        "\"email3@gmail.com\"",
                                        "\"0123456321\"",
                                        "\"amojcuribe38v\""
                                )
                        )
        );
        result.andExpect(status().isOk());


        ResultActions result2 = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                        "\"09123456321\"",
                                        "\"amir\"",
                                        "\"pooyan\"",
                                        "\"2000-10-10\"",
                                        "\"male\"",
                                        "\"included\"",
                                        "\"email3kjlb@gmail.com\"",
                                        "\"0123456321\"",
                                        "\"amojcuribe38v\""
                                )
                        )
        );
        // Assert
        result2.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.status", is("400")));
    }

    @Test
    public void testSignup_Invalid_Email() throws Exception {
        // Arrange
        setNewAccount("0123456789", "09123456789", "email2");
        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                        "\"09123456789\"",
                                        "\"amir\"",
                                        "\"pooyan\"",
                                        "\"2000-10-10\"",
                                        "\"male\"",
                                        "\"included\"",
                                        "\"email2\"",
                                        "\"0123456789\"",
                                        "\"amojcuribe38v\""
                                )
                        )
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    public void testSignup_Invalid_Email_NotUnique() throws Exception {
        ResultActions result1 = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                        "\"09183456321\"",
                                        "\"amir\"",
                                        "\"pooyan\"",
                                        "\"2000-10-10\"",
                                        "\"male\"",
                                        "\"included\"",
                                        "\"emailvbrskj@gmail.com\"",
                                        "\"0123456789\"",
                                        "\"amojcuribe38v\""
                                )
                        )
        );
        result1.andExpect(status().isOk());

        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                        "\"09143214564\"",
                                        "\"amir\"",
                                        "\"pooyan\"",
                                        "\"2000-10-10\"",
                                        "\"male\"",
                                        "\"included\"",
                                        "\"emailvbrskj@gmail.com\"",
                                        "\"0123456543\"",
                                        "\"amojcuribe38v\""
                                )
                        )
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("Account creation failed, national id or phone number or email already exist")))
                .andExpect(jsonPath("$.status", is("400")));
    }

    @Test
    public void testSignup_Invalid_Password_TooShort() throws Exception {
        // Arrange
        setNewAccount("0123456789", "09123456798", "email7@gmail.com");
        account.setPassword("and");
        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                        "\"09143214444\"",
                                        "\"amir\"",
                                        "\"pooyan\"",
                                        "\"2000-10-10\"",
                                        "\"male\"",
                                        "\"included\"",
                                        "\"emailvbrsdgkj@gmail.com\"",
                                        "\"0123454543\"",
                                        "\"amv\""
                                )
                        )
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());
    }
    @Test
    public void testSignup_Invalid_FirstName_Null() throws Exception {

        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                        "\"09143114564\"",
                                        "null",
                                        "\"pooyan\"",
                                        "\"2000-10-10\"",
                                        "\"male\"",
                                        "\"included\"",
                                        "\"emailvbrskflj@gmail.com\"",
                                        "\"0123006543\"",
                                        "\"amojcuribe38v\""
                                )
                        )
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());
    }
    @Test
    public void testSignup_Invalid_LastName_Null() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                        "\"09143210964\"",
                                        "\"amir\"",
                                        "null",
                                        "\"2000-10-10\"",
                                        "\"male\"",
                                        "\"included\"",
                                        "\"emailvbdfrskj@gmail.com\"",
                                        "\"0123482543\"",
                                        "\"amojcuribe38v\""
                                )
                        )
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }
    @Test
    public void testSignup_Invalid_BirthDate_Null() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                "\"09143291564\"",
                                "\"amir\"",
                                "\"pooyan\"",
                                "null",
                                "\"male\"",
                                "\"included\"",
                                "\"emailfcvvbrskj@gmail.com\"",
                                "\"0123451143\"",
                                "\"amojcuribe38v\""
                        ))
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());
    }
    @Test
    public void testSignup_Invalid_FirstName_Invalid() throws Exception {

        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                        "\"09143014564\"",
                                        "\"\"",
                                        "\"pooyan\"",
                                        "\"2000-10-10\"",
                                        "\"male\"",
                                        "\"included\"",
                                        "\"emaf6ilvbrskj@gmail.com\"",
                                        "\"0933456543\"",
                                        "\"amojcuribe38v\""
                                )
                        )
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    public void testSignup_Invalid_Gender_null() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                "\"09143214564\"",
                                "\"amir\"",
                                "\"pooyan\"",
                                "\"2000-10-10\"",
                                "null",
                                "\"included\"",
                                "\"emailvbrskj@gmail.com\"",
                                "\"0123456543\"",
                                "\"amojcuribe38v\""
                        ))
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    public void testSignup_Invalid_MilitaryStatus_Male_Invalid() throws Exception {
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                        "\"09143219154\"",
                                        "\"amir\"",
                                        "\"pooyan\"",
                                        "\"2000-10-10\"",
                                        "\"male\"",
                                        "null",
                                        "\"emailvbrskvtj@gmail.com\"",
                                        "\"0123456501\"",
                                        "\"amojcuribe38v\""
                                )
                                )
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());
    }
    @Test
    public void testSignup_Valid_MilitaryStatus_Male_Null() throws Exception {
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                        "\"09143210064\"",
                                        "\"amir\"",
                                        "\"pooyan\"",
                                        "\"2020-10-10\"",
                                        "\"male\"",
                                        "null",
                                        "\"emailkfvbrskj@gmail.com\"",
                                        "\"0123446543\"",
                                        "\"amojcuribe38v\""
                                )
                        )
        );
        // Assert
        result.andExpect(status().isOk());
    }

    @Test
    public void testSignup_Valid_MilitaryStatus_Female_Null() throws Exception {
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                        "\"09143218764\"",
                                        "\"amir\"",
                                        "\"pooyan\"",
                                        "\"2000-10-10\"",
                                        "\"female\"",
                                        "null",
                                        "\"emailvbfvrskj@gmail.com\"",
                                        "\"0123226543\"",
                                        "\"amojcuribe38v\""
                                )
                        )
        );
        // Assert
        result.andExpect(status().isOk());
    }

    @Test
    public void testSignup_Valid_MilitaryStatus_Other_Null() throws Exception {
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                        "\"09143218884\"",
                                        "\"amir\"",
                                        "\"pooyan\"",
                                        "\"2000-10-10\"",
                                        "\"others\"",
                                        "null",
                                        "\"emailvbinvcweornvj@gmail.com\"",
                                        "\"0123456333\"",
                                        "\"amojcuribe38v\""
                                )
                        )
        );
        // Assert
        result.andExpect(status().isOk());
    }


    @Test
    public void testLogin_Valid() throws Exception {
        mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                        "\"09140124564\"",
                                        "\"amir\"",
                                        "\"pooyan\"",
                                        "\"2000-10-10\"",
                                        "\"male\"",
                                        "\"included\"",
                                        "\"emailvbrsfdkj@gmail.com\"",
                                        "\"0123456445\"",
                                        "\"amojcuribe38v\""
                                )
                        )
        );

        LoginDto loginDto = new LoginDto();
        loginDto.setNationalId("0123456445");
        loginDto.setPassword("amojcuribe38v");

        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/login")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(loginDto))
        );

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.expiresIn").exists());
    }

    @Test
    public void testLogin_Invalid_Credentials() throws Exception {

        // Arrange
        mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                        "\"09140000564\"",
                                        "\"amir\"",
                                        "\"pooyan\"",
                                        "\"2000-10-10\"",
                                        "\"male\"",
                                        "\"included\"",
                                        "\"emailvbrdfbsfdkj@gmail.com\"",
                                        "\"0123456477\"",
                                        "\"amojcuribe38v\""
                                )
                        )
        );

        LoginDto loginDto = new LoginDto();
        loginDto.setNationalId("0123456477");
        loginDto.setPassword("amojcuribe3fb8v");

        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/login")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(loginDto))
        );

        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.detail", is("Invalid Credentials")));
    }


    @Test
    public void testChangePassword_Valid() throws Exception {
        mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                        "\"09140000564\"",
                                        "\"amir\"",
                                        "\"pooyan\"",
                                        "\"2000-10-10\"",
                                        "\"male\"",
                                        "\"included\"",
                                        "\"emailvbrdfbsfdkj@gmail.com\"",
                                        "\"0123456477\"",
                                        "\"amojcuribe38v\""
                                )
                        )
        );

        ChangePasswordDto changePasswordDto = new ChangePasswordDto();
        changePasswordDto.setNationalId("0123456477");
        changePasswordDto.setOldPassword("amojcuribe38v");
        changePasswordDto.setNewPassword("amojcuribe38vdfkj");

        // Act
        ResultActions result = mockMvc.perform(
                put("/api/account/password")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(changePasswordDto))
        );

        // Assert
        result.andExpect(status().isOk());
    }


    @Test
    public void testChangePassword_Invalid_NotExists() throws Exception {


        ChangePasswordDto changePasswordDto = new ChangePasswordDto();
        changePasswordDto.setNationalId("0978765498");
        changePasswordDto.setOldPassword("xrctvybuklnjh");
        changePasswordDto.setNewPassword("gfhgcvjhbjknjvyhbj");

        // Act
        ResultActions result = mockMvc.perform(
                put("/api/account/password")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(changePasswordDto))
        );

        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.status", is(403)));
    }


    @Test
    public void testChangePassword_Invalid_WrongPassword() throws Exception {
        mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(
                                String.format(accountJsonString,
                                        "\"09140003334\"",
                                        "\"amir\"",
                                        "\"pooyan\"",
                                        "\"2000-10-10\"",
                                        "\"male\"",
                                        "\"included\"",
                                        "\"emailvbrdfvlbsfdkj@gmail.com\"",
                                        "\"0123456888\"",
                                        "\"amojcuribe38v\""
                                )
                        )
        );

        ChangePasswordDto changePasswordDto = new ChangePasswordDto();
        changePasswordDto.setNationalId("0123456888");
        changePasswordDto.setOldPassword("amojcuribe38vlm");
        changePasswordDto.setNewPassword("amojcuribe38v0970h");

        // Act
        ResultActions result = mockMvc.perform(
                put("/api/account/password")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(changePasswordDto))
        );

        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.status", is(403)));
    }


}
