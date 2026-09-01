package com.fooddelivery.order.web;

import com.fooddelivery.order.domain.Order;
import com.fooddelivery.order.domain.OrderItem;
import com.fooddelivery.order.kafka.OrderEventProducer;
import com.fooddelivery.order.repository.OrderRepository;
import com.fooddelivery.order.web.dto.OrderItemRequest;
import com.fooddelivery.order.web.dto.OrderResponse;
import com.fooddelivery.order.web.dto.PlaceOrderRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    public OrderController(OrderRepository orderRepository, OrderEventProducer orderEventProducer) {
        this.orderRepository = orderRepository;
        this.orderEventProducer = orderEventProducer;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody PlaceOrderRequest request) {
        Order order = new Order();
        order.setRestaurantId(request.getRestaurantId());
        order.setCustomerName(request.getCustomerName());

        for (OrderItemRequest itemRequest : request.getItems()) {
            OrderItem item = new OrderItem();
            item.setName(itemRequest.getName());
            item.setPrice(itemRequest.getPrice());
            item.setQuantity(itemRequest.getQuantity());
            order.addItem(item);
        }

        Order saved = orderRepository.save(order);
        orderEventProducer.publishOrderPlaced(saved);

        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(order -> ResponseEntity.ok(OrderResponse.from(order)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<OrderResponse> listOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(OrderResponse::from)
                .toList();
    }
}
