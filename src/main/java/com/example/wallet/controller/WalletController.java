package com.example.wallet.controller;

import com.example.wallet.domain.entity.Wallet;
import com.example.wallet.security.aop.WalletOwnership;
import com.example.wallet.dto.WalletDto;
import com.example.wallet.dto.WalletUpdateDto;
import com.example.wallet.service.spec.ErrorResponseServiceSpec;
import com.example.wallet.service.spec.WalletServiceSpec;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/wallet")
public class WalletController {

	private final WalletServiceSpec walletService;
	private final ErrorResponseServiceSpec errorResponseService;

	private static final String TEMPLATE = "http://127.0.0.1:8080/api/wallet";


	@Autowired
	public WalletController(WalletServiceSpec walletService,
							ErrorResponseServiceSpec errorResponseService) {
		this.walletService = walletService;
		this.errorResponseService = errorResponseService;
	}

	@GetMapping(value = "", produces = "application/json")
	public CollectionModel<Wallet> getAll() {
		return CollectionModel.of(
				walletService.getAll()
						.stream().map(
								w -> w.add(
										Link.of(TEMPLATE + "/" + w.getId())
												.withSelfRel()
								)
						).toList()
		).add(
				Link.of(TEMPLATE)
						.withSelfRel()
		);
	}


	@GetMapping(value = "/{id}", produces = "application/json")
	@WalletOwnership
	public EntityModel<Wallet> get(@PathVariable Long id) {
		Wallet wallet = walletService.get(id);
		return EntityModel.of(wallet).add(
				Link.of(TEMPLATE + "/" + id)
						.withSelfRel()
		);
	}

	@PostMapping(value = "",
			consumes = "application/json",
			produces = "application/json")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<Object> create(
			@RequestBody @Valid WalletDto creationDto, BindingResult result) {
		if (result.hasErrors()) {
			return errorResponseService.returnValidationError(result);
		}
		var wallet = walletService.create(creationDto);
		return ResponseEntity.ok(wallet.add(
				Link.of(TEMPLATE + "/" + wallet.getId())
						.withSelfRel()
		));
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
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public ResponseEntity<Object> delete(@PathVariable Long id) {
		return walletService.delete(id);
	}

}
