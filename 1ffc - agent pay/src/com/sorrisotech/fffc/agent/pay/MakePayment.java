package com.sorrisotech.fffc.agent.pay;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sorrisotech.fffc.agent.pay.PaySession.PayStatus;
import com.sorrisotech.svcs.itfc.data.IStringData;
import com.sorrisotech.svcs.itfc.data.IUserData;
import com.sorrisotech.svcs.itfc.exceptions.MargaritaDataException;
import com.sorrisotech.utils.AppConfig;

public class MakePayment {

	//*************************************************************************
	private static final Logger LOG = LoggerFactory.getLogger(MakePayment.class);

	//*************************************************************************
	private Calendar   mDate      = null;
	private boolean    mImmediate = false;
	private BigDecimal mAmount    = null;
	private BigDecimal mSurcharge = null;

	//*************************************************************************
	public String saveDateAndAmount(
			final IUserData data,
			final String    date,
			final String    amount
			) throws ClassNotFoundException {
		final PaySession current = data.getJavaObj(ApiPay.class).current();

		//---------------------------------------------------------------------
		final var today = Calendar.getInstance();
				
		if (date.equalsIgnoreCase("today")) {
			mDate = (Calendar) today.clone();
		} else {
			final var format = new SimpleDateFormat("yyyy/MM/dd");
			try {
				mDate = Calendar.getInstance();
				mDate.setTime(format.parse(date));				
			} catch(ParseException e) {
				LOG.error("Could not parse date [" + date + "].");
				current.setStatus(PayStatus.error);
				return "invalid_date";
			}
		}
		
		// Getting the current date-time in the specified application locale ZoneId
		ZonedDateTime nowInApplicationTimeZone = ZonedDateTime
		        .now(ZoneId.of(AppConfig.get("application.locale.time.zone.id")));
		
		// Adjusting mDate by using the offset difference
		mDate.add(Calendar.SECOND, nowInApplicationTimeZone.getOffset().getTotalSeconds());

		//---------------------------------------------------------------------
		final var toLong   = new SimpleDateFormat("yyyyMMdd");
		final var todayNum = Long.parseLong(toLong.format(today.getTime()));
		final var payNum   = Long.parseLong(toLong.format(mDate.getTime()));
		
		if (payNum < todayNum) {
			current.setStatus(PayStatus.error);
			return "invalid_date";			
		} 
		
		mImmediate = (payNum == todayNum);

		//---------------------------------------------------------------------
		try {
			mAmount = new BigDecimal(amount);
		} catch(NumberFormatException e) {
			LOG.error("Could not parse amount [" + amount + "].");
			current.setStatus(PayStatus.error);
			return "invalid_amount";			
		}
		
		if (mAmount.compareTo(BigDecimal.ZERO) <= 0 || mAmount.scale() > 2) {
			LOG.error("Invalid amount amount [" + amount + "].");
			current.setStatus(PayStatus.error);
			return "invalid_amount";						
		}
		current.setStatus(PayStatus.oneTimePmtInProgress);

		return "success";
	}
	
	//*************************************************************************
	public String is_immediate_payment() {
		return mImmediate ? "true" : "false";	
	}

	//*************************************************************************
	public void totalAmount(
				final IStringData value
			) 
				throws MargaritaDataException {
		if (mAmount == null) throw new RuntimeException("No amount set.");
		
		final var df = new DecimalFormat("###.00");
		
		value.putValue(df.format(mSurcharge == null ? mAmount : mAmount.add(mSurcharge)));
	}
	
	//*************************************************************************
	public void payDate(
				final IStringData value
			) 
				throws MargaritaDataException {
		if (mDate == null) throw new RuntimeException("No date set.");
		final var f = new SimpleDateFormat("yyyy-MM-dd");	
		value.putValue(f.format(mDate.getTime()));
	}	
	
	//*************************************************************************
	public String setSurcharge(
				final String amount
			) {
		var retval = "failure";
				
		if (amount != null && amount.isEmpty() == false) {
			try {
				mSurcharge = new BigDecimal(amount);
				retval = "success";
			} catch(Throwable e) {
				LOG.error("Could not parse surcharge amount [" + amount + "].", e);
			}
		} else {
			mSurcharge = null;
			retval = "success";
		}		
		
		return retval;
	}
	
	//*************************************************************************
	public void accountJson(
				final IUserData   data,
				final IStringData value
			) throws ClassNotFoundException {
		final PaySession current = data.getJavaObj(ApiPay.class).current();

		final var mapper = new ObjectMapper();
		final var root   = mapper.createObjectNode();
		final var date   = new SimpleDateFormat("yyyy-MM-dd");
		
		root.put("payDate", date.format(mDate.getTime()));
		root.put("paymentGroup", current.payGroup());
		root.put("autoScheduledConfirm", false);
		
		final var method = mapper.createObjectNode();
		method.put("nickName", current.walletName());
		method.put("expiry", current.walletExpiry());
		method.put("token",current.walletToken());
		root.set("payMethod", method);
		
		final var grouping = mapper.createArrayNode();
		final var group    = mapper.createObjectNode();
		
		group.put("internalAccountNumber", current.accountId());
		group.put("displayAccountNumber", current.accountNumber());
		group.put("paymentGroup", current.payGroup());
		group.put("documentNumber", current.invoice());
		final var df = new DecimalFormat("###.00");
		group.put("amount", df.format(mAmount));
		group.put("totalAmount", df.format(mSurcharge == null ? mAmount : mAmount.add(mSurcharge)));
		group.put("surcharge", mSurcharge != null ? df.format(mSurcharge) : "0.00");
		group.put("interPayTransactionId", "N/A");
		
		grouping.add(group);
		
		root.set("grouping", grouping);
		
		try {
			final var json = mapper.writeValueAsString(root);
			value.putValue(json);
		} catch (Throwable e) {
			LOG.error("Internal error", e);
		}
	}	
}
