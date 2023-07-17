package com.example.repository;

import com.example.model.Transaction;
import com.example.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    @Modifying
    @Transactional
    @Query("update Transaction t set t.transactionStatus = ?2 where t.externalId = ?1")
    void updateTransaction(String externalTxnId, TransactionStatus transactionStatus);
}
