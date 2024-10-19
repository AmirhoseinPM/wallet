package com.example.wallet.account;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AccountDetailsRepository extends CrudRepository<AccountDetails, Long> {
    Optional<AccountDetails> findByAccount_NationalId(String nationalId);
}
