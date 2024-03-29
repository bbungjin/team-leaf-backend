package com.team.leaf.shopping.order.service;

import com.team.leaf.shopping.order.entity.OrderDetail;
import com.team.leaf.shopping.order.dto.OrderRes;
import com.team.leaf.shopping.order.repository.OrderRepository;
import com.team.leaf.user.account.entity.AccountDetail;
import com.team.leaf.user.account.repository.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final AccountRepository accountRepository;
    private final OrderRepository orderRepository;

    public Map<String, List<OrderRes>> getOrders(AccountDetail account) {
        AccountDetail accountDetail = accountRepository.findById(account.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<OrderDetail> orderDetails = orderRepository.findByAccountDetailUserId(accountDetail.getUserId());

        Map<String, List<OrderRes>> orderMap = new HashMap<>();
        for (OrderDetail orderDetail : orderDetails) {
            String status = orderDetail.getStatus();
            List<OrderRes> orderList = orderMap.getOrDefault(status, new ArrayList<>());

            OrderRes response = OrderRes.builder()
                    .orderId(orderDetail.getOrderId())
                    .productCount(orderDetail.getProductCount())
                    .totalPrice(orderDetail.getTotalPrice())
                    .commission(orderDetail.getCommission())
                    .status(orderDetail.getStatus())
                    .productId(orderDetail.getProduct().getProductId())
                    .productName(orderDetail.getProduct().getTitle())
                    .productPrice(orderDetail.getProduct().getPrice())
                    .recipient(orderDetail.getShippingAddress().getRecipient())
                    .phone(orderDetail.getShippingAddress().getPhone())
                    .address(orderDetail.getShippingAddress().getAddress())
                    .detailedAddress(orderDetail.getShippingAddress().getDetailedAddress())
                    .build();

            orderList.add(response);
            orderMap.put(status, orderList);
        }

        return orderMap;
    }

    @Transactional
    public String  deleteOrder(AccountDetail account, long orderId) {
        OrderDetail orderDetail = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        AccountDetail accountDetail1 = accountRepository.findById(account.getUserId())
                .orElseThrow(()  -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if(accountDetail1.getUserId() != orderDetail.getAccountDetail().getUserId()) {
            throw new RuntimeException("본인이 작성한 리뷰만 수정할 수 있습니다.");
        }
        orderRepository.delete(orderDetail);

        return "Success delete";
    }


}
