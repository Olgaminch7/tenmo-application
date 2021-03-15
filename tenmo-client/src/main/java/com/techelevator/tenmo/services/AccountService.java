package com.techelevator.tenmo.services;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.MediaType;
import com.techelevator.tenmo.models.Account;
import com.techelevator.tenmo.models.AuthenticatedUser;
import com.techelevator.tenmo.models.Transaction;

public class AccountService {
	
	private String baseUrl;
	private AuthenticatedUser currentUser;
	private RestTemplate restTemplate = new RestTemplate();
	
	public AccountService(String baseUrl, AuthenticatedUser currentUser) {
		this.currentUser = currentUser;
		this.baseUrl = baseUrl;
	}
	
	public Account getAccountInfoByUsername() throws AccountServiceException {
		Account account = null;
		try {
			account = restTemplate.exchange(baseUrl + "/users/account", HttpMethod.GET, headerEntity(), Account.class).getBody();
		} catch (RestClientResponseException ex) {
			throw new AccountServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}
		return account;
	}
	
	public List<Account> getAllAccounts() throws AccountServiceException {
		Account[] accounts = null;
		try {
			accounts = restTemplate.exchange(baseUrl + "users/accounts", HttpMethod.GET, headerEntity(), Account[].class).getBody();
		} catch (RestClientResponseException ex) {
			throw new AccountServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}
		return Arrays.asList(accounts);		
	}
	
	public Transaction sendMoney(Transaction newTransaction) throws AccountServiceException {
		Transaction transaction = null;
		try {
			transaction = restTemplate.exchange(baseUrl + "users/transactions", HttpMethod.POST, makeEntity(newTransaction), Transaction.class)
									  .getBody();
		} catch (RestClientResponseException ex) {
			throw new AccountServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}
		return transaction;
	}
	
	public Transaction createRequestForMoney(Transaction newTransaction) throws AccountServiceException {
		Transaction transaction = null;
		try {
			transaction = restTemplate.exchange(baseUrl + "users/transactions", HttpMethod.POST, makeEntity(newTransaction), Transaction.class)
									  .getBody();
		} catch (RestClientResponseException ex) {
			throw new AccountServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}
		return transaction;
	}
	
	public Transaction updateTransaction(Transaction newTransaction) throws AccountServiceException {
		Transaction transaction = null;
		try {
			transaction = restTemplate.exchange(baseUrl + "users/transactions", HttpMethod.PUT, makeEntity(newTransaction), Transaction.class)
									  .getBody();
		} catch (RestClientResponseException ex) {
			throw new AccountServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}
		return transaction;
	}
	
	public List<Transaction> getAllTransactionsByAccountId(long accountId, long statusId) throws AccountServiceException {
		Transaction[] transactions = null;
		try {
			transactions = restTemplate.exchange(baseUrl + "users/account/" + accountId + "/transactions" + "?statusId=" + statusId, HttpMethod.GET,
									    headerEntity(), Transaction[].class).getBody();
		} catch (RestClientResponseException ex) {
			throw new AccountServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}
		return Arrays.asList(transactions);
	}
	
	private HttpEntity<Transaction> makeEntity(Transaction newTransaction) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(currentUser.getToken());
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity <Transaction> entity = new HttpEntity<Transaction>(newTransaction, headers);
		return entity;
	}
	
	private HttpEntity headerEntity() {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(currentUser.getToken());
		HttpEntity entity = new HttpEntity(headers);
		return entity;
	}
}
