package com.techelevator.tenmo.dao;

import java.math.BigDecimal;
import java.util.List;

import com.techelevator.tenmo.exception.TransferIdNotFoundException;
import com.techelevator.tenmo.exception.UserIdNotFoundException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;

public interface TenmoServicesDAO {

	//	all master methods to be used by client to get resources

	BigDecimal getUserCurrentBalanceByID(int userID);

	List<User> getAllUsers();

	void UpdateToUserBalance(int toUser, BigDecimal amountTEBucks) throws UserIdNotFoundException;

	void UpdateFromUserBalance(int fromUser, BigDecimal amountTEBucks) throws UserIdNotFoundException;

	void transfer(int fromUser, int toUser, BigDecimal amountTEBucks) throws UserIdNotFoundException;

	Transfer getTransferByID(long transferID) throws TransferIdNotFoundException;

	List<Transfer> getAllTransfers();

	int getAccountIdFromUserId(int userId) throws UserIdNotFoundException;

	String getUsernameFromUserId(int userId) throws UserIdNotFoundException;

	int getUserIdFromAccountId(int accountId);

	String getTransferTypeDescFromTransferTypeId(int transferTypeId);

	String getTransferStatusDescFromTransferStatusId(int transferStatusId);
	
	void requestTransfer(int fromUser, int toUser, BigDecimal amountTEBucks) throws UserIdNotFoundException;
	
	void approvedTransfer(int transferID);
	
	void rejectedTransfer(int transferID);
	
	List<Transfer> getAllPendingTransfers();
	
	BigDecimal getTransferAmountByTransferID(int transferID);
	
	int getUserIdFromTransferId(int transferID);
}
