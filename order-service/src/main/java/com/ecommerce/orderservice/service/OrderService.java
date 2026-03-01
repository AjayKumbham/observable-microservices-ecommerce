package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.client.NotificationClient;
import com.ecommerce.orderservice.client.PaymentClient;
import com.ecommerce.orderservice.client.ProductClient;
import com.ecommerce.orderservice.dto.*;
import com.ecommerce.orderservice.exception.OrderNotFoundException;
import com.ecommerce.orderservice.exception.OrderStatusException;
import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.model.OrderItem;
import com.ecommerce.orderservice.model.OrderStatus;
import com.ecommerce.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Order service — orchestrates the checkout flow:
 * 1. Fetch product details from product-service
 * 2. Persist order as PENDING
 * 3. Call payment-service
 * 4. Update order status based on payment result
 * 5. Decrement stock in product-service
 * 6. Fire confirmation notification (best-effort)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final PaymentClient paymentClient;
    private final NotificationClient notificationClient;

    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {
        log.info("Creating order for user {}", request.getUserId());

        // Step 1: Build order items by fetching product data
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {
            ProductResponse product = productClient.getProductById(itemReq.getProductId());

            if (!product.isActive()) {
                throw new IllegalArgumentException("Product '" + product.getName() + "' is not available");
            }
            if (product.getStockQuantity() < itemReq.getQuantity()) {
                throw new IllegalArgumentException(
                    "Insufficient stock for '" + product.getName() + "'. Available: " + product.getStockQuantity());
            }

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            OrderItem item = OrderItem.builder()
                    .productId(product.getId()).productName(product.getName())
                    .productSku(product.getSku()).unitPrice(product.getPrice())
                    .quantity(itemReq.getQuantity()).subtotal(subtotal)
                    .build();
            orderItems.add(item);
            total = total.add(subtotal);
        }

        // Step 2: Persist order as PENDING
        Order order = Order.builder()
                .userId(request.getUserId()).shippingAddress(request.getShippingAddress())
                .totalAmount(total).status(OrderStatus.PENDING).notes(request.getNotes())
                .build();
        order = orderRepository.save(order);

        // Link items to order
        for (OrderItem item : orderItems) { item.setOrder(order); }
        order.setItems(orderItems);
        order = orderRepository.save(order);
        log.info("Order {} persisted as PENDING, total={}", order.getId(), total);

        // Step 3: Process payment
        PaymentRequest paymentReq = PaymentRequest.builder()
                .orderId(order.getId()).userId(request.getUserId())
                .amount(total).paymentMethod(request.getPaymentMethod().name())
                .build();

        PaymentResponse paymentResp = paymentClient.chargePayment(paymentReq);

        // Step 4: Update order status
        if ("SUCCESS".equals(paymentResp.getStatus())) {
            order.setStatus(OrderStatus.CONFIRMED);
            order.setPaymentTransactionId(paymentResp.getTransactionId());
            log.info("Order {} CONFIRMED, txn={}", order.getId(), paymentResp.getTransactionId());

            // Step 5: Decrement stock (best-effort)
            for (OrderItem item : orderItems) {
                productClient.decrementStock(item.getProductId(), item.getQuantity());
            }

            // Step 6: Send confirmation notification
            notificationClient.sendNotification(NotificationRequest.builder()
                    .recipient("user-" + request.getUserId() + "@ecommerce.com")  // resolved from userId
                    .subject("Order #" + order.getId() + " Confirmed!")
                    .body("Your order has been confirmed. Transaction ID: " + paymentResp.getTransactionId()
                          + ". Total: ₹" + total)
                    .type("EMAIL")
                    .referenceId(order.getId())
                    .build());
        } else {
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            log.warn("Order {} PAYMENT_FAILED: {}", order.getId(), paymentResp.getMessage());

            notificationClient.sendNotification(NotificationRequest.builder()
                    .recipient("user-" + request.getUserId() + "@ecommerce.com")
                    .subject("Payment Failed for Order #" + order.getId())
                    .body("Unfortunately your payment failed. Reason: " + paymentResp.getMessage())
                    .type("EMAIL")
                    .referenceId(order.getId())
                    .build());
        }

        return toDto(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id) {
        return toDto(orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id)));
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getOrdersByUserId(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(this::toDto);
    }

    @Transactional
    public OrderDto cancelOrder(Long id) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new OrderStatusException(
                "Cannot cancel order in status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        log.info("Order {} cancelled", id);
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto updateOrderStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));
        order.setStatus(newStatus);
        log.info("Order {} status updated to {}", id, newStatus);
        return toDto(orderRepository.save(order));
    }

    // -------------------------------------------------------------------------
    private OrderDto toDto(Order o) {
        List<OrderItemDto> itemDtos = o.getItems().stream().map(i ->
                OrderItemDto.builder().id(i.getId()).productId(i.getProductId())
                        .productName(i.getProductName()).productSku(i.getProductSku())
                        .unitPrice(i.getUnitPrice()).quantity(i.getQuantity())
                        .subtotal(i.getSubtotal()).build()
        ).collect(Collectors.toList());

        return OrderDto.builder().id(o.getId()).userId(o.getUserId())
                .items(itemDtos).totalAmount(o.getTotalAmount()).status(o.getStatus())
                .shippingAddress(o.getShippingAddress())
                .paymentTransactionId(o.getPaymentTransactionId())
                .notes(o.getNotes()).createdAt(o.getCreatedAt()).updatedAt(o.getUpdatedAt())
                .build();
    }
}
