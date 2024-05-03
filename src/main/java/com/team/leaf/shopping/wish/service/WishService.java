package com.team.leaf.shopping.wish.service;

import com.team.leaf.shopping.product.product.entity.Product;
import com.team.leaf.shopping.wish.dto.WishRequest;
import com.team.leaf.shopping.wish.dto.WishResponse;
import com.team.leaf.shopping.wish.entity.Wish;
import com.team.leaf.shopping.wish.repository.WishRepository;
import com.team.leaf.user.account.entity.AccountDetail;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WishService {
    private final WishRepository wishRepository;
    public List<WishResponse> findWishById(AccountDetail accountDetail){
        List<Wish> wishes = wishRepository.findAllByUserUserId(accountDetail.getUserId());
        List<WishResponse> wishResponses = new ArrayList<>();
            for(Wish wish : wishes){
                Product product = wish.getProduct();
                WishResponse wishResponse = WishResponse.builder()
                        .productId(product.getProductId())
                        .title(product.getTitle())
                        .price(product.getPrice())
                        .image(product.getImage())
                        .discountRate(product.getDiscountRate())
                        .registrationDate(product.getRegistrationDate())
                        .views(product.getViews())
                        .reviews(product.getReviews().size())
                        .build();
                wishResponses.add(wishResponse);
            }
        return wishResponses;
    }

    @Transactional
    public Boolean addWish(WishRequest wishRequest){

        if (wishRepository.existsByUserUserIdAndProductProductId(wishRequest.getUserId(), wishRequest.getProductId())) {
            return false;
        }

        Wish wish = Wish.builder()
                .user(AccountDetail.builder().userId(wishRequest.getUserId()).build())
                .product(Product.builder().productId(wishRequest.getProductId()).build())
                .build();
        try {
            wishRepository.save(wish);
            return true;
        } catch (DataIntegrityViolationException e){
            return false;
        }
    }

    @Transactional
    public Boolean deleteById(WishRequest wishRequest){

        return (wishRepository.deleteByUserUserIdAndProductProductId(wishRequest.getUserId(), wishRequest.getProductId()))>0;
    }
}