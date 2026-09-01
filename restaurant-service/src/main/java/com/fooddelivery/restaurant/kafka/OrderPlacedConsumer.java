package com.fooddelivery.restaurant.kafka;

import com.fooddelivery.restaurant.domain.QueueEntry;
import com.fooddelivery.restaurant.domain.QueueEntryItem;
import com.fooddelivery.restaurant.kafka.events.OrderPlacedEvent;
import com.fooddelivery.restaurant.repository.QueueEntryRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderPlacedConsumer {

    private final QueueEntryRepository queueEntryRepository;

    public OrderPlacedConsumer(QueueEntryRepository queueEntryRepository) {
        this.queueEntryRepository = queueEntryRepository;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_PLACED, groupId = "restaurant-service")
    public void onOrderPlaced(OrderPlacedEvent event) {
        if (queueEntryRepository.findByOrderId(event.getOrderId()).isPresent()) {
            return;
        }

        QueueEntry queueEntry = new QueueEntry();
        queueEntry.setOrderId(event.getOrderId());
        queueEntry.setRestaurantId(event.getRestaurantId());
        queueEntry.setCustomerName(event.getCustomerName());

        for (OrderPlacedEvent.Item item : event.getItems()) {
            QueueEntryItem queueEntryItem = new QueueEntryItem();
            queueEntryItem.setName(item.getName());
            queueEntryItem.setPrice(item.getPrice());
            queueEntryItem.setQuantity(item.getQuantity());
            queueEntry.addItem(queueEntryItem);
        }

        queueEntryRepository.save(queueEntry);
    }
}
