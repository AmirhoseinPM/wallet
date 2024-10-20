package com.example.wallet.repository;

import com.example.wallet.entity.Wallet;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface WalletRepository extends CrudRepository<Wallet, Long> {
    List<Wallet> findByAccount_NationalId(String nationalId);
}
