package com.sorrisotech.fffc.agent.pay;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sorrisotech.utils.Spring;
import com.sorrisotech.app.common.utils.I18n;
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
import com.sorrisotech.uc.payment.UcPaymentAction;

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
	private PaySession mCurrent   = null;
	private String     mIFrame    = null;
	private String     mError     = "";
	private String 	   mDeleteErr = "";
	
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
	public PaySession current() {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		return mCurrent;
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
	public void disableAch() {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		mCurrent.disableAch();
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
	public void accountId(
			final IStringData value
		) 
			throws MargaritaDataException {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		value.putValue(mCurrent.accountId());
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
			final IStringData value,
			final String identifierType,
			final String transactionId
		) 
			throws MargaritaDataException {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		
		boolean bPaymentIdentifierIsAccount = Optional.ofNullable(identifierType)
				.map(val -> "DISPLAY_ACCOUNT".equals(val))
				.orElse(false);
		
		if (bPaymentIdentifierIsAccount) {
			value.putValue(mCurrent.accountNumber());
		} else {
			value.putValue(transactionId);
		}

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
	public void clearWallet() {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");	
		
		mCurrent.wallet("", "", "", "", "", "");		
	}

	//*************************************************************************
	public void setWallet(
				final String token
			) {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");	
		
		final var wallet = mWalletDao.getPaymentWallet(mCurrent.userId());
		
		if (wallet != null && wallet.length > 0) {
			for(final PaymentWalletFields source : wallet) {
				if (isValid(source)) {
					if (source.getSourceId().equals(token)) {
						mCurrent.wallet(
							source.getSourceName(),
							source.getSourceType(),
							source.getSourceNum(),
							source.getSourceExpiry(),
							token,
							source.getSourceDefault()
						);
						mCurrent.setStatus(PayStatus.pmtAccountChosen);
						break;
					}
				}
			}
		}
	}

	//*************************************************************************
	public void setWallet(
				final String type,
				final String account,
				final String expiry,
				final String token
			) {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");

		final var wallet = mWalletDao.getPaymentWallet(mCurrent.userId());
		var       found  = false;
		if (wallet != null && wallet.length > 0) {
			for(final PaymentWalletFields source : wallet) {
				if (isValid(source)) {
					if (source.getSourceId().equals(token)) {
						mCurrent.wallet(
							source.getSourceName(),
							source.getSourceType(),
							source.getSourceNum(),
							source.getSourceExpiry(),
							token,
							source.getSourceDefault()
						);
						mCurrent.setStatus(PayStatus.pmtAccountChosen);
						found = true;
						break;
					}
				}
			}
		}
		
		if (found == false) {
			mCurrent.wallet(
				"Unsaved",
				type,
				account,
				expiry,
				token,
				"false"
			);
			mCurrent.setStatus(PayStatus.pmtAccountChosen);
		}
	}

	//*************************************************************************
	public String hasWallet() {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		return mCurrent.walletToken().isEmpty() ? "false" : "true";
	}

	//*************************************************************************
	public String doSurcharge(
			final IUserData data
			) {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		final var surcharge = UcPaymentAction.getSurchargeStatus();
		
		if (surcharge.equalsIgnoreCase("true") && mCurrent.walletType().equalsIgnoreCase("debit")) {
			return "true";
		}
		
		return "false";
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
	public String sourceName() {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		return mCurrent.walletName();		
	}

	//*************************************************************************
	public String sourceAccount() {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		return mCurrent.walletAccount();		
	}

	//*************************************************************************
	public String sourceType() {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		return mCurrent.walletType();		
	}
	
	//*************************************************************************
	public String sourceDefault() {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		return mCurrent.walletDefault();		
	}
	
	//*************************************************************************
	public String sourceToken() {
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		return mCurrent.walletToken();		
	}
	
	//*************************************************************************
	public void setError(
				final IServiceLocator2 locator,
				final IUserData        userData,
				final String error
			) {
		switch(error) {
		case "11": 	mError = I18n.translate(locator, userData, "paymentOneTime_addSourceGenericError"); break;
		case "12":  mError = I18n.translate(locator, userData, "paymentOneTime_addSourceCardNumberError"); break;
		case "13":  mError = I18n.translate(locator, userData, "paymentOneTime_addSourceAddressError"); break;
		case "14":  mError = I18n.translate(locator, userData, "paymentOneTime_addSourceCvvError"); break;
		case "15":  mError = I18n.translate(locator, userData, "paymentOneTime_addSourceCardExpiredError"); break;
		case "16":  mError = I18n.translate(locator, userData, "paymentOneTime_addSourceLostStolenFraudError"); break;
		case "17":  mError = I18n.translate(locator, userData, "paymentOneTime_addSourceInvalidMethodError"); break;
		case "18":  mError = I18n.translate(locator, userData, "paymentOneTime_addSourceCardDeclinedError"); break;
		case "132": mError = I18n.translate(locator, userData, "paymentOneTime_addSourceCardNotAcceptedError"); break;
		case "21":  mError = I18n.translate(locator, userData, "paymentOneTime_editSourceGenericError"); break;
		case "22":  mError = I18n.translate(locator, userData, "paymentOneTime_editSourceCardNumberError"); break;
		case "23":  mError = I18n.translate(locator, userData, "paymentOneTime_editSourceAddressError"); break;
		case "24":  mError = I18n.translate(locator, userData, "paymentOneTime_editSourceCvvError"); break;
		case "25":  mError = I18n.translate(locator, userData, "paymentOneTime_editSourceCardExpiredError"); break;
		case "26":  mError = I18n.translate(locator, userData, "paymentOneTime_editSourceLostStolenFraudError"); break;
		case "27":  mError = I18n.translate(locator, userData, "paymentOneTime_editSourceInvalidMethodError"); break;
		case "133": mError = I18n.translate(locator, userData, "paymentOneTime_editSourceCardNotAcceptedError"); break;	
		case "":
			mError = "";
			break;
		default:
			mError =I18n.translate(locator, userData, "apiStartChooseSource_sInternalError"); 
			break;				
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
		var wallet = mWalletDao.getPaymentWallet(mCurrent.userId());
		final var items  = new LinkedList<HashMap<String, Object>>();
		
		var currentName  = mCurrent.walletName();
		var currentNum   = mCurrent.walletAccount();
		var currentToken = mCurrent.walletToken();
		
		var found = false;
		
		boolean disableAch = false;
		
		if (mCurrent.isAchEnabled() == false) {
			List<PaymentWalletFields> cPaymentWalletList = new ArrayList<>(Arrays.asList(wallet));
			cPaymentWalletList.removeIf(val -> "bank".equals(val.getSourceType()));
			wallet = cPaymentWalletList.toArray(new PaymentWalletFields[cPaymentWalletList.size()]);
			disableAch = true;
		}
		
		if (wallet != null && wallet.length > 0) {
			for(final PaymentWalletFields source : wallet) {
				if (isValid(source)) {
					final var item = new HashMap<String, Object>();
					item.put("val", source.getSourceId());
					item.put("text", source.getSourceName() + " " + source.getSourceNum());
					items.add(item);
					
					if (currentToken == null || currentToken.isEmpty()) {
						mCurrent.wallet(
							source.getSourceName(),
							source.getSourceType(),
							source.getSourceNum(),
							source.getSourceExpiry(),
							source.getSourceId(),
							source.getSourceDefault()
						);
						currentName  = source.getSourceName();
						currentNum   = source.getSourceNum();
						currentToken = source.getSourceId();
						
						mCurrent.setStatus(PayStatus.pmtAccountChosen);						
						found = true;
					} else if (currentToken.equals(source.getSourceId())) {
						found = true;
					}
				}
			}
		}

		// --------------------------------------------------------------------
		if (found == false && currentToken != null && currentToken.isEmpty() == false) {
			final var item = new HashMap<String, Object>();
			item.put("val", currentToken);
			item.put("text", currentName + " " + currentNum);
			items.addFirst(item);
		}
		
		// --------------------------------------------------------------------
		context.put("code", mCurrent.id());
		context.put("type", mCurrent.saveSource() ? "automatic" : "onetime");
		context.put("hasWallet", !items.isEmpty());
		context.put("wallet", items.toArray());
		context.put("iframe", iframe);
		context.put("disableAch", disableAch);
		context.put("walletItem", mCurrent.walletToken());
		context.put("error", mError);
		context.put("deleteWalletError", mDeleteErr);
		
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
		if (mCurrent == null) throw new RuntimeException("There is no current session.");
		
		final var eStatus = mCurrent.getStatus();
		
		switch (eStatus) {
		case error: 
		case transactionComplete:
		case transactionIdExpired:
			clear (code);
			break;
		default:
			break;
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
	 * Automatic payment rule setup in progress
	 * 
	 * @param code
	 * @return
	 */
	public String setTransactionAutoPayScheduleInProgress (final String code) {
		return setStatus(code, PayStatus.automaticPmtRuleInProgress);
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

	public void clearDeleteError () {
		
		this.mDeleteErr = "";
	}
	
	public String isWalletDeletable() {
		
		if (mCurrent == null)
			throw new RuntimeException("There is no current session.");
		
		if (mCurrent.walletName().equals("Unsaved")) {
			// ----------------------------------------------------------------------------
			// Payment method is temporary, cannot delete.
			mDeleteErr = "unsaved";
			return "false";
		}
		
		return Optional.ofNullable(mWalletDao.getPaymentWallet(mCurrent.userId())).map(cWallet -> {
			var szWalletStatus = new UcPaymentAction().checkSource(mCurrent.userId(),
			        mCurrent.walletToken());
			switch (szWalletStatus) {
				case "":
					return "true";
				case "scheduled":
					// --------------------------------------------------------------------
					// Payment method is used in a scheduled payment, cannot delete.
					mDeleteErr = "scheduled";
					return "false";
				case "progress":
					// --------------------------------------------------------------------
					// Scheduled payment is currently be made with the payment method, cannot
					// delete.
					mDeleteErr = "progress";
					return "false";
				case "auto":
					// --------------------------------------------------------------------
					// Payment method is used in an auto payment schedule, cannot delete.
					mDeleteErr = "auto";
					return "false";
				default:
					// --------------------------------------------------------------------
					// Payment could not be delete for unknown reasons.
					mDeleteErr = "unknown";
					return "false";
			}
		}).orElseGet(() -> {
			// --------------------------------------------------------------------
			// Payment could not be delete for unknown reasons.
			mDeleteErr = "unknown";
			return "false";
		});
		
	}
}
