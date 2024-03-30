package com.team.leaf.user.account.dto.request.jwt;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetAccountRes {

    private long id;
    private String email;
    private String name;
    private String nickname;
    private String universityName;
    private String major;

}
