package com.example.wallet.account;


import com.example.wallet.jwt.JwtService;
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

    private void setNewAccount(String nationalId, String phoneNumber, String email) {
        account = new Account(
                nationalId, phoneNumber, email, "goodpassword34",
                "firstName", "lastName", LocalDate.parse("2010-10-10"), "m", "i",
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
                .andExpect(jsonPath("$.birthDate[0]",is(account.getBirthDate().getYear())))
                .andExpect(jsonPath("$.birthDate[1]",is(account.getBirthDate().getMonthValue())))
                .andExpect(jsonPath("$.birthDate[2]",is(account.getBirthDate().getDayOfMonth())))
                .andExpect(jsonPath("$.militaryStatus",is(account.getMilitaryStatus())))
                .andExpect(jsonPath("$.gender",is(account.getGender())));
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
        // Arrange
        setNewAccount("0123454312", "09123455432", "emailkbfvbd@gmail.com");
        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
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
    public void testSignup_Invalid_NationalId_TooShort() throws Exception {
        // Arrange
        setNewAccount("0123456", "09123456787", "email2@g.com");
        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());

    }

    @Test
    public void testSignup_Invalid_NationalId_TooLong() throws Exception {
        // Arrange
        setNewAccount("012345611111222", "09123456787", "email2@g.com");
        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());
    }
    @Test
    public void testSignup_Invalid_NationalId_BadChar() throws Exception {
        // Arrange
        setNewAccount("012345678o", "09123456799", "emailj2@g.com");
        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());
    }


    @Test
    public void testSignup_Invalid_NationalId_NotUnique() throws Exception {
        // Arrange
        setNewAccount("0123456787", "09123456787", "email2@g.com");
        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
        );
        result.andExpect(status().isOk());
        account.setPhoneNumber("09935985476");
        account.setEmail("anotherEmail@gmail.com");
        account.setId(null);
        ResultActions result2 = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
        );
        // Assert
        result2.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("Account creation failed, national id or phone number or email already exist")))
                .andExpect(jsonPath("$.status", is("400")));
    }
    @Test
    public void testSignup_Invalid_PhoneNumber_TooLong() throws Exception {
        // Arrange
        setNewAccount("0123456789", "091234567871", "email2@g.com");
        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());
    }
    @Test
    public void testSignup_Invalid_PhoneNumber_TooShort() throws Exception {
        // Arrange
        setNewAccount("0123456789", "0912345678", "email2@g.com");
        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
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
                        .content(objectMapper.writeValueAsString(account))
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());
    }
    @Test
    public void testSignup_Invalid_PhoneNumber_NotUnique() throws Exception {
        // Arrange
        setNewAccount("0123456321", "09123456321", "email3@gmail.com");
        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
        );
        result.andExpect(status().isOk());

        account.setEmail("otherEmail@gamil.com");
        account.setNationalId("0123456543");

        ResultActions result2 = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
        );
        // Assert
        result2.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("Account creation failed, national id or phone number or email already exist")))
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
                        .content(objectMapper.writeValueAsString(account))
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    public void testSignup_Invalid_Email_NotUnique() throws Exception {
        // Arrange
        setNewAccount("0912387645", "09183456321", "emailvbrskj@gmail.com");
        // Act
        ResultActions result1 = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
        );
        result1.andExpect(status().isOk());

        account.setPhoneNumber("09143214564");
        account.setNationalId("0123456543");
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
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
        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());
    }
    @Test
    public void testSignup_Invalid_FirstName_Null() throws Exception {
        // Arrange
        setNewAccount("0123456789", "09123456798", "email7@gmail.com");
        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());
    }
    @Test
    public void testSignup_Invalid_LastName_Null() throws Exception {
        // Arrange
        setNewAccount("0123456789", "09123456798", "email7@gmail.com");
        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());
    }
    @Test
    public void testSignup_Invalid_BirthDate_Null() throws Exception {
        // Arrange
        setNewAccount("0123456789", "09123456798", "email7@gmail.com");
        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());
    }
    @Test
    public void testSignup_Invalid_FirstName_Invalid() throws Exception {
        // Arrange
        setNewAccount("0123456789", "09123456798", "email7@gmail.com");
        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    public void testSignup_Invalid_Gender_null() throws Exception {
        // Arrange
        setNewAccount("0123456789", "09123456798", "email7@gmail.com");
        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    public void testSignup_Invalid_Gender_Invalid() throws Exception {
        // Arrange
        setNewAccount("0123456789", "09123456798", "email7@gmail.com");
        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    public void testSignup_Invalid_MilitaryStatus_Male_Null() throws Exception {
        // Arrange
        setNewAccount("0123456789", "09123456798", "email7@gmail.com");

        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    public void testSignup_Invalid_MilitaryStatus_Male_Invalid() throws Exception {
        // Arrange
        setNewAccount("0123456789", "09123456798", "email7@gmail.com");

        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
        );
        // Assert
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("bad request")))
                .andExpect(jsonPath("$.status", is("400")))
                .andExpect(jsonPath("$.errors").exists());
    }
    @Test
    public void testSignup_Valid_MilitaryStatus_Male_Null() throws Exception {
        // Arrange
        setNewAccount("0123458149", "09913456798", "emailkujb7@gmail.com");

        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
        );
        // Assert
        result.andExpect(status().isOk());
    }

    @Test
    public void testSignup_Valid_MilitaryStatus_Female_Null() throws Exception {
        // Arrange
        setNewAccount("0123456789", "09123456798", "email7@gmail.com");
        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
        );
        // Assert
        result.andExpect(status().isOk());
    }

    @Test
    public void testSignup_Valid_MilitaryStatus_Other_Null() throws Exception {
        // Arrange
        setNewAccount("0123419389", "09213456798", "email867@gmail.com");

        // Act
        ResultActions result = mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
        );
        // Assert
        result.andExpect(status().isOk());
    }


    @Test
    public void testLogin_Valid() throws Exception {

        // Arrange
        setNewAccount("0129416789", "09985716789", "emailtkyvgest@gvj.com");

        mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
        );

        LoginDto loginDto = new LoginDto();
        loginDto.setNationalId(account.getNationalId());
        loginDto.setPassword(account.getPassword());

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
        setNewAccount("0129416789", "09985716789", "emailtkyvgest@gvj.com");
        mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
        );

        LoginDto loginDto = new LoginDto();
        loginDto.setNationalId(account.getNationalId().replace("1", "3"));
        loginDto.setPassword(account.getPassword());

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

        // Arrange
        setNewAccount("0129021789", "09981706789", "emcvkyvgest@gvj.com");

        mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
        );

        ChangePasswordDto changePasswordDto = new ChangePasswordDto();
        changePasswordDto.setNationalId(account.getNationalId());
        changePasswordDto.setOldPassword(account.getPassword());
        changePasswordDto.setNewPassword(account.getPassword() + "123");

        // Act
        ResultActions result = mockMvc.perform(
                put("/api/account/password")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(changePasswordDto))
        );

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.password").exists());
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

        // Arrange
        setNewAccount("0126621789", "09911706789", "emcqadvgest@gvj.com");
        mockMvc.perform(
                post("/api/account/signup")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(account))
        );

        ChangePasswordDto changePasswordDto = new ChangePasswordDto();
        changePasswordDto.setNationalId(account.getNationalId());
        changePasswordDto.setOldPassword(account.getPassword() + "1");
        changePasswordDto.setNewPassword(account.getPassword() + "123");

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
