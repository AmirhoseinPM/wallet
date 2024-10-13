package com.example.wallet.wallet;

import com.example.wallet.exception.ErrorResponseService;
import jakarta.validation.Valid;
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


	@GetMapping(value = "/{id}", produces = "application/json")
	public Wallet get(@PathVariable Long id) {
		return walletService.get(id);
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
	@PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
	public ResponseEntity<Object> update(
			@RequestBody @Valid WalletUpdateDto updateDto,
			@PathVariable long id,
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
	public ResponseEntity<Object> delete(@PathVariable Long id) {
		return walletService.delete(id);
	}

}
