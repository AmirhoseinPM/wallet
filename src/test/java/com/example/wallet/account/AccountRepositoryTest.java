package com.example.wallet.account;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertTrue;


@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountRepositoryTest {

    @Autowired
    AccountRepository accountRepository;

    private final Logger log = LoggerFactory.getLogger(AccountRepositoryTest.class);

    @Test
    @DisplayName("Test 1:Save valid Account Test")
    @Order(1)
    @Rollback(value = false)
    public void saveAccountTest_ValidAccount(){

        //Action
        Account validAccount = accountRepository.save(
                new Account(
                        "0123456789", "09123456789", "email@g.com", "goodpassword34",
                        "firstName", "lastName", LocalDate.parse("2010-10-10"), "m", "i",
                        Date.from(Instant.now())
                )
        );
        //Verify
        log.info(validAccount.toString());
        assertTrue(validAccount.getId() > 0);
    }

    @Test
    @Order(2)
    @DisplayName("Test 2:Get Account")
    public void getAccountTest(){

        //Action
        Account  account = accountRepository.findById(1L).get();
        //Verify
        log.info(account.toString());
        Assertions.assertThat(account.getId()).isEqualTo(1L);
    }


    @Test
    @Order(3)
    @DisplayName("Test 1:Get Accounts List Test")
    public void getListOfEmployeesTest(){
        //Action
        List<Account> accounts = (List<Account>) accountRepository.findAll();
        //Verify
        log.info(accounts.toString());
        Assertions.assertThat(accounts.size()).isGreaterThan(0);

    }

    @Test
    @Order(4)
    @DisplayName("Test 1:Update Account Test")
    @Rollback(value = false)
    public void updateEmployeeTest(){

        //Action
        Account account = accountRepository.findById(1L).get();
        account.setEmail("samcurran@gmail.com");
        Account accountUpdated =  accountRepository.save(account);

        //Verify
        log.info(accountUpdated.toString());
        Assertions.assertThat(accountUpdated.getEmail()).isEqualTo("samcurran@gmail.com");

    }

    @Test
    @Order(5)
    @DisplayName("Test 1:Delete Account Test")
    @Rollback(value = false)
    public void deleteEmployeeTest(){
        //Action
        accountRepository.deleteById(1L);
        Account account = accountRepository.findById(1L).orElse(null);

        //Verify
        Assertions.assertThat(account).isEqualTo(null);
    }

}
