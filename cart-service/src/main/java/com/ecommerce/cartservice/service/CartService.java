package com.ecommerce.cartservice.service;

import com.ecommerce.cartservice.client.ProductClient;
import com.ecommerce.cartservice.dto.*;
import com.ecommerce.cartservice.exception.CartItemNotFoundException;
import com.ecommerce.cartservice.model.Cart;
import com.ecommerce.cartservice.model.CartItem;
import com.ecommerce.cartservice.repository.CartItemRepository;
import com.ecommerce.cartservice.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductClient productClient;

    @Transactional(readOnly = true)
    public CartDto getCartByUserId(Long userId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElse(Cart.builder().userId(userId).build());
        return toDto(cart);
    }

    @Transactional
    public CartDto addItem(Long userId, AddItemRequest request) {
        // Fetch live product data to snapshot into cart
        ProductResponse product = productClient.getProductById(request.getProductId());

        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().userId(userId).build()));

        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), request.getProductId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            item.recalculateSubtotal();
            log.debug("Incremented qty for product {} in cart {}", request.getProductId(), cart.getId());
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productId(product.getId())
                    .productName(product.getName())
                    .productSku(product.getSku())
                    .unitPrice(product.getPrice())
                    .quantity(request.getQuantity())
                    .imageUrl(product.getImageUrl())
                    .build();
            newItem.recalculateSubtotal();
            cart.getItems().add(newItem);
        }

        cart.recalculateTotal();
        return toDto(cartRepository.save(cart));
    }

    @Transactional
    public CartDto updateItemQuantity(Long userId, Long itemId, UpdateQuantityRequest request) {
        Cart cart = getOrThrow(userId);
        CartItem item = cartItemRepository.findById(itemId)
                .filter(i -> i.getCart().getId().equals(cart.getId()))
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found: " + itemId));

        if (request.getQuantity() == 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
            log.info("Removed item {} from cart {}", itemId, cart.getId());
        } else {
            item.setQuantity(request.getQuantity());
            item.recalculateSubtotal();
        }

        cart.recalculateTotal();
        return toDto(cartRepository.save(cart));
    }

    @Transactional
    public CartDto removeItem(Long userId, Long itemId) {
        Cart cart = getOrThrow(userId);
        CartItem item = cartItemRepository.findById(itemId)
                .filter(i -> i.getCart().getId().equals(cart.getId()))
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found: " + itemId));
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        cart.recalculateTotal();
        log.info("Removed item {} from cart for user {}", itemId, userId);
        return toDto(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().clear();
            cart.recalculateTotal();
            cartRepository.save(cart);
            log.info("Cleared cart for user {}", userId);
        });
    }

    // -------------------------------------------------------------------------
    private Cart getOrThrow(Long userId) {
        return cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new CartItemNotFoundException("No cart found for user: " + userId));
    }

    private CartDto toDto(Cart cart) {
        List<CartItemDto> itemDtos = cart.getItems().stream().map(i ->
                CartItemDto.builder()
                        .id(i.getId()).productId(i.getProductId())
                        .productName(i.getProductName()).productSku(i.getProductSku())
                        .unitPrice(i.getUnitPrice()).quantity(i.getQuantity())
                        .subtotal(i.getSubtotal()).imageUrl(i.getImageUrl())
                        .build()
        ).collect(Collectors.toList());

        return CartDto.builder()
                .id(cart.getId()).userId(cart.getUserId())
                .items(itemDtos).totalPrice(cart.getTotalPrice())
                .itemCount(itemDtos.size()).updatedAt(cart.getUpdatedAt())
                .build();
    }
}
