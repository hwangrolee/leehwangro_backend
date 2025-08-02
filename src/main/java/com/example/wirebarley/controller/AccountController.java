package com.example.wirebarley.controller;

import com.example.wirebarley.domain.Account;
import com.example.wirebarley.domain.Transaction;
import com.example.wirebarley.dto.*;
import com.example.wirebarley.service.AccountService;
import com.example.wirebarley.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "Accounts", description = "계좌 관련 API")
@RestController
@RequestMapping(value = "/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @Operation(summary = "신규 계좌 생성 API", description = "사용자 정보와 초기 비밀번호를 받아 새로운 계좌를 개설합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "계좌 생성 성공", content = @Content(schema = @Schema(implementation = AccountDTO.class))),
            @ApiResponse(responseCode = "400", description = "입력 데이터 오류 (필수 필드 누락 등)")
    })
    @PostMapping
    public ResponseEntity<AccountDTO> createAccount(@RequestBody CreateAccountRequestDTO requestDTO) {
        Account account = accountService.createAccount(requestDTO);
        return ResponseEntity.ok(account.toDTO());
    }

    @Operation(summary = "계좌 해지 API", description = "특정 계좌를 해지(삭제 상태로 변경)합니다. 잔액이 남아있는 경우 해지할 수 없습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "계좌 해지 성공"),
            @ApiResponse(responseCode = "400", description = "계좌 해지 실패 (잔액 존재 또는 비활성 계좌)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당 계좌를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping(value = "/{accountId}")
    public ResponseEntity deleteAccount(@PathVariable Long accountId) throws Throwable {
        accountService.deleteAccount(accountId);
        return ResponseEntity.ok(null);
    }

    @Operation(summary = "입금 API", description = "특정 계좌에 금액을 입금합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "입금 성공", content = @Content(schema = @Schema(implementation = AccountDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 입금액 (0원 이하)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당 계좌를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/{accountId}/deposit")
    public ResponseEntity<AccountDTO> deposit(@PathVariable Long accountId, @RequestBody DepositRequestDTO requestDTO) throws Throwable {
        Long amount = requestDTO.getAmount();
        Account account = accountService.deposit(accountId, amount);
        return ResponseEntity.ok(account.toDTO());
    }

    @Operation(summary = "출금 API", description = "특정 계좌에서 금액을 출금합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "출금 성공", content = @Content(schema = @Schema(implementation = AccountDTO.class))),
            @ApiResponse(responseCode = "400", description = "출금 실패 (잔액 부족 또는 일일 한도 초과)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당 계좌를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/{accountId}/withdraw")
    public ResponseEntity<AccountDTO> withdraw(@PathVariable Long accountId, @RequestBody WithdrawRequestDTO requestDTO) throws Throwable {
        Account account = accountService.findById(accountId);
        Long amount = requestDTO.getAmount();
        account = accountService.withdraw(account.getId(), amount);
        return ResponseEntity.ok(account.toDTO());
    }

    @Operation(summary = "계좌 이체 API", description = "계좌에서 다른 계좌로 금액을 이체합니다. 이체 시 1%의 수수료가 발생합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이체 성공", content = @Content(schema = @Schema(implementation = AccountDTO.class))),
            @ApiResponse(responseCode = "400", description = "이체 실패 (잔액 부족, 한도 초과, 수취인 계좌 오류 등)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "송금인 계좌를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/{accountId}/transfer")
    public ResponseEntity<AccountDTO> transfer(@PathVariable Long accountId, @RequestBody TransferRequestDTO requestDTO) throws Throwable {
        Account account = accountService.transfer(accountId, requestDTO);
        return ResponseEntity.ok(account.toDTO());
    }

    @Operation(summary = "거래 내역 조회", description = "특정 계좌의 입출금 및 이체 내역을 페이징하여 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 계좌를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(value = "/{accountId}/transaction")
    public ResponseEntity<List<TransactionDTO>> transactionHistory(@PathVariable Long accountId, Pageable pageable) {
        Page<Transaction> page = transactionService.findBy(accountId, pageable);

        List<TransactionDTO> dtos = page.getContent()
                .stream()
                .map(Transaction::toDTO)
                .toList();

        return ResponseEntity.ok(dtos);
    }
}
