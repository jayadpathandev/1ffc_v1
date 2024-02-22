package com.sorrisotech.fffc.agent.pay;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.utils.Spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sorrisotech.app.utils.Freemarker;
import com.sorrisotech.fffc.agent.pay.PaySession.PayStatus;
import com.sorrisotech.svcs.external.IExternalReuse;
import com.sorrisotech.svcs.external.IFileLocator;
import com.sorrisotech.svcs.external.IServiceLocator2;
import com.sorrisotech.svcs.itfc.data.IStringData;
import com.sorrisotech.svcs.itfc.data.IUserData;
import com.sorrisotech.svcs.itfc.exceptions.MargaritaDataException;
import com.sorrisotech.svcs.payment.dao.PaymentWalletDao;
import com.sorrisotech.svcs.payment.model.PaymentWalletFields;

public class ApiPay implements IExternalReuse {
	
	//*************************************************************************
	private static final long serialVersionUID = -372619522145577331L;

	//*************************************************************************
	private static final Logger LOG = LoggerFactory.getLogger(ApiPayDao.class);
	
	//*************************************************************************
	private static class SessionMap extends LinkedHashMap<Long, PaySession> {
		private static final long serialVersionUID = 6119668449805196625L;
		@Override
		protected boolean removeEldestEntry(
					final Map.Entry<Long, PaySession> entry
				) {
			return entry.getValue().isExpired(); 
		}
	}
	private static SessionMap mSessions = new SessionMap();

	//*************************************************************************
	private static PaymentWalletDao mWalletDao;
	static {
		mWalletDao = Spring.getPaymentQuery().getBean("pmtWallet", PaymentWalletDao.class);
	}
	
	//*************************************************************************
	private PaySession mCurrent = null;
	private Date       mDate    = null;
	private BigDecimal mAmount  = null;
	private String     mIFrame  = null;
	
	//*************************************************************************
	@Override
	public int getReuse() {
		return IExternalReuse.REUSE_SESSION_SINGLETON;
	}
	
	//*************************************************************************
	public String create(
				final String type
			) {
		// --------------------------------------------------------------------
		boolean bSave;
		if (type.equalsIgnoreCase("oneTime")) {
			bSave = false;
		} else if (type.equalsIgnoreCase("automatic")) {
			bSave = true;
		} else {
			return "error";
		}
		
		// --------------------------------------------------------------------
		long id = Math.abs(ThreadLocalRandom.current().nextLong());
		
		synchronized(mSessions) {
			while(mSessions.containsKey(id)) {
				id = Math.abs(ThreadLocalRandom.current().nextLong());
			}
			mCurrent = new PaySession(id);
			mSessions.put(id, mCurrent);
		}
		mCurrent.saveSource(bSave);

		return "success";
	}
	
	//*************************************************************************
	public String load(
				final String code
			) {
		long id = -1;
		
		try {
			id = Long.parseLong(code);
		} catch(NumberFormatException e) {
			LOG.warn("Invalid code [" + code + "]");
			return "false";
		}
		
		synchronized(mSessions) {
			mCurrent = mSessions.get(id);
		}
		
		return mCurrent != null ? "true" : "false";
	}	

	//*************************************************************************
	public String clear(
				final String code
			) {
		long id = -1;
		
		try {
			id = Long.parseLong(code);
		} catch(NumberFormatException e) {
			LOG.warn("Invalid code [" + code + "]");
			return "false";
		}
				
		mCurrent = null;
		synchronized(mSessions) {
			mCurrent = mSessions.remove(id);
		}
		
		return mCurrent != null ? "true" : "false";
	}

	//*************************************************************************
	public String saveDateAndAmount(
			final String date,
			final String amount
			) {
		
		if (date.equalsIgnoreCase("today")) {
			mDate = new Date();
		} else {
			final var format = new SimpleDateFormat("yyyy/MM/dd");
			try {
				mDate = format.parse(date);
			} catch(ParseException e) {
				LOG.error("Could not parse date [" + date + "].");
				mCurrent.setStatus(PayStatus.error);
				return "invalid_date";
			}
			
			if (mDate.before(new Date()) ) {
				mCurrent.setStatus(PayStatus.error);
				return "invalid_date";
			}
		}
		
		try {
			mAmount = new BigDecimal(amount);
		} catch(NumberFormatException e) {
			LOG.error("Could not parse amount [" + amount + "].");
			mCurrent.setStatus(PayStatus.error);
			return "invalid_amount";			
		}
		
		if (mAmount.compareTo(BigDecimal.ZERO) <= 0 || mAmount.scale() > 2) {
			LOG.error("Invalid amount amount [" + amount + "].");
			mCurrent.setStatus(PayStatus.error);
			return "invalid_amount";						
		}
		
		// -- saving date and amount updates status to oneTimePmtInProgress --
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		mCurrent.setStatus(PayStatus.oneTimePmtInProgress);
		
		return "success";
	}
	
	
	//*************************************************************************
	public String id() {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		return String.valueOf(mCurrent.id());
	}	
	
	//*************************************************************************
	public String isOneTime() {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		
		if (mCurrent.saveSource()) {
			return "false";
		}
		return "true";
	}	

	//*************************************************************************
	public void setCustomerId(
			final String value
			) {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		mCurrent.customerId(value);
	}	
	public String customerId() {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		return mCurrent.customerId();
	}	

	//*************************************************************************
	public void setAccountId(
			final String value
			) {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		mCurrent.accountId(value);
	}	
	public String accountId() {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		return mCurrent.accountId();
	}	

	//*************************************************************************
	public void setAccountNumber(
			final String value
			) {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		mCurrent.accountNumber(value);
	}	
	public String accountNumber() {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		return mCurrent.accountNumber();
	}	

	//*************************************************************************
	public void setPayGroup(
			final String value
			) {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		mCurrent.payGroup(value);
	}	
	public void payGroup(
				final IStringData value
			) 
				throws MargaritaDataException {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		value.putValue(mCurrent.payGroup());
	}	

	//*************************************************************************
	public void setInvoice(
			final String value
			) {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		mCurrent.invoice(value);
	}	
	
	//*************************************************************************
	public void setUserid(
			final String value
			) {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		mCurrent.userId(value);
	}	
	public String userid() {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		return mCurrent.userId();
	}	
	public void userid(
			final IStringData value
		) 
			throws MargaritaDataException {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		value.putValue(mCurrent.userId());
	}

	//*************************************************************************
	public void setCompanyId(
			final String value
			) {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		mCurrent.companyId(value);
	}	
	public void companyId(
			final IStringData value
		) 
			throws MargaritaDataException {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		value.putValue(mCurrent.companyId());
	}

	//*************************************************************************
	public void setUserName(
			final String value
			) {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		mCurrent.username(value);
	}	
	public String userName() {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		return mCurrent.username();
	}	

	//*************************************************************************
	public void setAutomaticPayment(
			final String value
			) {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		mCurrent.automaticPayment(value.equalsIgnoreCase("true"));
	}	
	public String automaticPayment() {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		return mCurrent.automaticPayment() ? "true" : "false";
	}	
	
	//*************************************************************************
	public void amount(
				final IStringData value
			) 
				throws MargaritaDataException {
		if (mAmount == null) throw new RuntimeException("No amount set.");
		value.putValue(mAmount.toPlainString());
	}
	
	//*************************************************************************
	public void payDate(
				final IStringData value
			) 
				throws MargaritaDataException {
		if (mAmount == null) throw new RuntimeException("No date set.");
		final var f = new SimpleDateFormat("yyyy-MM-dd");	
		value.putValue(f.format(mDate));
	}

	//*************************************************************************
	public void walletToken(
				final IStringData value
			) 
				throws MargaritaDataException {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		value.putValue(mCurrent.walletToken());
	}
	
	//*************************************************************************
	public String sourceUrl(
				final String base,
				final String shortcut
			) {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		
		final var str = new StringBuilder();
		str.append(base);
		if (base.endsWith("/") == false) str.append('/');
		str.append(shortcut);
		str.append("?code=");
		str.append(mCurrent.id());
		
		return str.toString();
	}

	//*************************************************************************
	private static boolean isValid(
				final PaymentWalletFields item
			) {
		final var expiry = item.getSourceExpiry();
		if (expiry == null || expiry.isEmpty()) return true;
		
		Calendar expiryDate = null;
		try {
			final var format = new SimpleDateFormat("MM/yyyy");
			final var date   = format.parse(expiry);
			
			expiryDate = Calendar.getInstance();			
			expiryDate.setTime(date);
			expiryDate.add(Calendar.MONTH, 1);
		}
		catch(ParseException e) {
			return true;
		}
				
		return Calendar.getInstance().before(expiryDate);
	}
	
	//*************************************************************************
	public void setWallet(
				final String id
			) {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");

		final var wallet = mWalletDao.getPaymentWallet(mCurrent.userId());
		
		if (wallet != null && wallet.length > 0) {
			for(final PaymentWalletFields source : wallet) {
				if (isValid(source)) {
					if (source.getSourceId().equals(id)) {
						mCurrent.wallet(
							source.getSourceName(),
							source.getSourceType(),
							source.getSourceNum(),
							source.getSourceExpiry(),
							id
						);
						// -- when wallet is called, the status changes to PmtAccountChosen --
						mCurrent.setStatus(PayStatus.pmtAccountChosen);

						break;
					}
				}
			}
		}
		else {
			mCurrent.setStatus(PayStatus.error);
		}
	}
	//*************************************************************************
	public void walletFrom(
				final IStringData value
			) 
				throws MargaritaDataException{
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		value.putValue(mCurrent.walletName() + "|" + mCurrent.walletType() + "|" + mCurrent.walletAccount());
	}

	//*************************************************************************
	public void makePaymentJson(
				final IStringData value
			) {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");

		final var mapper = new ObjectMapper();
		final var root   = mapper.createObjectNode();
		final var date   = new SimpleDateFormat("yyyy-MM-dd");
		
		root.put("payDate", date.format(mDate));
		root.put("paymentGroup", mCurrent.payGroup());
		root.put("autoScheduledConfirm", false);
		
		final var method = mapper.createObjectNode();
		method.put("nickName", mCurrent.walletName());
		method.put("expiry", mCurrent.walletExpiry());
		method.put("token",mCurrent.walletToken());
		root.set("payMethod", method);
		
		final var grouping = mapper.createArrayNode();
		final var group    = mapper.createObjectNode();
		
		group.put("internalAccountNumber", mCurrent.accountId());
		group.put("displayAccountNumber", mCurrent.accountNumber());
		group.put("paymentGroup", mCurrent.payGroup());
		group.put("documentNumber", mCurrent.invoice());
		group.put("amount", mAmount.toPlainString());
		group.put("totalAmount", mAmount.toPlainString());
		group.put("surcharge", "0.00");
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

	//*************************************************************************
	public void createAutomaticJson(
				final IStringData value
			) {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");

		final var mapper = new ObjectMapper();
		final var root   = mapper.createObjectNode();
		
		final var grouping = mapper.createArrayNode();
		final var group    = mapper.createObjectNode();
		
		group.put("internalAccountNumber", mCurrent.accountId());
		group.put("displayAccountNumber", mCurrent.accountNumber());
		group.put("paymentGroup", mCurrent.payGroup());
		
		grouping.add(group);
		
		root.set("grouping", grouping);
		
		try {
			final var json = mapper.writeValueAsString(root);
			value.putValue(json);
		} catch (Throwable e) {
			LOG.error("Internal error", e);
		}
	}
	
	//*************************************************************************
	public void prepareIframe(
				final IServiceLocator2 locator,
				final IUserData        data,
				final String           iframe
			) {
		// --------------------------------------------------------------------
		final var context = new HashMap<String, Object>();

		// --------------------------------------------------------------------
		final var wallet = mWalletDao.getPaymentWallet(mCurrent.userId());
		final var items  = new ArrayList<HashMap<String, Object>>();
		
		if (wallet != null && wallet.length > 0) {
			for(final PaymentWalletFields source : wallet) {
				if (isValid(source)) {
					final var item = new HashMap<String, Object>();
					item.put("val", source.getSourceId());
					item.put("text", source.getSourceName() + " " + source.getSourceNum());
					items.add(item);
					
					if (mCurrent.walletToken() == null || mCurrent.walletToken().isEmpty()) {
						setWallet(source.getSourceId());
					}
				}
			}
		}

		// --------------------------------------------------------------------
		context.put("code", mCurrent.id());
		context.put("type", mCurrent.saveSource() ? "automatic" : "onetime");
		context.put("hasWallet", !items.isEmpty());
		context.put("wallet", items.toArray());
		context.put("iframe", iframe);
		context.put("walletItem", mCurrent.walletToken());
		
		// --------------------------------------------------------------------
		try {
			mIFrame = Freemarker.resolveFreemarkerTemplate(
					locator,
					data,
					"api_start_add_source.ftl",
					context,
					"1FFC-ea0f1923-255f-4f12-a603-16a1ed4f950c",
					"en", "US"
				);
		} catch(Throwable e) {
			LOG.warn("Could not write out error iframe", e);
		}
	}

	//*************************************************************************
	public void showIframe(
				final HttpServletResponse response
			) {
		try {
			response.setContentType("text/html; charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);
			
			final var writer = response.getWriter();
			writer.print(mIFrame);
			writer.close();

		} catch(Throwable e) {
			LOG.warn("Could not write out error iframe", e);
		}
	}
	
	//*************************************************************************
	public static void showHtmlError(
				final HttpServletResponse response,
				final IServiceLocator2    locator,
				final String              file
			) {
		final String        filename = "linked-root/ftl/" + file;
		final IFileLocator  files  = locator.findService(IFileLocator.class);
		final InputStream   stream = files.getFileAsStream(filename);

		StringBuilder retval = new StringBuilder();
		if (stream != null) {
			try {
				final InputStreamReader reader = new InputStreamReader(stream, "utf-8");
				final char[]            buffer = new char[8192];
				
				int read = 0;
				while((read = reader.read(buffer, 0, 8192)) > 0) {
					retval.append(buffer, 0, read);
				}
				reader.close();
				
			} catch(Throwable e) {
				retval = new StringBuilder(
					"<html><body><h1>Could not read file: <b>" + filename + "</b> - " + e.getMessage() + "</h1></body></html>"
				);
				try { stream.close(); } catch(Throwable e2) {}
			}			
		} else {
			retval.append("<html><body><h1>Could not find HTML template <b>" + filename + "</b>.</h1></body></html>");
		}
		
		//---------------------------------------------------------------------
		response.setContentType("text/html; charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		
		try {
			final var writer = response.getWriter();
			writer.print(retval.toString());
			writer.close();
		} catch(final IOException e) {
			LOG.warn("Could not write out error iframe", e);
		}
		
	}
	
	//*************************************************************************
	public static void noop(
				final HttpServletResponse response
			) {
		response.setContentType("text/html; charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);		
	}
	
	/**
	 * Sets the status value internally as worker for external
	 * 	status specific calls --
	 * 
	 * @param cszPayStatus
	 * @return 'true' if success, 'false' if not
	 */
	private String setStatus(final String code, final PayStatus ceStatus) {

		long id = -1;
		PaySession lSession = null;
		
		try {
			id = Long.parseLong(code);
		} catch(NumberFormatException e) {
			LOG.warn("Invalid code [" + code + "]");
			return "false";
		}
		
		synchronized(mSessions) {
			lSession = mSessions.get(id);
		}
		
		lSession.setStatus(ceStatus);
		return "true";
	}
	
	/**
	 * Returns status for specified transaction. If its in a final state
	 * gets rid of the current session.
	 * 
	 * @param code -- current session id
	 * @return -- status value as a string
	 */
	public String getStatus(final String code) {

		long id = -1;
		PaySession lSession = null;
		PayStatus eStatus = PayStatus.transactionIdNotFound;
		
		try {
			id = Long.parseLong(code);
		} catch(NumberFormatException e) {
			LOG.warn("Invalid code [" + code + "]");
			return PayStatus.error.toString();
		}
		
		synchronized(mSessions) {
			lSession = mSessions.get(id);
		}

		if (null != lSession) {
			eStatus = lSession.getStatus();
		
			switch (eStatus) {
			case error: 
			case transactionComplete:
			case transactionIdExpired:
				lSession = null;
				clear (code);
				break;
			default:
				break;
			}
		}
		return eStatus.toString();
	}
	
	/**
	 * Successful transactions set this when they are done
	 * 
	 * @param code -- transaction id
	 * @return -- 'true' if this worked, 'false' otherwise
	 */
	public String setTransactionComplete(final String code) {

		return setStatus(code, PayStatus.transactionComplete);
	}
	
	/**
	 * Transaction errors caught at use case level call this
	 * 
	 * @param code -- transaction id
	 * @return -- 'true' if this worked, 'false' otherwise
	 */
	public String setTransactionError(final String code) {

		return setStatus(code, PayStatus.error);
	}
	
	/** 
	 * One-time payment in progress
	 * 
	 * @param code
	 * @return
	 */
	public String setTransactionOneTimeInProgress(final String code) {
		return setStatus(code, PayStatus.oneTimePmtInProgress);
	}
	
	/**
	 * StartPaymentForAgent completed successfully
	 * 
	 * @param code
	 * @return
	 */
	public String setTransactionStarted(final String code) {
		return setStatus(code, PayStatus.started); 
	}
	
}
