package com.example.wallet.transaction;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends CrudRepository<Transaction, Long> {
    List<Transaction> findByWallet_Account_NationalId(String nationalId);
    List<Transaction> findByWallet_Id(long walletId);


    @Query(value = "select " +
            " COALESCE(sum(t.amount), 0) " +
            " from Transaction AS t " +
            " WHERE t.wallet.id = :walletId " +
            " and t.createdAt >= :today " +
            " and t.isWithdrawal = true ")
    Optional<Long> calculateTodayWithdraw(@Param("today") LocalDateTime today,
                                         @Param("walletId") long walletId);
}
