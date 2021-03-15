package com.techelevator.tenmo.dao;

import java.util.List;

import com.techelevator.tenmo.model.Account;

public interface AccountDao {
	
	Account getAccountInfoByUsername(String name);
	
	List<Account> getAllAccounts();

}
