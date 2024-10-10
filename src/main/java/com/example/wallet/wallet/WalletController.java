package com.example.wallet.wallet;

import com.example.wallet.exception.ErrorResponse;
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

	@Autowired
	public WalletController(WalletService walletService) {
		this.walletService = walletService;
	}

	@GetMapping(value = "", produces = "application/json")
	public List<Wallet> get() {
		return walletService.getAll();
	}


	@GetMapping(value = "/{id}", produces = "application/json")
	public Wallet get(@PathVariable Long id) {
		return walletService.get(id);
	}


	@PostMapping(value = "", consumes = "application/json", produces = "application/json")
	public ResponseEntity<Object> create(
			@RequestBody @Valid WalletDto createtioDto, BindingResult result) {
		if (result.hasErrors()) {
			List<String> errors = result.getAllErrors()
					.stream()
					.map(e -> e.getDefaultMessage())
					.toList();
			return ResponseEntity.badRequest().body(
					new ErrorResponse("400", "bad request", errors));
		}
		return ResponseEntity.ok(
				walletService.create(createtioDto));
	}
	@PostMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
	public ResponseEntity<Object> update(
			@RequestBody @Valid WalletUpdateDto updateDto,
			@PathVariable long id,
			BindingResult result) {
		// set id to dto from path
		updateDto.setId(id);

		if (result.hasErrors()) {
			List<String> errors = result.getAllErrors()
					.stream()
					.map(e -> e.getDefaultMessage())
					.toList();
			return ResponseEntity.badRequest().body(
					new ErrorResponse("400", "bad request", errors));
		}
		return ResponseEntity.ok(
				walletService.update(updateDto));
	}
//
//	@DeleteMapping(value = "/{id}/")
//	public void delete(@PathVariable Long id) {
//		walletService.deleteById(id);
//	}
	
}
