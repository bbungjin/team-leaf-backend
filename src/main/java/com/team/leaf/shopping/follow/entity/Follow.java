package com.team.leaf.shopping.follow.entity;

import com.team.leaf.user.account.entity.AccountDetail;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Follow {

    @Id
    @GeneratedValue
    private long followId;

    // 팔로우 받는 사람 (toUser)
    @ManyToOne(fetch = FetchType.LAZY)
    private AccountDetail targetUser; // 대상

    @ManyToOne(fetch = FetchType.LAZY)
    private AccountDetail selfUser; // 기준

    // fromUser
    public static Follow createFollow(AccountDetail targetUser, AccountDetail selfUser) {
        return Follow.builder()
                .targetUser(targetUser)
                .selfUser(selfUser)
                .build();
    }

}
