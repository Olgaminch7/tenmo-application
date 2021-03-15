package com.techelevator.tenmo.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import com.techelevator.tenmo.model.Account;

@Component
public class JdbcAccountDao implements AccountDao {
	
	private JdbcTemplate jdbcTemplate;
	
	public JdbcAccountDao(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Account getAccountInfoByUsername(String name) {
		String sql = "SELECT account_id, balance, username FROM accounts " + 
				"JOIN users ON users.user_id = accounts.user_id " + 
				"WHERE username = ?";
		SqlRowSet row = jdbcTemplate.queryForRowSet(sql, name);
		row.next();
		Account account = mapRowToAccount(row);
		return account;
	}
	
	private Account mapRowToAccount(SqlRowSet row) {
		Account account = new Account();
		
		account.setId(row.getLong("account_id"));
		account.setAccountHolderName(row.getString("username"));
		account.setBalance(row.getDouble("balance"));
		return account;
	}

	@Override
	public List<Account> getAllAccounts() {
		List<Account> accounts = new ArrayList<Account>();
		
		String sql = "SELECT account_id, balance, username FROM accounts JOIN users ON users.user_id = accounts.user_id";
		SqlRowSet rows = jdbcTemplate.queryForRowSet(sql);
		
		while (rows.next()) {
			accounts.add(mapRowToAccount(rows));
		}	
		return accounts;
	}
	
	

}
