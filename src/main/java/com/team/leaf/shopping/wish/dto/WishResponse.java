package com.team.leaf.shopping.wish.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WishResponse {

    private long productId;

    private String title;

    private int price;

    private String image;

    private double discountRate;

    private LocalDateTime registrationDate;
    // 조회수
    private long views;

    private int reviews;


}
