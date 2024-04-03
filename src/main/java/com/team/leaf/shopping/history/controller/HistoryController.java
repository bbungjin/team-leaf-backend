package com.team.leaf.shopping.history.controller;

import com.team.leaf.common.custom.LogIn;
import com.team.leaf.shopping.history.dto.HistoryRequest;
import com.team.leaf.shopping.history.entity.History;
import com.team.leaf.shopping.history.service.HistoryService;
import com.team.leaf.user.account.entity.AccountDetail;
import com.team.leaf.user.account.exception.ApiResponse;
import com.team.leaf.user.account.exception.ApiResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/history")
public class HistoryController {

    private final HistoryService historyService;

    @DeleteMapping("/{historyId}")
    @Operation(summary= "[ 사용자 인증 정보 필요 ] 특정 history 삭제")
    public ApiResponse deleteHistory(@PathVariable long historyId, @LogIn @Parameter(hidden = true) AccountDetail accountDetail) {
        historyService.deleteHistory(historyId, accountDetail);

        return new ApiResponse(ApiResponseStatus.SUCCESS);
    }

    @PostMapping
    @Operation(summary= "[ 사용자 인증 정보 필요 ] history 등록")
    public ApiResponse addOrUpdateHistory(@RequestBody HistoryRequest request, @LogIn @Parameter(hidden = true) AccountDetail accountDetail) {
        historyService.addOrUpdateHistory(request, accountDetail);

        return new ApiResponse(ApiResponseStatus.SUCCESS);
    }

    @GetMapping
    @Operation(summary= "[ 사용자 인증 정보 필요 ] 모든 history 가져오기")
    public ApiResponse getAllHistory(@LogIn @Parameter(hidden = true) AccountDetail accountDetail) {
        List<History> result = historyService.getAllHistory(accountDetail);

        return new ApiResponse(result);
    }

    @DeleteMapping
    @Operation(summary= "[ 사용자 인증 정보 필요 ] 모든 history 삭제")
    public ApiResponse deleteAllHistory(@LogIn @Parameter(hidden = true) AccountDetail accountDetail) {
        historyService.deleteAllHistory(accountDetail);

        return new ApiResponse(ApiResponseStatus.SUCCESS);
    }

}
