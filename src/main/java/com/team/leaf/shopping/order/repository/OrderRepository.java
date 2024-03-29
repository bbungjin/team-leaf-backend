package com.team.leaf.shopping.order.repository;

import com.team.leaf.shopping.order.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderDetail, Long> {


    List<OrderDetail> findByAccountDetailUserId(long userId);
}
