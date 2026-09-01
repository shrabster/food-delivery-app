package com.fooddelivery.delivery.web;

import com.fooddelivery.delivery.domain.Delivery;
import com.fooddelivery.delivery.domain.DeliveryStatus;
import com.fooddelivery.delivery.kafka.DeliveryEventProducer;
import com.fooddelivery.delivery.repository.CourierRepository;
import com.fooddelivery.delivery.repository.DeliveryRepository;
import com.fooddelivery.delivery.web.dto.CourierResponse;
import com.fooddelivery.delivery.web.dto.DeliveryResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeliveryController {

    private final CourierRepository courierRepository;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryEventProducer deliveryEventProducer;

    public DeliveryController(
            CourierRepository courierRepository,
            DeliveryRepository deliveryRepository,
            DeliveryEventProducer deliveryEventProducer) {
        this.courierRepository = courierRepository;
        this.deliveryRepository = deliveryRepository;
        this.deliveryEventProducer = deliveryEventProducer;
    }

    @GetMapping("/couriers")
    public List<CourierResponse> listCouriers() {
        return courierRepository.findAll().stream().map(CourierResponse::from).toList();
    }

    @GetMapping("/couriers/{courierId}/deliveries")
    public List<DeliveryResponse> listDeliveries(@PathVariable Long courierId) {
        return deliveryRepository.findAllByCourierIdOrderByCreatedAtDesc(courierId).stream()
                .map(DeliveryResponse::from)
                .toList();
    }

    @PostMapping("/deliveries/{id}/picked-up")
    public ResponseEntity<DeliveryResponse> markPickedUp(@PathVariable Long id) {
        return updateStatus(id, DeliveryStatus.PICKED_UP);
    }

    @PostMapping("/deliveries/{id}/delivered")
    public ResponseEntity<DeliveryResponse> markDelivered(@PathVariable Long id) {
        return updateStatus(id, DeliveryStatus.DELIVERED);
    }

    private ResponseEntity<DeliveryResponse> updateStatus(Long deliveryId, DeliveryStatus status) {
        return deliveryRepository.findById(deliveryId)
                .map(delivery -> {
                    delivery.setStatus(status);
                    Delivery saved = deliveryRepository.save(delivery);

                    if (status == DeliveryStatus.DELIVERED) {
                        courierRepository.findById(saved.getCourierId()).ifPresent(courier -> {
                            courier.setAvailable(true);
                            courierRepository.save(courier);
                        });
                    }

                    deliveryEventProducer.publishStatusUpdated(saved.getOrderId(), status.name());
                    return ResponseEntity.ok(DeliveryResponse.from(saved));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
