package net.java.spring_security.banking.repository;

import net.java.spring_security.banking.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    List<Account> findByCustomerId(Integer customerId);

    @Query("SELECT a FROM Account a WHERE " +
            "CAST(a.accountNumber AS string) LIKE %:keyword% OR " +
            "CAST(a.customerId AS string) LIKE %:keyword%")
    List<Account> searchAccounts(@Param("keyword") String keyword);
}