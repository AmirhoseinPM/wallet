package com.example.wallet.service;

import com.example.wallet.entity.Account;
import com.example.wallet.entity.AccountDetails;
import com.example.wallet.exception.ResourceNotFoundException;
import com.example.wallet.exception.ValidationException;
import com.example.wallet.entity.Wallet;
import com.example.wallet.repository.WalletRepository;
import com.example.wallet.dto.WalletDto;
import com.example.wallet.dto.WalletUpdateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WalletService {

	private final WalletRepository walletRepository;
	private final WalletGenerator walletBuilderFactory;
	private final UserDetailsService userDetailsService;

	@Autowired
	public WalletService(WalletRepository walletRepository,
						 WalletGenerator walletBuilderFactory,
						 UserDetailsService userDetailsService) {
		this.walletRepository = walletRepository;
		this.walletBuilderFactory = walletBuilderFactory;
		this.userDetailsService = userDetailsService;
	}

	public Wallet get(Long id) {
		try {
			return walletRepository.findById(id).orElseThrow(
					() -> new ResourceNotFoundException("This wallet does not exists"));
		} catch (Exception e) {
			throw new ResourceNotFoundException("Wallet not found");
		}
	}
	
	public List<Wallet> getAll() {
		return walletRepository.findByAccount_NationalId(
				SecurityContextHolder.getContext().getAuthentication().getName())
				.stream().filter(Wallet::isActive).toList();
	}
	
	public Wallet create(WalletDto walletDto) {
		// get account from context
		Account account = getAccountFromSecurityContext();
		// build new wallet for account
		Wallet wallet = walletBuilderFactory.getNewWallet(walletDto, account.getNationalId());
		wallet.setAccount(account);
		wallet.setActive(true);

		try {
			wallet = walletRepository.save(wallet);
		} catch (Exception ex) {
			throw new ValidationException("Something went wrong!");
		}

		return wallet;
	}

	public Wallet update(WalletUpdateDto walletUpdateDto) {
		// get wallet
		Wallet wallet = walletRepository.findById(walletUpdateDto.getId())
						.orElseThrow(
							() -> new ResourceNotFoundException("Wallet not found"));
		wallet.setTitle(walletUpdateDto.getTitle());
		return walletRepository.save(wallet);
	}

	public ResponseEntity<Object> delete(Long id) {
		if (id == null)
			return ResponseEntity.badRequest().body("Bad request");

		// get wallet
		Wallet wallet = walletRepository.findById(id)
				.orElseThrow(
						() -> new ResourceNotFoundException("Wallet not found"));
		try{
			wallet.setActive(false);
			walletRepository.save(wallet);
			return ResponseEntity.ok().body("Wallet deleted successfully");
		} catch (Exception ex) {
			return ResponseEntity.badRequest().body("Bad request");
		}
	}

	private Account getAccountFromSecurityContext() {
		Authentication	authentication =
				SecurityContextHolder.getContext().getAuthentication();

		UserDetails userDetails = userDetailsService
				.loadUserByUsername(authentication.getName());
		try {
			return ((AccountDetails) userDetails).getAccount();
		} catch (Exception e) {
			throw new ValidationException(e.getMessage());
		}
	}

}
