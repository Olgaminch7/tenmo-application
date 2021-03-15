package com.techelevator.tenmo.controller;

import java.security.Principal;
import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.TransactionDAO;
import com.techelevator.tenmo.exception.AccountNotFoundException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transaction;

@RestController
@PreAuthorize("isAuthenticated()")
public class AccountController {
	
	private AccountDao accountDao;
	private TransactionDAO transactionDao;
	
	public AccountController(AccountDao accountDao, TransactionDAO transactionDAO) {
		this.accountDao = accountDao;
		this.transactionDao = transactionDAO;
	}
	
	@RequestMapping(path="/users/account", method=RequestMethod.GET) // account or balance for end point
	public Account getBalanceByUsername(Principal principal) {
		String userName = principal.getName();
		return accountDao.getAccountInfoByUsername(userName);
	}
	
	@RequestMapping(path="users/accounts", method=RequestMethod.GET)
	public List<Account> getListOfAccounts() {
		return accountDao.getAllAccounts();
	}
	
	@RequestMapping(path="users/account/{id}/transactions", method=RequestMethod.GET)
	public List<Transaction> getTransactionsByAccountId(Principal principal, @PathVariable("id") long accountId, 
							@RequestParam(required = false, defaultValue = "2") long statusId) throws AccountNotFoundException {
		String userName = principal.getName();
		List<Transaction> transactions = transactionDao.getApprovedTransactionsByAccountId(userName, accountId);
		if (statusId == 1) {
			transactions = transactionDao.getPendingTransactionsByAccountId(userName, accountId);
		} else if (statusId == 2) {
			transactions = transactionDao.getApprovedTransactionsByAccountId(userName, accountId);
		} 		
		return transactions;
	}
	
	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(path="users/transactions", method=RequestMethod.POST)
	public Transaction addTransaction(@Valid @RequestBody Transaction transaction, Principal principal) {
		String userName = principal.getName();
		Transaction addTransaction = null;
		if(transaction.getTypeId() == 1) {
			addTransaction = transactionDao.addPendingTransfer(transaction, userName);
		} else if (transaction.getTypeId() == 2) {
			addTransaction = transactionDao.addTransfer(transaction, userName);
		}
		return addTransaction;
	}
	
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RequestMapping(path="users/transactions", method=RequestMethod.PUT)
	public void updateTransaction(Principal principal, @Valid @RequestBody Transaction transaction) {
		String userName = principal.getName();
		if(transaction.getStatusId() == 2) {
			transactionDao.updateTransactionIfApproved(transaction,userName);
		} else if (transaction.getStatusId() == 3) {
			transactionDao.updateTransactionIfRejected(transaction, userName);
		}
	}
		
	@RequestMapping(path="users/transactions", method=RequestMethod.GET)
	public List<Transaction> getAllTransactions(Principal principal) {
		String userName = principal.getName();
		return transactionDao.getAllTransactions(userName);
	}

}
