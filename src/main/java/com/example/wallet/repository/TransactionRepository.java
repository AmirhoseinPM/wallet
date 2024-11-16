package com.example.wallet.repository;

import com.example.wallet.domain.entity.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TransactionRepository extends CrudRepository<Transaction, Long> {
    @Query(value = "select " +
            " COALESCE(sum(t.amount), 0) " +
            " from #{#entityName} AS t " +
            " WHERE t.wallet.id = :walletId " +
            " and t.createdAt >= :today " +
            " and t.isWithdrawal = true ")
    Optional<Long> calculateTodayWithdraw(@Param("today") LocalDateTime today,
                                          @Param("walletId") long walletId);
}
