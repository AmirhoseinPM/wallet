package com.example.wallet.repository;

import com.example.wallet.domain.entity.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionPagingAndSortingRepo
        extends PagingAndSortingRepository<Transaction, Long> {

    @Query(value = "select " +
            " t " +
            " from #{#entityName} AS t " +
            " WHERE t.wallet.account.nationalId = :nationalId " +
            " and t.wallet.isActive = true ")
    List<Transaction> findByWallet_Account_NationalIdAndWallet_IsActive(
            @Param("nationalId") String nationalId, Pageable pageable);

    List<Transaction> findByWallet_Id(long walletId, Pageable pageable);
}
