package com.db.awmd.challenge.web;

import com.db.awmd.challenge.domain.*;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientBalanceException;
import com.db.awmd.challenge.exception.InvalidAccountException;
import com.db.awmd.challenge.exception.InvalidAmountException;
import com.db.awmd.challenge.service.AccountsService;
//import com.db.awmd.challenge.service.TransferFundsService;
import com.db.awmd.challenge.util.Constants;

import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

  private final AccountsService accountsService;

  @Autowired
  public AccountsController(AccountsService accountsService) {
    this.accountsService = accountsService;
  }
  
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
    log.info("Creating account {}", account);
    try {
    	this.accountsService.createAccount(account);
    } catch (DuplicateAccountIdException daie) {
      return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>(HttpStatus.CREATED);
  }
  
  @PostMapping(path="/transfer",consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> transferMoney(@RequestBody Transfer transfer) {
    log.info("Transferring money: {}", transfer);
    JSONObject response = null;
    
    try {
    	response = this.accountsService.transferFunds(transfer);
    	if(null != response){
    		log.info("Funds has been transferred successfully.");
    	    return new ResponseEntity<>(response, HttpStatus.OK);
    	}
    	
    	log.error("Funds transfer failed.");
        return new ResponseEntity<>(Constants.ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR);

    }catch (InvalidAccountException | InvalidAmountException | InsufficientBalanceException ex) {
    	JSONObject errorResponse = new JSONObject();
    	errorResponse.put(Constants.ERROR, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    } 
  }

  @GetMapping(path = "/{accountId}")
  public Account getAccount(@PathVariable String accountId) {
    log.info("Retrieving account for id {}", accountId);
    return this.accountsService.getAccount(accountId);
  }

}
