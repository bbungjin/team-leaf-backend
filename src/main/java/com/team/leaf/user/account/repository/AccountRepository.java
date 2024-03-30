package com.team.leaf.user.account.repository;

import com.team.leaf.user.account.entity.AccountDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountDetail, Long> {

    Optional<AccountDetail> findByEmail(String email);
    Optional<AccountDetail> findByNickname(String nickname);
    boolean existsByNickname(String nickname);
    Optional<AccountDetail> findByPhone(String phone);

}