package net.java.spring_security.banking.repository;

import net.java.spring_security.banking.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    List<Customer> findByEmail(String email);
    Optional<Customer> findByPhone(String phone);

    @Query("SELECT c FROM Customer c WHERE " +
            "CAST(c.customerId AS string) LIKE %:keyword% OR " +
            "c.phone LIKE %:keyword% OR " +
            "c.email LIKE %:keyword% OR " +
            "c.firstName LIKE %:keyword% OR " +
            "c.lastName LIKE %:keyword%")
    List<Customer> searchCustomers(@Param("keyword") String keyword);
}