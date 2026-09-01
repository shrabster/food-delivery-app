package com.fooddelivery.restaurant.web;

import com.fooddelivery.restaurant.domain.QueueEntry;
import com.fooddelivery.restaurant.domain.QueueStatus;
import com.fooddelivery.restaurant.kafka.OrderEventProducer;
import com.fooddelivery.restaurant.repository.MenuItemRepository;
import com.fooddelivery.restaurant.repository.QueueEntryRepository;
import com.fooddelivery.restaurant.repository.RestaurantRepository;
import com.fooddelivery.restaurant.web.dto.MenuItemResponse;
import com.fooddelivery.restaurant.web.dto.QueueEntryResponse;
import com.fooddelivery.restaurant.web.dto.RestaurantResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestaurantController {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final QueueEntryRepository queueEntryRepository;
    private final OrderEventProducer orderEventProducer;

    public RestaurantController(
            RestaurantRepository restaurantRepository,
            MenuItemRepository menuItemRepository,
            QueueEntryRepository queueEntryRepository,
            OrderEventProducer orderEventProducer) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.queueEntryRepository = queueEntryRepository;
        this.orderEventProducer = orderEventProducer;
    }

    @GetMapping("/restaurants")
    public List<RestaurantResponse> listRestaurants() {
        return restaurantRepository.findAll().stream().map(RestaurantResponse::from).toList();
    }

    @GetMapping("/restaurants/{id}/menu")
    public List<MenuItemResponse> getMenu(@PathVariable Long id) {
        return menuItemRepository.findAllByRestaurantId(id).stream().map(MenuItemResponse::from).toList();
    }

    @GetMapping("/restaurants/{id}/queue")
    public List<QueueEntryResponse> getQueue(@PathVariable Long id) {
        return queueEntryRepository.findAllByRestaurantIdOrderByCreatedAtAsc(id).stream()
                .map(QueueEntryResponse::from)
                .toList();
    }

    @PostMapping("/queue/{orderId}/ready")
    public ResponseEntity<QueueEntryResponse> markReady(@PathVariable Long orderId) {
        return queueEntryRepository.findByOrderId(orderId)
                .map(entry -> {
                    entry.setStatus(QueueStatus.READY);
                    QueueEntry saved = queueEntryRepository.save(entry);
                    orderEventProducer.publishOrderReady(saved.getOrderId(), saved.getRestaurantId());
                    return ResponseEntity.ok(QueueEntryResponse.from(saved));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
