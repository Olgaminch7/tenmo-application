package com.techelevator.tenmo.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.techelevator.tenmo.model.Transaction;

@Component
public class JdbcTransactionDao implements TransactionDAO {
	
	private JdbcTemplate jdbcTemplate;
	
	public JdbcTransactionDao (JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	@Transactional
	public Transaction addTransfer(Transaction transaction, String name) {
		String sql = "INSERT INTO transfers (transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount) VALUES (DEFAULT, ?, ?, ?, ?, ?) RETURNING transfer_id";
		Long transferId = jdbcTemplate.queryForObject(sql, Long.class, transaction.getTypeId(), transaction.getStatusId(),
				transaction.getSenderAccountId(), transaction.getRecieverAccountId(), transaction.getTransferAmount());
		transaction.setId(transferId);
		depositToBalance(transaction.getRecieverAccountId(), transaction.getTransferAmount());
		withdrawFromBalance(transaction.getSenderAccountId(), transaction.getTransferAmount());
		return transaction;
	}
	
	@Override
	@Transactional
	public Transaction addPendingTransfer(Transaction transaction, String name) {
		String sql = "INSERT INTO transfers (transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount) VALUES (DEFAULT, ?, ?, ?, ?, ?) RETURNING transfer_id";
		Long transferId = jdbcTemplate.queryForObject(sql, Long.class, transaction.getTypeId(), transaction.getStatusId(),
				transaction.getSenderAccountId(), transaction.getRecieverAccountId(), transaction.getTransferAmount());
		transaction.setId(transferId);
		return transaction;
	}
	
	@Override
	public void updateTransactionIfApproved(Transaction transaction, String name) {
		jdbcTemplate.update("UPDATE transfers SET transfer_status_id = ? WHERE transfer_id = ?", transaction.getStatusId(), transaction.getId());
		depositToBalance(transaction.getSenderAccountId(), transaction.getTransferAmount());
		withdrawFromBalance(transaction.getRecieverAccountId(), transaction.getTransferAmount());
	}
	
	@Override
	public void updateTransactionIfRejected(Transaction transaction, String name) {
		jdbcTemplate.update("UPDATE transfers SET transfer_status_id = ? WHERE transfer_id = ?", transaction.getStatusId(), transaction.getId());
	}

	private void depositToBalance(long receiverAccountId, double amount) {
		String sql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
		jdbcTemplate.update(sql, amount, receiverAccountId);
	}

	private void withdrawFromBalance(long currentAccountId, double amount) {
		String sql = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
		jdbcTemplate.update(sql, amount, currentAccountId);
	}

	@Override
	public List<Transaction> getAllTransactions(String name) {
		List<Transaction> transactions = new ArrayList<Transaction>();
		String sql = "SELECT transfer_id, account_from, username as sender_name, account_to, transfers.transfer_status_id as status_id, " +
				"transfers.transfer_type_id as type_id, amount " +
				"FROM transfers " + 
				"JOIN transfer_statuses ON transfer_statuses.transfer_status_id = transfers.transfer_status_id " + 
				"JOIN transfer_types ON transfer_types.transfer_type_id = transfers.transfer_type_id " + 
				"JOIN accounts ON accounts.account_id = transfers.account_from " + 
				"JOIN users ON users.user_id = accounts.user_id";
		SqlRowSet rows = jdbcTemplate.queryForRowSet(sql);
		while(rows.next()) {
			transactions.add(mapRowToTransaction(rows));
		}
		return transactions;
	}
	
	@Override
	public List<Transaction> getApprovedTransactionsByAccountId(String name, long accountId) {
		List<Transaction> transactions = new ArrayList<Transaction>();
		String sql = "SELECT transfer_id, account_from, username as sender_name, account_to, transfers.transfer_status_id as status_id, transfers.transfer_type_id as type_id, amount " + 
				"FROM transfers JOIN transfer_statuses ON transfer_statuses.transfer_status_id = transfers.transfer_status_id " +
				"JOIN transfer_types ON transfer_types.transfer_type_id = transfers.transfer_type_id JOIN accounts ON accounts.account_id = transfers.account_from " +
				"JOIN users ON users.user_id = accounts.user_id " +
				"WHERE transfers.transfer_status_id = 2 AND (account_from = ? OR account_to = ?)";
		SqlRowSet rows = jdbcTemplate.queryForRowSet(sql, accountId, accountId);
		while(rows.next()) {
			transactions.add(mapRowToTransaction(rows));
		}
		return transactions;
	}
	
	@Override
	public List<Transaction> getPendingTransactionsByAccountId(String name, long accountId) {
		List<Transaction> transactions = new ArrayList<Transaction>();
		String sql = "SELECT transfer_id, account_from, username as sender_name, account_to, transfers.transfer_status_id as status_id, transfers.transfer_type_id as type_id, amount " + 
				"FROM transfers JOIN transfer_statuses ON transfer_statuses.transfer_status_id = transfers.transfer_status_id " +
				"JOIN transfer_types ON transfer_types.transfer_type_id = transfers.transfer_type_id JOIN accounts ON accounts.account_id = transfers.account_from " +
				"JOIN users ON users.user_id = accounts.user_id " +
				"WHERE transfers.transfer_status_id = 1 AND (account_from = ? OR account_to = ?)";
		SqlRowSet rows = jdbcTemplate.queryForRowSet(sql, accountId, accountId);
		while(rows.next()) {
			transactions.add(mapRowToTransaction(rows));
		}
		return transactions;
	}
	
	private String getReceiverName(long transerId) {
		String sql = "SELECT username as reciever_name " + 
				"FROM transfers " + 
				"JOIN transfer_statuses ON transfer_statuses.transfer_status_id = transfers.transfer_status_id " + 
				"JOIN transfer_types ON transfer_types.transfer_type_id = transfers.transfer_type_id " + 
				"JOIN accounts ON accounts.account_id = transfers.account_to " + 
				"JOIN users ON users.user_id = accounts.user_id " +
				"WHERE transfer_id = ?";
		String receiverName = jdbcTemplate.queryForObject(sql, String.class, transerId);
		return receiverName;
	}
	

	private Transaction mapRowToTransaction(SqlRowSet row) {
		Transaction transaction = new Transaction();
		transaction.setId(row.getLong("transfer_id"));
		String nameString = getReceiverName(row.getLong("transfer_id"));
		transaction.setReceiverName(nameString);
		transaction.setSenderName(row.getString("sender_name"));
		transaction.setSenderAccountId(row.getLong("account_from"));
		transaction.setRecieverAccountId(row.getLong("account_to"));
		transaction.setStatusId(row.getInt("status_id"));		
		transaction.setTypeId(row.getInt("type_id"));
		transaction.setTransferAmount(row.getDouble("amount"));
		return transaction;
	}

	

	
	

	
	
}
