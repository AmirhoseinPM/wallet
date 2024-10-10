package com.example.wallet.wallet;

import com.example.wallet.account.Account;
import com.example.wallet.account.AccountService;
import com.example.wallet.exception.ResourceNotFoundException;
import com.example.wallet.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

	private final Logger log = LoggerFactory.getLogger(WalletService.class);

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

		if (!wallet.getAccount().getNationalId()
				.equals(getNationalIdFromSecurityContext()))
			throw new AccessDeniedException("This account is not accessible for you");

		return wallet;
	}
	
	public List<Wallet> getAll() {
		Account account = getAccountFromSecurityContext();
		return repo.findByAccount_NationalId(account.getNationalId());
	}
	
	public Wallet create(WalletDto walletDto) {
		// get account from context
		Account account = getAccountFromSecurityContext();
		// build new wallet for account
		Wallet wallet = walletBuilderFactory.getNewWallet(walletDto, account.getNationalId());
		wallet.setAccount(account);

		try {
			wallet = repo.save(wallet);
		} catch (Exception ex) {
			throw new ValidationException("Sql exception in creating wallet for you");
		}

		return wallet;
	}

	private Account getAccountFromSecurityContext() {
		Authentication	authentication =
					SecurityContextHolder.getContext().getAuthentication();
		return accountService.findByNationalId(
				authentication.getName()
		);
	}

	private String getNationalIdFromSecurityContext() {
		Authentication authentication =
				SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();
	}

	public Wallet update(WalletUpdateDto walletUpdateDto) {
		Wallet wallet = validateWalletOwnership(walletUpdateDto.getId());
		wallet.setTitle(walletUpdateDto.getTitle());
		return repo.save(wallet);
	}

	private Wallet validateWalletOwnership(long walletId) {
		// get wallet
		Wallet wallet = repo.findById(walletId)
				.orElseThrow(
						() -> new ResourceNotFoundException("Wallet not found"));

		// check ownership
		if (!wallet.getAccount().getNationalId().equals(getNationalIdFromSecurityContext()))
			throw new AccessDeniedException("Your access denied to this wallet");

		return wallet;
	}

}
