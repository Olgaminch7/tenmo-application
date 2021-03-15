package com.techelevator.tenmo.dao;

import java.util.List;

import com.techelevator.tenmo.model.Transaction;

public interface TransactionDAO {
	
	Transaction addTransfer(Transaction transaction, String name);
	
	Transaction addPendingTransfer(Transaction transaction, String name);
	
	void updateTransactionIfApproved(Transaction transaction, String name);
	
	public void updateTransactionIfRejected(Transaction transaction, String name);
	
	List<Transaction> getAllTransactions(String name);
	
	List<Transaction> getApprovedTransactionsByAccountId(String name, long accountId);
	
	List<Transaction> getPendingTransactionsByAccountId(String name, long accountId);
	
}
