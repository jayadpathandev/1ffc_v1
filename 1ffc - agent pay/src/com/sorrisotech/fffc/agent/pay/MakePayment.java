package com.sorrisotech.fffc.agent.pay;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

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
		var today = Calendar.getInstance();

		// Getting the current date-time in the specified application locale ZoneId
		ZonedDateTime nowInApplicationTimeZone = ZonedDateTime
				.now(ZoneId.of(AppConfig.get("application.locale.time.zone.id")));

		// Adjusting today by using the offset difference
		today.add(Calendar.SECOND, nowInApplicationTimeZone.getOffset().getTotalSeconds());
		
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
		method.put("type", current.walletType());
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

	// *************************************************************************
	public void payAmount(final IStringData value) throws MargaritaDataException {
		if (mAmount == null)
			throw new RuntimeException("No amount set.");

		final var df = new DecimalFormat("###.00");

		value.putValue(df.format(mAmount));
	}
	
	// *************************************************************************
	public void flexValue(
			final IUserData   data,
			final IStringData value
		) throws MargaritaDataException, ClassNotFoundException {
		
		StringBuilder result = new StringBuilder();
		final PaySession current = data.getJavaObj(ApiPay.class).current();
		
		if ("debit".equals(current.walletType()) ) {
			
			if (mSurcharge != null && BigDecimal.ZERO.compareTo(mSurcharge) != 0) {
				result.append("convenienceFee=true" + "|" + "convenienceFeeAmount=" + mSurcharge.setScale(2, RoundingMode.HALF_UP).toPlainString());
				result.append("|");
			}
			
			if (current.accountNumber() == null) {
				throw new RuntimeException("DisplayAccountNumber is not set.");
			}
			
			result.append("displayAccountNumber=" + current.accountNumber());
			
			value.putValue(result.toString());
		}
		
	}
	
	// *************************************************************************
	public void flexDefinition(
			final IUserData   data,
			final IStringData value,
			final IStringData definition
		) throws MargaritaDataException, ClassNotFoundException {

		final PaySession current = data.getJavaObj(ApiPay.class).current();
		
		if ("debit".equals(current.walletType())) {
			value.putValue(definition.getValue());
		}
	}
	
	/**************************************************************************
	 * Checks if the wallet Expire before payment
	 * 
	 * @param szSourceExpiry
	 * @return "true", "false" or "error"
	 */
	public String willWalletExpireBeforePayDate(final String szSourceExpiry) {
		String szResult = "false";
		
		if (szSourceExpiry.isEmpty()) return szResult;
		
		try {
			if (szSourceExpiry != null && !szSourceExpiry.isEmpty()) {
				SimpleDateFormat cFormat = new SimpleDateFormat("MM/yyyy");
				cFormat.setLenient(false); // for strict date formatting
				
				Date cExpDate = cFormat.parse(szSourceExpiry);
				Calendar cExpCal = Calendar.getInstance();
				cExpCal.setTime(cExpDate);
				cExpCal.set(Calendar.DAY_OF_MONTH, cExpCal.getActualMaximum(Calendar.DAY_OF_MONTH)); // Set to the end of the month

				Calendar cPayDateCal = this.mDate;
				
				// Wallet expires before the payment date
				if (cExpCal.before(cPayDateCal)) {
	                szResult = "true"; 
	            }
				
			}
		} catch (Exception e) {
			LOG.error("MakePayment.....willWalletExpireBeforePayDate()...An exception was thrown", e);
			szResult = "error";
		}
		
		return szResult;
	}
}
