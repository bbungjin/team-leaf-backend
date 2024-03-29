package com.team.leaf.shopping.follow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowRes {

    private List<Map<String, Object>> following;

    private List<Map<String, Object>> followers;

}
