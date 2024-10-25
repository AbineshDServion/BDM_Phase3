package com.servion.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.db.dataservices.DataServices;
import com.db.resource.DBConstants;
import com.db.resource.VRUDBDataServicesInstance;
import com.reportbean.IvrData;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.context.Context;
import com.servion.dao.UtilityBillPaymentCCDAO;
import com.servion.dao.UtilityBillPaymentDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.AnncIDMap;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;
import com.servion.model.MenuIDMap;
import com.servion.model.billPayment.UpdatePaymentDetailsCC_HostRes;
import com.servion.model.billPayment.UpdatePaymentDetails_HostRes;
import com.servion.model.billPayment.Utility_BeneficiaryDetails;
import com.servion.model.callerIdentification.AcctInfo;
import com.servion.model.reporting.HostReportDetails;

public class UtilityBillPaymentConfirmationImpl implements IUtilityBillPaymentConfirmation{
	private static Logger logger = LoggerObject.getLogger();

	private UtilityBillPaymentCCDAO utilityBillPaymentCCDAO;
	private UtilityBillPaymentDAO utilityBillPaymentDAO;
	private MessageSource messageSource;

	public MessageSource getMessageSource() {
		return messageSource;
	}

	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public UtilityBillPaymentCCDAO getUtilityBillPaymentCCDAO() {
		return utilityBillPaymentCCDAO;
	}

	public void setUtilityBillPaymentCCDAO(
			UtilityBillPaymentCCDAO utilityBillPaymentCCDAO) {
		this.utilityBillPaymentCCDAO = utilityBillPaymentCCDAO;
	}

	public UtilityBillPaymentDAO getUtilityBillPaymentDAO() {
		return utilityBillPaymentDAO;
	}

	public void setUtilityBillPaymentDAO(UtilityBillPaymentDAO utilityBillPaymentDAO) {
		this.utilityBillPaymentDAO = utilityBillPaymentDAO;
	}

	@Override
	public String UpdateBillPayment(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: UtilityBillPaymentConfirmationImpl.UpdateBillPayment()");}
		String code = Constants.EMPTY_STRING;
		String billNo = Constants.EMPTY_STRING;
		String dueAmount = Constants.EMPTY_STRING;
		String accessChannel = Constants.EMPTY_STRING;
		String paymentMethod = Constants.EMPTY_STRING;
		String paymentStatus = Constants.EMPTY_STRING;
		BigDecimal bigDueAmt = null;
		String electricityType = Constants.EMPTY_STRING, billerCode = Constants.EMPTY_STRING; 
		//		getConfigurationParam(callInfo);
		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}

			ICEFeatureData ivr_FeatureData  = (ICEFeatureData) callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(ivr_FeatureData)){
				throw new ServiceException("ivr_FeatureData object is null");
			}

			
			
			/**
			 * Checking the condition for calling list beneficiary and beneficiary details host access 
			 */
			boolean isOTPCalledAfterDisconnected = util.isNullOrEmpty(callInfo.getField(Field.ISOTPCALLEDAFTERDISCONNECT)) ? false : (boolean)callInfo.getField(Field.ISOTPCALLEDAFTERDISCONNECT);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is OTP Called After Disconnected " +isOTPCalledAfterDisconnected );}
			if(isOTPCalledAfterDisconnected){
				String returnCode = Constants.ONE;
				if(Constants.FEATURENAME_UTILITYBILLPAYMENTELECTRICITY.equalsIgnoreCase((String)callInfo.getField(Field.FEATURENAME))){
					billNo = (String) callInfo.getField(Field.BILL_NO);
					
					if(!util.isNullOrEmpty(Context.getiUtilityBillPaymentElectricity())){
						returnCode = Context.getiUtilityBillPaymentElectricity().getElectricityPayeeList(callInfo);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Return code of List Beneficiary web service of electricity is " +returnCode );}

						if(Constants.ZERO.equalsIgnoreCase(returnCode)){
							returnCode = Context.getiUtilityBillPaymentElectricity().getElectricityPayeeDetails(callInfo);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Return code of Beneficiary Details web service is " +returnCode );}

							if(Constants.ONE.equalsIgnoreCase(returnCode)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response from beneficiary Details host access");}
								throw new ServiceException("Got failure response from beneficiary Detail host access");
							}

						}else{
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response from list beneficiary host access");}
							throw new ServiceException("Got failure response from list beneficiary host access");
						}

					}else{
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Context.getiFundsTransferCharity() is null or empty");}
						throw new ServiceException("Context.getiFundsTransferCharity() is null or empty");
					}
				}else if(Constants.FEATURENAME_UTILITYBILLPAYMENTMOBILELANDLINEINTERNET.equalsIgnoreCase((String)callInfo.getField(Field.FEATURENAME))){
					if(!util.isNullOrEmpty(Context.getiUtilityBillPaymentMobLandIntr())){
						returnCode = Context.getiUtilityBillPaymentMobLandIntr().getMobileBroadPayeeList(callInfo);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Return code of List Beneficiary web service of Mob/ Internet / Landline is " +returnCode );}

						if(Constants.ZERO.equalsIgnoreCase(returnCode)){
							returnCode = Context.getiUtilityBillPaymentMobLandIntr().getMobileBroadPayeeDetails(callInfo);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Return code of Beneficiary Details web service is " +returnCode );}

							if(Constants.ONE.equalsIgnoreCase(returnCode)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response from beneficiary Details host access");}
								throw new ServiceException("Got failure response from beneficiary Detail host access of Mob / Landline / Internet");
							}

						}else{
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response from list beneficiary host access");}
							throw new ServiceException("Got failure response from list beneficiary host access");
						}

					}else{
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Context.getiFundsTransferCharity() is null or empty");}
						throw new ServiceException("Context.getiFundsTransferCharity() is null or empty");
					}
				}else if(Constants.FEATURENAME_UTILITYBILLPAYMENTSCHOOL.equalsIgnoreCase((String)callInfo.getField(Field.FEATURENAME))){

					if(!util.isNullOrEmpty(Context.getiUtilityBillPaymentSchoolFee())){
						returnCode = Context.getiUtilityBillPaymentSchoolFee().getSchoolFeePayeeList(callInfo);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Return code of List Beneficiary web service of UPB School is " +returnCode );}

						if(Constants.ZERO.equalsIgnoreCase(returnCode)){
							returnCode = Context.getiUtilityBillPaymentSchoolFee().getSchoolFeePayeeDetails(callInfo);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Return code of Beneficiary Details of  UPB School web service is " +returnCode );}

							if(Constants.ONE.equalsIgnoreCase(returnCode)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response from beneficiary Details host access of  UPB School");}
								throw new ServiceException("Got failure response from beneficiary Detail host access");
							}

						}else{
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response from list beneficiary host access");}
							throw new ServiceException("Got failure response from list beneficiary host access");
						}

					}else{
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Context.getiFundsTransferThirdPartyOutsideBM() is null or empty");}
						throw new ServiceException("Context.getiFundsTransferThirdPartyOutsideBM() is null or empty");
					}

				}else if(Constants.FEATURENAME_UTILITYBILLPAYMENTWATER.equalsIgnoreCase((String)callInfo.getField(Field.FEATURENAME))){
					billNo = (String) callInfo.getField(Field.BILL_NO);
					if(!util.isNullOrEmpty(Context.getiUtilityBillPaymentWater())){
						returnCode = Context.getiUtilityBillPaymentWater().getWaterBillPayeeList(callInfo);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Return code of List Beneficiary web service of Utility water payment is " +returnCode );}

						if(Constants.ZERO.equalsIgnoreCase(returnCode)){
							returnCode = Context.getiUtilityBillPaymentWater().getWaterBillPayeeDetail(callInfo);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Return code of Beneficiary Details web service of Utility water payment is " +returnCode );}

							if(Constants.ONE.equalsIgnoreCase(returnCode)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response from beneficiary Details host access");}
								throw new ServiceException("Got failure response from beneficiary Detail host access");
							}

						}else{
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response from list beneficiary host access");}
							throw new ServiceException("Got failure response from list beneficiary host access");
						}

					}else{
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Context.getiFundsTransferThirdPartyWithinBM() is null or empty");}
						throw new ServiceException("Context.getiFundsTransferCharity() is null or empty");
					}

				}
			}

			//END - FundsTransferConfirmation
			
			
			String paymentType = (String) ivr_FeatureData.getConfig().getParamValue(Constants.CUI_UBP_PAYMENTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Payment type is "+ paymentType);}

			String selectedacctNo = (String)callInfo.getField(Field.DESTNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "User selected account / mobile / landline / internet number is "+ util.getSubstring(selectedacctNo, Constants.GL_FOUR));}
			
			String serviceProviderCode = (String)callInfo.getField(Field.SELECTEDSERVICEPROVIDER);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "User selected service porvide code is "+ serviceProviderCode);}
			
			String utilityCode = (String)callInfo.getField(Field.UTILITYCODE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Utility code is "+ utilityCode);}
			
			String contractNo = Constants.EMPTY_STRING;
					
			//String billNo = Constants.EMPTY_STRING;
			
			String debitAcctID = (String)callInfo.getField(Field.SRCNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Debit Account Number is "+ util.getSubstring(debitAcctID, Constants.GL_FOUR));}
			
			String benfCode = Constants.EMPTY_STRING;
			String payAmt = Constants.EMPTY_STRING;
			String debitAmt = Constants.EMPTY_STRING;
			
			String calledFrom = (String)callInfo.getField(Field.FEATURENAME);
			boolean isFromTopUp = Constants.FEATURENAME_UTILITYBILLPAYMENTTOPUP.equalsIgnoreCase(calledFrom)?true:false;
			
			String str_serviceProviderCode = serviceProviderCode;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected service provider code is "+ str_serviceProviderCode);}
			

			int msisdn = Constants.GL_ZERO;

			if(isFromTopUp){
				msisdn = 	selectedacctNo!=null? Integer.parseInt(selectedacctNo): Constants.GL_ZERO;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Its a TOP Up module and its Selected MSISDN number is "+ selectedacctNo);}
			}


			if(!isFromTopUp){
				HashMap<String, ArrayList<Utility_BeneficiaryDetails>> utilityCodeDtlMap = null;
				ArrayList<Utility_BeneficiaryDetails> beneficiaryDtlList = null;

				if(!util.isNullOrEmpty(callInfo.getUtility_BenfPayeeDetails_HostRes())){
					if(!util.isNullOrEmpty(callInfo.getUtility_BenfPayeeDetails_HostRes().getUtility_utilityCodeMap())){
						utilityCodeDtlMap = (HashMap<String, ArrayList<Utility_BeneficiaryDetails>>) callInfo.getUtility_BenfPayeeDetails_HostRes().getUtility_utilityCodeMap();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved the  utility code and beneficiary detail map"+ utilityCodeDtlMap);}

					}
				}else{
					throw new ServiceException("Utility host response bean is null / empty");
				}

				if(!util.isNullOrEmpty(utilityCodeDtlMap)){
					beneficiaryDtlList = utilityCodeDtlMap.get(utilityCode);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved the  beneficiary detail list"+ beneficiaryDtlList);}

				}else{
					throw new ServiceException("Utility host response bean is null / empty");
				}
				
				Utility_BeneficiaryDetails beneficiaryDtl = null;
				if(!util.isNullOrEmpty(beneficiaryDtlList)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}

					//				String ui_landlineCode = (String) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_UTILITY_CODE_FOR_LANDLINE);
					//				String ui_MobileCode = (String) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_UTILITY_CODE_FOR_MOBILE);
					//				String ui_InternetCode = (String) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_UTILITY_CODE_FOR_INTERNET);
					//
					//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Landline Utility code is "+ ui_landlineCode);}
					//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Mobile Utility code is " + ui_MobileCode);}
					//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Internet Utility code is " + ui_InternetCode);}
					//				
					//				ui_landlineCode = util.isNullOrEmpty(ui_landlineCode)?Constants.EMPTY_STRING:ui_landlineCode;
					//				ui_MobileCode = util.isNullOrEmpty(ui_MobileCode)?Constants.EMPTY_STRING:ui_MobileCode;
					//				ui_InternetCode = util.isNullOrEmpty(ui_InternetCode)?Constants.EMPTY_STRING:ui_InternetCode;


					for(int count= 0; count < beneficiaryDtlList.size(); count++){
						beneficiaryDtl = beneficiaryDtlList.get(count);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved "+count+"beneficiary detail "+ beneficiaryDtl);}

						if(selectedacctNo.equalsIgnoreCase(beneficiaryDtl.getBenefContractNo())){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The beneficiary details object is "  + beneficiaryDtl.getBenefAccountNo());}
							break;
						}
						
						
						if(selectedacctNo.equalsIgnoreCase(beneficiaryDtl.getBenefTelephoneNo())){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected is a landline service provider");}
							break;
						}else if(selectedacctNo.equalsIgnoreCase(beneficiaryDtl.getBenefAccountNo())){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected is a Internet / Electricity / Water service provider");}
							break;
						}else if(selectedacctNo.equalsIgnoreCase(beneficiaryDtl.getBenefMobileNo())){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected is a Mobile service provider");}
							break;
						}else if(selectedacctNo.equalsIgnoreCase(beneficiaryDtl.getBenefGSMNo())){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected is a School");}
							break;
						}
					}
					
					
					serviceProviderCode = beneficiaryDtl.getServiceProviderCode();
					str_serviceProviderCode = serviceProviderCode;

					contractNo = beneficiaryDtl.getBenefContractNo();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requesting contract number is " + contractNo);}
					
					if(Constants.FEATURENAME_UTILITYBILLPAYMENTELECTRICITY.equalsIgnoreCase((String)callInfo.getField(Field.FEATURENAME))
							|| Constants.FEATURENAME_UTILITYBILLPAYMENTWATER.equalsIgnoreCase((String)callInfo.getField(Field.FEATURENAME))){
						billNo = (String) callInfo.getField(Field.BILL_NO);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requesting bill number for water or electricity is " + billNo);}
						
						dueAmount = util.isNullOrEmpty(callInfo.getField(Field.UTILITY_DUE_AMOUNT))? Constants.EMPTY_STRING :(String)callInfo.getField(Field.UTILITY_DUE_AMOUNT);
						bigDueAmt = util.isNullOrEmpty(dueAmount)?new BigDecimal(Constants.GL_ZERO): new BigDecimal(dueAmount);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requesting Due amount is " + bigDueAmt);}
						
						
						accessChannel = (String) ivr_FeatureData.getConfig().getParamValue(Constants.CUI_UBP_ACCESS_CHANNEL);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Access Channel is "+ accessChannel);}
						
						paymentMethod = (String) ivr_FeatureData.getConfig().getParamValue(Constants.CUI_UBP_PAYMENT_METHOD);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Payment Method is "+ paymentMethod);}
						
						paymentStatus = (String) ivr_FeatureData.getConfig().getParamValue(Constants.CUI_UBP_PAYMENT_STATUS);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Payment Status is "+ paymentStatus);}

					}else{
						billNo = beneficiaryDtl.getBenefBillNo();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requesting bill number is " + billNo);}
					}
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Beneficiary GSM No is " + beneficiaryDtl.getBenefGSMNo());}

					if(beneficiaryDtl.getBenefGSMNo() != null && !Constants.EMPTY_STRING.equalsIgnoreCase(beneficiaryDtl.getBenefGSMNo())
							&& !"null".equalsIgnoreCase(beneficiaryDtl.getBenefGSMNo())){
						
						msisdn = Integer.parseInt(beneficiaryDtl.getBenefGSMNo());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requesting MSISDN number is " + msisdn);}
					}else{
						msisdn = Constants.GL_ZERO;
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requesting MSISDN number is " + msisdn);}
					}
					
					

//					debitAcctID = beneficiaryDtl.getCustomerDrAcctNo();
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requesting debitAcctID number is " + debitAcctID);}

					benfCode = beneficiaryDtl.getBeneficiaryID();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requesting benfCode number is " + benfCode);}
					
				}
			}
			
			
			debitAmt = util.isNullOrEmpty(callInfo.getField(Field.AMOUNT)) ? Constants.EMPTY_STRING : (String)callInfo.getField(Field.AMOUNT);
			BigDecimal bigDebitAmt = util.isNullOrEmpty(debitAmt)?new BigDecimal(Constants.GL_ZERO):new BigDecimal(debitAmt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requesting debitAmt number is " + bigDebitAmt);}

			payAmt = util.isNullOrEmpty(callInfo.getField(Field.AMOUNT))? Constants.EMPTY_STRING :(String)callInfo.getField(Field.AMOUNT);
			BigDecimal bigPayamt = util.isNullOrEmpty(payAmt)?new BigDecimal(Constants.GL_ZERO): new BigDecimal(payAmt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requesting payAmt number is " + bigPayamt);}

			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			
			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("ICE Feature Data is null / Empty");
			}
			
			int int_BonusRechargeAmt = Constants.GL_ZERO;
			
			if(isFromTopUp){
				String nawraseServiceCode = (String)ivr_FeatureData.getConfig().getParamValue(Constants.CUI_NAWRAS_UTILITY_CODE);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Nawras Service Code configured is " + nawraseServiceCode);}

				if(util.isCodePresentInTheConfigurationList(utilityCode, nawraseServiceCode)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requesting payAmt number is " + bigPayamt);}

					String str_bonusRechrgAmt = (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_BONUSRECHARGE_FOR_NAWRAS);
					int_BonusRechargeAmt = util.isNullOrEmpty(str_bonusRechrgAmt)?Constants.GL_ZERO:Integer.parseInt(str_bonusRechrgAmt);

				}
			}
			BigDecimal bonusRechrgAmt = new BigDecimal(int_BonusRechargeAmt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requesting bonusRechrgAmt number is " + bonusRechrgAmt);}


			Calendar cal = Calendar.getInstance();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Calendar time "+cal.getTime());}
			XMLGregorianCalendar debitValueDate = DatatypeFactory.newInstance().newXMLGregorianCalendarDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,cal.get(Calendar.DAY_OF_MONTH), DatatypeConstants.FIELD_UNDEFINED);
			debitValueDate.setTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),cal.get(Calendar.SECOND));
			debitValueDate.setMillisecond(cal.get(Calendar.MILLISECOND));
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The debitValueDate is "+ debitValueDate);}

			/**
			 * For Reporting Purpose
			 */
			HostReportDetails hostReportDetails = new HostReportDetails();

			String featureId = (String)callInfo.getField(Field.FEATUREID);
			hostReportDetails.setHostActiveMenu(featureId);
			//hostReportDetails.setHostCounter(hostCounter);
			//hostReportDetails.setHostEndTime(hostEndTime);
			String strHostInParam = Constants.NA;
			try{
			strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + callInfo.getField(Field.CUSTOMERID)
					+Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO +util.maskCardOrAccountNumber((String)callInfo.getField(Field.SRCNO))
					+Constants.COMMA + Constants.HOST_INPUT_PARAM_DESTINATION_NO + Constants.EQUALTO + util.maskCardOrAccountNumber((String)callInfo.getField(Field.DESTNO))
					+Constants.COMMA + Constants.HOST_INPUT_PARAM_AMOUNT + Constants.EQUALTO + callInfo.getField(Field.AMOUNT)
					+Constants.COMMA + Constants.HOST_INPUT_PARAM_SERVICE_PROVIDER_CODE + Constants.EQUALTO + callInfo.getField(Field.SELECTEDSERVICEPROVIDER)
					+Constants.COMMA + Constants.HOST_INPUT_PARAM_UTILITY_NUMBER + Constants.EQUALTO + callInfo.getField(Field.UTILITYCODE)
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			}catch(Exception e){}
			hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_UTILITYBILLPAYMENTBANK);
			//hostReportDetails.setHostOutParams(hostOutParams);
			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);

			hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It should be in the format of 31/07/2013 18:11:11
			hostReportDetails.setHostType(Constants.HOST_TYPE_ACCOUNTSERVICES);
			//End Reporting
			
			/*
			 *  Setting NA values
			 */
			hostReportDetails.setHostEndTime(Constants.NA);
			hostReportDetails.setHostOutParams(Constants.NA);
			hostReportDetails.setHostResponse(Constants.NA);
			
			callInfo.setHostReportDetails(hostReportDetails);
			IvrData ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
			callInfo.insertHostDetails(ivrdata);
			
			/* END */
			
			String hostEndTime = Constants.EMPTY_STRING;
			String hostResCode = Constants.EMPTY_STRING;

			
			String errDesc = Constants.NA;
			
			if(Constants.FEATURE_TYPE_BANK.equalsIgnoreCase((String)callInfo.getField(Field.FEATURETYPE))){

				String requestType = (String)ivr_FeatureData.getConfig().getParamValue(Constants.CUI_UTILITYBILLPAYMENT_REQUESTTYPE);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Request type is "+ requestType);}		
				
				electricityType = (String) callInfo.getField(Field.ELECTRICITY_TYPE);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Electricity type is " + electricityType );}
				
				billerCode = (String) callInfo.getField(Field.BILLER_CODE);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Biller Code is " + billerCode );}
				
				UpdatePaymentDetails_HostRes updatePaymentDetails_HostRes = utilityBillPaymentDAO.getUtilityBillUpdPaymentHostRes(callInfo, paymentType, utilityCode, 
						str_serviceProviderCode, electricityType, billerCode, benfCode, contractNo, billNo, accessChannel, paymentMethod, paymentStatus, msisdn, debitAcctID, bigDebitAmt, debitValueDate, bigPayamt, bigDueAmt, bonusRechrgAmt, requestType);
				
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "updatePaymentDetails_HostRes Object is :"+ updatePaymentDetails_HostRes);}

				callInfo.setUpdatePaymentDetails_HostRes(updatePaymentDetails_HostRes);

				code = updatePaymentDetails_HostRes.getErrorCode();
				errDesc = updatePaymentDetails_HostRes.getErrorDesc();
				//Setting transactionRefNo
				callInfo.setField(Field.Transaction_Ref_No, updatePaymentDetails_HostRes.getXferID());

				hostEndTime = updatePaymentDetails_HostRes.getHostEndTime();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
				hostReportDetails.setHostEndTime(hostEndTime);

				hostResCode = updatePaymentDetails_HostRes.getHostResponseCode();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
				hostReportDetails.setHostResponse(hostResCode);
				
				
				
				/**
				 * Updating the isOTPCalledAfterDisconnect flag as false
				 */
				callInfo.setField(Field.ISOTPCALLEDAFTERDISCONNECT, false);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "updating Is OTP Called After Disconnected " +false );}
				//END
				
				
				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Success for UtilityBillPaymentConfirmationImpl.UpdateBillPayment");}
					
					
					
					
					/**
					 * Following are the changes done for Utility Mobile top up module
					 */
					
					if(Constants.FEATURENAME_UTILITYBILLPAYMENTTOPUP.equalsIgnoreCase((String)callInfo.getField(Field.FEATURENAME))){
						
						HashMap<String, String>lastEnteredMobileNumberMap = null;
						
						lastEnteredMobileNumberMap = util.isNullOrEmpty(callInfo.getField(Field.LASTENTEREDMOBILENOMAP)) ? new HashMap<String, String>() : (HashMap<String, String>) callInfo.getField(Field.LASTENTEREDMOBILENOMAP);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Last Entered Mobile Number map retrieved from the DB is "+ lastEnteredMobileNumberMap);}
						
						selectedacctNo = (String)callInfo.getField(Field.DESTNO);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "User selected account / mobile / landline / internet number is "+ util.getSubstring(selectedacctNo, Constants.GL_FOUR));}
						
						serviceProviderCode = (String)callInfo.getField(Field.SELECTEDSERVICEPROVIDER);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "User selected service porvide code is "+ serviceProviderCode);}
						
						lastEnteredMobileNumberMap.put(serviceProviderCode, selectedacctNo);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Finalized last selected mobile number map is  "+ lastEnteredMobileNumberMap);}
						
						callInfo.setField(Field.LASTENTEREDMOBILENOMAP, lastEnteredMobileNumberMap);
						callInfo.setField(Field.LASTSELECTEDMOBILENUMBER,selectedacctNo);
					}

					//END - Vinoth
					
					
					/**
					 * Following are handled for updating the enter amount after getting success from host
					 */
					try{
					IEnterAmount iEnterAmount = Context.getiEnterAmount();
					String result = iEnterAmount.UpdateEnteredAmount(callInfo);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The result of updating the entered amount result is "+ result);}
					}catch(Exception e){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Exception occured in while updating the entered amount result is "+ e);}
					}
					//End
					
				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for Deposit Dtls Inquriry host service");}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + updatePaymentDetails_HostRes.getHostResponseCode());}

					util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_UTILITYBILLPAYMENTBANK, updatePaymentDetails_HostRes.getHostResponseCode());
					/**
					 * Following will be called only if there occured account selection before this host access
					 */
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
					util.setEligibleAccountCounts(callInfo, hostResCode);

				}

			}
			else{
			String cardNumber = null;
			String traceNo = null;
			String merchantCategoryCode = null;
			String acquiringInstitutionCountry = null;
			String merchantID = null;
			String retrievalReference = null;
			String terminalID = null;
			String transactionCCY = null; 
			String addlPOSInfo = null;
			
			
			
			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("iceFeatureData object is null or empty");
			}
			
			String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_UTILITYBILLPAYMENT_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_UTILITYBILLPAYMENT_REQUESTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}

			
			 UpdatePaymentDetailsCC_HostRes updatePaymentDetails_HostRes_CC = utilityBillPaymentCCDAO.getCCUtilityBillUpdPaymentHostRes(callInfo, paymentType, 
					utilityCode, str_serviceProviderCode, contractNo, billNo, msisdn, bigPayamt, bonusRechrgAmt, cardNumber, traceNo, 
					merchantCategoryCode, acquiringInstitutionCountry, merchantID, retrievalReference, terminalID, transactionCCY, addlPOSInfo, requestType);
			 
			 if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "updatePaymentDetails_HostRes_CC Object is :"+ updatePaymentDetails_HostRes_CC);}

				callInfo.setUpdatePaymentDetailsCC_HostRes(updatePaymentDetails_HostRes_CC);

				code = updatePaymentDetails_HostRes_CC.getErrorCode();
				errDesc = updatePaymentDetails_HostRes_CC.getErrorDesc();
				
				//Setting transactionRefNo
				callInfo.setField(Field.Transaction_Ref_No, updatePaymentDetails_HostRes_CC.getXferID());

				hostEndTime = updatePaymentDetails_HostRes_CC.getHostEndTime();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
				hostReportDetails.setHostEndTime(hostEndTime);

				hostResCode = updatePaymentDetails_HostRes_CC.getHostResponseCode();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
				hostReportDetails.setHostResponse(hostResCode);
			
				/**
				 * Updating the isOTPCalledAfterDisconnect flag as false
				 */
				callInfo.setField(Field.ISOTPCALLEDAFTERDISCONNECT, false);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "updating Is OTP Called After Disconnected " +false );}
				//END
				
				
				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Success for UtilityBillPaymentConfirmationImpl.UpdateBillPayment");}
					
					/**
					 * Following are handled for updating the enter amount after getting success from host
					 */
					try{
					IEnterAmount iEnterAmount = Context.getiEnterAmount();
					String result = iEnterAmount.UpdateEnteredAmount(callInfo);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The result of updating the entered amount result is "+ result);}
					}catch(Exception e){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Exception occured in while updating the entered amount result is "+ e);}
					}
					//End
				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for Deposit Dtls Inquriry host service");}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + updatePaymentDetails_HostRes_CC.getHostResponseCode());}

					util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_UTILITYBILLPAYMENTBANK, updatePaymentDetails_HostRes_CC.getHostResponseCode());
					/**
					 * Following will be called only if there occured account selection before this host access
					 */
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
					util.setEligibleAccountCounts(callInfo, hostResCode);

				}
			}
			
			 String responseDesc = Constants.HOST_FAILURE;
				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
					responseDesc = Constants.HOST_SUCCESS;
				}
				
				//String strHostInParam = Constants.NA;
				try{
				strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + callInfo.getField(Field.CUSTOMERID)
						+Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO +util.maskCardOrAccountNumber((String)callInfo.getField(Field.SRCNO))
						+Constants.COMMA + Constants.HOST_INPUT_PARAM_DESTINATION_NO + Constants.EQUALTO + util.maskCardOrAccountNumber((String)callInfo.getField(Field.DESTNO))
						+Constants.COMMA + Constants.HOST_INPUT_PARAM_AMOUNT + Constants.EQUALTO + callInfo.getField(Field.AMOUNT)
						+Constants.COMMA + Constants.HOST_INPUT_PARAM_SERVICE_PROVIDER_CODE + Constants.EQUALTO + callInfo.getField(Field.SELECTEDSERVICEPROVIDER)
						+Constants.COMMA + Constants.HOST_INPUT_PARAM_UTILITY_NUMBER + Constants.EQUALTO + callInfo.getField(Field.UTILITYCODE)
						+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
				}catch(Exception e){}
				hostReportDetails.setHostInParams(strHostInParam);
				
				
				String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
						+ Constants.EQUALTO + hostResCode+ hostResCode+Constants.COMMA+Constants.HOST_OUTPUT_PARAM_TRANSREFNO + Constants.EQUALTO +callInfo.getField(Field.Transaction_Ref_No)
				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(errDesc) ?"NA" :errDesc);
				hostReportDetails.setHostOutParams(hostOutputParam);

				callInfo.setHostReportDetails(hostReportDetails);
				ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

				callInfo.updateHostDetails(ivrdata);
				//End Reporting
			 
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: UtilityBillPaymentConfirmationImpl.UpdateBillPayment()");}

		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at UtilityBillPaymentConfirmationImpl.UpdateBillPayment() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;
	}

	@Override
	public String UpdateRechargedMobNo(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: UtilityBillPaymentConfirmationImpl.UpdateRechargedMobNo()");}
		String dbcode = Constants.ONE;
		
		try {

			String sessionId = util.isNullOrEmpty( callInfo.getField(Field.SESSIONID))? Constants.EMPTY_STRING :  (String) callInfo.getField(Field.SESSIONID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Session id is " + sessionId);}
			
			HashMap<String, Object> configMap = new HashMap<String, Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the input for ConfigMap");}

			sessionId = (String)callInfo.getField(Field.SESSIONID);
			
			String customerId = util.isNullOrEmpty(callInfo.getField(Field.CUSTOMERID)) ?Constants.EMPTY_STRING : (String)(callInfo.getField(Field.CUSTOMERID));
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer id is " + customerId);}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method ");}
			configMap = new HashMap<String, Object>();
			
			String currentDate = util.getTodayDateOrTime(Constants.DATEFORMAT_YYYYMMDDHHMMSS);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current date is "+ currentDate);}
			
			String regMobileNo = util.isNullOrEmpty(callInfo.getField(Field.REG_MOBILENO)) ? Constants.EMPTY_STRING : (String)callInfo.getField(Field.REG_MOBILENO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Registered mobile number is "+ regMobileNo);}
			
			String lastSelectedMobileNo = util.isNullOrEmpty(callInfo.getField(Field.LASTSELECTEDMOBILENUMBER)) ? Constants.EMPTY_STRING : (String)callInfo.getField(Field.LASTSELECTEDMOBILENUMBER);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Last Selected mobile number is "+ lastSelectedMobileNo);}
			
			configMap.put(DBConstants.CLI, regMobileNo);
			configMap.put(DBConstants.TOPUP_MOBILE_NUMBER, lastSelectedMobileNo);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Registered Mobile number / CLI is " + regMobileNo );}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Top UP Mobile number is " + lastSelectedMobileNo );}
			
			String uui = (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UUI of the call is " + uui);}

			
			DataServices dataServices = VRUDBDataServicesInstance.getInstance();
			try {
				dbcode = dataServices.insertCustDetails(logger, sessionId, uui, configMap);
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Inserterd the Cust Details successfully");}
				
			} catch (com.db.exception.ServiceException e) {
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR: callerIdenticiationImpl.calling insertcustdetail()");}
				dbcode = Constants.ONE;
				//e.printStackTrace();
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Result of the DB method call is " + dbcode );}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Exit: callerIdenticiationImpl.calling insertcustdetail()");}
			
		} catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR, e,   "EXCEPTION at:  callerIdenticiationImpl.calling insertcustdetail()" + e.getMessage());
			throw new ServiceException(e);
		}
	
		return dbcode;
	}

	@Override
	public String UtilityBillPaymentConfPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: UtilityBillPaymentConfirmationImpl.UtilityBillPaymentConfPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			//Need to get the FeatureConfig Data

			String selectedServProv = (String)callInfo.getField(Field.SELECTEDSERVICEPROVIDER);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "selectedServProv code is "+ selectedServProv);}
			
			String utilityCode = (String)callInfo.getField(Field.UTILITYCODE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected utility code is "+ utilityCode);}

			String amount = (String)callInfo.getField(Field.AMOUNT);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected Amount is "+ amount);}
			
			String billTypePhrase = Constants.EMPTY_STRING;

			String ui_landlineCode = (String) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_UTILITY_CODE_FOR_LANDLINE);
			String ui_MobileCode = (String) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_UTILITY_CODE_FOR_MOBILE);
			String ui_InternetCode = (String) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_UTILITY_CODE_FOR_INTERNET);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Landline Utility code is "+ ui_landlineCode);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Mobile Utility code is " + ui_MobileCode);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Internet Utility code is " + ui_InternetCode);}

			ui_landlineCode = util.isNullOrEmpty(ui_landlineCode)?Constants.EMPTY_STRING:ui_landlineCode;
			ui_MobileCode = util.isNullOrEmpty(ui_MobileCode)?Constants.EMPTY_STRING:ui_MobileCode;
			ui_InternetCode = util.isNullOrEmpty(ui_InternetCode)?Constants.EMPTY_STRING:ui_InternetCode;

			if(util.isCodePresentInTheConfigurationList(utilityCode, ui_landlineCode)){
				billTypePhrase = DynaPhraseConstants.Top_Up_1013;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Considering Landling as billtype");}
			}else if(util.isCodePresentInTheConfigurationList(utilityCode, ui_MobileCode)){
				billTypePhrase = DynaPhraseConstants.Top_Up_1014;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Considering mobile as billtype");}
			}else if(util.isCodePresentInTheConfigurationList(utilityCode, ui_InternetCode)){
				billTypePhrase = DynaPhraseConstants.Top_Up_1015;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Considering internet as billtype");}
			}else{
				billTypePhrase = DynaPhraseConstants.Account_Selection_1001;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Considering general account as billtype");}
			}


			String destNumber = (String)callInfo.getField(Field.DESTNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Destination number" +util.maskCardOrAccountNumber(destNumber));}

			String sourceTypePhrase = Constants.EMPTY_STRING;
			String featureType = (String)callInfo.getField(Field.FEATURETYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Feature type is " + featureType);}

			if(Constants.FEATURE_TYPE_CARD.equalsIgnoreCase(featureType)){
				sourceTypePhrase = DynaPhraseConstants.Account_Selection_1004;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Credit card as source type phrase");}
			}else{
				//Since the recorded phrases have been recorded wrongly modifing below prompts
//				sourceTypePhrase = DynaPhraseConstants.Account_Selection_1003;
				sourceTypePhrase = DynaPhraseConstants.SILENCE_PHRASE;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Account as source type phrase");}
			}

			String moreCount = (String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);

			String sourceAcctNo = (String)callInfo.getField(Field.SRCNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected source number ending with "+ util.getSubstring(sourceAcctNo, Constants.GL_FOUR));}
			if(!util.isNullOrEmpty(sourceAcctNo)){
				sourceAcctNo = util.getSubstring(sourceAcctNo, Constants.GL_FOUR);
			}
			
			
			
			
			/**
			 * Getting the customer category type from the account detail map object
			 */
			String customerCategoryType = Constants.DEFAULT;
			if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "getCallerIdentification_HostRes is not empty or null ");}
				
				if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getAccountDetailMap())){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account Detail Map object is null or empty");}
					
					HashMap<String, AcctInfo>acctDetailMap = callInfo.getCallerIdentification_HostRes().getAccountDetailMap();
					if(!util.isNullOrEmpty((String)callInfo.getField(Field.SRCNO)) && !util.isNullOrEmpty(acctDetailMap.get((String)callInfo.getField(Field.SRCNO)))){
						
						customerCategoryType = acctDetailMap.get((String)callInfo.getField(Field.SRCNO)).getCategory();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Category type is "+ customerCategoryType);}
						
						if(util.isNullOrEmpty(customerCategoryType) || util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_UBP_TRANSACTION_FEE + Constants.UNDERSCORE+ customerCategoryType))){
							customerCategoryType = Constants.DEFAULT;
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Category type is resetted to Default"+ customerCategoryType);}	
						}
					}
				}
				
			}
			
			
			String transactionFee = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_UBP_TRANSACTION_FEE + Constants.UNDERSCORE+ customerCategoryType))?
					Constants.ZERO : (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_UBP_TRANSACTION_FEE + Constants.UNDERSCORE+ customerCategoryType);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured UBP Transaction fee amount is "+transactionFee);}
			

			//String transactionFee = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_UBP_TRANSACTION_FEE);
			callInfo.setField(Field.TransactionFee, transactionFee);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured UBP Transaction fee amount is "+transactionFee);}
			
			double double_TransFee = util.isNullOrEmpty(transactionFee)?Constants.GL_ZERO : Double.parseDouble(transactionFee);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction fee converted to double  is "+double_TransFee);}
			
			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;

			//Need to handle the Dynamic phrase list and Mannual Grammar portions

			dynamicValueArray.add(amount);
			dynamicValueArray.add((selectedServProv+Constants.WAV_EXTENSION).trim());
			dynamicValueArray.add(billTypePhrase);
			dynamicValueArray.add(destNumber);
			dynamicValueArray.add(sourceTypePhrase);
			dynamicValueArray.add(sourceAcctNo);

			if(!util.isNullOrEmpty(double_TransFee) && double_TransFee > Constants.GL_ZERO){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "including transaction fee also for confirmation announcements");}
				dynamicValueArray.add(DynaPhraseConstants.PHRASE_CHARGING_TRANSFEE); 
				dynamicValueArray.add(transactionFee); 
			}else{
				dynamicValueArray.add(Constants.NA); 
				dynamicValueArray.add(Constants.NA); 
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("UTILITY_PAYMENT_CONFIRMATION");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("Utility_Bill_Payment_Confirmation");
			String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+menuID;

			/**
			 * Special handling for confirmation menus only
			 */
			if(util.isNullOrEmpty(double_TransFee) || double_TransFee <= Constants.GL_ZERO){
				combinedKey = combinedKey+Constants.CONSTANT_A;
				combinedKey = combinedKey.trim();
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

			Object[] object = new Object[dynamicValueArray.size()];
			for(int count=0; count<dynamicValueArray.size();count++){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ count +"element: "+dynamicValueArray.get(count) +"into Object array ");}
				object[count] = dynamicValueArray.get(count);

			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"objArray  is :" + object);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Default wave file  is :" + DynaPhraseConstants.SILENCE_PHRASE);}


			str_GetMessage =  this.messageSource.getMessage(combinedKey, object, DynaPhraseConstants.SILENCE_PHRASE, locale );
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The property value for the get Message method is " + str_GetMessage);}

			if(str_GetMessage.equalsIgnoreCase(DynaPhraseConstants.SILENCE_PHRASE)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Assigning Silence phrase as result");}
				return (DynaPhraseConstants.SILENCE_PHRASE);
			}

			int totalPrompt = util.getTotalPromptCount(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt received from the dynaproperty file is "+totalPrompt);}


//			//Overriding the total prompts, received from the property file
//			if(!util.isNullOrEmpty(double_TransFee) && double_TransFee > Constants.GL_ZERO){
//				totalPrompt = Constants.GL_TWENTY;
//			}
//			else{
//				totalPrompt = Constants.GL_EIGHTEEN;
//			}
//
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}

			//To have the property file grammar, need to call that util method here

			grammar = util.getGrammar(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}
			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);

			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}


			//Need to handle if we want to append pipeseperator sign

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: UtilityBillPaymentConfirmationImpl.UtilityBillPaymentConfPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at UtilityBillPaymentConfirmationImpl.UtilityBillPaymentConfPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public String UtilityBillPaymentSuccPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: UtilityBillPaymentConfirmationImpl.UtilityBillPaymentSuccPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();

			//Need to get the FeatureConfig Data
			String transactionRefNumber = Constants.EMPTY_STRING;

			if(!util.isNullOrEmpty(callInfo.getUpdatePaymentDetails_HostRes())){
				if(!util.isNullOrEmpty(callInfo.getUpdatePaymentDetails_HostRes().getXferID())){
					transactionRefNumber = (String)callInfo.getUpdatePaymentDetails_HostRes().getXferID();
				}
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The transaction reference id is "+transactionRefNumber);}

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_Str = Constants.EMPTY_STRING;

			//Need to handle the Dynamic phrase list and Mannual Grammar portions

			if(!util.isNullOrEmpty(transactionRefNumber)){
				dynamicValueArray.add(DynaPhraseConstants.PHRASE_TRANS_REF_NO);
				dynamicValueArray.add(transactionRefNumber);
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "the grammar value is" + grammar);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			//String menuID = MenuIDMap.getMenuID("");
			String anncID = AnncIDMap.getAnncID("Transaction_Success_Message");
			String featureID = FeatureIDMap.getFeatureID("Utility_Bill_Payment_Confirmation");
			String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

			Object[] object = new Object[dynamicValueArray.size()];
			for(int count=0; count<dynamicValueArray.size();count++){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ count +"element: "+dynamicValueArray.get(count) +"into Object array ");}
				object[count] = dynamicValueArray.get(count);

			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"objArray  is :" + object);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Default wave file  is :" + DynaPhraseConstants.SILENCE_PHRASE);}


			str_GetMessage =  this.messageSource.getMessage(combinedKey, object, DynaPhraseConstants.SILENCE_PHRASE, locale );
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The property value for the get Message method is " + str_GetMessage);}

			if(str_GetMessage.equalsIgnoreCase(DynaPhraseConstants.SILENCE_PHRASE)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Assigning Silence phrase as result");}
				return (DynaPhraseConstants.SILENCE_PHRASE);
			}

			int totalPrompt = util.getTotalPromptCount(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt received from the dynaproperty file is "+totalPrompt);}


			//Overriding the total prompts, received from the property file
			if(!util.isNullOrEmpty(transactionRefNumber)){
				totalPrompt = Constants.GL_THREE;
			}
			else{
				totalPrompt = Constants.GL_ONE;
			}
			//To have the property file grammar, need to call that util method here
			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);

			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}

			//Need to handle if we want to append pipeseperator sign
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: UtilityBillPaymentConfirmationImpl.UtilityBillPaymentSuccPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at UtilityBillPaymentConfirmationImpl.UtilityBillPaymentSuccPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

}
