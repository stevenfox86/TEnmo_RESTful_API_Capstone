package com.techelevator.tenmo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Transfer ID not found.")
public class TransferIdNotFoundException extends Exception {
	private static final long serialVersionUID = 1L;

	public TransferIdNotFoundException() {
		super("Transfer ID not found.");
	}

}
