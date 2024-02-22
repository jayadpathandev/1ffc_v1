package com.sorrisotech.fffc.agent.pay;

import java.math.BigDecimal;

import com.sorrisotech.svcs.itfc.data.IStringData;
import com.sorrisotech.svcs.itfc.exceptions.MargaritaDataException;

public class Automatic {
	// ************************************************************************
	private String mErrorCode = "none";
	private String mErrorText = "<none>";
	
	
	// ************************************************************************
	public String errorCode() {
		return mErrorCode;
	}
	public String errorText() {
		return mErrorText;
	}
	
	// ************************************************************************
	public void error(
				final String code,
				final String key,
				final String value
			) {
		mErrorCode = code;
		mErrorText = "Unable to decode [" + key + "] value [" + value + "]";
	}
	
	// ************************************************************************
	// option1 = Bill balance/Bill amount
	// option2 = Minimum due
	// option3 = Up to amount
	public String translateAmountOption(
				final String      input,
				final IStringData output
			) throws MargaritaDataException {
		var retval = "bad";
		
		if (input.equalsIgnoreCase("billAmount")) {
			output.putValue("option1");
			retval = "good";			
		} else if (input.equalsIgnoreCase("minimumAmountDue")) {
			output.putValue("option2");
			retval = "good";
		} else if (input.toLowerCase().startsWith("uptoamount=")) {
			output.putValue("option3");
			retval = "good";
		} else {
			error("invalid_payment_amount_rule", "paymentAmountRule", input);
		}
		
		return retval;
	}
	
	// ************************************************************************
	public String translateAmount(
				final String      input,
				final IStringData output
			) throws MargaritaDataException {
		var retval = "bad";
		
		if (input.equalsIgnoreCase("billAmount") || input.equalsIgnoreCase("minimumAmountDue")) {
			output.putValue("");
			retval = "good";			
		} else if (input.toLowerCase().startsWith("uptoamount=")) {
			try {
				final var offset = input.indexOf('=');
				final var amount = new BigDecimal(input.substring(offset+1));
				
				if (amount.compareTo(BigDecimal.ZERO) <= 0) {
					mErrorCode = "invalid_payment_amount_rule";
					mErrorText = "Payment amount is less than or equal to zero.";
				} else if (amount.scale() > 2) {
					mErrorCode = "invalid_payment_amount_rule";
					mErrorText = "Payment amount contains partial cents.";
				} else {
					output.putValue(amount.toPlainString());
					retval = "good";
				}				
			} catch(NumberFormatException e) {
				error("invalid_payment_amount_rule", "paymentAmountRule", input);
			}
		} else {
			error("invalid_payment_amount_rule", "paymentAmountRule", input);
		}
		
		return retval;
	}

	// ************************************************************************
	// option1 = Day of the Month
	// option2 = Days before Due
	public String translateDateOption(
				final String      input,
				final IStringData output
			) throws MargaritaDataException {
		var       retval = "bad";
		final var offset = input.indexOf('=');
		
		if (offset == -1) {
			error("invalid_payment_date_rule", "paymentDateRule", input);
		} else {
			final var code = input.substring(0, offset);
			
			if (code.equalsIgnoreCase("dayOfMonth")) {
				output.putValue("option1");
				retval = "good";							
			} else if (code.equalsIgnoreCase("daysBefore")) {
				output.putValue("option2");
				retval = "good";				
			} else {
				error("invalid_payment_date_rule", "paymentDateRule", input);
			}
		}
		
		return retval;
	}

	// ************************************************************************
	public String translateDate(
				final String      input,
				final IStringData date,
				final IStringData prior
			) throws MargaritaDataException {
		var       retval = "bad";
		final var offset = input.indexOf('=');
		
		if (offset == -1) {
			error("invalid_payment_date_rule", "paymentDateRule", input);
		} else {
			try {
				final var mode = input.substring(0, offset);
				final var day = Long.parseLong(input.substring(offset+1));
				
				if (day <= 0) {
					mErrorCode = "invalid_payment_date_rule";
					mErrorText = "Payment day is less than or equal to zero.";
				} else if (mode.equalsIgnoreCase("dayOfMonth") && day > 31) {
					mErrorCode = "invalid_payment_date_rule";
					mErrorText = "Payment day is greater than 31.";
				} else if (mode.equalsIgnoreCase("daysBefore") && day > 14) {
					mErrorCode = "invalid_payment_date_rule";
					mErrorText = "Payment day is greater than 14 days before.";
				} else if (mode.equalsIgnoreCase("dayOfMonth")) {
					date.putValue(String.valueOf(day));
					prior.putValue("0");
					retval = "good";
				} else if (mode.equalsIgnoreCase("daysBefore")) {
					date.putValue("0");
					prior.putValue(String.valueOf(day));
					retval = "good";
				} else {
					error("invalid_payment_date_rule", "paymentDateRule", input);					
				}
			} catch(NumberFormatException e) {
				error("invalid_payment_date_rule", "paymentAmountRule", input);
			}
		}
		
		return retval;
	}

	// ************************************************************************
	// option1 = Until Cancelled
	// option2 = Until Date
	// option3 = ## payments are made
	public String translateCountOption(
				final String      input,
				final IStringData output
			) throws MargaritaDataException {
		var retval = "bad";
		
		if (input.equalsIgnoreCase("untilCanceled")) {
			output.putValue("option1");
			retval = "good";			
		} else if (input.toLowerCase().startsWith("paymentcount=")) {
			output.putValue("option3");
			retval = "good";
		} else {
			error("invalid_payment_count_rule", "paymentCountRule", input);
		}
		
		return retval;

	}

	// ************************************************************************
	public String translateCout(
				final String      input,
				final IStringData output
			) throws MargaritaDataException {
		var       retval = "bad";
		final var offset = input.indexOf('=');
		
		if (offset != -1) {
			try {
				final var count = Long.parseLong(input.substring(offset+1));
				
				if (count <= 0) {
					mErrorCode = "invalid_payment_count_rule";
					mErrorText = "Payment count is less than or equal to zero.";
				} else {
					output.putValue(String.valueOf(count));
					retval = "good";
				}
			} catch (NumberFormatException e) {
				error("invalid_payment_count_rule", "paymentCountRule", input);
			}			
		} else {
			output.putValue("");
			retval = "good";
		}
		
		return retval;
	}
	
}
