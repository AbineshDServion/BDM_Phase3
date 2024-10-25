package com.servion.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.db.dataservices.DataServices;
import com.db.resource.DBConstants;
import com.db.resource.VRUDBDataServicesInstance;
import com.reportbean.IvrData;
import com.reportbean.IvrData.AnnounceDetails.Announce;
import com.reportbean.IvrData.FeatureDetails.Feature;
import com.reportbean.IvrData.HostDetails.Host;
import com.reportbean.IvrData.MenuDetails.Menu;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICECallData;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.ice.RuleEngine.ICERuleParam;
import com.servion.model.CustDtls.CustDtls_HostRes;
import com.servion.model.IVRCallEngine.IVR_ICECallData;
import com.servion.model.IVRCallEngine.IVR_ICEFeatureData;
import com.servion.model.IVRCallEngine.IVR_ICEGlobalConfig;
import com.servion.model.IVRCallEngine.IVR_ICERuleParam;
import com.servion.model.accountBalance.AccountBalance_HostRes;
import com.servion.model.apinValidation.APINCustomerProfileDetails_HostRes;
import com.servion.model.apinValidation.ValidatePIN_HostRes;
import com.servion.model.bankingFlashBalance.BankingBalanceFlashDetails_HostRes;
import com.servion.model.billPayment.GetUtilityBillInfo_HostRes;
import com.servion.model.billPayment.TelecomCustomerInfo_HostRes;
import com.servion.model.billPayment.TelecomPostpaidBalanceDetails_HostRes;
import com.servion.model.billPayment.TelecomPrepaidNumberVal_HostRes;
import com.servion.model.billPayment.TelecomSubscriberInfo_HostRes;
import com.servion.model.billPayment.UpdateMobileNoDetails_DBRes;
import com.servion.model.billPayment.UpdatePaymentDetailsCC_HostRes;
import com.servion.model.billPayment.UpdatePaymentDetails_HostRes;
import com.servion.model.billPayment.UtilitySubscriberInfo_HostRes;
import com.servion.model.billPayment.Utility_BeneficiaryDetailList_HostRes;
import com.servion.model.billPayment.Utility_BenfPayeeDetails_HostRes;
import com.servion.model.birthdayGreeting.UpdateBirthdayAnncStatus_DBRes;
import com.servion.model.callerIdentification.CallerIdenf_DebitCardDetails;
import com.servion.model.callerIdentification.CallerIdentification_HostRes;
import com.servion.model.chequeBookRequest.CheckForChequeBookRequest_DBRes;
import com.servion.model.chequeBookRequest.UpdateChequeBookOrder_HostRes;
import com.servion.model.chequeBookRequest.UpdateChequeBookRequest_DBRes;
import com.servion.model.complaintAlert.CheckComplaintID_HostRes;
import com.servion.model.creditCardBalance.CreditCardBalanceDetails_HostRes;
import com.servion.model.creditCardBalance.CreditCardGroupInq_HostRes;
import com.servion.model.creditCardPayment.CCPayment_BeneficiaryDetailList_HostRes;
import com.servion.model.creditCardPayment.CCPayment_BenfPayeeDetails_HostRes;
import com.servion.model.creditCardPayment.CreditCardDetails_HostRes;
import com.servion.model.creditCardPayment.UpdCCPaymentDetails_HostRes;
import com.servion.model.creditCardPayment.UpdateCreditCardPaymenTxnPosttDetails_HostRes;
import com.servion.model.debitCardActivation.ActivateCard_HostRes;
import com.servion.model.enterAmount.PerDayPerTransLimit_DBRes;
import com.servion.model.enterAmount.UpdateEnterAmount_DBRes;
import com.servion.model.exchangeRates.ExchangeRateDetails_DBRes;
import com.servion.model.exchangeRates.ExchangeRateInquiry_HostRes;
import com.servion.model.exchangeRates.UpdateExchangeRateCurr_DBRes;
import com.servion.model.fax.LoggingFaxRequest_HostRes;
import com.servion.model.fetchCardServiceHistory.FetchCardServiceHistory_HostRes;
import com.servion.model.fixedDepositBalance.FixedDepositBalance_HostRes;
import com.servion.model.fixedDepositRates.FixedDepositsRateDetails_DBRes;
import com.servion.model.forms.StaticFormsDetails_DBRes;
import com.servion.model.forms.UpdateFormRequest_DBRes;
import com.servion.model.fundsTransfer.FT_BeneficiaryDetailList_HostRes;
import com.servion.model.fundsTransfer.FT_BenfPayeeDetails_HostRes;
import com.servion.model.fundsTransfer.FT_ExchangeRateDetails_HostRes;
import com.servion.model.fundsTransfer.UpdateFTIntraPayment_HostRes;
import com.servion.model.fundsTransfer.UpdateFTRemittPayment_HostRes;
import com.servion.model.fundsTransfer.UpdateFTUtilityPaymentCharity_HostRes;
import com.servion.model.getCCCustDtls.GetCCCustDtls_HostRes;
import com.servion.model.getDCCustDtls.GetDCCustDtls_HostRes;
import com.servion.model.keyExAuth.KeyExAuth_HostRes;
import com.servion.model.loanBalance.LoanBalanceDetails_HostRes;
import com.servion.model.mobileNumberChange.MobileNumberChange_HostRes;
import com.servion.model.otpGeneration.LoggingGeneratedOTP_DBRes;
import com.servion.model.otpValidation.ValidOTPDetails_DBRes;
import com.servion.model.payeeRegistration.BeneficiaryRegistration_HostRes;
import com.servion.model.promotions.PromotionDetails_DBRes;
import com.servion.model.promotions.UpdatePromotionOffered_DBRes;
import com.servion.model.rapCTI.GetData_HostRes;
import com.servion.model.rapCTI.SetData_HostRes;
import com.servion.model.recentTransactionBank.RecentTransactionBank_HostRes;
import com.servion.model.recentTransactionCards.RecentTransactionCards_HostRes;
import com.servion.model.reportLostCard.LostStolenCard_HostRes;
import com.servion.model.reporting.AnnounceReportDetails;
import com.servion.model.reporting.FeatureReportDetails;
import com.servion.model.reporting.HostReportDetails;
import com.servion.model.reporting.MenuReportDetails;
import com.servion.model.sendSMS.SendingSMS_HostRes;
import com.servion.model.tenureAlert.CustomerDetails_DBRes;
import com.servion.model.tenureAlert.UpdateTenureAnncStatus_DBRes;
import com.servion.model.thirdPartyRemittance.TPR_BenfPayeeDetails_HostRes;
import com.servion.model.thirdPartyRemittance.TPR_ExchangeRateDetails_HostRes;
import com.servion.model.thirdPartyRemittance.TPR_RetrieveBenfPayeeList_HostRes;
import com.servion.model.thirdPartyRemittance.TPR_UpdatePaymentDetails_HostRes;
import com.servion.model.transactionDetailBank.LoggingEmailRequestBank_DBRes;
import com.servion.model.transactionDetailBank.TransactionDetailsBank_HostRes;
import com.servion.model.transactionDetaitCards.LoggingEmailRequestCards_DBRes;
import com.servion.model.transactionDetaitCards.TransactionDetailCards_HostReq;
import com.servion.model.transferOnAbandon.UpdateUserCallBackOption_DBRes;
import com.servion.model.transferToAgent.LogMobileNumber_DBRes;
import com.servion.model.updatePin.UpdatePIN_HostRes;
import com.servion.promotionAlert.Alert_PromotionDetails_DBRes;


/**
 * CallData represents a call session and is a common entity for all DD modules.
 * It is a container for all information required during a call session to enable DD and BL collaboration.
 * Fields are stored in a Map and values can be set using setField(key, value) method and retrieved using getField(key).
 * 
 * @author Servion Global Solution
 *
 */
public class CallInfoImpl implements CallInfo {



	/**
	 * This fieldMap variable will contain a HashMap used for global purposes
	 * 
	 * This Map will always contains the key as Field entity and value as any string
	 * 
	 * This is mainly used to have the value retried from ICE configurable parameter
	 */
	private Map<Field, Object> fields;

	private IvrData ivrData;
	private AnnounceReportDetails announceReportDetails;
	private FeatureReportDetails featureReportDetails;
	private HostReportDetails hostReportDetails;
	private MenuReportDetails menuReportDetails;

	private ICERuleParam iceRuleParam;
	private ICEGlobalConfig iceGlobalConfig;
	private ICEFeatureData iceFeatureData;
	private ICECallData iceCallData;

	private String announcePath;
	private String menuPath;
	private String featurePath;
	private String callPath;

	//Module specific objects
	private PromotionDetails_DBRes promotionDetails_DBRes;
	private UpdatePromotionOffered_DBRes updatePromotionOffered_DBRes;
	private LostStolenCard_HostRes lostStolenCard_HostRes;
	private ActivateCard_HostRes activateCard_HostRes;
	private AccountBalance_HostRes accountBalance_HostRes;
	private CallerIdentification_HostRes callerIdentification_HostRes;
	private CallerIdenf_DebitCardDetails callerIdenf_DebitCardDetails;
	private ValidatePIN_HostRes validatePIN_HostRes;
	private UpdatePIN_HostRes updatePIN_HostRes;
	private APINCustomerProfileDetails_HostRes aPINCustomerProfileDetails_HostRes;
	private BankingBalanceFlashDetails_HostRes bankingBalanceFlashDetails_HostRes;
	private Utility_BenfPayeeDetails_HostRes utility_BenfPayeeDetails_HostRes;
	private UpdateMobileNoDetails_DBRes updateMobileNoDetails_DBRes;
	private UpdatePaymentDetails_HostRes updatePaymentDetails_HostRes;
	private UpdateBirthdayAnncStatus_DBRes updateBirthdayAnncStatus_DBRes;
	private CheckForChequeBookRequest_DBRes checkForChequeBookRequest_DBRes;
	private UpdateChequeBookOrder_HostRes updateChequeBookOrder_HostRes;
	private UpdateChequeBookRequest_DBRes updateChequeBookRequest_DBRes;
	private CheckComplaintID_HostRes checkComplaintID_HostRes;
	private CreditCardBalanceDetails_HostRes creditCardBalanceDetails_HostRes;
	private CCPayment_BenfPayeeDetails_HostRes ccPayment_BenfPayeeDetails_HostRes;
	private CreditCardDetails_HostRes creditCardDetails_HostRes;
	private UpdateCreditCardPaymenTxnPosttDetails_HostRes updateCreditCardPaymenTxnPosttDetails_HostRes;
	private PerDayPerTransLimit_DBRes perDayPerTransLimit_DBRes;
	private UpdateEnterAmount_DBRes updateEnterAmount_DBRes;
	private ExchangeRateInquiry_HostRes exchangeRateInquiry_HostRes;
	private ExchangeRateDetails_DBRes exchangeRateDetails_DBRes;
	private UpdateExchangeRateCurr_DBRes updateExchangeRateCurr_DBRes;
	private LoggingFaxRequest_HostRes loggingFaxRequest_HostRes;
	private FixedDepositBalance_HostRes fixedDepositBalance_HostRes;
	private FixedDepositsRateDetails_DBRes fixedDepositsRateDetails_DBRes;
	private StaticFormsDetails_DBRes staticFormsDetails_DBRes;
	private UpdateFormRequest_DBRes updateFormRequest_DBRes;
	private FT_BenfPayeeDetails_HostRes ft_BenfPayeeDetails_HostRes;
	private FT_ExchangeRateDetails_HostRes ft_ExchangeRateDetails_HostRes;
	private UpdateFTIntraPayment_HostRes updateFTIntraPayment_HostRes;
	private UpdateFTRemittPayment_HostRes updateFTRemittPayment_HostRes;
	private UpdateFTUtilityPaymentCharity_HostRes updateFTUtilityPaymentCharity_HostRes;

	private LoanBalanceDetails_HostRes LoanBalanceDetails_HostRes;
	private LoggingGeneratedOTP_DBRes loggingGeneratedOTP_DBRes;
	private ValidOTPDetails_DBRes validOTPDetails_DBRes;
	private BeneficiaryRegistration_HostRes beneficiaryRegistration_HostRes;
	private RecentTransactionBank_HostRes recentTransactionBank_HostRes;
	private RecentTransactionCards_HostRes recentTransactionCards_HostRes;
	private SendingSMS_HostRes sendingSMS_HostRes;
	private CustomerDetails_DBRes customerDetails_DBRes;
	private UpdateTenureAnncStatus_DBRes updateTenureAnncStatus_DBRes;
	private TPR_ExchangeRateDetails_HostRes tpr_ExchangeRateDetails_HostRes;
	private TPR_RetrieveBenfPayeeList_HostRes tpr_RetrieveBenfPayeeList_HostRes;
	private TPR_UpdatePaymentDetails_HostRes tpr_UpdatePaymentDetails_HostRes;
	private TransactionDetailsBank_HostRes transactionDetailsBank_HostRes;
	private LoggingEmailRequestBank_DBRes loggingEmailRequestBank_DBRes;
	private TransactionDetailCards_HostReq transactionDetailCards_HostReq;
	private LoggingEmailRequestCards_DBRes loggingEmailRequestCards_DBRes;
	private UpdateUserCallBackOption_DBRes updateUserCallBackOption_DBRes;
	private LogMobileNumber_DBRes logMobileNumber_DBRes;
	private Alert_PromotionDetails_DBRes alert_PromotionDetails_DBRes;
	private MobileNumberChange_HostRes mobileNumberChange_HostRes;


	private KeyExAuth_HostRes keyExAuth_HostRes;
	private GetData_HostRes getData_HostRes;
	private SetData_HostRes setData_HostRes;

	private FT_BeneficiaryDetailList_HostRes ft_BeneficiaryDetailList_HostRes;

	private CreditCardGroupInq_HostRes creditCardGroupInq_HostRes;
	private FetchCardServiceHistory_HostRes fetchCardServiceHistory_HostRes;
	private UpdCCPaymentDetails_HostRes updCCPaymentDetails_HostRes;
	private Utility_BeneficiaryDetailList_HostRes utility_BeneficiaryDetailList_HostRes;
	private CCPayment_BeneficiaryDetailList_HostRes ccPayment_BeneficiaryDetailList_HostRe;

	private UpdatePaymentDetailsCC_HostRes updatePaymentDetailsCC_HostRes;
	
	private GetCCCustDtls_HostRes getCCCustDtls_HostRes;
	private GetDCCustDtls_HostRes getDCCustDtls_HostRes;
	private CustDtls_HostRes custDtls_HostRes;
	/**
	 * Assigning logger object from LoggerObject util class
	 */
	private Logger logger = LoggerObject.getLogger();


	/**
	 * For ICE Configuration Parameters
	 */

	private IVR_ICECallData ivr_ICECallData;
	private IVR_ICEFeatureData ivr_ICEFeatureData;
	private IVR_ICEGlobalConfig ivr_ICEGlobalConfig;
	private IVR_ICERuleParam ivr_ICERuleParam;


	private TPR_BenfPayeeDetails_HostRes TPR_BenfPayeeDetails_HostRes;


	private TelecomPostpaidBalanceDetails_HostRes TelecomPostpaidBalanceDetails_HostRes;
	private TelecomSubscriberInfo_HostRes TelecomSubscriberInfo_HostRes;
	
	private UtilitySubscriberInfo_HostRes utilitySubscriberInfo_HostRes;
	
	private GetUtilityBillInfo_HostRes getUtilityBillInfo_HostRes;
	
	
	
	/**
	 * Following is for NEW CRs
	 */
	
	private TelecomCustomerInfo_HostRes telecomCustomerInfo_HostRes;
	private TelecomPrepaidNumberVal_HostRes telecomPrepaidNumberVal_HostRes;
	

	public TelecomCustomerInfo_HostRes getTelecomCustomerInfo_HostRes() {
		return telecomCustomerInfo_HostRes;
	}

	public void setTelecomCustomerInfo_HostRes(
			TelecomCustomerInfo_HostRes telecomCustomerInfo_HostRes) {
		this.telecomCustomerInfo_HostRes = telecomCustomerInfo_HostRes;
	}

	public TelecomPrepaidNumberVal_HostRes getTelecomPrepaidNumberVal_HostRes() {
		return telecomPrepaidNumberVal_HostRes;
	}

	public void setTelecomPrepaidNumberVal_HostRes(
			TelecomPrepaidNumberVal_HostRes telecomPrepaidNumberVal_HostRes) {
		this.telecomPrepaidNumberVal_HostRes = telecomPrepaidNumberVal_HostRes;
	}

	//END for New CRs
	
	
	/**
	 * Returns an instance of CallInfo. fields, MenuIDFields, AnncIDFields, FeatureIDFields  map will be initialized with a new HashMap instance.
	 */
	public CallInfoImpl() {
		super();
		initFieldMap();
	}

	private void initFieldMap() {
		this.fields = new HashMap<Field, Object>();
		this.fields.put(Field.SERVICE_EXCEPTION, Boolean.FALSE.toString());
		this.fields.put(Field.LOGGER, LoggerObject.getLogger());
		this.fields.put(Field.SESSIONID, LoggerObject.getSessionId());
	}

	public void setField(Field field, Object value) {
		String session_ID = Constants.EMPTY;
		
		try{
			
			session_ID = (String)this.fields.get(Field.SESSIONID);
			
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) getICEGlobalConfig();

			if(ivr_ICEGlobalConfig==null){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"ICEGlonbal config is not yet initialized");};	
			}else{

				String fieldsNotToLog = util.isNullOrEmpty((String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_FIELDS_NEEDTOBE_SKIPPED_FROM_LOG))?"APIN" : (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_FIELDS_NEEDTOBE_SKIPPED_FROM_LOG);

				if(!util.isCodePresentInTheConfigurationList(field.name(), fieldsNotToLog)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"### Setting Field [" + field.name() + "] = " + value);}
				}
			}

			this.fields.put(field, value);
			
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeUtil(WriteLog.DEBUG,"exception while setting the field" + field.name());}
		}
	}

	public Object getField(Field field) {
		Object sValue = this.fields.get(field);
		
		String session_ID = Constants.EMPTY;
		
		try{
			
			session_ID = (String)this.fields.get(Field.SESSIONID);
			
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) getICEGlobalConfig();

			if(ivr_ICEGlobalConfig==null){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"ICEGlonbal config is not yet initialized");};	
			}else{
				String fieldsNotToLog = util.isNullOrEmpty((String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_FIELDS_NEEDTOBE_SKIPPED_FROM_LOG))?"APIN" : (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_FIELDS_NEEDTOBE_SKIPPED_FROM_LOG);

				if(!util.isCodePresentInTheConfigurationList(field.name(), fieldsNotToLog)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"### Getting Field [" + field.name() + "] = " + sValue);};
				}
			}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"exception while setting the field" + field.name());}
		}

		return sValue;
	}

	/**
	 * @param Bean for IVRData
	 * 
	 */
	public void setIVRData(IvrData ivrData){
		this.ivrData = ivrData;
		insertCallInfo(ivrData);
	}

	public IvrData getIVRData(){
		return ivrData;
	}

	/**
	 * @param Bean for AnnounceReportDetails
	 * 
	 */

	public AnnounceReportDetails getAnnounceReportDetails() {
		return announceReportDetails;
	}

	public void setAnnounceReportDetails(AnnounceReportDetails announceReportDetails) {
		this.announceReportDetails = announceReportDetails;
	}

	/**
	 * @param Bean for FeatureReportDetails
	 * 
	 */

	public FeatureReportDetails getFeatureReportDetails() {
		return featureReportDetails;
	}

	public void setFeatureReportDetails(FeatureReportDetails featureReportDetails) {
		this.featureReportDetails = featureReportDetails;
	}

	/**
	 * @param Bean for HostReportDetails
	 * 
	 */

	public HostReportDetails getHostReportDetails() {
		return hostReportDetails;
	}

	public void setHostReportDetails(HostReportDetails hostReportDetails) {
		this.hostReportDetails = hostReportDetails;
	}


	/**
	 * @param Bean for MenuReportDetails
	 * 
	 */
	public MenuReportDetails getMenuReportDetails() {
		return menuReportDetails;
	}

	public void setMenuReportDetails(MenuReportDetails menuReportDetails) {
		this.menuReportDetails = menuReportDetails;
	}


	public void insertAnnounceDetails(IvrData ivrData){
		try{
			logger = (Logger)getField(Field.LOGGER);
		}catch(Exception e){}
		
		//It will obtained from OD
		AnnounceReportDetails announceReportDetailsObj = (AnnounceReportDetails)getAnnounceReportDetails();

		//Though it is set from OD..Report logger class will intialze this class
		Announce announceObj = ivrData.getAnnounce();

		announceObj.setAnnounceEndtime(announceReportDetailsObj.getAnnounceEndTime());
		announceObj.setAnnounceID(announceReportDetailsObj.getAnnounceID(), ivrData.getCallInfo());
		announceObj.setAnnounceStarttime(announceReportDetailsObj.getAnnounceStartTime());
		announceObj.setFeatureID(announceReportDetailsObj.getFeatureID());


		//TODO 
		//		Announce_Reserve1, 2, 3, 4 are yet to be defined.

		//Setting in IVRDATA object
		ivrData.addAnnounceDetails(announceObj);

		//for Announce Path
		//		String report_AnnouncePath = getAnnouncePath();
		//			if(report_AnnouncePath==null){
		//				report_AnnouncePath = "";
		//			}
		//			report_AnnouncePath = announceReportDetailsObj.getAnnounceID();
		//			report_AnnouncePath = report_AnnouncePath.concat("\\|");
		//	
		//			
		//			setAnnouncePath(report_AnnouncePath);

		//Need to set the value again in the session variable
		//session
	}



	public void insertFeatureDetails(IvrData ivrData){
		try{
			logger = (Logger)getField(Field.LOGGER);
		}catch(Exception e){}
		
		//It will obtained from OD
		FeatureReportDetails featureReportDetailsObj = (FeatureReportDetails) getFeatureReportDetails();

		//Though it is set from OD..Report logger class will intialze this class
		Feature featureObj=ivrData.getFeature();

		featureObj.setStarttime(featureReportDetailsObj.getFeatureStartTime());
		featureObj.setEndtime(featureReportDetailsObj.getFeatureEndTime());
		featureObj.setFailureReason(featureReportDetailsObj.getFailureReason());
		featureObj.setFeatureID(featureReportDetailsObj.getFeatureID(), ivrData.getCallInfo());
		featureObj.setFeatureReserve1(featureReportDetailsObj.getFeatureReserve1());
		featureObj.setFeatureReserve2(featureReportDetailsObj.getFeatureReserve2());
		featureObj.setFeatureReserve3(featureReportDetailsObj.getFeatureReserve3());
		featureObj.setFeatureReserve4(featureReportDetailsObj.getFeatureReserve4());
		featureObj.setResult(featureReportDetailsObj.getResult());

		//Setting in IVRDATA object
		ivrData.addFeatureDetails(featureObj);

		//for Announce Path
		//		String report_FeaturePath = getFeaturePath();
		//			if(report_FeaturePath==null){
		//				report_FeaturePath = "";
		//			}
		//			report_FeaturePath = featureReportDetailsObj.getFeatureID();
		//			report_FeaturePath = report_FeaturePath.concat("\\|");
		//	
		//			setFeaturePath(report_FeaturePath);
		//Need to set the value again in the session variable
		//session
	}


	/**
	 * Following are the changes done to insert the feature report though we disconnected the calls before a feature ends
	 */

	public void updateFeatureDetails(IvrData ivrData){
		try{
			logger = (Logger)getField(Field.LOGGER);
		}catch(Exception e){}
		
		//It will obtained from OD
		FeatureReportDetails featureReportDetailsObj = (FeatureReportDetails) getFeatureReportDetails();

		//Though it is set from OD..Report logger class will intialze this class
		Feature featureObj=ivrData.getFeature();

		featureObj.setStarttime(featureReportDetailsObj.getFeatureStartTime());
		featureObj.setEndtime(featureReportDetailsObj.getFeatureEndTime());
		featureObj.setFailureReason(featureReportDetailsObj.getFailureReason());
		featureObj.setFeatureID(featureReportDetailsObj.getFeatureID(), ivrData.getCallInfo());
		featureObj.setFeatureReserve1(featureReportDetailsObj.getFeatureReserve1());
		featureObj.setFeatureReserve2(featureReportDetailsObj.getFeatureReserve2());
		featureObj.setFeatureReserve3(featureReportDetailsObj.getFeatureReserve3());
		featureObj.setFeatureReserve4(featureReportDetailsObj.getFeatureReserve4());
		featureObj.setResult(featureReportDetailsObj.getResult());

		//Setting in IVRDATA object
		ivrData.updateFeatureDetails(featureObj);

		//for Announce Path
		//		String report_FeaturePath = getFeaturePath();
		//			if(report_FeaturePath==null){
		//				report_FeaturePath = "";
		//			}
		//			report_FeaturePath = featureReportDetailsObj.getFeatureID();
		//			report_FeaturePath = report_FeaturePath.concat("\\|");
		//	
		//			setFeaturePath(report_FeaturePath);
		//Need to set the value again in the session variable
		//session
	}



	public 	void insertHostDetails(IvrData ivrData){
		try{
			logger = (Logger)getField(Field.LOGGER);
		}catch(Exception e){}
		
		//It will obtained from OD
		HostReportDetails hostReportDetailsObj = (HostReportDetails)getHostReportDetails();

		//Though it is set from OD..Report logger class will intialze this class
		Host hostObj = ivrData.getHost();

		hostObj.setHostStarttime(hostReportDetailsObj.getHostStartTime());

		byte hostCounter= util.isNullOrEmpty(hostReportDetailsObj.getHostCounter())?Constants.GL_MINUS_ONE:Byte.parseByte(hostReportDetailsObj.getHostCounter());
		hostObj.setHostCounter(hostCounter);

		hostObj.setHostEndtime(hostReportDetailsObj.getHostEndTime());
		hostObj.setHostInparams(hostReportDetailsObj.getHostInParams());
		hostObj.setHostMethod(hostReportDetailsObj.getHostMethod());
		hostObj.setHostOutparams(hostReportDetailsObj.getHostOutParams());
		hostObj.setHostReserve1(hostReportDetailsObj.getHostReserve1());
		hostObj.setHostReserve2(hostReportDetailsObj.getHostReserve2());
		hostObj.setHostReserve3(hostReportDetailsObj.getHostReserve3());
		hostObj.setHostReserve4(hostReportDetailsObj.getHostReserve4());

		//byte hostResponseByte = util.isNullOrEmpty(hostReportDetailsObj.getHostResponse())?Constants.GL_MINUS_ONE:Byte.parseByte(hostReportDetailsObj.getHostResponse());
		//byte hostResponseByte = 0;
		//hostObj.setHostResponse(hostResponseByte);

		String hostResponseByte = hostReportDetailsObj.getHostResponse();
		hostObj.setHostResponse(hostResponseByte);
		hostObj.setHostType(hostReportDetailsObj.getHostType());

		String featureID = util.isNullOrEmpty(getField(Field.FEATUREID))? Constants.EMPTY : (String)getField(Field.FEATUREID);
		String featureStartTime = util.isNullOrEmpty(getField(Field.FEATURESTARTTIME)) ? Constants.EMPTY : (String)getField(Field.FEATURESTARTTIME);

//		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,"###  The Feature id setted for host is " + featureID);}
//		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,"### The Feature start time is " + featureStartTime);}

		hostObj.setFeatureID(featureID);
		hostObj.setFeatureIDStarttime(featureStartTime);

		//Setting in IVRDATA object
		ivrData.addHostDetails(hostObj);

		//for Announce Path
		//		String report_MenuPath = get();
		//			if(report_AnnouncePath==null){
		//				report_AnnouncePath = "";
		//			}
		//			report_AnnouncePath = hostReportDetailsObj.getHostType();
		//			report_AnnouncePath = report_AnnouncePath.concat("\\|");
		//	
		//			
		//			setAnnouncePath(report_AnnouncePath);

		//Need to set the value again in the session variable
		//session
	}




	public void updateHostDetails(IvrData ivrData){
		try{
			logger = (Logger)getField(Field.LOGGER);
		}catch(Exception e){}
		
		//It will obtained from OD
		HostReportDetails hostReportDetailsObj = (HostReportDetails)getHostReportDetails();

		//Though it is set from OD..Report logger class will intialze this class
		Host hostObj = ivrData.getHost();

		hostObj.setHostStarttime(hostReportDetailsObj.getHostStartTime());

		byte hostCounter= util.isNullOrEmpty(hostReportDetailsObj.getHostCounter())?Constants.GL_MINUS_ONE:Byte.parseByte(hostReportDetailsObj.getHostCounter());
		hostObj.setHostCounter(hostCounter);

		hostObj.setHostEndtime(hostReportDetailsObj.getHostEndTime());
		hostObj.setHostInparams(hostReportDetailsObj.getHostInParams());
		hostObj.setHostMethod(hostReportDetailsObj.getHostMethod());
		hostObj.setHostOutparams(hostReportDetailsObj.getHostOutParams());
		hostObj.setHostReserve1(hostReportDetailsObj.getHostReserve1());
		hostObj.setHostReserve2(hostReportDetailsObj.getHostReserve2());
		hostObj.setHostReserve3(hostReportDetailsObj.getHostReserve3());
		hostObj.setHostReserve4(hostReportDetailsObj.getHostReserve4());

		//byte hostResponseByte = util.isNullOrEmpty(hostReportDetailsObj.getHostResponse())?Constants.GL_MINUS_ONE:Byte.parseByte(hostReportDetailsObj.getHostResponse());
		//byte hostResponseByte = 0;
		//hostObj.setHostResponse(hostResponseByte);

		String hostResponseByte = hostReportDetailsObj.getHostResponse();
		hostObj.setHostResponse(hostResponseByte);
		hostObj.setHostType(hostReportDetailsObj.getHostType());

		String featureID = util.isNullOrEmpty(getField(Field.FEATUREID))? Constants.EMPTY : (String)getField(Field.FEATUREID);
		String featureStartTime = util.isNullOrEmpty(getField(Field.FEATURESTARTTIME)) ? Constants.EMPTY : (String)getField(Field.FEATURESTARTTIME);

//		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,"###  Updating the Feature id setted for host is " + featureID);}
//		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,"### Updating the Feature start time is " + featureStartTime);}

		hostObj.setFeatureID(featureID);
		hostObj.setFeatureIDStarttime(featureStartTime);

		//Setting in IVRDATA object
		ivrData.updateHostDetails(hostObj);

		//for Announce Path
		//		String report_MenuPath = get();
		//			if(report_AnnouncePath==null){
		//				report_AnnouncePath = "";
		//			}
		//			report_AnnouncePath = hostReportDetailsObj.getHostType();
		//			report_AnnouncePath = report_AnnouncePath.concat("\\|");
		//	
		//			
		//			setAnnouncePath(report_AnnouncePath);

		//Need to set the value again in the session variable
		//session

	}

	public void insertMenuDetails(IvrData ivrData){
		String session_ID = Constants.EMPTY;
		try{
			logger = (Logger)getField(Field.LOGGER);
			session_ID = (String)getField(Field.SESSIONID);
		}catch(Exception e){}
		
		//It will obtained from OD
		MenuReportDetails menuReportDetailsObj = (MenuReportDetails)getMenuReportDetails();

		Menu menuObj = ivrData.getMenu();

		menuObj.setMenuStarttime(menuReportDetailsObj.getMenuStartTime());
		menuObj.setMenuEndtime(menuReportDetailsObj.getMenuEndTime());
		menuObj.setMenuID(menuReportDetailsObj.getMenuID());

		String menuOption = util.isNullOrEmpty(menuReportDetailsObj.getMenuOption())?Constants.EMPTY_STRING:menuReportDetailsObj.getMenuOption();

		if(MenuIDMap.getMenuID("CALLER_IDENTIFICATION").equalsIgnoreCase(menuReportDetailsObj.getMenuID())){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"### Its for Caller Identification Menu is " + menuReportDetailsObj.getMenuID());}
			menuOption = Constants.MASKING_START + util.getSubstring(menuOption, Constants.GL_FOUR);
		}

		if(MenuIDMap.getMenuID("APIN_VALIDATION").equalsIgnoreCase(menuReportDetailsObj.getMenuID())){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"### Its for APIN Validation Menu is " + menuReportDetailsObj.getMenuID());}
			menuOption = Constants.EMPTY_STRING;
		}
		menuObj.setMenuOption(menuOption);

		String menuOptionDesc = menuReportDetailsObj.getMenuOptionDesc();
		if(MenuIDMap.getMenuID("CALLER_IDENTIFICATION").equalsIgnoreCase(menuReportDetailsObj.getMenuID())){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"### Its for Caller Identification Menu is " + menuReportDetailsObj.getMenuID());}
			menuOptionDesc = Constants.MASKING_START + util.getSubstring(menuOptionDesc, Constants.GL_FOUR);
		}

		if(MenuIDMap.getMenuID("APIN_VALIDATION").equalsIgnoreCase(menuReportDetailsObj.getMenuID())){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"### Its for APIN Validation Menu is " + menuReportDetailsObj.getMenuID());}
			menuOptionDesc = Constants.EMPTY_STRING;
		}
		menuObj.setMenuOptionDesc(menuReportDetailsObj.getMenuOptionDesc());
		menuObj.setMenuReserve1(menuReportDetailsObj.getMenuReserve1());
		menuObj.setMenuReserve2(menuReportDetailsObj.getMenuReserve2());
		menuObj.setMenuReserve3(menuReportDetailsObj.getMenuReserve3());
		menuObj.setMenuReserve4(menuReportDetailsObj.getMenuReserve4());

		String menuIsTransferred = "N";
		if(menuReportDetailsObj.isTransfered()){
			menuIsTransferred = "Y";
		}
		menuObj.setTransfered(menuIsTransferred);

		//Setting in IVRDATA object
		ivrData.addMenuDetails(menuObj);

		//for Announce Path
		//		String report_MenuPath = getMenuPath();
		//			if(report_MenuPath==null){
		//				report_MenuPath = "";
		//			}
		//			report_MenuPath = menuReportDetailsObj.getMenuID();
		//			report_MenuPath = report_MenuPath.concat("\\|");
		//	
		//			setMenuPath(report_MenuPath);
		//			
		//Need to set the value again in the session variable
		//session
	}

	/**
	 * This method is used to insert the whole call report
	 */
	public void insertCallInfo(IvrData ivrData){
		
		String session_ID = Constants.EMPTY;
		try{
			logger = (Logger)getField(Field.LOGGER);
			session_ID = (String)getField(Field.SESSIONID);
		}catch(Exception e){}
		
		
		//code for insertCallInfo
		com.reportbean.IvrData.CallInfo report_CallInfo = (com.reportbean.IvrData.CallInfo) ivrData.getCallInfo();

		String language = util.isNullOrEmpty(getField(Field.LANGUAGE))?Constants.EMPTY : (String)getField(Field.LANGUAGE);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"### language is " + language);}
		report_CallInfo.setLanguage(language);

		String customerID= util.isNullOrEmpty(getField(Field.CUSTOMERID))?Constants.EMPTY : (String) getField(Field.CUSTOMERID);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"### Cutomer ID is " + customerID);}
		report_CallInfo.setCIReserve1(customerID);

		String cin = util.isNullOrEmpty(getField(Field.CIN))?Constants.EMPTY : (String)getField(Field.CIN);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"### CIN is " + util.maskCardOrAccountNumber(cin));}
		cin = util.maskCardOrAccountNumber(cin);
		report_CallInfo.setCIReserve2(cin);

		String cin_Type = util.isNullOrEmpty(getField(Field.CIN_TYPE))?Constants.EMPTY : (String)getField(Field.CIN_TYPE);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"### CIN TYPE is " + cin_Type);}
		report_CallInfo.setCIReserve3(cin_Type);

		String apin_Status = util.isNullOrEmpty(getField(Field.APIN_STATUS))?Constants.EMPTY : (String)getField(Field.APIN_STATUS);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"### APIN Status is " + apin_Status);}
		report_CallInfo.setCIReserve4(apin_Status);

		boolean caller_Identified = util.isNullOrEmpty(getField(Field.ISCALLERIDENTIFIED))? false : (boolean)getField(Field.ISCALLERIDENTIFIED);
		String str_CallerIdentified = Constants.EMPTY_STRING;
		if(caller_Identified){
			str_CallerIdentified = Constants.Y;
		}else{
			str_CallerIdentified = Constants.N;
		}
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"### caller_Identified is " + caller_Identified);}
		report_CallInfo.setCIReserve6(str_CallerIdentified);

		boolean apin_Validated = util.isNullOrEmpty(getField(Field.APIN_VALIDATED))? false : (boolean)getField(Field.APIN_VALIDATED);
		String str_ApinValidated = Constants.EMPTY_STRING;
		if(apin_Validated){
			str_ApinValidated = Constants.Y;
		}else{
			str_ApinValidated = Constants.N;
		}

		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"### apin_Validated is " + apin_Validated);}
		report_CallInfo.setCIReserve7(str_ApinValidated);

		String reg_FaxNo = util.isNullOrEmpty(getField(Field.REG_FAXNO)) ? Constants.EMPTY : (String)getField(Field.REG_FAXNO);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"### reg_FaxNo is " + reg_FaxNo);}
		report_CallInfo.setCIReserve8(reg_FaxNo);

		String reg_Mobile = util.isNullOrEmpty(getField(Field.REG_MOBILENO))? Constants.EMPTY : (String)getField(Field.REG_MOBILENO);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"### reg_Mobile is " + reg_Mobile);}
		report_CallInfo.setCIReserve9(reg_Mobile);

		String reg_Email = util.isNullOrEmpty(getField(Field.REG_EMAIL))? Constants.EMPTY : (String)getField(Field.REG_EMAIL);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"### reg_Email is " + reg_Email);}
		report_CallInfo.setCIReserve10(reg_Email);


		//APIN Tries count included on 12-May-2014
		String apin_Tries = util.isNullOrEmpty(getField(Field.APIN_TRIES))? Constants.EMPTY : (String)getField(Field.APIN_TRIES);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"### APIN_TRIES is " + apin_Tries);}
		report_CallInfo.setCIReserve14(apin_Tries);


		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"### Call Disposition is " + getField(Field.CALL_DISPOSITION));}
		report_CallInfo.setCallDisposition(Constants.EMPTY + getField(Field.CALL_DISPOSITION));

		String dnisType = util.isNullOrEmpty(getField(Field.CUST_SEGMENT_TYPE))?Constants.EMPTY : (String)getField(Field.CUST_SEGMENT_TYPE);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"### DNIS type is " + dnisType);}
		report_CallInfo.setDnisType(dnisType);

		String callID = util.isNullOrEmpty(getField(Field.UUI))?Constants.EMPTY : (String)getField(Field.UUI);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"### callID is " + callID);}
		report_CallInfo.setCallID(callID);

		report_CallInfo.setApp_ID(Constants.ONE);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"### APP ID is " + Constants.ONE);}

		//		report_CallInfo.setAnnouncePath(getAnnouncePath());
		//		report_CallInfo.setMenuPath(getMenuPath());
	}


	/**
	 * Following are the method used to get and set the values of ICE objects
	 * 
	 * OD will use only the setter to set the object.  BI layer will use  the getter method and set all
	 * the relevant FIELD's enum variables by using insert methods
	 */

	public ICECallData getICECallData(){
		return iceCallData;
	}

	public void setICECallData(ICECallData iceCallData){
		this.iceCallData = iceCallData;
		String session_ID = Constants.EMPTY;
		try{
			logger = (Logger)getField(Field.LOGGER);
			session_ID = (String)getField(Field.SESSIONID);
		}catch(Exception e){}
		
		
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"### Setting iceCallData object to Field [" + (Field.ICECallData + "] = " + iceCallData));}
		this.fields.put(Field.ICECallData, iceCallData);

	}


	public ICEFeatureData getICEFeatureData(){
		return iceFeatureData;
	}

	public void setICEFeatureData(ICEFeatureData iceFeatureData){
		this.iceFeatureData = iceFeatureData;
		String session_ID = Constants.EMPTY;
		try{
			logger = (Logger)getField(Field.LOGGER);
			session_ID = (String)getField(Field.SESSIONID);
		}catch(Exception e){}
		
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"### Setting iceFeatureData object to Field [" + (Field.ICEFeatureData + "] = " + iceFeatureData));}
		this.fields.put(Field.ICEFeatureData, iceFeatureData);
	}


	public ICEGlobalConfig getICEGlobalConfig(){
		return iceGlobalConfig;
	}

	public void setICEGlobalConfig(ICEGlobalConfig iceGlobalConfig){
		this.iceGlobalConfig = iceGlobalConfig;
		String session_ID = Constants.EMPTY;
		try{
			logger = (Logger)getField(Field.LOGGER);
			session_ID = (String)getField(Field.SESSIONID);
		}catch(Exception e){}
		
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"### Setting iceGlobalConfig object to Field [" + (Field.ICEGlobalConfig + "] = " + iceGlobalConfig));}
		this.fields.put(Field.ICEGlobalConfig, iceGlobalConfig);
	}


	public ICERuleParam getICERuleParam(){
		return iceRuleParam;
	}

	public void setICERuleParam(ICERuleParam iceRuleParam){
		this.iceRuleParam = iceRuleParam;
		String session_ID = Constants.EMPTY;
		try{
			logger = (Logger)getField(Field.LOGGER);
			session_ID = (String)getField(Field.SESSIONID);
		}catch(Exception e){}

		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID,"### Setting iCERuleParam object to Field [" + (Field.ICERuleParam + "] = " + iceRuleParam));}
		this.fields.put(Field.ICERuleParam, iceRuleParam);
	}


	//inserting Method used to insert values to FIELD Enum variables
	public void insertICERuleParam(){
		//Code to insert Rule param values to Fields
	}

	public void insertICEGlobalConfig(){
		//Code to insert Global Config values to Fields
	}

	public void insertICEFeatureData(){
		//Code to insert Feature Data values to Fields
	}

	public void insertICECallData(){
		//Code to insert Call Data values to Fields
	}


	/**
	 * The following method is used to fetch the property file path from 
	 * feature config object and send to OD layer.
	 */

	public String getDMPropertyFilePath() {
		return (String)getField(Field.DMPROPERTIESLOCATION);
	}


	public String getAnnouncePath() {
		return announcePath;
	}

	public void setAnnouncePath(String announcePath) {
		this.announcePath = announcePath;
	}

	public String getMenuPath() {
		return menuPath;
	}

	public void setMenuPath(String menuPath) {
		this.menuPath = menuPath;
	}

	public String getFeaturePath() {
		return featurePath;
	}

	public void setFeaturePath(String featurePath) {
		this.featurePath = featurePath;
	}

	public String getCallPath() {
		return callPath;
	}

	public void setCallPath(String callPath) {
		this.callPath = callPath;
	}



	/**
	 * Module specific getter and setter methods
	 */

	public PromotionDetails_DBRes getPromotionDetails_DBRes() {
		return promotionDetails_DBRes;
	}

	public void setPromotionDetails_DBRes(
			PromotionDetails_DBRes promotionDetails_DBRes) {
		this.promotionDetails_DBRes = promotionDetails_DBRes;
	}

	public UpdatePromotionOffered_DBRes getUpdatePromotionOffered_DBRes() {
		return updatePromotionOffered_DBRes;
	}

	public void setUpdatePromotionOffered_DBRes(
			UpdatePromotionOffered_DBRes updatePromotionOffered_DBRes) {
		this.updatePromotionOffered_DBRes = updatePromotionOffered_DBRes;
	}

	public LostStolenCard_HostRes getLostStolenCard_HostRes() {
		return lostStolenCard_HostRes;
	}

	public void setLostStolenCard_HostRes(
			LostStolenCard_HostRes lostStolenCard_HostRes) {
		this.lostStolenCard_HostRes = lostStolenCard_HostRes;
	}

	public ActivateCard_HostRes getActivateCard_HostRes() {
		return activateCard_HostRes;
	}

	public void setActivateCard_HostRes(ActivateCard_HostRes activateCard_HostRes) {
		this.activateCard_HostRes = activateCard_HostRes;
	}

	public AccountBalance_HostRes getAccountBalance_HostRes() {
		return accountBalance_HostRes;
	}

	public void setAccountBalance_HostRes(
			AccountBalance_HostRes accountBalance_HostRes) {
		this.accountBalance_HostRes = accountBalance_HostRes;
	}

	public CallerIdentification_HostRes getCallerIdentification_HostRes() {
		return callerIdentification_HostRes;
	}

	public void setCallerIdentification_HostRes(
			CallerIdentification_HostRes callerIdentification_HostRes) {
		this.callerIdentification_HostRes = callerIdentification_HostRes;
	}


	public APINCustomerProfileDetails_HostRes getAPINCustomerProfileDetails_HostRes() {
		return aPINCustomerProfileDetails_HostRes;
	}

	public void setAPINCustomerProfileDetails_HostRes(
			APINCustomerProfileDetails_HostRes aPINCustomerProfileDetails_HostRes) {
		this.aPINCustomerProfileDetails_HostRes = aPINCustomerProfileDetails_HostRes;
	}

	public ValidatePIN_HostRes getValidatePIN_HostRes() {
		return validatePIN_HostRes;
	}

	public void setValidatePIN_HostRes(ValidatePIN_HostRes validatePIN_HostRes) {
		this.validatePIN_HostRes = validatePIN_HostRes;
	}
	
	public UpdatePIN_HostRes getUpdatePIN_HostRes() {
		return updatePIN_HostRes;
	}

	public void setUpdatePIN_HostRes(UpdatePIN_HostRes updatePIN_HostRes) {
		this.updatePIN_HostRes = updatePIN_HostRes;
	}

	public BankingBalanceFlashDetails_HostRes getBankingBalanceFlashDetails_HostRes() {
		return bankingBalanceFlashDetails_HostRes;
	}

	public void setBankingBalanceFlashDetails_HostRes(
			BankingBalanceFlashDetails_HostRes bankingBalanceFlashDetails_HostRes) {
		this.bankingBalanceFlashDetails_HostRes = bankingBalanceFlashDetails_HostRes;
	}

	public UpdateMobileNoDetails_DBRes getUpdateMobileNoDetails_DBRes() {
		return updateMobileNoDetails_DBRes;
	}

	public void setUpdateMobileNoDetails_DBRes(
			UpdateMobileNoDetails_DBRes updateMobileNoDetails_DBRes) {
		this.updateMobileNoDetails_DBRes = updateMobileNoDetails_DBRes;
	}

	public UpdatePaymentDetails_HostRes getUpdatePaymentDetails_HostRes() {
		return updatePaymentDetails_HostRes;
	}

	public void setUpdatePaymentDetails_HostRes(
			UpdatePaymentDetails_HostRes updatePaymentDetails_HostRes) {
		this.updatePaymentDetails_HostRes = updatePaymentDetails_HostRes;
	}

	public UpdateBirthdayAnncStatus_DBRes getUpdateBirthdayAnncStatus_DBRes() {
		return updateBirthdayAnncStatus_DBRes;
	}

	public void setUpdateBirthdayAnncStatus_DBRes(
			UpdateBirthdayAnncStatus_DBRes updateBirthdayAnncStatus_DBRes) {
		this.updateBirthdayAnncStatus_DBRes = updateBirthdayAnncStatus_DBRes;
	}

	public CheckForChequeBookRequest_DBRes getCheckForChequeBookRequest_DBRes() {
		return checkForChequeBookRequest_DBRes;
	}

	public void setCheckForChequeBookRequest_DBRes(
			CheckForChequeBookRequest_DBRes checkForChequeBookRequest_DBRes) {
		this.checkForChequeBookRequest_DBRes = checkForChequeBookRequest_DBRes;
	}

	public UpdateChequeBookOrder_HostRes getUpdateChequeBookOrder_HostRes() {
		return updateChequeBookOrder_HostRes;
	}

	public void setUpdateChequeBookOrder_HostRes(
			UpdateChequeBookOrder_HostRes updateChequeBookOrder_HostRes) {
		this.updateChequeBookOrder_HostRes = updateChequeBookOrder_HostRes;
	}

	public UpdateChequeBookRequest_DBRes getUpdateChequeBookRequest_DBRes() {
		return updateChequeBookRequest_DBRes;
	}

	public void setUpdateChequeBookRequest_DBRes(
			UpdateChequeBookRequest_DBRes updateChequeBookRequest_DBRes) {
		this.updateChequeBookRequest_DBRes = updateChequeBookRequest_DBRes;
	}

	public CheckComplaintID_HostRes getCheckComplaintID_HostRes() {
		return checkComplaintID_HostRes;
	}

	public void setCheckComplaintID_HostRes(
			CheckComplaintID_HostRes checkComplaintID_HostRes) {
		this.checkComplaintID_HostRes = checkComplaintID_HostRes;
	}

	public CreditCardBalanceDetails_HostRes getCreditCardBalanceDetails_HostRes() {
		return creditCardBalanceDetails_HostRes;
	}

	public void setCreditCardBalanceDetails_HostRes(
			CreditCardBalanceDetails_HostRes creditCardBalanceDetails_HostRes) {
		this.creditCardBalanceDetails_HostRes = creditCardBalanceDetails_HostRes;
	}

	public CreditCardDetails_HostRes getCreditCardDetails_HostRes() {
		return creditCardDetails_HostRes;
	}

	public void setCreditCardDetails_HostRes(
			CreditCardDetails_HostRes creditCardDetails_HostRes) {
		this.creditCardDetails_HostRes = creditCardDetails_HostRes;
	}


	public UpdateCreditCardPaymenTxnPosttDetails_HostRes getUpdateCreditCardPaymenTxnPosttDetails_HostRes() {
		return updateCreditCardPaymenTxnPosttDetails_HostRes;
	}

	public void setUpdateCreditCardPaymenTxnPosttDetails_HostRes(
			UpdateCreditCardPaymenTxnPosttDetails_HostRes updateCreditCardPaymenTxnPosttDetails_HostRes) {
		this.updateCreditCardPaymenTxnPosttDetails_HostRes = updateCreditCardPaymenTxnPosttDetails_HostRes;
	}

	public PerDayPerTransLimit_DBRes getPerDayPerTransLimit_DBRes() {
		return perDayPerTransLimit_DBRes;
	}

	public void setPerDayPerTransLimit_DBRes(
			PerDayPerTransLimit_DBRes perDayPerTransLimit_DBRes) {
		this.perDayPerTransLimit_DBRes = perDayPerTransLimit_DBRes;
	}

	public UpdateEnterAmount_DBRes getUpdateEnterAmount_DBRes() {
		return updateEnterAmount_DBRes;
	}

	public void setUpdateEnterAmount_DBRes(
			UpdateEnterAmount_DBRes updateEnterAmount_DBRes) {
		this.updateEnterAmount_DBRes = updateEnterAmount_DBRes;
	}

	public ExchangeRateInquiry_HostRes getExchangeRateInquiry_HostRes() {
		return exchangeRateInquiry_HostRes;
	}

	public void setExchangeRateInquiry_HostRes(
			ExchangeRateInquiry_HostRes exchangeRateInquiry_HostRes) {
		this.exchangeRateInquiry_HostRes = exchangeRateInquiry_HostRes;
	}

	public ExchangeRateDetails_DBRes getExchangeRateDetails_DBRes() {
		return exchangeRateDetails_DBRes;
	}

	public void setExchangeRateDetails_DBRes(
			ExchangeRateDetails_DBRes exchangeRateDetails_DBRes) {
		this.exchangeRateDetails_DBRes = exchangeRateDetails_DBRes;
	}

	public UpdateExchangeRateCurr_DBRes getUpdateExchangeRateCurr_DBRes() {
		return updateExchangeRateCurr_DBRes;
	}

	public void setUpdateExchangeRateCurr_DBRes(
			UpdateExchangeRateCurr_DBRes updateExchangeRateCurr_DBRes) {
		this.updateExchangeRateCurr_DBRes = updateExchangeRateCurr_DBRes;
	}

	public LoggingFaxRequest_HostRes getLoggingFaxRequest_HostRes() {
		return loggingFaxRequest_HostRes;
	}

	public void setLoggingFaxRequest_HostRes(
			LoggingFaxRequest_HostRes loggingFaxRequest_HostRes) {
		this.loggingFaxRequest_HostRes = loggingFaxRequest_HostRes;
	}

	public FixedDepositBalance_HostRes getFixedDepositBalance_HostRes() {
		return fixedDepositBalance_HostRes;
	}

	public void setFixedDepositBalance_HostRes(
			FixedDepositBalance_HostRes fixedDepositBalance_HostRes) {
		this.fixedDepositBalance_HostRes = fixedDepositBalance_HostRes;
	}

	public FixedDepositsRateDetails_DBRes getFixedDepositsRateDetails_DBRes() {
		return fixedDepositsRateDetails_DBRes;
	}

	public void setFixedDepositsRateDetails_DBRes(
			FixedDepositsRateDetails_DBRes fixedDepositsRateDetails_DBRes) {
		this.fixedDepositsRateDetails_DBRes = fixedDepositsRateDetails_DBRes;
	}

	public StaticFormsDetails_DBRes getStaticFormsDetails_DBRes() {
		return staticFormsDetails_DBRes;
	}

	public void setStaticFormsDetails_DBRes(
			StaticFormsDetails_DBRes staticFormsDetails_DBRes) {
		this.staticFormsDetails_DBRes = staticFormsDetails_DBRes;
	}

	public UpdateFormRequest_DBRes getUpdateFormRequest_DBRes() {
		return updateFormRequest_DBRes;
	}

	public void setUpdateFormRequest_DBRes(
			UpdateFormRequest_DBRes updateFormRequest_DBRes) {
		this.updateFormRequest_DBRes = updateFormRequest_DBRes;
	}

	public FT_BenfPayeeDetails_HostRes getFT_BenfPayeeDetails_HostRes() {
		return ft_BenfPayeeDetails_HostRes;
	}

	public void setFT_BenfPayeeDetails_HostRes(
			FT_BenfPayeeDetails_HostRes ft_BenfPayeeDetails_HostRes) {
		this.ft_BenfPayeeDetails_HostRes = ft_BenfPayeeDetails_HostRes;
	}

	public FT_ExchangeRateDetails_HostRes getFT_ExchangeRateDetails_HostRes() {
		return ft_ExchangeRateDetails_HostRes;
	}

	public void setFT_ExchangeRateDetails_HostRes(
			FT_ExchangeRateDetails_HostRes ft_ExchangeRateDetails_HostRes) {
		this.ft_ExchangeRateDetails_HostRes = ft_ExchangeRateDetails_HostRes;
	}

	public LoanBalanceDetails_HostRes getLoanBalanceDetails_HostRes() {
		return LoanBalanceDetails_HostRes;
	}

	public void setLoanBalanceDetails_HostRes(
			LoanBalanceDetails_HostRes loanBalanceDetails_HostRes) {
		LoanBalanceDetails_HostRes = loanBalanceDetails_HostRes;
	}

	public LoggingGeneratedOTP_DBRes getLoggingGeneratedOTP_DBRes() {
		return loggingGeneratedOTP_DBRes;
	}

	public void setLoggingGeneratedOTP_DBRes(
			LoggingGeneratedOTP_DBRes loggingGeneratedOTP_DBRes) {
		this.loggingGeneratedOTP_DBRes = loggingGeneratedOTP_DBRes;
	}

	public ValidOTPDetails_DBRes getValidOTPDetails_DBRes() {
		return validOTPDetails_DBRes;
	}

	public void setValidOTPDetails_DBRes(ValidOTPDetails_DBRes validOTPDetails_DBRes) {
		this.validOTPDetails_DBRes = validOTPDetails_DBRes;
	}

	public BeneficiaryRegistration_HostRes getBeneficiaryRegistration_HostRes() {
		return beneficiaryRegistration_HostRes;
	}

	public void setBeneficiaryRegistration_HostRes(
			BeneficiaryRegistration_HostRes beneficiaryRegistration_HostRes) {
		this.beneficiaryRegistration_HostRes = beneficiaryRegistration_HostRes;
	}

	public SendingSMS_HostRes getSendingSMS_HostRes() {
		return sendingSMS_HostRes;
	}

	public void setSendingSMS_HostRes(SendingSMS_HostRes sendingSMS_HostRes) {
		this.sendingSMS_HostRes = sendingSMS_HostRes;
	}

	public CustomerDetails_DBRes getCustomerDetails_DBRes() {
		return customerDetails_DBRes;
	}

	public void setCustomerDetails_DBRes(CustomerDetails_DBRes customerDetails_DBRes) {
		this.customerDetails_DBRes = customerDetails_DBRes;
	}

	public UpdateTenureAnncStatus_DBRes getUpdateTenureAnncStatus_DBRes() {
		return updateTenureAnncStatus_DBRes;
	}

	public void setUpdateTenureAnncStatus_DBRes(
			UpdateTenureAnncStatus_DBRes updateTenureAnncStatus_DBRes) {
		this.updateTenureAnncStatus_DBRes = updateTenureAnncStatus_DBRes;
	}

	public TPR_ExchangeRateDetails_HostRes getTPR_ExchangeRateDetails_HostRes() {
		return tpr_ExchangeRateDetails_HostRes;
	}

	public void setTPR_ExchangeRateDetails_HostRes(
			TPR_ExchangeRateDetails_HostRes tpr_ExchangeRateDetails_HostRes) {
		this.tpr_ExchangeRateDetails_HostRes = tpr_ExchangeRateDetails_HostRes;
	}

	public TPR_RetrieveBenfPayeeList_HostRes getTPR_RetrieveBenfPayeeList_HostRes() {
		return tpr_RetrieveBenfPayeeList_HostRes;
	}

	public void setTPR_RetrieveBenfPayeeList_HostRes(
			TPR_RetrieveBenfPayeeList_HostRes tpr_RetrieveBenfPayeeList_HostRes) {
		this.tpr_RetrieveBenfPayeeList_HostRes = tpr_RetrieveBenfPayeeList_HostRes;
	}

	public TPR_UpdatePaymentDetails_HostRes getTPR_UpdatePaymentDetails_HostRes() {
		return tpr_UpdatePaymentDetails_HostRes;
	}

	public void setTPR_UpdatePaymentDetails_HostRes(
			TPR_UpdatePaymentDetails_HostRes tpr_UpdatePaymentDetails_HostRes) {
		this.tpr_UpdatePaymentDetails_HostRes = tpr_UpdatePaymentDetails_HostRes;
	}

	public TransactionDetailsBank_HostRes getTransactionDetailsBank_HostRes() {
		return transactionDetailsBank_HostRes;
	}

	public void setTransactionDetailsBank_HostRes(
			TransactionDetailsBank_HostRes transactionDetailsBank_HostRes) {
		this.transactionDetailsBank_HostRes = transactionDetailsBank_HostRes;
	}

	public LoggingEmailRequestBank_DBRes getLoggingEmailRequestBank_DBRes() {
		return loggingEmailRequestBank_DBRes;
	}

	public void setLoggingEmailRequestBank_DBRes(
			LoggingEmailRequestBank_DBRes loggingEmailRequestBank_DBRes) {
		this.loggingEmailRequestBank_DBRes = loggingEmailRequestBank_DBRes;
	}

	public TransactionDetailCards_HostReq getTransactionDetailCards_HostReq() {
		return transactionDetailCards_HostReq;
	}

	public void setTransactionDetailCards_HostReq(
			TransactionDetailCards_HostReq transactionDetailCards_HostReq) {
		this.transactionDetailCards_HostReq = transactionDetailCards_HostReq;
	}

	public LoggingEmailRequestCards_DBRes getLoggingEmailRequestCards_DBRes() {
		return loggingEmailRequestCards_DBRes;
	}

	public void setLoggingEmailRequestCards_DBRes(
			LoggingEmailRequestCards_DBRes loggingEmailRequestCards_DBRes) {
		this.loggingEmailRequestCards_DBRes = loggingEmailRequestCards_DBRes;
	}

	public UpdateUserCallBackOption_DBRes getUpdateUserCallBackOption_DBRes() {
		return updateUserCallBackOption_DBRes;
	}

	public void setUpdateUserCallBackOption_DBRes(
			UpdateUserCallBackOption_DBRes updateUserCallBackOption_DBRes) {
		this.updateUserCallBackOption_DBRes = updateUserCallBackOption_DBRes;
	}

	public LogMobileNumber_DBRes getLogMobileNumber_DBRes() {
		return logMobileNumber_DBRes;
	}

	public void setLogMobileNumber_DBRes(LogMobileNumber_DBRes logMobileNumber_DBRes) {
		this.logMobileNumber_DBRes = logMobileNumber_DBRes;
	}

	public Alert_PromotionDetails_DBRes getAlert_PromotionDetails_DBRes() {
		return alert_PromotionDetails_DBRes;
	}

	public void setAlert_PromotionDetails_DBRes(
			Alert_PromotionDetails_DBRes alert_PromotionDetails_DBRes) {
		this.alert_PromotionDetails_DBRes = alert_PromotionDetails_DBRes;
	}


	/**
	 * For ICE Configuration paramters
	 */


	public IVR_ICECallData getIvr_ICECallData() {
		return ivr_ICECallData;
	}

	public void setIvr_ICECallData(IVR_ICECallData ivr_ICECallData) {
		this.ivr_ICECallData = ivr_ICECallData;
	}

	public IVR_ICEFeatureData getIvr_ICEFeatureData() {
		return ivr_ICEFeatureData;
	}

	public void setIvr_ICEFeatureData(IVR_ICEFeatureData ivr_ICEFeatureData) {
		this.ivr_ICEFeatureData = ivr_ICEFeatureData;
	}

	public IVR_ICEGlobalConfig getIvr_ICEGlobalConfig() {
		return ivr_ICEGlobalConfig;
	}

	public void setIvr_ICEGlobalConfig(IVR_ICEGlobalConfig ivr_ICEGlobalConfig) {
		this.ivr_ICEGlobalConfig = ivr_ICEGlobalConfig;
	}

	public IVR_ICERuleParam getIvr_ICERuleParam() {
		return ivr_ICERuleParam;
	}

	public void setIvr_ICERuleParam(IVR_ICERuleParam ivr_ICERuleParam) {
		this.ivr_ICERuleParam = ivr_ICERuleParam;
	}

	public Utility_BenfPayeeDetails_HostRes getUtility_BenfPayeeDetails_HostRes() {
		return utility_BenfPayeeDetails_HostRes;
	}

	public void setUtility_BenfPayeeDetails_HostRes(
			Utility_BenfPayeeDetails_HostRes utility_BenfPayeeDetails_HostRes) {
		this.utility_BenfPayeeDetails_HostRes = utility_BenfPayeeDetails_HostRes;
	}

	public CCPayment_BenfPayeeDetails_HostRes getCcPayment_BenfPayeeDetails_HostRes() {
		return ccPayment_BenfPayeeDetails_HostRes;
	}

	public void setCcPayment_BenfPayeeDetails_HostRes(
			CCPayment_BenfPayeeDetails_HostRes ccPayment_BenfPayeeDetails_HostRes) {
		this.ccPayment_BenfPayeeDetails_HostRes = ccPayment_BenfPayeeDetails_HostRes;
	}

	public RecentTransactionBank_HostRes getRecentTransactionBank_HostRes() {
		return recentTransactionBank_HostRes;
	}

	public void setRecentTransactionBank_HostRes(
			RecentTransactionBank_HostRes recentTransactionBank_HostRes) {
		this.recentTransactionBank_HostRes = recentTransactionBank_HostRes;
	}

	public RecentTransactionCards_HostRes getRecentTransactionCards_HostRes() {
		return recentTransactionCards_HostRes;
	}

	public void setRecentTransactionCards_HostRes(
			RecentTransactionCards_HostRes recentTransactionCards_HostRes) {
		this.recentTransactionCards_HostRes = recentTransactionCards_HostRes;
	}

	public UpdateFTIntraPayment_HostRes getUpdateFTIntraPayment_HostRes() {
		return updateFTIntraPayment_HostRes;
	}

	public void setUpdateFTIntraPayment_HostRes(
			UpdateFTIntraPayment_HostRes updateFTIntraPayment_HostRes) {
		this.updateFTIntraPayment_HostRes = updateFTIntraPayment_HostRes;
	}

	public UpdateFTRemittPayment_HostRes getUpdateFTRemittPayment_HostRes() {
		return updateFTRemittPayment_HostRes;
	}

	public void setUpdateFTRemittPayment_HostRes(
			UpdateFTRemittPayment_HostRes updateFTRemittPayment_HostRes) {
		this.updateFTRemittPayment_HostRes = updateFTRemittPayment_HostRes;
	}

	public UpdateFTUtilityPaymentCharity_HostRes getUpdateFTUtilityPaymentCharity_HostRes() {
		return updateFTUtilityPaymentCharity_HostRes;
	}

	public void setUpdateFTUtilityPaymentCharity_HostRes(
			UpdateFTUtilityPaymentCharity_HostRes updateFTUtilityPaymentCharity_HostRes) {
		this.updateFTUtilityPaymentCharity_HostRes = updateFTUtilityPaymentCharity_HostRes;
	}

	public CallerIdenf_DebitCardDetails getCallerIdenf_DebitCardDetails() {
		return callerIdenf_DebitCardDetails;
	}

	public void setCallerIdenf_DebitCardDetails(
			CallerIdenf_DebitCardDetails callerIdenf_DebitCardDetails) {
		this.callerIdenf_DebitCardDetails = callerIdenf_DebitCardDetails;
	}

	public KeyExAuth_HostRes getKeyExAuth_HostRes() {
		return keyExAuth_HostRes;
	}

	public void setKeyExAuth_HostRes(KeyExAuth_HostRes keyExAuth_HostRes) {
		this.keyExAuth_HostRes = keyExAuth_HostRes;
	}

	public GetData_HostRes getGetData_HostRes() {
		return getData_HostRes;
	}

	public void setGetData_HostRes(GetData_HostRes getData_HostRes) {
		this.getData_HostRes = getData_HostRes;
	}

	public SetData_HostRes getSetData_HostRes() {
		return setData_HostRes;
	}

	public void setSetData_HostRes(SetData_HostRes setData_HostRes) {
		this.setData_HostRes = setData_HostRes;
	}

	public FT_BeneficiaryDetailList_HostRes getFT_BeneficiaryDetailList_HostRes() {
		return ft_BeneficiaryDetailList_HostRes;
	}

	public void setFT_BeneficiaryDetailList_HostRes(
			FT_BeneficiaryDetailList_HostRes ft_BeneficiaryDetailList_HostRes) {
		this.ft_BeneficiaryDetailList_HostRes = ft_BeneficiaryDetailList_HostRes;
	}

	public CreditCardGroupInq_HostRes getCreditCardGroupInq_HostRes() {
		return creditCardGroupInq_HostRes;
	}

	public void setCreditCardGroupInq_HostRes(
			CreditCardGroupInq_HostRes creditCardGroupInq_HostRes) {
		this.creditCardGroupInq_HostRes = creditCardGroupInq_HostRes;
	}

	public UpdCCPaymentDetails_HostRes getUpdCCPaymentDetails_HostRes() {
		return updCCPaymentDetails_HostRes;
	}

	public void setUpdCCPaymentDetails_HostRes(
			UpdCCPaymentDetails_HostRes updCCPaymentDetails_HostRes) {
		this.updCCPaymentDetails_HostRes = updCCPaymentDetails_HostRes;
	}

	public Utility_BeneficiaryDetailList_HostRes getUtility_BeneficiaryDetailList_HostRes() {
		return utility_BeneficiaryDetailList_HostRes;
	}

	public void setUtility_BeneficiaryDetailList_HostRes(
			Utility_BeneficiaryDetailList_HostRes utility_BeneficiaryDetailList_HostRes) {
		this.utility_BeneficiaryDetailList_HostRes = utility_BeneficiaryDetailList_HostRes;
	}

	public CCPayment_BeneficiaryDetailList_HostRes getCcPayment_BeneficiaryDetailList_HostRe() {
		return ccPayment_BeneficiaryDetailList_HostRe;
	}

	public void setCcPayment_BeneficiaryDetailList_HostRe(
			CCPayment_BeneficiaryDetailList_HostRes ccPayment_BeneficiaryDetailList_HostRe) {
		this.ccPayment_BeneficiaryDetailList_HostRe = ccPayment_BeneficiaryDetailList_HostRe;
	}

	public UpdatePaymentDetailsCC_HostRes getUpdatePaymentDetailsCC_HostRes() {
		return updatePaymentDetailsCC_HostRes;
	}

	public void setUpdatePaymentDetailsCC_HostRes(
			UpdatePaymentDetailsCC_HostRes updatePaymentDetailsCC_HostRes) {
		this.updatePaymentDetailsCC_HostRes = updatePaymentDetailsCC_HostRes;
	}


	public TPR_BenfPayeeDetails_HostRes getTPR_BenfPayeeDetails_HostRes() {
		return TPR_BenfPayeeDetails_HostRes;
	}

	public void setTPR_BenfPayeeDetails_HostRes(
			TPR_BenfPayeeDetails_HostRes tPR_BenfPayeeDetails_HostRes) {
		TPR_BenfPayeeDetails_HostRes = tPR_BenfPayeeDetails_HostRes;
	}


	public TelecomPostpaidBalanceDetails_HostRes getTelecomPostpaidBalanceDetails_HostRes() {
		return TelecomPostpaidBalanceDetails_HostRes;
	}

	public void setTelecomPostpaidBalanceDetails_HostRes(
			TelecomPostpaidBalanceDetails_HostRes telecomPostpaidBalanceDetails_HostRes) {
		TelecomPostpaidBalanceDetails_HostRes = telecomPostpaidBalanceDetails_HostRes;
	}

	public TelecomSubscriberInfo_HostRes getTelecomSubscriberInfo_HostRes() {
		return TelecomSubscriberInfo_HostRes;
	}

	public void setTelecomSubscriberInfo_HostRes(
			TelecomSubscriberInfo_HostRes telecomSubscriberInfo_HostRes) {
		TelecomSubscriberInfo_HostRes = telecomSubscriberInfo_HostRes;
	}

	public UtilitySubscriberInfo_HostRes getUtilitySubscriberInfo_HostRes() {
		return utilitySubscriberInfo_HostRes;
	}

	public void setUtilitySubscriberInfo_HostRes(
			UtilitySubscriberInfo_HostRes utilitySubscriberInfo_HostRes) {
		this.utilitySubscriberInfo_HostRes = utilitySubscriberInfo_HostRes;
	}
	
	public FetchCardServiceHistory_HostRes getFetchCardServiceHistory_HostRes() {
		return fetchCardServiceHistory_HostRes;
	}

	public void setFetchCardServiceHistory_HostRes(
			FetchCardServiceHistory_HostRes fetchCardServiceHistory_HostRes) {
		this.fetchCardServiceHistory_HostRes = fetchCardServiceHistory_HostRes;
	}
	
	public GetUtilityBillInfo_HostRes getGetUtilityBillInfo_HostRes() {
		return getUtilityBillInfo_HostRes;
	}

	public void setGetUtilityBillInfo_HostRes(
			GetUtilityBillInfo_HostRes getUtilityBillInfo_HostRes) {
		this.getUtilityBillInfo_HostRes = getUtilityBillInfo_HostRes;
	}

	@Override
	public String insertReportData(String reportXML, CallInfo callInfo)throws ServiceException {
		//Following for the insertReportData

		String db_Code = Constants.ONE;
		String sessionId = (String) callInfo.getField(Field.SESSIONID);
		String session_ID = Constants.EMPTY;
		try{
			logger = (Logger)getField(Field.LOGGER);
			session_ID = (String)getField(Field.SESSIONID);
		}catch(Exception e){}
		try{

			if(util.isNullOrEmpty(sessionId)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID, "Session ID is null or empty");}
				throw new ServiceException("Session id is null or empty");
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID, "Calling the DB Method ");}
			HashMap<String, Object> configMap = new HashMap<String, Object>();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID, "Setting the input for getSequenceNo");}

			String uui = (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID, "UUI of the call is " + uui);}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID, "Fetching the ICE Global Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}


			String xmlFailureLoc  = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_REPORT_DATA_FAILURE_LOCATION);
			xmlFailureLoc = util.isNullOrEmpty(xmlFailureLoc)? Constants.NA : xmlFailureLoc;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID, "XML failure location is " + xmlFailureLoc);}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID, " #### #### Final Report XML value is #### #### " + reportXML);}

			configMap.put(DBConstants.REPORTDATA, reportXML);
			configMap.put(DBConstants.XMLFAILURELOCATION, xmlFailureLoc);

			//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, "Report data is " + configMap.get(DBConstants.REPORTDATA) );}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID, "XML failure location" + configMap.get(DBConstants.XMLFAILURELOCATION));}

			DataServices dataServices = VRUDBDataServicesInstance.getInstance();
			if(util.isNullOrEmpty(dataServices)){
				throw new ServiceException("Data Service object is null or empty");
			}
			db_Code = dataServices.insertReportData(logger, sessionId, uui, configMap);

			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(db_Code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID, "Successfully inserted into the report");}
			}
			else{
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID, "Error in the S1 sequencial DB response");}
				throw new ServiceException("insertReportData DB access throwing error");
			}
		}
		catch (com.db.exception.ServiceException e) {
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID, "ERROR: callInfoImpl.insertReportData()");}
			throw new ServiceException("S1 Sequencial number DB access throwing error");
			//e.printStackTrace();
		}
		return db_Code;
	}

	public GetCCCustDtls_HostRes getGetCCCustDtls_HostRes() {
		return getCCCustDtls_HostRes;
	}

	public void setGetCCCustDtls_HostRes(GetCCCustDtls_HostRes getCCCustDtls_HostRes) {
		this.getCCCustDtls_HostRes = getCCCustDtls_HostRes;
	}

	public GetDCCustDtls_HostRes getGetDCCustDtls_HostRes() {
		return getDCCustDtls_HostRes;
	}

	public void setGetDCCustDtls_HostRes(GetDCCustDtls_HostRes getDCCustDtls_HostRes) {
		this.getDCCustDtls_HostRes = getDCCustDtls_HostRes;
	}
	@Override
	public GetCCCustDtls_HostRes getCCCustDtls_HostRes() {
		// TODO Auto-generated method stub
		return getCCCustDtls_HostRes;
	}

	@Override
	public GetDCCustDtls_HostRes getDCCustDtls_HostRes() {
		// TODO Auto-generated method stub
		return getDCCustDtls_HostRes;
	}

	@Override
	public void setCCCustDtls_HostRes(GetCCCustDtls_HostRes getCCCustDtls_HostRes) {
		this.getCCCustDtls_HostRes = getCCCustDtls_HostRes;
		
	}

	@Override
	public void setDCCustDtls_HostRes(GetDCCustDtls_HostRes getDCCustDtls_HostRes) {
		this.getDCCustDtls_HostRes = getDCCustDtls_HostRes;
		
	}

	public CustDtls_HostRes getCustDtls_HostRes() {
		return custDtls_HostRes;
	}

	
	public void setCustDtls_HostRes(CustDtls_HostRes custDtls_HostRes) {
		this.custDtls_HostRes = custDtls_HostRes;
	}

	@Override
	public CustDtls_HostRes custDtls_HostRes() {
		// TODO Auto-generated method stub
		return custDtls_HostRes;
	}

	public MobileNumberChange_HostRes getMobileNumberChange_HostRes() {
		return mobileNumberChange_HostRes;
	}

	public void setMobileNumberChange_HostRes(
			MobileNumberChange_HostRes mobileNumberChange_HostRes) {
		this.mobileNumberChange_HostRes = mobileNumberChange_HostRes;
	}
	
}
