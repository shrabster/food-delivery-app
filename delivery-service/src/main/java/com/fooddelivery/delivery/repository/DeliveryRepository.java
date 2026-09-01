package com.fooddelivery.delivery.repository;

import com.fooddelivery.delivery.domain.Delivery;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByOrderId(Long orderId);

    List<Delivery> findAllByCourierIdOrderByCreatedAtDesc(Long courierId);
}
