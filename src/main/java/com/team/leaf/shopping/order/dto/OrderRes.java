package com.team.leaf.shopping.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRes {

    private long orderId;

    private long productId;

    private int productCount;

    private int totalPrice;

    private String commission;

    private String status;

    private String productName;

    private int productPrice;

    private String recipient;

    private String phone;

    private String address;

    private String detailedAddress;

    private LocalDateTime orderDate;


}
