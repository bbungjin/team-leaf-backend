package com.team.leaf.shopping.history.repository;

import com.team.leaf.shopping.history.entity.History;
import com.team.leaf.user.account.entity.AccountDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HistoryRepository extends JpaRepository<History, Long> {

    long deleteByHistoryIdAndAccountDetail(long historyId, AccountDetail accountDetail);

    List<History> findAllByAccountDetailOrderByDateDesc(AccountDetail accountDetail);

    List<History> findAllByAccountDetail(AccountDetail accountDetail);

    long deleteAllByAccountDetail(AccountDetail accountDetail);

    Optional<History> findByAccountDetailAndContent(AccountDetail accountDetail, String content);

}
