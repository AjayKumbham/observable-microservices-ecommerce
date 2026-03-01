package com.ecommerce.cartservice.controller;

import com.ecommerce.cartservice.dto.AddItemRequest;
import com.ecommerce.cartservice.dto.CartDto;
import com.ecommerce.cartservice.dto.UpdateQuantityRequest;
import com.ecommerce.cartservice.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * userId is resolved from the X-User-Id header injected by API Gateway.
     * In a real system you'd resolve email->id via a lookup; here we pass userId directly for simplicity.
     */
    @GetMapping
    public ResponseEntity<CartDto> getCart(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(cartService.getCartByUserId(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addItem(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AddItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(userId, request));
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<CartDto> updateItemQuantity(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateQuantityRequest request) {
        return ResponseEntity.ok(cartService.updateItemQuantity(userId, itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDto> removeItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(userId, itemId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @RequestHeader("X-User-Id") Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
