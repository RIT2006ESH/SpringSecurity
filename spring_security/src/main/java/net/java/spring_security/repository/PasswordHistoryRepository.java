package net.java.spring_security.repository;

import net.java.spring_security.model.PasswordHistory;
import net.java.spring_security.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {

    // Fetch the most recent N password hashes for a user (pass PageRequest.of(0, N))
    List<PasswordHistory> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}