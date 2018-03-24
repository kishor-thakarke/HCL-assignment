package com.db.awmd.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

@Data
public class Transfer {

  @NotNull
  @NotEmpty
  private final String accountFromId;
  
  @NotNull
  @NotEmpty
  private final String accountToId;
  
  @NotNull
  @Min(value = 0, message = "Amount to be transferred should be positive")
  private final BigDecimal amountToTransfer;
  
 
  @JsonCreator
  public Transfer(@JsonProperty("accountFromId") String accountFromId,
		  @JsonProperty("accountToId") String accountToId,
		  @JsonProperty("amountToTransfer") BigDecimal amountToTransfer) {
	  	this.accountFromId = accountFromId;
	    this.accountToId = accountToId;
	    this.amountToTransfer = amountToTransfer;
  }


public String getAccountFromId() {
	return accountFromId;
}


public String getAccountToId() {
	return accountToId;
}


public BigDecimal getAmountToTransfer() {
	return amountToTransfer;
}
  
  
}
