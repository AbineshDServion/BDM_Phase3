package com.servion.services;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.bankmuscat.esb.commontypes.AddressInfoType;
import com.bankmuscat.esb.commontypes.PersonInfoType;
import com.reportbean.IvrData;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.context.Context;
import com.servion.dao.CCAcctStmtInqDAO;
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
import com.servion.model.creditCardBalance.CCEntityFields;
import com.servion.model.creditCardBalance.CreditCardBalanceDetails_HostRes;
import com.servion.model.reporting.HostReportDetails;
import com.servion.model.transactionDetaitCards.TransDtls_CCTrxnDetails;
import com.servion.model.transactionDetaitCards.TransDtls_CardStmtDetails;
import com.servion.model.transactionDetaitCards.TransactionDetailCards_HostReq;

public class TransactionDetailsCardsImpl implements ITransactionDetailsCards  {
	
	private static Logger logger = LoggerObject.getLogger();
	private CCAcctStmtInqDAO ccAcctStmtInqDAO;
	public CCAcctStmtInqDAO getCcAcctStmtInqDAO() {
		return ccAcctStmtInqDAO;
	}

	public void setCcAcctStmtInqDAO(CCAcctStmtInqDAO ccAcctStmtInqDAO) {
		this.ccAcctStmtInqDAO = ccAcctStmtInqDAO;
	}

	private MessageSource messageSource;

	public MessageSource getMessageSource() {
		return messageSource;
	}

	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
	

	@Override
	public String getCardsEmailStatementRequest(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: TransactionDetailsCardImpl.getCardsEmailStatementRequest()");}
		String code = Constants.EMPTY_STRING;

		try{
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "FaxImpl.SendLogFaxRequest()");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalObject = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalObject)){
				throw new ServiceException("ivr_ICEGlobalObject is null / empty");
			}

			String featureType = (String)callInfo.getField(Field.FEATURETYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Feature type is "+ featureType);}

			String featureName = (String)callInfo.getField(Field.FEATURENAME);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Feature name is "+ featureName);}


			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}

			/**  
			 * For Reporting Purpose
			 */
			HostReportDetails hostReportDetails = new HostReportDetails();

			String featureId = (String)callInfo.getField(Field.FEATUREID);
			hostReportDetails.setHostActiveMenu(featureId);
			//hostReportDetails.setHostCounter(hostCounter);
			//hostReportDetails.setHostEndTime(hostEndTime);
			
			
			String smtp_ToEmailID = Constants.EMPTY_STRING + callInfo.getField(Field.REG_EMAIL);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Email id is " + smtp_ToEmailID);}
			
			/*String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + 
					callInfo.getField(Field.CUSTOMERID) + Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + callInfo.getField(Field.SRCNO)
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_EMAIL_ID + Constants.EQUALTO + smtp_ToEmailID
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;*/
			String sourceNo = util.isNullOrEmpty(callInfo.getField(Field.SRCNO))? Constants.EMPTY_STRING : (String)callInfo.getField(Field.SRCNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected credit card number is "+ util.maskCardOrAccountNumber(sourceNo));}
			//08-09-2015 As per vijay Comment
			String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + callInfo.getField(Field.CUSTOMERID) + Constants.COMMA + Constants.HOST_INPUT_PARAM_FORM_TYPE + Constants.EQUALTO + "NA"
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_FORM_NAME + Constants.EQUALTO + "NA"
					//TODO: As per Judes requested on 14-09-2015
					/*****************/
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_TYPE + Constants.EQUALTO + (util.isNullOrEmpty(callInfo.getField(Field.SRCTYPE)) ? Constants.NA : (String) callInfo.getField(Field.SRCTYPE))
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_STMT_TYPE +Constants.EQUALTO + "STEMINI"
					/*****************/
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_REQUEST_TYPE + Constants.EQUALTO + Constants.FORMS_REQUEST_TYPE_EMAIL
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_EMAIL_ID + Constants.EQUALTO + smtp_ToEmailID
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + util.maskCardOrAccountNumber(sourceNo) // 29-03-2015 based on kaarthik & vijay request for report 
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
		
			
			hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_SMTPEMAIL);
			//hostReportDetails.setHostOutParams(hostOutParams);
			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);

			hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It should be in the formate of 31/07/2013 18:11:11
			hostReportDetails.setHostType(Constants.HOST_METHOD_SMTPEMAIL);

			String faxNumber = (String) callInfo.getField(Field.LASTSELECTEDFAXNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requested faxNumber "+ faxNumber);}

			String emailFileCopyLoc = null;
					//(String)callInfo.getField(Field.FAXFILELOCATION);

			emailFileCopyLoc = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_EMAIL_FILE_COPY_LOCATION))? null : (String) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_EMAIL_FILE_COPY_LOCATION);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requested fax file copy Location "+ emailFileCopyLoc);}
			
			String faxFileName = Constants.EMPTY_STRING;
			
			//Generating Fax file
			HashMap<String, Object> faxInfo = new HashMap<String, Object>();

			String customerName = Constants.EMPTY;
			String customerAddr1 = Constants.EMPTY;
			String customerAddr2= Constants.EMPTY;
			String customerAddr3 = Constants.EMPTY;
			String customerAddr4 = Constants.EMPTY;
			String customerAddr5 = Constants.EMPTY;
			String zip = Constants.EMPTY_STRING;
			String country = Constants.EMPTY_STRING;
			String state = Constants.EMPTY_STRING;
			String street = Constants.EMPTY_STRING;
			String cardOrAcctNumber = Constants.EMPTY;
			String currency = Constants.EMPTY;
			String accountType = Constants.EMPTY;
			String customerBranchCode = Constants.EMPTY;
			String period = Constants.EMPTY;
			String branchCode = Constants.EMPTY;
			

			if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerShortDetails())){
				if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerShortDetails().getPersonalInfoTypeList())){
					if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerShortDetails().getPersonalInfoTypeList().get(Constants.GL_ZERO))){
						PersonInfoType personInfoType = callInfo.getCallerIdentification_HostRes().getCustomerShortDetails().getPersonalInfoTypeList().get(Constants.GL_ZERO);
						customerName = personInfoType.getFirstName();
						if(!util.isNullOrEmpty(personInfoType.getAddress())){
							AddressInfoType addressInfoType =  personInfoType.getAddress().get(Constants.GL_ZERO);
							customerAddr1 = addressInfoType.getAddr1();
							customerAddr2 = addressInfoType.getAddr2();
							customerAddr3 = addressInfoType.getAddr3();
							customerAddr4 = addressInfoType.getAddr4();
							customerAddr5 = addressInfoType.getAddr5();
							zip = addressInfoType.getZip();
							country = addressInfoType.getCountry();
							state = addressInfoType.getState();
							street = addressInfoType.getStreet();
							branchCode = addressInfoType.getDelBranch();
						}
					}
				}
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer Address 1 " + customerAddr1);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer Address 2 " + customerAddr2);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer Address 3 " + customerAddr3);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer Address 4 " + customerAddr4);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer Address 5 " + customerAddr5);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "zip " + zip);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "country "  +country);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "state " + state);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "street " + street);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Branch Code is  " + branchCode);}

			String srcNo = (String)callInfo.getField(Field.SRCNO);
			cardOrAcctNumber = Constants.MASKING_START + util.getSubstring(srcNo, Constants.GL_FOUR);

			accountType = util.isNullOrEmpty(callInfo.getField(Field.SRCTYPE))? Constants.EMPTY : (String)callInfo.getField(Field.SRCTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account type is " + accountType) ;}

			//End Reporting
			int deviceID = util.isNullOrEmpty(callInfo.getField(Field.DEVICE_ID))? Constants.GL_ZERO :  Integer.parseInt((String)callInfo.getField(Field.DEVICE_ID));
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Device ID "+ deviceID);}
			
			String customerId = util.isNullOrEmpty(callInfo.getField(Field.CUSTOMERID))? Constants.EMPTY : (String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID is "+ customerId);}

			if(!util.isNullOrEmpty(customerId)){
				faxFileName = customerId;
				faxFileName =faxFileName +  Constants.UNDERSCORE;
			}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fax file with customer id is "+ faxFileName);}
//			if(!util.isNullOrEmpty(srcNo)){
//				faxFileName = faxFileName +  srcNo;
//				faxFileName = faxFileName + Constants.UNDERSCORE;
//			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fax file with customer id + source no is generated");}
			if(Constants.FEATURE_TYPE_BANK.equalsIgnoreCase(featureType)){
				faxFileName = faxFileName + Constants.FEATURE_TYPE_BANK;
				faxFileName = faxFileName + Constants.UNDERSCORE;
			}else{
				faxFileName = faxFileName + Constants.FEATURE_TYPE_CARD;
				faxFileName = faxFileName + Constants.UNDERSCORE;
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fax file with customer id + source no  + feature type is generated ");}
			
			String currentDate = util.getTodayDateOrTime(Constants.DATEFORMAT_YYYYMMDDHHMMSS);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Today's date and time is "+ currentDate);}

			faxFileName = faxFileName + currentDate;
			faxFileName = faxFileName + Constants.PDF_EXTENSION;
			
			String pdfFaxFormName = faxFileName;
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final fax file location is "+ emailFileCopyLoc);}
			
			faxFileName = emailFileCopyLoc + faxFileName;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Fax File name is formed");}
			callInfo.setField(Field.FAXFILENAME, faxFileName);

			if(util.isNullOrEmpty(emailFileCopyLoc)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fax file location is null / empty");}
				throw new ServiceException("Fax file location is null or empty");
			}


			String customerAddress = Constants.EMPTY_STRING;

			if(!util.isNullOrEmpty(customerAddr1)){
				customerAddress = customerAddr1+"\n";
			}

			if(!util.isNullOrEmpty(customerAddr1)){
				customerAddress = customerAddr1+"\n";
			}
			if(!util.isNullOrEmpty(customerAddr2)){
				customerAddress = customerAddress + customerAddr2+"\n";
			}
			if(!util.isNullOrEmpty(customerAddr3)){
				customerAddress = customerAddress + customerAddr3+"\n";
			}
			if(!util.isNullOrEmpty(customerAddr4)){
				customerAddress = customerAddress + customerAddr4+"\n";
			}
			if(!util.isNullOrEmpty(customerAddr5)){
				customerAddress = customerAddress + customerAddr5+"\n";
			}
			if(!util.isNullOrEmpty(street)){
				customerAddress = customerAddress + street+"\n";
			}
			if(!util.isNullOrEmpty(state)){
				customerAddress = customerAddress + state+"\n";
			}
			if(!util.isNullOrEmpty(country)){
				customerAddress = customerAddress + country+"\n";
			}
			if(!util.isNullOrEmpty(zip)){
				customerAddress = customerAddress + zip+"\n";
			}

			String mergingFaxFileLoc = util.isNullOrEmpty(callInfo.getField(Field.MERGINGFAXLOCATION))? Constants.EMPTY : (String)callInfo.getField(Field.MERGINGFAXLOCATION);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Merging fax file location is "+ mergingFaxFileLoc);}

			if(util.isNullOrEmpty(mergingFaxFileLoc)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Merging fax file location is null / empty");}
				throw new ServiceException("Merging fax file location is null or empty");
			}
			
			String channelNo = util.isNullOrEmpty(callInfo.getField(Field.CHANNELNO))?Constants.EMPTY : (String)callInfo.getField(Field.CHANNELNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Channel No is "+ channelNo);}
			
			mergingFaxFileLoc = mergingFaxFileLoc + channelNo + Constants.BACKSLASH;
			mergingFaxFileLoc = mergingFaxFileLoc + Constants.FAX_MODULE_MERGEFAXFILE;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Updated merging fax location with channel no is "+ mergingFaxFileLoc);}
			
			boolean faxSent = false;
			
			String fileTemplatePath = (String)callInfo.getField(Field.FAXTEMPLATEPATH);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fax file template path is "+ fileTemplatePath);}

			String noOfTransaction = (String)callInfo.getField(Field.NOOFTRANSPERPAGE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "No Of Transaction per page"+ noOfTransaction);}
			
			
			faxInfo.put(Constants.FAX_MODULE_FAXFILENAME, faxFileName);
			faxInfo.put(Constants.FAX_MODULE_NOOFTRANSPERPAGE, noOfTransaction);
			faxInfo.put(Constants.FAX_MODULE_ADDRESS1, customerAddr1);
			faxInfo.put(Constants.FAX_MODULE_ADDRESS2, customerAddr2);
			faxInfo.put(Constants.FAX_MODULE_ADDRESS3, customerAddr3);
			faxInfo.put(Constants.FAX_MODULE_ADDRESS4, customerAddr4);
			faxInfo.put(Constants.FAX_MODULE_ADDRESS5, customerAddr5);
			faxInfo.put(Constants.FAX_MODULE_ZIP, zip);
			faxInfo.put(Constants.FAX_MODULE_STATE, state);
			faxInfo.put(Constants.FAX_MODULE_STREET, street);
			faxInfo.put(Constants.FAX_MODULE_COUNTRY, country);
			faxInfo.put(Constants.FAX_MODULE_BRANCH, branchCode);
			faxInfo.put(Constants.FAX_MODULE_MERGEDTEMPLATEFORTHISCALLER, mergingFaxFileLoc);

			

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Going to fetch the transaction details of Cards");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Upding the Card Template file name along with the fax template name "+ fileTemplatePath);}
			fileTemplatePath = fileTemplatePath + Constants.CARD_TEMPLATE_NAME;
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account final template file name and path is "+ fileTemplatePath);}
			faxInfo.put(Constants.FAX_MODULE_FAXTEMPLATEPATH, fileTemplatePath);
			
			ArrayList<TransDtls_CCTrxnDetails> transDtls_CcTrxnDetails = null;

			int int_CashADv = 0;
			
//			String cashAdv = Constants.EMPTY_STRING;
			String paymentAndOtherCredit = Constants.EMPTY_STRING;
			String cashAdv = Constants.EMPTY_STRING;
			double temp = 0;
			double temp_CashAdv = 0;
			double double_paymentAndOtherCredit = 0;
			double double_CashAdv = 0;
			double double_PreviousBalance = 0;
			double double_PurchaseAndCharges = 0;
			
			if(!util.isNullOrEmpty(callInfo.getTransactionDetailCards_HostReq()) && !util.isNullOrEmpty(callInfo.getTransactionDetailCards_HostReq().getTransDtls_Transaction())
					&& !util.isNullOrEmpty(callInfo.getTransactionDetailCards_HostReq().getTransDtls_Transaction().getTransactionMap())){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Going to fetch the transaction details of Cards");}

				transDtls_CcTrxnDetails = callInfo.getTransactionDetailCards_HostReq().getTransDtls_Transaction().getTransactionMap().get(srcNo);

				String mcc = Constants.EMPTY_STRING;
				String procCode = Constants.EMPTY_STRING;

				String cui_Mcc = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_CASH_ADV_MCC_CODE);
				String cui_Proc = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_CASH_ADV_PROC_CODE);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured cash Advance MCC code is "  + cui_Mcc);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured cash Advance Proc code is " + cui_Proc);}

				TransDtls_CCTrxnDetails transDtls_CCTrxnDetails = null;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Obtained transaction list of selected credit card is " + transDtls_CcTrxnDetails);}

				//				if(!util.isNullOrEmpty(transDtls_CcTrxnDetails) ){
				//					if(!util.isNullOrEmpty(cui_Mcc) || !util.isNullOrEmpty(cui_Proc)){
				//						for(int i= 0; i < transDtls_CcTrxnDetails.size(); i ++){
				//
				//							transDtls_CCTrxnDetails = transDtls_CcTrxnDetails.get(i);
				//							mcc = transDtls_CCTrxnDetails.getMcc();
				//
				//							if(util.isCodePresentInTheConfigurationList(mcc, cui_Mcc) ||
				//									util.isCodePresentInTheConfigurationList(procCode, cui_Proc)){
				//								cashAdv = transDtls_CCTrxnDetails.getTrxnAmount();
				//								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Cash Adv amount is "  + cashAdv);}
				//
				//								temp = !util.isNullOrEmpty(cashAdv)? Integer.parseInt(cashAdv): Constants.GL_ZERO;
				//								int_CashADv = int_CashADv + temp;
				//							}
				//
				//						}
				//					}
				//				}

				if(!util.isNullOrEmpty(transDtls_CcTrxnDetails) ){
					if(!util.isNullOrEmpty(cui_Mcc) || !util.isNullOrEmpty(cui_Proc)){
						for(int i= 0; i < transDtls_CcTrxnDetails.size(); i ++){

							transDtls_CCTrxnDetails = transDtls_CcTrxnDetails.get(i);

							if(!util.isNullOrEmpty(transDtls_CCTrxnDetails)){

								mcc = transDtls_CCTrxnDetails.getMcc();
								procCode = transDtls_CCTrxnDetails.getProcCode();

								paymentAndOtherCredit = transDtls_CCTrxnDetails.getAmount();
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "paymentAndOtherCredit is "  + paymentAndOtherCredit);}

								temp = !util.isNullOrEmpty(paymentAndOtherCredit)? Double.parseDouble(paymentAndOtherCredit): Constants.GL_ZERO;

								if(temp > Constants.GL_ZERO){
									double_paymentAndOtherCredit = double_paymentAndOtherCredit + temp;
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the payment and other credit calculation since, it is a not a negative amount" + double_paymentAndOtherCredit);}
								}else{
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Neglecting the payment and other credit calculation since, it is a negative amount");}
								}

								if(util.isCodePresentInTheConfigurationList(mcc, cui_Mcc) ||
										util.isCodePresentInTheConfigurationList(procCode, cui_Proc)){

									cashAdv = transDtls_CCTrxnDetails.getAmount();
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Cash Advance is "  + cashAdv);}

									temp_CashAdv = !util.isNullOrEmpty(cashAdv)? Double.parseDouble(cashAdv): Constants.GL_ZERO;
									double_CashAdv = double_CashAdv + temp_CashAdv;

								}
							}
						}
					}
				}
			}

			
			/**
			 * Following are the fix done on 22-Jan-2015 for rounding off the value
			 */
			NumberFormat formatter = new DecimalFormat("#.###");
			formatter.setRoundingMode(RoundingMode.DOWN);
			String roundOffValue = Constants.EMPTY;
			//END - Vinoth
			
//			double_paymentAndOtherCredit = Math.round(double_paymentAndOtherCredit*1000)/1000;
//			paymentAndOtherCredit = double_paymentAndOtherCredit + Constants.EMPTY_STRING;
			paymentAndOtherCredit = formatter.format(double_paymentAndOtherCredit);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final paymentAndOtherCreditvalue is " + paymentAndOtherCredit);}

//			double_CashAdv = Math.round(double_CashAdv*1000)/1000;
//			cashAdv = double_CashAdv + Constants.EMPTY_STRING;
			cashAdv = formatter.format(double_CashAdv);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Cash Advance value is " + cashAdv);}

//			cashAdv = int_CashADv + Constants.EMPTY_STRING;
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final cash Adv value is " + cashAdv);}

			String previousBalance = Constants.EMPTY_STRING;
			String lastPaymentDate = Constants.EMPTY_STRING;
			double totalAmountDue = Constants.GL_ZERO;
			String minAmountDue = Constants.EMPTY_STRING;
			String creditLimit = Constants.EMPTY_STRING;
			
			TransDtls_CardStmtDetails transDtls_CardStmtDetails = null;
			
			String CCcard_accountNumber = String.valueOf(callInfo.getField(Field.CCACCTNOFORSTMTREQ));
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Obtained Account number is  : "+ CCcard_accountNumber);}

			if(!util.isNullOrEmpty(callInfo.getTransactionDetailCards_HostReq()) && !util.isNullOrEmpty(callInfo.getTransactionDetailCards_HostReq().getTransDtls_CardStatementHeader())
					&& !util.isNullOrEmpty(callInfo.getTransactionDetailCards_HostReq().getTransDtls_CardStatementHeader().getCardStamtDetailMap())){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Going to fetch the transDtls_CardStmtDetails of Cards");}

				//transDtls_CardStmtDetails = callInfo.getTransactionDetailCards_HostReq().getTransDtls_CardStatementHeader().getCardStamtDetailMap().get(srcNo);//27-02-2015 based on faisal & kaarthick comments and map contains the account number as a key not a card number
				transDtls_CardStmtDetails = callInfo.getTransactionDetailCards_HostReq().getTransDtls_CardStatementHeader().getCardStamtDetailMap().get(CCcard_accountNumber);
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Obtained transaction Map1 : "+ callInfo.getTransactionDetailCards_HostReq().getTransDtls_CardStatementHeader().getCardStamtDetailMap());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Obtained transaction Map2 : "+ callInfo.getTransactionDetailCards_HostReq().getTransDtls_CardStatementHeader().getCardStamtDetailMap());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Obtained transaction Map1 : "+ callInfo.getTransactionDetailCards_HostReq().getTransDtls_CardStatementHeader().getCardStamtDetailMap().get(srcNo));}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Obtained transaction Map2 : "+ callInfo.getTransactionDetailCards_HostReq().getTransDtls_CardStatementHeader().getCardStamtDetailMap().get(CCcard_accountNumber));}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Obtained transaction Map3 : "+ callInfo.getTransactionDetailCards_HostReq().getTransDtls_Transaction().getTransactionMap());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Obtained transDtls_CardStmtDetails object of selected credit card is " + transDtls_CardStmtDetails);}
			previousBalance = util.isNullOrEmpty(transDtls_CardStmtDetails)? Constants.EMPTY_STRING : transDtls_CardStmtDetails.getOpeningBalance();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Previous Balance is  " + previousBalance);}

			double_PreviousBalance = !util.isNullOrEmpty(previousBalance)?Double.parseDouble(previousBalance): Constants.GL_ZERO;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Double converted Previous Balance is  " + double_PreviousBalance);}
			
			previousBalance =  formatter.format(double_PreviousBalance);

//			double_PreviousBalance = Math.round(double_PreviousBalance*1000)/1000;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final double_PreviousBalance value is " + double_PreviousBalance);}

			/**
			 * Modified as per Faraz confirmation - 12-Jun-2014
			 */
			lastPaymentDate = util.isNullOrEmpty(transDtls_CardStmtDetails)? Constants.EMPTY_STRING : transDtls_CardStmtDetails.getDueDate();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Last payment Date is "+ lastPaymentDate);}

			totalAmountDue =  util.isNullOrEmpty(transDtls_CardStmtDetails)? Constants.GL_ZERO : Double.parseDouble(transDtls_CardStmtDetails.getClosingBalance());
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total Amount due is "+ totalAmountDue);}

			/**
			 * As per Hussain's advise done the below on 30-Jun-2014
			 */
			/**
			 * Removing the rounding off at 21-Jan-2015
			 */
			double_PurchaseAndCharges = (-1 * totalAmountDue) - (-1 * double_PreviousBalance) - double_CashAdv + double_paymentAndOtherCredit;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The final purchase and other charges is "+ double_PurchaseAndCharges);}

//			double_PurchaseAndCharges = Math.round(double_PurchaseAndCharges*1000)/1000;
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "After rounding off the final purchase and other charges is "+ double_PurchaseAndCharges);}

			minAmountDue =  util.isNullOrEmpty(transDtls_CardStmtDetails)? Constants.EMPTY_STRING : transDtls_CardStmtDetails.getMinDueAmt();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Minimum Amount due is "+ minAmountDue);}
			
			double minAmountDue_dbt =  util.isNullOrEmpty(minAmountDue)? Constants.GL_ZERO : Double.parseDouble(minAmountDue);
			minAmountDue = formatter.format(minAmountDue_dbt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Rounded Minimum Amount due is "+ minAmountDue);}

			creditLimit = util.isNullOrEmpty(transDtls_CardStmtDetails)? Constants.EMPTY_STRING : transDtls_CardStmtDetails.getCreditLimit();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "creditLimit is "+ creditLimit);}
			
			double creditLimit_dbt =  util.isNullOrEmpty(creditLimit)? Constants.GL_ZERO : Double.parseDouble(creditLimit);
			creditLimit = formatter.format(creditLimit_dbt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Rounded Minimum Amount due is "+ creditLimit);}

			
			ICardBalance iCardBalance = Context.getiCardBalance();
			String ccBalance_Code = iCardBalance.getCreditCardBalance(callInfo);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Call For CC Balance host access is " + ccBalance_Code);}

			String statementDate = Constants.EMPTY_STRING;
			String paymentDueDate = Constants.EMPTY_STRING;
//			String lastPaymentAmount = Constants.EMPTY_STRING;
//			String lastPaymentDate = Constants.EMPTY_STRING;
//			String balance = Constants.EMPTY_STRING;
//			String overDueAmount = Constants.EMPTY_STRING;

			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(ccBalance_Code)){
				CreditCardBalanceDetails_HostRes creditCardBalanceDetails_HostRes = callInfo.getCreditCardBalanceDetails_HostRes();
				if(util.isNullOrEmpty(creditCardBalanceDetails_HostRes)){
					throw new ServiceException("Card balance host response object bean is null");
				}

				Iterator iterator = null;
				HashMap<String, CCEntityFields> acctNo_AccountDetailMap =  null;
				CCEntityFields ccEntityFields = null;

				if(!util.isNullOrEmpty(creditCardBalanceDetails_HostRes.getCutomerID_AccountNumberMap())  && 
						creditCardBalanceDetails_HostRes.getCutomerID_AccountNumberMap().size() > Constants.GL_ZERO){

					acctNo_AccountDetailMap = creditCardBalanceDetails_HostRes.getAcctNo_AccountDetailMap();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account Detail object from the host is "+ acctNo_AccountDetailMap);}

					if(!util.isNullOrEmpty(acctNo_AccountDetailMap) && acctNo_AccountDetailMap.size() > Constants.GL_ZERO){

						iterator = acctNo_AccountDetailMap.entrySet().iterator();
						Map.Entry mapEntry = (Map.Entry) iterator.next();

						String accountNumber = (String) mapEntry.getKey();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account number retrieved from the host is "+ accountNumber);}

						ccEntityFields = (CCEntityFields) mapEntry.getValue();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved the CCEntityField object "+ ccEntityFields);}

						if(!util.isNullOrEmpty(ccEntityFields)){

							accountType = ccEntityFields.getAcctProduct();
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account product type is "+ accountType);}

							statementDate = ccEntityFields.getStmtGenerateDate();
							statementDate = util.convertDateStringFormat(statementDate, Constants.DATEFORMAT_YYYYMMDD, Constants.DATEFORMAT_YYYY_MM_DD);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "After Converting the date into yyyy-MM-dd the statement generation date is" + statementDate );}
							
							paymentDueDate = ccEntityFields.getStmtDueDate();
							paymentDueDate = util.convertDateStringFormat(paymentDueDate, Constants.DATEFORMAT_YYYYMMDD, Constants.DATEFORMAT_YYYY_MM_DD);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "After Converting the date into yyyy-MM-dd the payment due date is" + paymentDueDate );}

							//									lastPaymentAmount = ccEntityFields.getPaymentAmount();
							//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Last payment amount is "+ lastPaymentAmount);}

							//									balance = ccEntityFields.getStmtClosingDate();
							//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Statement Closing balance is "+ balance);}
							//
							//									overDueAmount = ccEntityFields.getOverDueAmt();
							//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Over Due amount is "+ overDueAmount);}
						}
					}
				}

			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Failure response for the host Call of CC Balance host access is " + ccBalance_Code);}
				throw new ServiceException("CCEntityInquiry host access is failed");
			}

			
			/**
			 * Commenting below as per the confirmation from Faraz 12-Jun-2014
			 */
			//					double int_balance = balance!=null ? Double.parseDouble(balance) : Constants.GL_ZERO;
			//					double int_overDueAmt = balance!=null ? Double.parseDouble(overDueAmount) : Constants.GL_ZERO;
			//					totalAmountDue = int_balance + int_overDueAmt;
			srcNo = util.maskCardOrAccountNumber(srcNo);
			
			
			faxInfo.put(Constants.FAX_MODULE_CARDNUMBER, srcNo);
			faxInfo.put(Constants.FAX_MODULE_CARDTYPE, accountType);
			faxInfo.put(Constants.FAX_MODULE_STATEMENTDATE, statementDate);
			faxInfo.put(Constants.FAX_MODULE_PAYMENTDUEDATE, lastPaymentDate); //commented based on kaarthick response
			//faxInfo.put(Constants.FAX_MODULE_PAYMENTDUEDATE, paymentDueDate);

			
			roundOffValue = formatter.format(totalAmountDue);
			faxInfo.put(Constants.FAX_MODULE_TOTALAMOUNTDUE, (roundOffValue));

		//	double minAmtDue_dbl = util.isNullOrEmpty(minAmountDue)?Constants.GL_ZERO : Double.parseDouble(minAmountDue);
			//minAmountDue = Constants.FAX_LOCAL_CURR + minAmtDue_dbl;
			faxInfo.put(Constants.FAX_MODULE_MINAMOUNTDUE, minAmountDue );
			faxInfo.put(Constants.FAX_MODULE_PREVIOUSBALANCE, previousBalance);
			
			roundOffValue = formatter.format(double_PurchaseAndCharges);
			faxInfo.put(Constants.FAX_MODULE_PURCHASECHARGES, roundOffValue);
			
			faxInfo.put(Constants.FAX_MODULE_CASHADVANCE, cashAdv);
			faxInfo.put(Constants.FAX_MODULE_PAYMENTCREDIT, paymentAndOtherCredit);
			faxInfo.put(Constants.FAX_MODULE_TRANSACTIONLIST, transDtls_CcTrxnDetails);
			faxInfo.put(Constants.FAX_MODULE_CREDITLIMIT, creditLimit);
			faxInfo.put(Constants.FAX_MODULE_CASHLIMIT, Constants.ZERO); //As per the confirmation given by Faraz it was kept as 0 for all customer 12-Jun-2014

			
			/**
			 * Following are the parameters need to be set to generated secure PDF attachments while emailing.
			 */
			callInfo.setField(Field.ISTOPROTECTPDFFILE, Constants.TRUE);
			callInfo.setField(Field.PDFPROCTECTINGPASSWD, util.getSubstring(srcNo, Constants.GL_FOUR));
			//END - for generating secure pdf file 
			

			faxSent = util.generateFaxFileForCard(faxInfo, callInfo);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Has the fax file sent to the destination path " + faxSent);}
		
			/**
			 * Following are for EMail server calling methods
			 */

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final FaxNumber is " + faxNumber);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Email File Location is " + emailFileCopyLoc);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Fax File Name is " + faxFileName);}


//			String smtp_ToEmailID = Constants.EMPTY_STRING + callInfo.getField(Field.REG_EMAIL);

			String smtp_HostName = Constants.EMPTY_STRING  + callInfo.getField(Field.EMAILHOSTNAME);
			String smtp_UserName = Constants.EMPTY_STRING  + callInfo.getField(Field.EMAILUSERNAME);
			String smtp_Password = Constants.EMPTY_STRING  + callInfo.getField(Field.EMAILPASSWORD);
			String smtp_Auth = Constants.EMPTY_STRING  + callInfo.getField(Field.EMAILAUTH);
			String smtp_FromEmailId = Constants.EMPTY_STRING  + callInfo.getField(Field.EMAILFROMEMAILID);
			String smtp_SubjectLine = Constants.EMPTY_STRING  + callInfo.getField(Field.EMAILSUBJECTLINE);
			String smtp_mailBodyText = Constants.EMPTY_STRING  + callInfo.getField(Field.EMAILBODYTEXT);
			String smtp_mailPort =  Constants.EMPTY_STRING  + callInfo.getField(Field.EMAILPORT);
			String smtp_emailGifLocation = Constants.EMPTY_STRING + callInfo.getField(Field.EMAILGIFLOCATION);

			callInfo.setField(Field.EMAILFILELOCATION, faxFileName);
			String smtp_FileName =  faxFileName;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Statement Host Name " + smtp_HostName);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Statement User Name " + smtp_UserName);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Statement Auth details " + smtp_Auth);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Statement To Eamil id " + smtp_FileName);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Statement From Eamil id " + smtp_FromEmailId);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Statement subject line " + smtp_SubjectLine);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Statement mail Body text " + smtp_mailBodyText);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Statement file name " + faxFileName);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Statement mail port" + smtp_mailPort);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Registered email id is " + smtp_ToEmailID);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Statement Email Gif Loction is " + smtp_emailGifLocation);}
			
			HashMap<String, String> statementInfoMap = new HashMap<>();

			statementInfoMap.put(Constants.SMTP_HOSTNAME, smtp_HostName);
			statementInfoMap.put(Constants.SMTP_USERNAME, smtp_UserName);
			statementInfoMap.put(Constants.SMTP_AUTH, smtp_Auth);
			statementInfoMap.put(Constants.SMTP_filename, smtp_FileName);
			statementInfoMap.put(Constants.SMTP_FROMEMAILID, smtp_FromEmailId);
			statementInfoMap.put(Constants.SMTP_subjectLine, smtp_SubjectLine);
			statementInfoMap.put(Constants.SMTP_mailBodyText, smtp_mailBodyText);
			statementInfoMap.put(Constants.SMTP_filename, faxFileName);
			statementInfoMap.put(Constants.SMTP_mailPort, smtp_mailPort);
			statementInfoMap.put(Constants.SMTP_ToEmailId, smtp_ToEmailID);
			statementInfoMap.put(Constants.SMTP_PASSWORD,smtp_Password);
			statementInfoMap.put(Constants.SMTP_EMAILGIFLOCATION, smtp_emailGifLocation);
			
			/**
			 * Follwoing are the fixes done to show the fax file name in the fax template header
			 */
			statementInfoMap.put(Constants.SMTP_FORMTYPE, pdfFaxFormName);
			//END 
			
			/**
			 * Added on 26-Dec-2014 for Forms description issue
			 */
			statementInfoMap.put(Constants.SMTP_CALLEDFROM_FORMS, "false");
			//END

			code = util.statementByEmail(statementInfoMap);
			/*
			 * For Reporting Start
			 */
			String hostEndTime = util.getCurrentDateTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			String hostResCode = code;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);

			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;
			}else{

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for WriteFax host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + code);}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_SMTPEMAIL, code);

			}
			
			/****Duplicate RRN Fix 25012016 *****/
			strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + callInfo.getField(Field.CUSTOMERID) + Constants.COMMA + Constants.HOST_INPUT_PARAM_FORM_TYPE + Constants.EQUALTO + "NA"
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_FORM_NAME + Constants.EQUALTO + "NA"
					//TODO: As per Judes requested on 14-09-2015
					/*****************/
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_TYPE + Constants.EQUALTO + (util.isNullOrEmpty(callInfo.getField(Field.SRCTYPE)) ? Constants.NA : (String) callInfo.getField(Field.SRCTYPE))
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_STMT_TYPE +Constants.EQUALTO + "STEMINI"
					/*****************/
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_REQUEST_TYPE + Constants.EQUALTO + Constants.FORMS_REQUEST_TYPE_EMAIL
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_EMAIL_ID + Constants.EQUALTO + smtp_ToEmailID
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + util.maskCardOrAccountNumber(sourceNo) // 29-03-2015 based on kaarthik & vijay request for report 
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			
			
			hostReportDetails.setHostInParams(strHostInParam);
			/************************************/
			
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode;
//			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(accountBalance_HostRes.getErrorDesc()) ?"NA" :accountBalance_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			IvrData ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.insertHostDetails(ivrdata);
			//End Reporting
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: TransactionDetailsCardsImpl.getCardsEmailStatementRequest()");}


			

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at FaxImpl.GetAlreadyExistingFaxNoPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return code;
		
		
	}

	@Override
	public String getCardsTransactionChargesAnncPhrases(CallInfo callInfo)
			throws ServiceException {
		
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: TransactionDetailsCardsImpl.getCardsTransactionChargesAnncPhrases()");}
		String str_GetMessage, finalResult;
		
		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();
		//	ICEGlobalConfig iceGlobal = (ICEGlobalConfig)callInfo.getICEGlobalConfig();
			
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null or empty");
			}
			
			String selectedStmtProcessType = (String) callInfo.getField(Field.SELECTEDSTMTPROCESSINGTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected Statment processing type is " + selectedStmtProcessType);}
			
			ArrayList<String> chargeMsgList = null;
			if(Constants.STMTPROCESSING_TYPE_FAX.equalsIgnoreCase(selectedStmtProcessType)){
				chargeMsgList = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_TRANSACTIONDETAILS_CHARGEMSG_FAX_CARD))? null : (ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_TRANSACTIONDETAILS_CHARGEMSG_FAX_CARD);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieving Charge msg of CARD FAX" + chargeMsgList);}
			}else if(Constants.STMTPROCESSING_TYPE_EMAIL.equalsIgnoreCase(selectedStmtProcessType)){
				chargeMsgList = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_TRANSACTIONDETAILS_CHARGEMSG_EMAIL_CARD))? null :(ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_TRANSACTIONDETAILS_CHARGEMSG_EMAIL_CARD);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieving Charge msg of CARD EMAIL" + chargeMsgList);}
			}
		
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Transaction Charge Phrase list retrieved is :" + chargeMsgList);}
			
			String moreCount = (String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);
			
			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_Str = Constants.EMPTY_STRING;
			
			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			
			/**
			 * Note temp_Str is nothing but the dynamic name.  The wave file also should recorded in the same dynamic name
			 * 
			 * eg Dynamic1 --> Dynamic1.wav
			 * 
			 */
			int temp_MoreCount = int_moreCount - 1;

			if(!util.isNullOrEmpty(chargeMsgList)){
				for(int count=Constants.GL_ZERO;count<chargeMsgList.size();count++){
					temp_Str = chargeMsgList.get(count);
					dynamicValueArray.add(temp_Str.trim());

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added Disclaimer Phrase "+temp_Str);}
				}
			}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}
			
			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			//String menuID = MenuIDMap.getMenuID("");
			String anncID = AnncIDMap.getAnncID("Announce_Charges_Message");
			String featureID = FeatureIDMap.getFeatureID("Statement_Request_Cards");
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
			totalPrompt = chargeMsgList.size();
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}
			
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
			
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: TransactionDetailsCardsImpl.getCardsTransactionChargesAnncPhrases()");}
			
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at TransactionDetailsCardsImpl.getCardsTransactionChargesAnncPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
			}
		return finalResult;
	}

	@Override
	public String getCardsTransactionDisclaimerPhrases(CallInfo callInfo)
			throws ServiceException {
		
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: TransactionDetailsCardsImpl.getCardsTransactionDisclaimerPhrases()");}
		String str_GetMessage, finalResult;
		
		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();
			
			ArrayList<String> disclaimerMsgList = null;
			disclaimerMsgList = (ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_TRANSACTIONDETAILS_DISCLAIMERMSG_CARD);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Transaction Disclaimer list retrieved is :" + disclaimerMsgList);}
			
			String moreCount = (String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);
			
			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_Str = Constants.EMPTY_STRING;
			
			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			
			/**
			 * Note temp_Str is nothing but the dynamic name.  The wave file also should recorded in the same dynamic name
			 * 
			 * eg Dynamic1 --> Dynamic1.wav
			 * 
			 */
			int temp_MoreCount = int_moreCount - 1;
			
			for(int count=Constants.GL_ZERO;count<disclaimerMsgList.size();count++){
				temp_Str = disclaimerMsgList.get(count);
				dynamicValueArray.add(temp_Str.trim());
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added Disclaimer Phrase "+temp_Str);}
			}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}
			
			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			//String menuID = MenuIDMap.getMenuID("");
			String anncID = AnncIDMap.getAnncID("Disclaimer_Message");
			String featureID = FeatureIDMap.getFeatureID("Statement_Request_Cards");
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
			totalPrompt = disclaimerMsgList.size();
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}
			
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
			
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: TransactionDetailsCardsImpl.getCardsTransactionDisclaimerPhrases()");}
			
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at TransactionDetailsCardsImpl.getCardsTransactionDisclaimerPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
			}
		return finalResult;
	}

	@Override
	public String getFaxEmailCardsTransactionPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: TransactionDetailsCardsImpl.getFaxEmailCardsTransactionPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			//Need to get the FeatureConfig Data
			ArrayList<String> stmtProcessingTypeList = (ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_STATEMENTPROCESSINGTYPE_CARD);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Statement Processing type list retrieved is :" + stmtProcessingTypeList);}

			String moreCount = (String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_Str = Constants.EMPTY_STRING;

			//Need to handle the Dynamic phrase list and Mannual Grammar portions

			for(int count=Constants.GL_ZERO;count<stmtProcessingTypeList.size();count++){
				temp_Str = stmtProcessingTypeList.get(count);
				if(Constants.STMTPROCESSING_TYPE_FAX.equalsIgnoreCase(temp_Str)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the phrase for fax" + stmtProcessingTypeList);}
					dynamicValueArray.add(DynaPhraseConstants.Statement_Request_1006);
				}else if(Constants.STMTPROCESSING_TYPE_EMAIL.equalsIgnoreCase(temp_Str)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the phrase for EMAIL" + stmtProcessingTypeList);}
					dynamicValueArray.add(DynaPhraseConstants.Statement_Request_1007);
				}

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added Product type "+temp_Str);}

				if(Constants.STMTPROCESSING_TYPE_FAX.equalsIgnoreCase(temp_Str) || 
						Constants.STMTPROCESSING_TYPE_EMAIL.equalsIgnoreCase(temp_Str)){
					
					if(util.isNullOrEmpty(grammar)){
						grammar = temp_Str;
					}else{
						grammar = grammar + Constants.COMMA + temp_Str;
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the grammar value" + grammar);}
				}

			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("STATEMENT_REQUEST_CARD_OPTION");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("Statement_Request_Cards");
			String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+menuID;

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
			totalPrompt = stmtProcessingTypeList.size() * Constants.GL_TWO;
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}
			

			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);

			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}


			//Need to handle if we want to append pipe seperator sign

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: TransactionDetailsCardsImpl.getFaxEmailCardsTransactionPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at  TransactionDetailsCardsImpl.getFaxEmailCardsTransactionPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public String getTransactionDetailsCards(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: TransactionDetailsCardsImpl.getTransactionDetailsCards()");}
		
		//String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: TransactionDetailsBankImpl.getTransactionDetailsBank()");}
		
		
		
		String code = Constants.EMPTY_STRING;
		//		getConfigurationParam(callInfo);
		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}



			String cardEmbossNum = null;

//			if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())){
//				if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl())){
//					if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl().getCardEmbossNumber())){
//						cardEmbossNum = callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl().getCardEmbossNumber();
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Card Emboss Number is " + cardEmbossNum);}
//					}
//				}
//
//			}


//			String cardAcctNo = (String)callInfo.getField(Field.SRCNO);
//			if(!util.isNullOrEmpty(cardAcctNo))
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected credit card number ending with " + util.getSubstring(cardAcctNo, Constants.GL_FOUR));}
//

			
			//TODO - CC
			
//			cardEmbossNum = (String)callInfo.getField(Field.SRCNO);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected credit card number ending with " + util.getSubstring(cardEmbossNum, Constants.GL_FOUR));}
//			
//			if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())){
//				if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl())){
//					cardAcctNo = callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl().getFirstCCAccountNo();
//				}
//			}

			HashMap<String, ArrayList<String>>ccAcctMap = util.isNullOrEmpty(callInfo.getField(Field.CCACCTMAP))?new HashMap<String, ArrayList<String>>():(HashMap<String, ArrayList<String>>)callInfo.getField(Field.CCACCTMAP);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Received Credit card account map is "+ ccAcctMap);}
			Iterator iter = null;
			iter = ccAcctMap.keySet().iterator();
			ArrayList<String>cardList = null;
			String sourceNo = util.isNullOrEmpty(callInfo.getField(Field.SRCNO))? Constants.EMPTY_STRING : (String)callInfo.getField(Field.SRCNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected credit card number is "+ util.maskCardOrAccountNumber(sourceNo));}
			String key = Constants.EMPTY_STRING;
			
			while(iter.hasNext()) {
				key = (String)iter.next();
				cardList = ccAcctMap.get(key);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The credit card list is "+ cardList);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The list of credit cards objects are retrieved");}
				
				if(cardList.contains(sourceNo)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The current list which has the account number "+util.maskCardOrAccountNumber(key)+"Contains the credit card number "+util.maskCardOrAccountNumber(sourceNo));}
					cardEmbossNum = key;
					break;
				}

			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected credit card's Account number is "+ util.maskCardOrAccountNumber(cardEmbossNum));}
			
			callInfo.setField(Field.CCACCTNOFORSTMTREQ, cardEmbossNum);
			
			/**
			 * For Credit Card Account Statement Request Host access , below are the constants values need to be sent
			 */

			ICEGlobalConfig iceGlobalConfig = (ICEGlobalConfig)callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(iceGlobalConfig)){
				throw new ServiceException("ICEGlobal object is null or empty");
			}

			String statementType = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_CCSTMTENQUIRY_STMTTYPE+Constants._TRANS);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Statement Type" + statementType);}

			String reqType = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_CCSTMTENQUIRY_REQTYPE+Constants._TRANS);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Req Type " + reqType);}

			String returnContent = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_CCSTMTENQUIRY_RETURNCONTENT+Constants._TRANS);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured returnContent" + returnContent);}

			String ccyCodeType = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_CCSTMTENQUIRY_CCYCODETYPE+Constants._TRANS);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured CcycodeType" + ccyCodeType);}

			String groupTrxn = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_CCSTMTENQUIRY_GROUPTRXN+Constants._TRANS);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Group Trxn" + groupTrxn);}

			String entitySize = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_UI_CCSTMTENQUIRY_SIZE+Constants._TRANS);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured entitySize" + entitySize);}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the start Date and end Date value as null, since the requested statement type is mini");}


			XMLGregorianCalendar startDate = null;

			XMLGregorianCalendar endDate = null;
			/**
			 * Setting the start Date and end date as null
			 */

			
			/**
			 * For Reporting Purpose
			 */
			HostReportDetails hostReportDetails = new HostReportDetails();

			String featureId = (String)callInfo.getField(Field.FEATUREID);
			hostReportDetails.setHostActiveMenu(featureId);
			//hostReportDetails.setHostCounter(hostCounter);
			//hostReportDetails.setHostEndTime(hostEndTime);
//			String strHostInParam = Constants.HOST_INPUT_PARAM_CARDEMBOSSNUMBER + Constants.EQUALTO + cardEmbossNum;
//			hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_CCACCTSTMTINQ);
			//hostReportDetails.setHostOutParams(hostOutParams);
			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);

			String startTime = util.getCurrentDateTime();
			hostReportDetails.setHostStartTime(startTime); //It should be in the formate of 31/07/2013 18:11:11
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

			//TODO need to confirm to remove
//			String strStartDate = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_FROMTRANSDATE_TOGET_CARD); 
//			XMLGregorianCalendar startDate = util.convertDateStringtoXMLGregCalendar(strStartDate, Constants.DATEFORMAT_yyyy_MM_ddHH_mm);
//			XMLGregorianCalendar startDate = null;

//			String strToDate = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_TOTRANSDATE_TOGET_CARD); 
//			XMLGregorianCalendar toDate = util.convertDateStringtoXMLGregCalendar(strToDate, Constants.DATEFORMAT_yyyy_MM_ddHH_mm);
//			XMLGregorianCalendar toDate = null;

			
			TransactionDetailCards_HostReq transactionDetailCards_HostReq = ccAcctStmtInqDAO.getTransactionDeatilCardsHostRes(callInfo, statementType, reqType, 
					returnContent, startDate, endDate, cardEmbossNum, ccyCodeType, groupTrxn, entitySize);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "getTransactionDetailsCards Object is :"+ transactionDetailCards_HostReq);}
			callInfo.setTransactionDetailCards_HostReq(transactionDetailCards_HostReq);

			code = transactionDetailCards_HostReq.getErrorCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction detail card's code is " + code);}
			/*
			 * For Reporting Start
			 */

			String hostEndTime = transactionDetailCards_HostReq.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			String hostResCode = transactionDetailCards_HostReq.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);


			
			String duration = util.hostServiceTimeDuration(startTime, hostEndTime, Constants.DATEFORMAT_dd_MM_yyyyHH_mm_ss);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The host time duration result is " + duration);}
			
			
			String customerIDObbj = util.isNullOrEmpty(callInfo.getField(Field.CUSTOMERID))?Constants.NA : (String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID going to insert in host is " + customerIDObbj);}

			String strHostInParam =Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA +  Constants.HOST_INPUT_PARAM_CARDEMBOSSNUMBER + Constants.EQUALTO + cardEmbossNum
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO + util.maskCardOrAccountNumber(cardEmbossNum)
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_TYPE + Constants.EQUALTO + callInfo.getField(Field.SRCTYPE)
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_REQUEST_TYPE + Constants.EQUALTO + statementType
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_DURATION + Constants.EQUALTO + duration
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			
			
			
			
			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				
				responseDesc = Constants.HOST_SUCCESS;
//				String srcNo = util.isNullOrEmpty(callInfo.getField(Field.SRCNO))? Constants.EMPTY_STRING : (String)callInfo.getField(Field.SRCNO);
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Source card number "+ util.maskCardOrAccountNumber(srcNo));}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected source credit card number ending with "+ util.maskCardOrAccountNumber(sourceNo) );}
				int totTransAvail = Constants.GL_ZERO;
				if(!util.isNullOrEmpty(callInfo.getTransactionDetailCards_HostReq())){
					
					if(!util.isNullOrEmpty(transactionDetailCards_HostReq.getTransDtls_Transaction().getTransactionMap())
							&& !util.isNullOrEmpty(callInfo.getTransactionDetailCards_HostReq().getTransDtls_Transaction().getTransactionMap().get(sourceNo)) ){
						totTransAvail = (callInfo.getTransactionDetailCards_HostReq().getTransDtls_Transaction().getTransactionMap().get(sourceNo)).size();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Available transaction total count "+ totTransAvail );}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Available transaction Map : "+ callInfo.getTransactionDetailCards_HostReq().getTransDtls_Transaction().getTransactionMap());}
					}
				}
					callInfo.setField(Field.NOOFTRANSACTION,totTransAvail);
					
					
					if(totTransAvail > Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is Transaction Available "+ totTransAvail );}
						callInfo.setField(Field.ISTRANSACTIONAVAILABLE, true);
					}
					else{
						callInfo.setField(Field.ISTRANSACTIONAVAILABLE, false);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is Transaction Available "+ totTransAvail );}
					}
					
					
			}
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(transactionDetailCards_HostReq.getErrorDesc()) ?"NA" :transactionDetailCards_HostReq.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);
			//End Reporting

			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for CCAcctStmtInq service");}

			}else{

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for Update CCAcctStmtInq host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + transactionDetailCards_HostReq.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_CCACCTSTMTINQ, transactionDetailCards_HostReq.getHostResponseCode());
				/**
				 * Following will be called only if there occured account selection before this host access
				 */
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
				util.setEligibleAccountCounts(callInfo, hostResCode);
			}

		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at TransactionDetailsCardsImpl.getTransactionDetailsCards() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;
	}

	//TODO Not using the following method
	
	@Override
	public String getTransactionDetailsCardsPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: TransactionDetailsCardsImpl.getTransactionDetailsCardsBankPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			//Need to get the FeatureConfig Data
			ArrayList<String> transactionTypeList = null;
			transactionTypeList = (ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_TRANSACTIONTYPELIST_CARD);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Transaction type list retrieved is :" + transactionTypeList);}


			String moreCount = (String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_Str = Constants.EMPTY_STRING;

			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			String transactionType = Constants.EMPTY_STRING;
			for(int count=Constants.GL_ZERO;count<transactionTypeList.size();count++){

				transactionType = transactionTypeList.get(count);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding dynamic phrase for the transaction type" + transactionTypeList.get(count));}

				if(Constants.TRANSACTIONTYPE_TRANSDETAILS.equalsIgnoreCase(transactionType)){
					dynamicValueArray.add(DynaPhraseConstants.Card_Statement_1001);

				}else if(Constants.TRANSACTIONTYPE_LASTMONTH.equalsIgnoreCase(transactionType)){
					dynamicValueArray.add(DynaPhraseConstants.Statement_Request_1001);
				}


				if(util.isNullOrEmpty(grammar)){
					grammar = transactionType;
				}else{
					grammar = grammar + Constants.COMMA + transactionType;
				}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the grammar value" + grammar);}

			}


			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("STATEMENT_REQUEST_CARD");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("Statement_Request_Cards");
			String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+menuID;

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
			totalPrompt = Constants.GL_TWO * transactionTypeList.size();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}

			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);

			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}


			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: TransactionDetailsCardImpl.getTransactionDetailsCardPhrases()");}

			
			
			
			
			
			
			
			
			
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "***********************************************************************************************");}
			
//			
//			String cardEmbossNum = null;
//
////			if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())){
////				if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl())){
////					if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl().getCardEmbossNumber())){
////						cardEmbossNum = callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl().getCardEmbossNumber();
////						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Card Emboss Number is " + cardEmbossNum);}
////					}
////				}
////			}
//
//
//			String cardAcctNo = (String)callInfo.getField(Field.SRCNO);
//			if(!util.isNullOrEmpty(cardAcctNo))
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected credit card number ending with " + util.getSubstring(cardAcctNo, Constants.GL_FOUR));}
//
//
//			
//			//TODO - CC
//			cardEmbossNum = (String)callInfo.getField(Field.SRCNO);
//			if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())){
//				if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl())){
//					cardAcctNo = callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl().getFirstCCAccountNo();
//				}
//			}
//
//
//			/**
//			 * For Credit Card Account Statement Request Host access , below are the constants values need to be sent
//			 */
//
//			String statementType = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CCSTMTENQUIRY_STMTTYPE);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Statement Type" + statementType);}
//
//			String reqType = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CCSTMTENQUIRY_REQTYPE);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Req Type " + reqType);}
//
//			String returnContent = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CCSTMTENQUIRY_RETURNCONTENT);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured returnContent" + returnContent);}
//			
//			String ccyCodeType = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CCSTMTENQUIRY_CCYCODETYPE);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured CcycodeType" + ccyCodeType);}
//			
//			String groupTrxn = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CCSTMTENQUIRY_GROUPTRXN);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Group Trxn" + groupTrxn);}
//			
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the start Date and end Date value as null, since the requested statement type is mini");}
//
//
//
//			/**
//			 * Setting the start Date and end date as null
//			 */
//
//			
//			/**
//			 * For Reporting Purpose
//			 */
//			HostReportDetails hostReportDetails = new HostReportDetails();
//
//			String featureId = (String)callInfo.getField(Field.FEATUREID);
//			hostReportDetails.setHostActiveMenu(featureId);
//			//hostReportDetails.setHostCounter(hostCounter);
//			//hostReportDetails.setHostEndTime(hostEndTime);
//			String strHostInParam = Constants.HOST_INPUT_PARAM_CARDNUMBER + Constants.EQUALTO + cardAcctNo+
//					Constants.COMMA + Constants.HOST_INPUT_PARAM_CARDEMBOSSNUMBER + Constants.EQUALTO + cardEmbossNum;
//			hostReportDetails.setHostInParams(strHostInParam);
//			hostReportDetails.setHostMethod(Constants.HOST_METHOD_CCACCTSTMTINQ);
//			//hostReportDetails.setHostOutParams(hostOutParams);
//			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
//			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
//			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
//			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);
//
//			hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It should be in the formate of 31/07/2013 18:11:11
//			hostReportDetails.setHostType(Constants.HOST_TYPE_ACCOUNTSERVICES);
//			//End Reporting
//
//			//TODO need to confirm to remove
////			String strStartDate = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_FROMTRANSDATE_TOGET_CARD); 
////			XMLGregorianCalendar startDate = util.convertDateStringtoXMLGregCalendar(strStartDate, Constants.DATEFORMAT_yyyy_MM_ddHH_mm);
//			XMLGregorianCalendar startDate = null;
//
////			String strToDate = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_TOTRANSDATE_TOGET_CARD); 
////			XMLGregorianCalendar toDate = util.convertDateStringtoXMLGregCalendar(strToDate, Constants.DATEFORMAT_yyyy_MM_ddHH_mm);
//			XMLGregorianCalendar toDate = null;
//			TransactionDetailCards_HostReq transactionDetailCards_HostReq = new TransactionDetailCards_HostReq();
//			
////			TransactionDetailCards_HostReq transactionDetailCards_HostReq = ccAcctStmtInqDAO.getTransactionDeatilCardsHostRes(callInfo, statementType, reqType, 
////					returnContent, startDate, toDate, cardAcctNo, cardEmbossNum);
//
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CallerIdenf_DebitCardDetails Object is :"+ transactionDetailCards_HostReq);}
//			callInfo.setTransactionDetailCards_HostReq(transactionDetailCards_HostReq);
//
//			String code = transactionDetailCards_HostReq.getErrorCode();
//
//			/*
//			 * For Reporting Start
//			 */
//
//			String hostEndTime = transactionDetailCards_HostReq.getHostEndTime();
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
//			hostReportDetails.setHostEndTime(hostEndTime);
//
//			String hostResCode = transactionDetailCards_HostReq.getHostResponseCode();
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
//			hostReportDetails.setHostResponse(hostResCode);
//
//			String responseDesc = Constants.HOST_FAILURE;
//			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
//				responseDesc = Constants.HOST_SUCCESS;
//				
//				int totTransAvail = Constants.GL_ZERO;
//				if(!util.isNullOrEmpty(callInfo.getTransactionDetailCards_HostReq())){
//					
//					if(!util.isNullOrEmpty(callInfo.getTransactionDetailCards_HostReq().getTransDtls_Transaction())
//							&& !util.isNullOrEmpty(callInfo.getTransactionDetailCards_HostReq().getTransDtls_Transaction().getTransactionMap())){
//						totTransAvail = callInfo.getTransactionDetailCards_HostReq().getTransDtls_Transaction().getTransactionMap().size();
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Available transaction total count "+ totTransAvail );}
//					}
//				}
//					callInfo.setField(Field.NOOFTRANSACTION, totTransAvail);
//			}
//			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
//					+ Constants.EQUALTO + hostResCode;
//
//			
//			hostReportDetails.setHostOutParams(hostOutputParam);
//
//			callInfo.setHostReportDetails(hostReportDetails);
//			IvrData ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
//
//			callInfo.insertHostDetails(ivrdata);
//			//End Reporting
//
//			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for CCAcctStmtInq service");}
//
//			}else{
//
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for Update CCAcctStmtInq host service");}
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + transactionDetailCards_HostReq.getHostResponseCode());}
//
//				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_CCACCTSTMTINQ, transactionDetailCards_HostReq.getHostResponseCode());
//				/**
//				 * Following will be called only if there occured account selection before this host access
//				 */
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
//				util.setEligibleAccountCounts(callInfo);
//			}
//			
//			
//			
//			
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "***********************************************************************************************");}
//			
//			
//			

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at TransactionDetailsCardImpl.getTransactionDetailsCardPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public boolean isChargesApplicable(CallInfo callInfo) throws ServiceException {
		
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: TransactionDetailsCardsImpl.isChargesApplicable()");}
		boolean isChargesApplicable = false;
		try{
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();
			
			isChargesApplicable = Boolean.parseBoolean((String) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_ISCHARGEAPPLICABLE_CARDTRANSDETAILS));
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is Chargers applicable for cards transactions " + isChargesApplicable);}
			
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: TransactionDetailsCardsImpl.isChargesApplicable()");}
		}
		catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at TransactionDetailsCardsImpl.isChargesApplicable() "	+ e.getMessage());}
			throw new ServiceException(e);
			}
		return isChargesApplicable;
	}

}
