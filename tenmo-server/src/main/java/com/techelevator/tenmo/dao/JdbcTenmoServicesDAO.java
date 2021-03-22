package com.techelevator.tenmo.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import com.techelevator.tenmo.exception.TransferIdNotFoundException;
import com.techelevator.tenmo.exception.UserIdNotFoundException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;

@Component
public class JdbcTenmoServicesDAO implements TenmoServicesDAO {

	private JdbcTemplate jdbcTemplate;

	public JdbcTenmoServicesDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	// GETS CURRENT_USERS BALANCE BY USER ID
	@Override
	public BigDecimal getUserCurrentBalanceByID(int userId) {

		String sql = "SELECT balance FROM accounts WHERE user_id = ?";

		BigDecimal result = jdbcTemplate.queryForObject(sql, BigDecimal.class, userId);

		return result;

	}

	// GETS A LIST OF ALL USERS
	@Override
	public List<User> getAllUsers() {
		List<User> users = new ArrayList<>();

		// WE EXCLUDED THE PASSWORD HASH AS THE USER SHOULD NEVER HAVE ACCESS TO THIS
		// PRIVALIDGED COMPANY INFORMAION!!!!!!!!!!
		// ALL PASSWORD HASHES APPEAR AS NULL!!!!

		String sql = "SELECT user_id, username FROM users";

		SqlRowSet results = jdbcTemplate.queryForRowSet(sql);

		while (results.next()) {
			User user = mapRowToUser(results);
			users.add(user);
		}
		return users;
	}

	// CREATE A TRANSFER
	public void transfer(int fromUser, int toUser, BigDecimal amountTEBucks) throws UserIdNotFoundException {

		try {
			String sql = "INSERT INTO transfers(transfer_type_id, transfer_status_id, account_from, account_to, amount) \r\n"
					+ "VALUES(2, 2, ?, ?, ?)";
			jdbcTemplate.update(sql, fromUser, toUser, amountTEBucks);

		} catch (DataAccessException e) {
		}
	}

	// UPDATE BALANCE RECIPIENT OF FUNDS
	@Override
	public void UpdateToUserBalance(int toUser, BigDecimal amountTEBucks) throws UserIdNotFoundException {

		String sqlToUser = "UPDATE accounts SET balance = ? WHERE user_id = ?";
		jdbcTemplate.update(sqlToUser, amountTEBucks, toUser);

	}

	// UPDATE BALANCE SENDER OF FUNDS
	@Override
	public void UpdateFromUserBalance(int fromUser, BigDecimal amountTEBucks) throws UserIdNotFoundException {

		String sqlFromUser = "UPDATE accounts SET balance = ? WHERE user_id = ?";
		jdbcTemplate.update(sqlFromUser, amountTEBucks, fromUser);

	}

	// CREATES A LIST OF ALL TRANSFERS
	@Override
	public List<Transfer> getAllTransfers() {

		List<Transfer> allTransfers = new ArrayList<>();

		String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount FROM transfers";
		SqlRowSet results = jdbcTemplate.queryForRowSet(sql);

		while (results.next()) {
			Transfer transferObject = mapRowToTransfer(results);
			allTransfers.add(transferObject);

		}
		return allTransfers;
	}

	// GET USER_ID FROM ACCOUNT_ID
	@Override
	public int getUserIdFromAccountId(int accountId) {

		// should we make an account not found exception?

		int userId = 0;

		String sql = "SELECT user_id FROM accounts WHERE account_id = ?";
		userId = jdbcTemplate.queryForObject(sql, int.class, accountId);

		return userId;
	}

	// GET USERNAME FROM USER_ID
	@Override
	public String getUsernameFromUserId(int userId) throws UserIdNotFoundException {

		String username = "";

		String sql = "SELECT username FROM users WHERE user_id = ?";
		username = jdbcTemplate.queryForObject(sql, String.class, userId);

		return username;
	}

	// GET TRASNFER DETAILS BY TRANSFER_ID
	@Override
	public Transfer getTransferByID(long transferID) throws TransferIdNotFoundException {

		Transfer transferObject = new Transfer();

		String sql = "SELECT * FROM transfers WHERE transfer_id = ? ";
		SqlRowSet results = jdbcTemplate.queryForRowSet(sql, transferID);

		if (results.next()) {
			transferObject = mapRowToTransfer(results);
		}

		return transferObject;

	}

	// GET TRANSFER_TYPE_DESCRIPTION FROM TRANSFER_TYPE_ID
	@Override
	public String getTransferTypeDescFromTransferTypeId(int transferTypeId) {

		String TransfTypeDesc = "";

		String sql = "SELECT transfer_type_desc FROM transfer_types WHERE transfer_type_id = ?";
		TransfTypeDesc = jdbcTemplate.queryForObject(sql, String.class, transferTypeId);

		return TransfTypeDesc;

	}

	// GET TRANSFER_STATUS_DESCRIPTION FROM TRANSFER_STATUS_ID
	@Override
	public String getTransferStatusDescFromTransferStatusId(int transferStatusId) {

		String TransfStatusDesc = "";

		String sql = "SELECT transfer_status_desc FROM transfer_statuses WHERE transfer_status_id = ?";

		TransfStatusDesc = jdbcTemplate.queryForObject(sql, String.class, transferStatusId);

		return TransfStatusDesc;

	}

	// CREATE A TRANSFER - TRASNFER TYPE REQUEST
	@Override
	public void requestTransfer(int fromUser, int toUser, BigDecimal amountTEBucks) throws UserIdNotFoundException {

		try {
			String sql = "INSERT INTO transfers(transfer_type_id, transfer_status_id, account_from, account_to, amount) \r\n"
					+ "VALUES(1, 1, ?, ?, ?)";
			jdbcTemplate.update(sql, fromUser, toUser, amountTEBucks);

		} catch (DataAccessException e) {

		}

	}

	// GET ACCOUNT_ID FROM USER_ID
	@Override
	public int getAccountIdFromUserId(int userId) throws UserIdNotFoundException {

		int accountId = 0;

		String sql = "SELECT account_id FROM accounts WHERE user_id = ?";
		accountId = jdbcTemplate.queryForObject(sql, int.class, userId);

		return accountId;

	}

	// UPDATE TRANSFER STATUS TO APPROVED
	@Override
	public void approvedTransfer(int transferID) {

		String sqlFromUser = "UPDATE transfers SET transfer_status_id = 2 WHERE transfer_id = ?";
		jdbcTemplate.update(sqlFromUser, transferID);

	}

	// UPDATE TRANSFER STATUS TO REJECTED
	@Override
	public void rejectedTransfer(int transferID) {

		String sqlFromUser = "UPDATE transfers SET transfer_status_id = 3 WHERE transfer_id = ?";
		jdbcTemplate.update(sqlFromUser, transferID);

	}

	// GET A LIST OF ALL TRANSFERS - TRANSFER STATUS PENDING
	@Override
	public List<Transfer> getAllPendingTransfers() {
		List<Transfer> allPendingTransfers = new ArrayList<>();

		String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount FROM transfers WHERE transfer_status_id = 1";
		SqlRowSet results = jdbcTemplate.queryForRowSet(sql);

		while (results.next()) {
			Transfer transferObject = mapRowToTransfer(results);
			allPendingTransfers.add(transferObject);

		}
		return allPendingTransfers;
	}

	// GET TRANSFER AMOUNT BY TRANSFER_ID
	@Override
	public BigDecimal getTransferAmountByTransferID(int transferID) {

		String sql = "SELECT amount FROM transfers WHERE transfer_id = ?";

		BigDecimal result = jdbcTemplate.queryForObject(sql, BigDecimal.class, transferID);

		return result;
	}

	// GET USER_ID FROM TRANSFER_ID
	@Override
	public int getUserIdFromTransferId(int transferID) {
		int userId = 0;

		String sql = "SELECT user_id from accounts INNER JOIN transfers ON"
				+ " accounts.account_id = transfers.account_from WHERE transfer_id = ?";

		userId = jdbcTemplate.queryForObject(sql, int.class, transferID);

		return userId;
	}

	// MAP ROW TO - METHODS
	private User mapRowToUser(SqlRowSet rs) {
		User user = new User();
		user.setId(rs.getLong("user_id"));
		user.setUsername(rs.getString("username"));
		// user.setPassword(rs.getString("password_hash")); -EXCLUDED FOR SECURITY
		// REASONS-
		user.setActivated(true);
		user.setAuthorities("USER");
		return user;
	}

	private Transfer mapRowToTransfer(SqlRowSet ts) {
		Transfer transfer = new Transfer();
		transfer.setTransfer_id(ts.getInt("transfer_id"));
		transfer.setTransfer_type_id(ts.getInt("transfer_type_id"));
		transfer.setTransfer_status_id(ts.getInt("transfer_status_id"));
		transfer.setAccount_from(ts.getInt("account_from"));
		transfer.setAccount_to(ts.getInt("account_to"));
		transfer.setAmount(ts.getBigDecimal("amount"));
		return transfer;
	}

}
