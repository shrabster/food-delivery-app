package com.fooddelivery.delivery.repository;

import com.fooddelivery.delivery.domain.Courier;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CourierRepository extends JpaRepository<Courier, Long> {

    /**
     * FOR UPDATE locks the row so a concurrent caller can't read the same courier as available
     * before this transaction commits; SKIP LOCKED lets that caller move on to the next available
     * one instead of blocking behind the lock.
     */
    @Query(
            value = "SELECT * FROM couriers WHERE available = true ORDER BY id LIMIT 1 FOR UPDATE SKIP LOCKED",
            nativeQuery = true)
    Optional<Courier> findNextAvailableForUpdate();
}
