package com.team.leaf.shopping.follow.service;

import com.team.leaf.shopping.follow.dto.FollowRequest;
import com.team.leaf.shopping.follow.dto.FollowRes;
import com.team.leaf.shopping.follow.entity.Follow;
import com.team.leaf.shopping.follow.repository.FollowRepository;
import com.team.leaf.user.account.entity.AccountDetail;
import com.team.leaf.user.account.repository.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public void addFollow(AccountDetail accountDetail, FollowRequest request) {
        AccountDetail targetUser = accountRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("not found User"));

        Follow follow = Follow.createFollow(targetUser, accountDetail);
        if(followRepository.findFollowByTargetUserAndSelfUser(targetUser, accountDetail).isPresent()) {
            throw new RuntimeException("이미 팔로우 중인 사용자입니다.");
        }

        followRepository.save(follow);
    }

    @Transactional
    public void deleteFollow(AccountDetail accountDetail, FollowRequest request) {
        AccountDetail targetUser = accountRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("not found User"));

        followRepository.deleteByTargetUserAndSelfUser(targetUser, accountDetail);
    }

    @Transactional
    public FollowRes getFollow(AccountDetail account) {
        AccountDetail accountDetail = accountRepository.findById(account.getUserId())
                .orElseThrow(() -> new RuntimeException("not found User"));

        List<Follow> following = followRepository.findBySelfUser(accountDetail);
        List<Follow> followers = followRepository.findByTargetUser(accountDetail);

        List<AccountDetail> followingList = following.stream()
                .map(Follow::getTargetUser)
                .collect(Collectors.toList());

        List<AccountDetail> followerList = followers.stream()
                .map(Follow::getSelfUser)
                .collect(Collectors.toList());

        FollowRes response = FollowRes.builder()
                .following(getDetailsFromUsers(followingList))
                .followers(getDetailsFromUsers(followerList))
                .build();

        return response;

    }

    private List<Map<String, Object>> getDetailsFromUsers(List<AccountDetail> users) {
        return users.stream()
                .map(u -> new HashMap<String, Object>() {{
                    put("id", u.getUserId());
                    put("email", u.getEmail());
                    put("nickname", u.getNickname());
                    // 필요에 따라 추가 정보 설정 가능
                }})
                .collect(Collectors.toList());
    }

}
