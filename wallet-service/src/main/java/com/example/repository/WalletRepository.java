package com.example.repository;

import com.example.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface WalletRepository extends JpaRepository<Wallet, Integer> {

    Wallet findByWalletId(String walletId);

    @Modifying
    @Transactional
    @Query("update Wallet w set w.balance = w.balance + :amount where w.walletId = :walletId")
    void updateWallet(String walletId, Long amount);

//    @Query("update wallet w set w.balance = w.balance + : amount where w.walletId = :walletId")
//    void incrementWallet(String walletId, Long amount);
//
//    @Query("update wallet w set w.balance = w.balance - : amount where w.walletId = :walletId")
//    void decrementWallet(String walletId, Long amount);
}
