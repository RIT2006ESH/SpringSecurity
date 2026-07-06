package net.java.spring_security.banking.repository;

import net.java.spring_security.banking.model.EncryptionHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EncryptionHistoryRepository
        extends JpaRepository<EncryptionHistory, Integer> {

    // Get last 3 records for a customer
    @Query("SELECT e FROM EncryptionHistory e " +
            "WHERE e.customerId = :customerId " +
            "ORDER BY e.performedAt DESC")
    List<EncryptionHistory> findLast3ByCustomerId(
            @Param("customerId") Integer customerId,
            Pageable pageable);

    List<EncryptionHistory> findByCustomerIdOrderByPerformedAtDesc(
            Integer customerId);
}