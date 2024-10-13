package com.example.wallet.wallet;

import com.example.wallet.account.Account;
import com.example.wallet.account.AccountService;
import com.example.wallet.exception.ResourceNotFoundException;
import com.example.wallet.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WalletService {

	private final WalletRepository repo;
	private final WalletBuilderFactory walletBuilderFactory;
	private final AccountService accountService;

	@Autowired
	public WalletService(WalletRepository repo,
						 WalletBuilderFactory walletBuilderFactory,
						 AccountService accountService) {
		this.repo = repo;
		this.walletBuilderFactory = walletBuilderFactory;
		this.accountService = accountService;
	}

	public Wallet get(Long id) {
		Wallet wallet = repo.findById(id).orElseThrow(
				() -> new ResourceNotFoundException("This wallet does not exists"));

		if (
				(!wallet.getAccount().isActive()) ||
				(!wallet.isActive())
		)
			throw new ResourceNotFoundException("This wallet does not exists");

		if (!wallet.getAccount().getNationalId()
				.equals(getNationalIdFromSecurityContext()))
			throw new AccessDeniedException("This account is not accessible for you");

		return wallet;
	}
	
	public List<Wallet> getAll() {
		Account account = getAccountFromSecurityContext();
		return repo.findByAccount_NationalId(account.getNationalId())
				.stream().filter(wallet -> wallet.isActive()).toList();
	}
	
	public Wallet create(WalletDto walletDto) {
		// get account from context
		Account account = getAccountFromSecurityContext();
		// build new wallet for account
		Wallet wallet = walletBuilderFactory.getNewWallet(walletDto, account.getNationalId());
		wallet.setAccount(account);
		wallet.setActive(true);

		try {
			wallet = repo.save(wallet);
		} catch (Exception ex) {
			throw new ValidationException("Something went wrong!");
		}

		return wallet;
	}

	public Wallet update(WalletUpdateDto walletUpdateDto) {
		Wallet wallet = validateWalletOwnership(walletUpdateDto.getId());
		wallet.setTitle(walletUpdateDto.getTitle());
		return repo.save(wallet);
	}

	public ResponseEntity<Object> delete(Long id) {
		if (id == null)
			return ResponseEntity.badRequest().body("Bad request");

		Wallet wallet = validateWalletOwnership(id);
		try{
			wallet.setActive(false);
			repo.save(wallet);
			return ResponseEntity.ok().body("Wallet deleted successfully");
		} catch (Exception ex) {
			return ResponseEntity.badRequest().body("Bad request");
		}
	}

	private Account getAccountFromSecurityContext() {
		Authentication	authentication =
				SecurityContextHolder.getContext().getAuthentication();
		Account account = accountService.findByNationalId(
				authentication.getName());
		if (!account.isActive())
			throw new ResourceNotFoundException("This wallet does not exists");

		return account;
	}

	private String getNationalIdFromSecurityContext() {
		Authentication authentication =
				SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();
	}

	private Wallet validateWalletOwnership(long walletId) {
		// get wallet
		Wallet wallet = repo.findById(walletId)
				.orElseThrow(
						() -> new ResourceNotFoundException("Wallet not found"));

		if (
				(!wallet.getAccount().isActive()) ||
				(!wallet.isActive())
		)
			throw new ResourceNotFoundException("This wallet does not exists");

		// check ownership
		if ((wallet.getAccount() == null) || (wallet.getAccount().getNationalId() == null) ||
				(!wallet.getAccount().getNationalId().equals(getNationalIdFromSecurityContext())))
			throw new AccessDeniedException("Your access denied to this wallet");

		return wallet;
	}
}
