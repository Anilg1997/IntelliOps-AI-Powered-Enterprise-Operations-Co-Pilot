package com.intellops.billing.repository;

import com.intellops.billing.entity.BillingAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillingAccountRepository extends JpaRepository<BillingAccount, Long> {
    Optional<BillingAccount> findByAccountNumber(String accountNumber);
    Optional<BillingAccount> findByCustomerEmail(String customerEmail);
    boolean existsByCustomerEmail(String customerEmail);
}
