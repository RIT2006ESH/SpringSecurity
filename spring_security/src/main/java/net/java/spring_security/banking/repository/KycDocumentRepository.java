package net.java.spring_security.banking.repository;

import net.java.spring_security.banking.model.KycDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, Integer> {
    Optional<KycDocument> findByCustomerId(Integer customerId);
    List<KycDocument> findByStatus(KycDocument.KycStatus status);
}