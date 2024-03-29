package com.team.leaf.shopping.order.entity;

import com.team.leaf.shopping.product.product.entity.Product;
import com.team.leaf.user.account.entity.AccountDetail;
import com.team.leaf.user.account.entity.ShippingAddress;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long orderId;

    private int productCount;

    private int totalPrice;

    private String commission;

    private String status;

    private LocalDateTime orderDate;

    @OneToOne(cascade = CascadeType.PERSIST)
    private OrderPaymentDetail orderDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    private AccountDetail accountDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @OneToOne(cascade = CascadeType.PERSIST)
    private ShippingAddress shippingAddress;

}
