package com.techelevator.tenmo.models;

import java.math.BigDecimal;

public class Account {

	//	PRIVATE MEMBERS/ INSTANCE VARIABLES
	private int account_id;
	private int user_id;
	private BigDecimal balance;

	//	GETTERS AND SETTERS
	public int getAccount_id() {
		return account_id;
	}

	public void setAccount_id(int account_id) {
		this.account_id = account_id;
	}

	public int getUser_id() {
		return user_id;
	}

	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	//	TO STRING METHOD
	@Override
	public String toString() {
		return "Accounts [account_id=" + account_id + ", user_id=" + user_id + ", balance=" + balance + "]";
	}
}
