package com.example.wallet.wallet;

import com.example.wallet.exception.ErrorResponseService;
import com.example.wallet.security.WalletOwnership;
import com.example.wallet.wallet.helper.WalletDto;
import com.example.wallet.wallet.helper.WalletUpdateDto;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

	private final WalletService walletService;
	private final ErrorResponseService errorResponseService;
	private final Logger log = LoggerFactory.getLogger(WalletController.class);

	@Autowired
	public WalletController(WalletService walletService,
							ErrorResponseService errorResponseService) {
		this.walletService = walletService;
		this.errorResponseService = errorResponseService;
	}

	@GetMapping(value = "", produces = "application/json")
	public List<Wallet> getAll() {
		return walletService.getAll();
	}


	@PostMapping(value = "", consumes = "application/json", produces = "application/json")
	public ResponseEntity<Object> create(
			@RequestBody @Valid WalletDto creationDto, BindingResult result) {
		if (result.hasErrors()) {
			return errorResponseService.returnValidationError(result);
		}
		return ResponseEntity.ok(
				walletService.create(creationDto));
	}

	@GetMapping(value = "/{id}", produces = "application/json")
	@WalletOwnership
	public Wallet get(@PathVariable Long id) {
		log.info("start response to: " + id);
		return walletService.get(id);
	}


	@PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
	@WalletOwnership
	public ResponseEntity<Object> update(
			@PathVariable Long id,
			@RequestBody @Valid WalletUpdateDto updateDto,
			BindingResult result) {
		// set id to dto from path
		updateDto.setId(id);

		if (result.hasErrors()) {
			return errorResponseService.returnValidationError(result);
		}
		return ResponseEntity.ok(
				walletService.update(updateDto));
	}

	@DeleteMapping("/{id}")
	@WalletOwnership
	public ResponseEntity<Object> delete(@PathVariable Long id) {
		return walletService.delete(id);
	}

}
