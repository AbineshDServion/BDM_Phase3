package com.servion.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.bankmuscat.esb.commontypes.AddressInfoType;
import com.bankmuscat.esb.commontypes.PersonInfoType;
import com.reportbean.IvrData;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.LastNNumTransInquiryDAO;
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
import com.servion.model.callerIdentification.AcctInfo;
import com.servion.model.callerIdentification.CallerIdentification_HostRes;
import com.servion.model.reporting.HostReportDetails;
import com.servion.model.transactionDetailBank.BankStatementInformation;
import com.servion.model.transactionDetailBank.TransactionDetailsBank_HostRes;

public class TransactionDetailsBankImpl implements ITransactionDetailsBank{

	private static Logger logger = LoggerObject.getLogger();
	private LastNNumTransInquiryDAO lastNNumTransInquiryDAO;
	public LastNNumTransInquiryDAO getLastNNumTransInquiryDAO() {
		return lastNNumTransInquiryDAO;
	}

	public void setLastNNumTransInquiryDAO(
			LastNNumTransInquiryDAO lastNNumTransInquiryDAO) {
		this.lastNNumTransInquiryDAO = lastNNumTransInquiryDAO;
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
	public String getBankEmailStatementRequest(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: TransactionDetailsBankImpl.getBankEmailStatementRequest()");}
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


			String smtp_ToEmailID = Constants.EMPTY_STRING + callInfo.getField(Field.REG_EMAIL);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Email id is " + smtp_ToEmailID);}

			
			
			/**  
			 * For Reporting Purpose
			 */
			HostReportDetails hostReportDetails = new HostReportDetails();

			String featureId = (String)callInfo.getField(Field.FEATUREID);
			hostReportDetails.setHostActiveMenu(featureId);
			//hostReportDetails.setHostCounter(hostCounter);
			//hostReportDetails.setHostEndTime(hostEndTime);
			/*String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + 
					callInfo.getField(Field.CUSTOMERID) + Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + callInfo.getField(Field.SRCNO)
				 + Constants.COMMA + Constants.HOST_INPUT_PARAM_EMAIL_ID + Constants.EQUALTO +smtp_ToEmailID
				 + Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;*/
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
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + callInfo.getField(Field.SRCNO) // 29-03-2015 based on kaarthik & vijay request for report 
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
		
			
			hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_SMTPEMAIL);
			//hostReportDetails.setHostOutParams(hostOutParams);
			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);

			hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It should be in the format of 31/07/2013 18:11:11
			hostReportDetails.setHostType(Constants.HOST_METHOD_SMTPEMAIL);

//			String faxNumber = (String) callInfo.getField(Field.LASTSELECTEDFAXNO);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requested faxNumber "+ faxNumber);}

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


			if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getAccountDetailMap())){
				AcctInfo acctInfo = callInfo.getCallerIdentification_HostRes().getAccountDetailMap().get(srcNo);

				if(!util.isNullOrEmpty(acctInfo)){
					currency = acctInfo.getAcctCurr();
					customerBranchCode = acctInfo.getBranchCode();
				}
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account / Card Currency is " + currency) ;}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account / Card Branch Code is " + customerBranchCode) ;}

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
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fax file with customer id is "+ faxFileName);}
//			if(!util.isNullOrEmpty(srcNo)){
//				faxFileName = faxFileName +  srcNo;
//				faxFileName = faxFileName + Constants.UNDERSCORE;
//			}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fax file with customer id + source no is "+ faxFileName);}
			if(Constants.FEATURE_TYPE_BANK.equalsIgnoreCase(featureType)){
				faxFileName = faxFileName + Constants.FEATURE_TYPE_BANK;
				faxFileName = faxFileName + Constants.UNDERSCORE;
			}else{
				faxFileName = faxFileName + Constants.FEATURE_TYPE_CARD;
				faxFileName = faxFileName + Constants.UNDERSCORE;
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fax file with customer id + source no  + feature type is "+ faxFileName);}

			String currentDate = util.getTodayDateOrTime(Constants.DATEFORMAT_YYYYMMDDHHMMSS);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Today's date and time is "+ currentDate);}

			faxFileName = faxFileName + currentDate;
			faxFileName = faxFileName + Constants.PDF_EXTENSION;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final fax file name is "+ faxFileName);}
			
			String pdfFaxFormName = faxFileName;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final fax file location is "+ emailFileCopyLoc);}

			faxFileName = emailFileCopyLoc + faxFileName;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Fax File name is "+ faxFileName);}
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
			faxInfo.put(Constants.FAX_MODULE_NOOFTRANSPERPAGE, noOfTransaction );
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


			if(Constants.FEATURE_TYPE_BANK.equalsIgnoreCase(featureType)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Called from a Banking Features " );}

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Upding the Card Template file name along with the fax template name "+ fileTemplatePath);}
				fileTemplatePath = fileTemplatePath + Constants.ACCOUNT_TEMPLATE_NAME;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Template final template file name and path is "+ fileTemplatePath);}

				ArrayList<BankStatementInformation> transactionList = null;
				if(!util.isNullOrEmpty(callInfo.getTransactionDetailsBank_HostRes()) && !util.isNullOrEmpty(callInfo.getTransactionDetailsBank_HostRes().getBankStmtTypeInfoList()))
				{
					transactionList = callInfo.getTransactionDetailsBank_HostRes().getBankStmtTypeInfoList();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "transactionList obtained from the transaction details of bank is " + transactionList);}

					if(!util.isNullOrEmpty(transactionList.get(Constants.GL_ZERO).getOrigDate())){
						period = util.convertXMLCalendarToString(transactionList.get(Constants.GL_ZERO).getOrigDate(), Constants.DATEFORMAT_YYYYMMDD);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Period with initial transaction orig date" + period );}
						period = period +"\t" + Constants.MINUS + "\t";
						period = period  + util.convertXMLCalendarToString(transactionList.get(transactionList.size()-Constants.GL_ONE).getOrigDate(), Constants.DATEFORMAT_YYYYMMDD);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Period with last transaction orig date" + period );}
					}

				}

				/**
				 * Setting Fax file credentials
				 */
				faxInfo.put(Constants.FAX_MODULE_FAXTEMPLATEPATH, fileTemplatePath);
				faxInfo.put(Constants.FAX_MODULE_ACCOUNTTYPE, accountType);
				faxInfo.put(Constants.FAX_MODULE_TRANSACTIONLIST, transactionList);
				faxInfo.put(Constants.FAX_MODULE_CUSTOMERNAME, customerName);
				faxInfo.put(Constants.FAX_MODULE_CUSTOMERADDR, customerAddress);
				faxInfo.put(Constants.FAX_MODULE_CARDORACCTNUMBER, srcNo);
				faxInfo.put(Constants.FAX_MODULE_CURRENCY, currency);
				faxInfo.put(Constants.FAX_MODULE_CUSTOMERBRANCHCODE, customerBranchCode);
				faxInfo.put(Constants.FAX_MODULE_PERIOD, period);

				
				/**
				 * Following are the parameters need to be set to generated secure PDF attachments while emailing.
				 */
				callInfo.setField(Field.ISTOPROTECTPDFFILE, Constants.TRUE);
				callInfo.setField(Field.PDFPROCTECTINGPASSWD, util.getSubstring(srcNo, Constants.GL_FOUR));
				//END - for generating secure pdf file 
				
				faxSent = util.generateFaxFileForBank(faxInfo, callInfo);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Has the fax file sent to the destination path " + faxSent);}

			}

			/**
			 * Following are for EMail server calling methods
			 */

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
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + callInfo.getField(Field.SRCNO) // 29-03-2015 based on kaarthik & vijay request for report 
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
		
			
			hostReportDetails.setHostInParams(strHostInParam);
			/************************************/
			
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode;
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			IvrData ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.insertHostDetails(ivrdata);
			//End Reporting
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: TransactionDetailsBankImpl.getBankEmailStatementRequest()");}



		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at FaxImpl.SendLogEmailRequest() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;

	}

	@Override
	public String getBankTransactionChargesAnncPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: TransactionDetailsBankImpl.getBankTransactionChargesAnncPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();


			String selectedStmtProcessType = (String) callInfo.getField(Field.SELECTEDSTMTPROCESSINGTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected Statment processing type is " + selectedStmtProcessType);}

			ArrayList<String> chargeMsgList = null;
			if(Constants.STMTPROCESSING_TYPE_FAX.equalsIgnoreCase(selectedStmtProcessType)){
				chargeMsgList = util.isNullOrEmpty((ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_TRANSACTIONDETAILS_CHARGEMSG_FAX_BANK))? null : (ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_TRANSACTIONDETAILS_CHARGEMSG_FAX_BANK);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieving Charge msg of BANK FAX" + chargeMsgList);}
			}else{
				chargeMsgList = util.isNullOrEmpty((ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_TRANSACTIONDETAILS_CHARGEMSG_EMAIL_BANK))? null : (ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_TRANSACTIONDETAILS_CHARGEMSG_EMAIL_BANK);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieving Charge msg of BANK EMAIL" + chargeMsgList);}
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
			String featureID = FeatureIDMap.getFeatureID("Statement_Request_Banks");
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

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: TransactionDetailsBankImpl.getBankTransactionChargesAnncPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at TransactionDetailsBankImpl.getBankTransactionChargesAnncPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public String getBankTransactionDisclaimerPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: TransactionDetailsBankImpl.getBankTransactionDisclaimerPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			ArrayList<String> disclaimerMsgList = null;
			disclaimerMsgList = (ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_TRANSACTIONDETAILS_DISCLAIMERMSG_BANK);
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
			String featureID = FeatureIDMap.getFeatureID("Statement_Request_Banks");
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


			//Need to handle if we want to append pipe seperator sign
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: TransactionDetailsBankImpl.getBankTransactionDisclaimerPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at TransactionDetailsBankImpl.getBankTransactionDisclaimerPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public String getFaxEmailBankTransactionPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: TransactionDetailsBankImpl.getFaxEmailBankTransactionPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			//Need to get the FeatureConfig Data
			ArrayList<String> stmtProcessingTypeList = (ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_STATEMENTPROCESSINGTYPE_BANK);
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

			String menuID = MenuIDMap.getMenuID("STATEMENT_REQUEST_BANK_OPTION");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("Statement_Request_Banks");
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

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: TransactionDetailsBankImpl.getFaxEmailBankTransactionPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at  TransactionDetailsBankImpl.getFaxEmailBankTransactionPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public String getTransactionDetailsBank(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: TransactionDetailsBankImpl.getTransactionDetailsBank()");}
		String code = Constants.EMPTY_STRING;

		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}


			String SelectedCardOrAcctNo = Constants.EMPTY_STRING;
			CallerIdentification_HostRes callerIdentification_HostRes = callInfo.getCallerIdentification_HostRes();
			if(util.isNullOrEmpty(callerIdentification_HostRes)){
				throw new ServiceException("CallerIdentification Object is null / EMpty");
			}

			SelectedCardOrAcctNo = (String)callInfo.getField(Field.SRCNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The first account ID number of the retrieved account type is :" + util.maskCardOrAccountNumber(SelectedCardOrAcctNo));}



			if(util.isNullOrEmpty(SelectedCardOrAcctNo)){
				throw new ServiceException("Selected Acct No is empty or null");
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting selected card or acct no as entered cin" + util.getSubstring(SelectedCardOrAcctNo, Constants.GL_FOUR));}


			/**
			 * For Reporting Purpose
			 */
			HostReportDetails hostReportDetails = new HostReportDetails();

			String featureId = (String)callInfo.getField(Field.FEATUREID);
			hostReportDetails.setHostActiveMenu(featureId);
			//hostReportDetails.setHostCounter(hostCounter);
			//hostReportDetails.setHostEndTime(hostEndTime);
			//			String strHostInParam = Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + SelectedCardOrAcctNo;
			//			hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_LASTNNUMTRANSINQ);
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

			String noOfTxn = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_NOOFTRANSACTION_TOFETCH_FROM_HOST); 
			int intNoOFTxn = util.isNullOrEmpty(noOfTxn)?Constants.GL_ZERO: Integer.parseInt(noOfTxn);

			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("iceFeatureData object is null or empty");
			}

			String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_LASTNNUMTRANSINQ_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_LASTNNUMTRANSINQ_REQUESTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}


			TransactionDetailsBank_HostRes transactionDetailsBank_HostRes = lastNNumTransInquiryDAO.getTransactioDetailsBankHostRes(callInfo, SelectedCardOrAcctNo, intNoOFTxn, requestType);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "recentTransactionBank_HostRes Object is :"+ transactionDetailsBank_HostRes);}
			callInfo.setTransactionDetailsBank_HostRes(transactionDetailsBank_HostRes);

			code = transactionDetailsBank_HostRes.getErrorCode();

			/*
			 * For Reporting Start
			 */

			String hostEndTime = transactionDetailsBank_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			String hostResCode = transactionDetailsBank_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);


			String durationTime = util.hostServiceTimeDuration(startTime, hostEndTime, Constants.DATEFORMAT_dd_MM_yyyyHH_mm_ss);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Access duration is " + durationTime);}

			String customerIDObbj = util.isNullOrEmpty(callInfo.getField(Field.CUSTOMERID))?Constants.NA : (String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID going to insert in host is " + customerIDObbj);}
			
			String strHostInParam =Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO + util.maskCardOrAccountNumber(SelectedCardOrAcctNo)
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_TYPE + Constants.EQUALTO + (util.isNullOrEmpty(callInfo.getField(Field.SRCTYPE)) ? Constants.NA : (String) callInfo.getField(Field.SRCTYPE))
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_REQUEST_TYPE +Constants.EQUALTO + requestType
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_DURATION + Constants.EQUALTO + durationTime
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);


			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;
			}
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(transactionDetailsBank_HostRes.getErrorDesc()) ?"NA" :transactionDetailsBank_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);
			//End Reporting

			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for TransactionDetailsBankImpl.getTransactionDetailsBank");}
				int totTransAvail = Constants.GL_ZERO;
				if(!util.isNullOrEmpty(callInfo.getTransactionDetailsBank_HostRes())){

					if(!util.isNullOrEmpty(callInfo.getTransactionDetailsBank_HostRes().getRecordCount())){
						totTransAvail = Integer.parseInt((String) callInfo.getTransactionDetailsBank_HostRes().getRecordCount());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Available transaction total count "+ totTransAvail );}
					}
				}
				callInfo.setField(Field.NOOFTRANSACTION, totTransAvail);
				
				if(totTransAvail > Constants.GL_ZERO)
					callInfo.setField(Field.ISTRANSACTIONAVAILABLE, true);
				else
					callInfo.setField(Field.ISTRANSACTIONAVAILABLE, false);

			}else{

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for Acccount balance host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + transactionDetailsBank_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_LASTNNUMTRANSINQ, transactionDetailsBank_HostRes.getHostResponseCode());

				/**
				 * Following will be called only if there occured account selection before this host access
				 */
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
				util.setEligibleAccountCounts(callInfo, hostResCode);

			}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: TransactionDetailsBankImpl.getTransactionDetailsBank()");}
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at TransactionDetailsBankImpl.getTransactionDetailsBank() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;
	}

	@Override
	public String getTransactionDetailsBankPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: TransactionDetailsBankImpl.getTransactionDetailsBankPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			//Need to get the FeatureConfig Data
			ArrayList<String> transactionTypeList = null;
			transactionTypeList = (ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_TRANSACTIONTYPELIST_BANK);
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

				if(Constants.TRANSACTIONTYPE_MINI.equalsIgnoreCase(transactionType)){
					dynamicValueArray.add(DynaPhraseConstants.Statement_Request_1000);

				}else if(Constants.TRANSACTIONTYPE_OTHERMONTH.equalsIgnoreCase(transactionType)){
					dynamicValueArray.add(DynaPhraseConstants.Statement_Request_1004);
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

			String menuID = MenuIDMap.getMenuID("STATEMENT_REQUEST_BANK");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("Statement_Request_Banks");
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


			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: TransactionDetailsBankImpl.getTransactionDetailsBankPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at TransactionDetailsBankImpl.getTransactionDetailsBankPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public boolean isChargesApplicable(CallInfo callInfo) throws ServiceException {
		// TODO Auto-generated method stub

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: TransactionDetailsBankImpl.isChargesApplicable()");}
		boolean isChargesApplicable = false;
		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			isChargesApplicable = Boolean.parseBoolean((String) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_ISCHARGEAPPLICABLE_BANKTRANSDETAILS));
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is Chargers applicable for Bank transactions " + isChargesApplicable);}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: TransactionDetailsBankImpl.isChargesApplicable()");}
		}
		catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at TransactionDetailsBankImpl.isChargesApplicable() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return isChargesApplicable;
	}

}
