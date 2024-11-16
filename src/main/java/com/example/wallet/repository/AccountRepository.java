package com.example.wallet.repository;

import com.example.wallet.domain.entity.Account;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AccountRepository extends CrudRepository<Account, Long> {
    Optional<Account> findByNationalId(String nationalId);
}
