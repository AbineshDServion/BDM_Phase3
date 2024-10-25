package com.servion.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bankmuscat.esb.cardmanagementservice.CCEntityInqResType;
import com.bankmuscat.esb.cardmanagementservice.CCEntityInqResType.Customer;
import com.bankmuscat.esb.cardmanagementservice.CCEntityInqResType.Customer.Account;
import com.bankmuscat.esb.cardmanagementservice.CCEntityInqResType.Customer.Account.AccountData;
import com.bankmuscat.esb.cardmanagementservice.CCEntityInqResType.Customer.Account.Address;
import com.bankmuscat.esb.cardmanagementservice.CCEntityInqResType.Customer.Account.Card;
import com.bankmuscat.esb.cardmanagementservice.CCEntityInqResType.Customer.Account.Card.Authorization;
import com.bankmuscat.esb.cardmanagementservice.CCEntityInqResType.Customer.Account.Card.CardData;
import com.bankmuscat.esb.cardmanagementservice.CCEntityInqResType.Customer.Account.Card.Online;
import com.bankmuscat.esb.cardmanagementservice.CCEntityInqResType.Customer.Account.Overdue;
import com.bankmuscat.esb.cardmanagementservice.CCEntityInqResType.Customer.Account.Payment;
import com.bankmuscat.esb.cardmanagementservice.CCEntityInqResType.Customer.Account.Rewards;
import com.bankmuscat.esb.cardmanagementservice.CCEntityInqResType.Customer.Account.Statement;
import com.bankmuscat.esb.cardmanagementservice.ExtentionsType;
import com.bankmuscat.esb.cardmanagementservice.Paging;
import com.bankmuscat.esb.cardmanagementservice.PersonEntity;
import com.ibm.icu.text.DecimalFormat;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.CCEntityInquiryDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.apinValidation.APINCustomerProfileDetails_HostRes;
import com.servion.model.apinValidation.APIN_CCEntityFields;
import com.servion.model.apinValidation.APIN_CardDetails;
import com.servion.model.creditCardBalance.CCEntityFields;
import com.servion.model.creditCardBalance.CardDetails;
import com.servion.model.creditCardBalance.CreditCardBalanceDetails_HostRes;
import com.servion.model.creditCardPayment.CCP_CCEntityFields;
import com.servion.model.creditCardPayment.CCP_CardDetails;
import com.servion.model.creditCardPayment.CreditCardDetails_HostRes;
import com.servion.ws.exception.DaoException;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.header.WS_ResponseHeader;
import com.servion.ws.header.impl.WS_ResponseHeaderImpl;
import com.servion.ws.service.CCEntityInquiryService;
import com.servion.ws.util.DAOLayerUtils;

public class CCEntityInquiryDAOImpl implements CCEntityInquiryDAO{
	private static Logger logger = LoggerObject.getLogger();

	@Autowired
	CCEntityInquiryService ccEntityInquiryService;


	public CCEntityInquiryService getCcEntityInquiryService() {
		return ccEntityInquiryService;
	}

	public void setCcEntityInquiryService(
			CCEntityInquiryService ccEntityInquiryService) {
		this.ccEntityInquiryService = ccEntityInquiryService;
	}

	//Not initializing through BEAN Injection
	private WS_ResponseHeader ws_ResponseHeader = new WS_ResponseHeaderImpl();


	@Override
	public APINCustomerProfileDetails_HostRes getAPINValCustProfDetailsHostRes(
			CallInfo callInfo, String entyityInquiryType,
			String inquiryReference, ArrayList<String> numberList, String returnReplacedCards, String entityEnqSize, String requestType)
					throws DaoException {
		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: CCEntityInquiryDAOImpl.getAPINValCustProfDetailsHostRes()");}
		DecimalFormat formatter = new DecimalFormat("0.000");
		
		APINCustomerProfileDetails_HostRes beanResponse = new APINCustomerProfileDetails_HostRes();

		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			CCEntityInqResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callCreditCardBalanceHost");}

			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = ccEntityInquiryService.callCreditCardBalanceHost(logger, sessionID, entyityInquiryType, inquiryReference, numberList, returnReplacedCards, entityEnqSize, requestType, str_UUI, generateXML, callInfo);

			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callCreditCardBalanceHost is : "+code);}
			WriteLog.hostLogWrite(sessionID, (String)callInfo.getField(Field.HOST_SERVICE_NAME), code);

			//Setting the ESB request reference number for reporting
			String esbReqRefNum = util.isNullOrEmpty(ws_ResponseHeader.getReqRefNum()) ? Constants.NA :  ws_ResponseHeader.getReqRefNum();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The ESB Request Reference number is "+ws_ResponseHeader.getReqRefNum());}
			callInfo.setField(Field.ESBREQREFNUM, esbReqRefNum);
			//END
			
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}

			beanResponse.setHostResponseCode(code);

			beanResponse.setErrorDesc(ws_ResponseHeader.getEsbErrDesc());
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for getCCBalanceHostRes is : "+ws_ResponseHeader.getEsbErrDesc());}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### CCEntityInq HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
			
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_CCEntityInquiry_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_CCEntityInquiry_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_CCEntityInquiry_Succ_ErrorCode);
			}
			
			
			code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}

			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){

					//Host Response fields
					Paging paging = response.getPaging();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Got Response for the field Paging "+paging);}

					if(!util.isNullOrEmpty(paging)){
						beanResponse.setPagingKey(paging.getKey());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Paging Key is "+paging.getKey());}

						beanResponse.setPagingKey(paging.getSize() + Constants.EMPTY_STRING);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Paging Size is "+paging.getSize());}
					}


					List<Customer> customerList = response.getCustomer();
					Customer customer = null;
					List<Account> accountList = null;
					
					HashMap<String, ArrayList<String>>customerID_AcctNOMap = new HashMap<String, ArrayList<String>>();
					HashMap<String, APIN_CCEntityFields> acctNoAccountDetailMap = new HashMap<String, APIN_CCEntityFields>();
					ArrayList<String>acctNolist = new ArrayList<String>();
					
					//Bean Response fields
					APIN_CCEntityFields ccEntityFields = null;
					HashMap<String, APIN_CardDetails> cardDetailsMap = null;

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Retrieved the customer list from the host , the customer list object is "+customerList);}

					for(int i = 0; i < customerList.size(); i++ ){
						customer = customerList.get(i);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The " + i + " index of the customerList is "+ customer);}

						accountList = customer.getAccount();
						String customerID = customer.getNumber();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting customer id in the customer lead zero's field");}
						callInfo.setField(Field.CUSTOMER_ID_LEAD_ZEROS, customerID);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Retrieved the customer ID from the host , the customer ID object is(before formatting) "+customerID);}
						customerID = util.trimLeadingZeros(customerID);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Retrieved the customer ID from the host , the customer ID object is(after formatting) "+customerID);}
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting customer id in the field");}
						callInfo.setField(Field.CUSTOMERID, customerID);
						Account account = null;
						AccountData accountData = null;
						Statement statement = null;
						Overdue overdue = null;
						Payment payment = null;
						Rewards rewards = null;
						PersonEntity personEntity = null;
						Address address = null;
						ExtentionsType extentionsType = null;
						List<Card> cardDetailList = null;

						for(int count =0; count < accountList.size(); count++){
							account = accountList.get(count);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "the "+count +"Account list objec is "+ account);}
							ccEntityFields = new APIN_CCEntityFields();
							ccEntityFields.setCustomerID(customerID);
							cardDetailsMap = new HashMap<String, APIN_CardDetails>();
							
							if(!util.isNullOrEmpty(account)){

								ccEntityFields.setAccountNumber(account.getNumber());
								acctNolist.add(account.getNumber());

								accountData = account.getAccountData();
								if(!util.isNullOrEmpty(accountData)){

									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Acct Product is "+accountData.getProduct());}
									ccEntityFields.setAcctProduct(accountData.getProduct());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Credit Limit is "+accountData.getCreditLimit());}
									ccEntityFields.setCreditLimit(accountData.getCreditLimit() + Constants.EMPTY_STRING);
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## St General is "+ accountData.getStGeneral());}
									ccEntityFields.setStGeneral(accountData.getStGeneral());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Currency is "+accountData.getCurrency());}
									ccEntityFields.setCurrency(accountData.getCurrency());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Balance is "+ accountData.getBalance());}
									/***
									 * 26th Feb 2017 As per BM, correct decimal upto 3 places.
									 * ***/
									if(accountData.getBalance()!=null){
										ccEntityFields.setBalance(formatter.format(accountData.getBalance()) + Constants.EMPTY_STRING);
									}else{
										ccEntityFields.setBalance(accountData.getBalance() + Constants.EMPTY_STRING);
									}
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Last Txn Date is "+ accountData.getLastTrxnDate());}
									ccEntityFields.setLastTxnDate(accountData.getLastTrxnDate() + Constants.EMPTY_STRING);
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Currency Min Amount is "+ accountData.getCurrentMinimumAmount());}
									ccEntityFields.setCurrencyMinAmount(accountData.getCurrentMinimumAmount() + Constants.EMPTY_STRING);
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Bank Sort Code is "+ accountData.getBankSortCode());}
									ccEntityFields.setBankSortCode(accountData.getBankSortCode());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Bank Acct name is "+ accountData.getBankAccName());}
									ccEntityFields.setBankAccName(accountData.getBankAccName());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Bank Branch is "+ accountData.getBankBranch());}
									ccEntityFields.setBankBranch(accountData.getBankBranch());
								}

								statement = account.getStatement();
								if(!util.isNullOrEmpty(statement)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Stmt Generate Date is "+statement.getGenerateDate() );}
									ccEntityFields.setStmtGenerateDate(statement.getGenerateDate() + Constants.EMPTY_STRING);
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Stmt Opening Date is "+statement.getOpeningBalance());}
									ccEntityFields.setStmtOpeningDate(statement.getOpeningBalance() + Constants.EMPTY_STRING);
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## stmt Closing date is "+statement.getClosingBalance());}
									ccEntityFields.setStmtClosingDate(statement.getClosingBalance() + Constants.EMPTY_STRING);
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## stmt Min Date is "+statement.getMinDue());}
									/***
									 * 26th Feb 2017 As per BM, correct decimal upto 3 places.
									 * ***/
									if(statement.getMinDue()!=null){
										ccEntityFields.setStmtMinDue(formatter.format(statement.getMinDue()) + Constants.EMPTY_STRING);
									}else{
										ccEntityFields.setStmtMinDue(statement.getMinDue() + Constants.EMPTY_STRING);
									}
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Stmt Due date is "+statement.getDueDate() );}
									ccEntityFields.setStmtDueDate(statement.getDueDate() + Constants.EMPTY_STRING);
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  is stmt Prinit Due date "+statement.getPrintDueDate() );}
									ccEntityFields.setStmtPrintDueDate(statement.getPrintDueDate() + Constants.EMPTY_STRING);
								}

								overdue = account.getOverdue();
								if(!util.isNullOrEmpty(overdue)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  is Amount "+ overdue.getAmount() );}
									/***
									 * 26th Feb 2017 As per BM, correct decimal upto 3 places.
									 * ***/
									if(overdue.getAmount()!=null){
										ccEntityFields.setOverDueAmt(formatter.format(overdue.getAmount()) + Constants.EMPTY_STRING);
									}else{
										ccEntityFields.setOverDueAmt(overdue.getAmount() + Constants.EMPTY_STRING);
									}
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  is Over due cycles "+ overdue.getOverdueCycles() );}
									ccEntityFields.setOverDueCycles(overdue.getOverdueCycles() + Constants.EMPTY_STRING);
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  is Current over due amount "+ overdue.getCurrentOverdueAmount() );}
									ccEntityFields.setCurrentOverDueAmt(overdue.getCurrentOverdueAmount() + Constants.EMPTY_STRING);
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  is Current Over due cycle "+ overdue.getCurrentOverdueCycles());}
									ccEntityFields.setCurrentOverDueCycles(overdue.getCurrentOverdueCycles() + Constants.EMPTY_STRING);
								}


								payment = account.getPayment();
								if(!util.isNullOrEmpty(payment)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  is Payment date "+ payment.getPaymentDate() );}
									ccEntityFields.setPaymentDate(payment.getPaymentDate() + Constants.EMPTY_STRING);
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  is Payment amount "+ payment.getPaymentAmount()  );}
									ccEntityFields.setPaymentAmount(payment.getPaymentAmount() + Constants.EMPTY_STRING);

								}

								rewards = account.getRewards();
								if(!util.isNullOrEmpty(rewards)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Reward Amount is  "+ rewards.getAmount() );}
									ccEntityFields.setRewardAmount(rewards.getAmount() + Constants.EMPTY_STRING);

								}

								personEntity = account.getPerson();
								if(!util.isNullOrEmpty(personEntity)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Title name is  "+ personEntity.getTitle() );}
									ccEntityFields.setAccountLevelTitleName(personEntity.getTitle());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  First Name is  "+ personEntity.getFirstName());}
									ccEntityFields.setAccountLevelFirstName(personEntity.getFirstName());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Middle Name is  "+ personEntity.getMiddleName() );}
									ccEntityFields.setAccountLevelMiddleName(personEntity.getMiddleName());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Last Name is  "+ personEntity.getLastName());}
									ccEntityFields.setAccountLevelLastName(personEntity.getLastName());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  SSN is  "+ personEntity.getSSN() );}
									ccEntityFields.setAccountLevelSSN(personEntity.getSSN());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Cust ID is  "+ personEntity.getCustId() );}
									ccEntityFields.setAccountLevelCustID(personEntity.getCustId());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Language Code is  "+ personEntity.getLanguageCode() );}
									ccEntityFields.setAccountLevelLanguageCode(personEntity.getLanguageCode());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  VIP is  "+ personEntity.getVIP());}
									ccEntityFields.setAccountLevelVIP(personEntity.getVIP());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  DOB is  "+ personEntity.getDOB());}
									ccEntityFields.setDOB(personEntity.getDOB());
								}

								address = account.getAddress();
								if(!util.isNullOrEmpty(address)){

									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Location is  "+ personEntity.getVIP());}
									ccEntityFields.setLocation(address.getLocation());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Addr 1 is  "+ personEntity.getVIP());}
									ccEntityFields.setAddr1(address.getAddress1());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Addr 2 is  "+ personEntity.getVIP());}
									ccEntityFields.setAddr2(address.getAddress2());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Addr 3 is  "+ personEntity.getVIP());}
									ccEntityFields.setAddr3(address.getAddress3());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Addr 4 is  "+ personEntity.getVIP());}
									ccEntityFields.setAddr4(address.getAddress4());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Addr 5 is  "+ personEntity.getVIP());}
									ccEntityFields.setAddr5(address.getAddress5());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  City  is  "+ personEntity.getVIP());}
									ccEntityFields.setCity(address.getCity());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  State is  "+ personEntity.getVIP());}
									ccEntityFields.setState(address.getState());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Country is  "+ personEntity.getVIP());}
									ccEntityFields.setCountry(address.getCountry());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  ZIP is  "+ personEntity.getVIP());}
									ccEntityFields.setZip(address.getZIP());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  phone 1 is  "+ personEntity.getVIP());}
									ccEntityFields.setPhone1(address.getPhone1());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Phone 2 is  "+ personEntity.getVIP());}
									ccEntityFields.setPhone2(address.getPhone2());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Fax is  "+ personEntity.getVIP());}
									ccEntityFields.setFax(address.getFax());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Mobile is  "+ personEntity.getVIP());}
									ccEntityFields.setMobile(address.getMobile());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Email is  "+ personEntity.getVIP());}
									ccEntityFields.setEmail(address.getEmail());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Position is  "+ personEntity.getVIP());}
									ccEntityFields.setPosition(address.getPosition());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Type is  "+ personEntity.getVIP());}
									ccEntityFields.setType(address.getType());
								}

								extentionsType = account.getExtensions();
								if(!util.isNullOrEmpty(extentionsType) && !util.isNullOrEmpty(extentionsType.getField()) && !util.isNullOrEmpty(extentionsType.getField().get(Constants.GL_ZERO))){
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Extention Number is  "+ extentionsType.getField().get(Constants.GL_ZERO).getNumber());}
									ccEntityFields.setAtNumber(extentionsType.getField().get(Constants.GL_ZERO).getNumber());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Extension Field is  "+ extentionsType.getField().get(Constants.GL_ZERO).getValue());}
									ccEntityFields.setField(extentionsType.getField().get(Constants.GL_ZERO).getValue());
								}
								cardDetailList = account.getCard();
								Card card = null;
								APIN_CardDetails cardDetails = null;
								
								CardData cardData = null;
								PersonEntity personEntity2 = null;
								Authorization authorization = null;
								Online online = null; 
								ExtentionsType extentionsType2 = null;
								
								if(!util.isNullOrEmpty(cardDetailList)){
									for(int j=0; j<cardDetailList.size(); j++){
										card = cardDetailList.get(j);
										
										cardDetails = new APIN_CardDetails();
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Card Number is  "+ util.maskCardOrAccountNumber(card.getNumber()));}
										cardDetails.setCardNumber(card.getNumber());
										
										cardData = card.getCardData();
										if(!util.isNullOrEmpty(cardData)){
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Card Product is  "+ cardData.getProduct());}
											cardDetails.setCardProduct(cardData.getProduct());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Card Embossing number is  "+ util.maskCardOrAccountNumber(cardData.getEmbossingName()));}
											cardDetails.setEmbossingNumber(cardData.getEmbossingName());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Card Exp Date  is **** ");}
											cardDetails.setExpDate(cardData.getExpDate() + Constants.EMPTY_STRING);
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Card Previous Expiry Date  is **** ");}
											cardDetails.setPreviousExpiryDate(cardData.getPreviousExpiryDate());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Card St General is  "+ cardData.getStGeneral());}
											cardDetails.setStGeneral(cardData.getStGeneral());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Card Primary Card is  "+ cardData.getPrimaryCard());}
											cardDetails.setPrimaryCard(cardData.getPrimaryCard());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Card Activation Status is  "+ cardData.getCardActivationStatus());}
											cardDetails.setCardActivationStatus(cardData.getCardActivationStatus());
										}
										
										personEntity2 = card.getPerson();
										if(!util.isNullOrEmpty(personEntity2)){
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Title  is  "+ personEntity2.getTitle());}
											cardDetails.setCardTitle(personEntity2.getTitle());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## First  Name is  "+ personEntity2.getFirstName());}
											
											cardDetails.setCardFirstName(personEntity2.getFirstName());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Middle Name  is  "+ personEntity2.getMiddleName());}
											cardDetails.setCardMiddleName(personEntity2.getMiddleName());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  SSN is  "+ personEntity2.getSSN());}
											cardDetails.setCardSSN(personEntity2.getSSN());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Cust ID  is  "+ personEntity2.getCustId());}
											cardDetails.setCardCustID(personEntity2.getCustId());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Langauge Code is  "+ personEntity2.getLanguageCode());}
											cardDetails.setCardLanguageCode(personEntity2.getLanguageCode());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## VIP  is  "+ personEntity2.getVIP());}
											cardDetails.setCardVIP(personEntity2.getVIP());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  DOB is  "+ personEntity2.getDOB());}
											cardDetails.setDOB(personEntity2.getDOB());
										}
										
										authorization = card.getAuthorization();
										if(!util.isNullOrEmpty(authorization)){
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Auth Limit  is  "+ authorization.getAuthLimit());}
											cardDetails.setAuthLimit(authorization.getAuthLimit() + Constants.EMPTY_STRING);
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Auth Cash Limit  is  "+ authorization.getAuthCashLimit());}
											cardDetails.setAuthCashLimit(authorization.getAuthCashLimit() + Constants.EMPTY_STRING);
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Auth Tmp Bonus  is  "+ authorization.getAuthTmpBonus());}
											cardDetails.setAuthTmpBonus(authorization.getAuthTmpBonus() + Constants.EMPTY_STRING);
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Auth Tmp Bonus Date  is  "+ authorization.getAuthTmpBonusDate());}
											cardDetails.setAuthTmpBonusDate(authorization.getAuthTmpBonusDate() + Constants.EMPTY_STRING);
											
										}
										
										online = card.getOnline();
										if(!util.isNullOrEmpty(online)){
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Auth OTB  is  "+ online.getOTB());}
											cardDetails.setOtb(online.getOTB() + Constants.EMPTY_STRING);
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Auth Tmp Last Auth Date  is  "+ online.getLastAuthDate());}
											cardDetails.setLastAuthDate(online.getLastAuthDate() + Constants.EMPTY_STRING);
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Auth Tmp Outstanding Amt is  "+ online.getOutstandingAmount());}
											cardDetails.setOutstandingAmt(online.getOutstandingAmount() + Constants.EMPTY_STRING);
											
										}
										
										extentionsType2 = card.getExtensions();
										if(!util.isNullOrEmpty(extentionsType2) && !util.isNullOrEmpty(extentionsType2.getField()) && !util.isNullOrEmpty(extentionsType2.getField().get(Constants.GL_ZERO))){
											cardDetails.setField(extentionsType2.getField().get(Constants.GL_ZERO).getValue());
										}
										
										cardDetailsMap.put(card.getNumber(), cardDetails);
									}
								}
								
								ccEntityFields.setCardDetailsMap(cardDetailsMap);
								acctNoAccountDetailMap.put(account.getNumber(), ccEntityFields);
							}
						}

						customerID_AcctNOMap.put(customer.getNumber(), acctNolist);
					}

					beanResponse.setAcctNo_AccountDetailMap(acctNoAccountDetailMap);
					beanResponse.setCutomerID_AccountNumberMap(customerID_AcctNOMap);
					//For EPIN CR
					beanResponse.setCardDetailsMap(cardDetailsMap);
			}
		}
		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at CCEntityInquiryDAOImpl.getAPINValCustProfDetailsHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CCEntityInquiryDAOImpl.getAPINValCustProfDetailsHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: CCEntityInquiryDAOImpl.getAPINValCustProfDetailsHostRes()");}
		return beanResponse;
	}

	@Override
	public CreditCardBalanceDetails_HostRes getCCBalanceHostRes(
			CallInfo callInfo, String entyityInquiryType,
			String inquiryReference, ArrayList<String> numberList, String returnReplacedCards, String entityEnquirySize, String requestType) throws DaoException {
		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: CCEntityInquiryDAOImpl.getCCBalanceHostRes()");}
		DecimalFormat formatter = new DecimalFormat("0.000");
		CreditCardBalanceDetails_HostRes beanResponse = new CreditCardBalanceDetails_HostRes();

		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			CCEntityInqResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callCreditCardBalanceHost");}

			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = ccEntityInquiryService.callCreditCardBalanceHost(logger, sessionID, entyityInquiryType, inquiryReference, numberList, returnReplacedCards, entityEnquirySize, requestType, str_UUI, generateXML, callInfo);

			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callCreditCardBalanceHost is : "+code);}

			//Setting the ESB request reference number for reporting
			String esbReqRefNum = util.isNullOrEmpty(ws_ResponseHeader.getReqRefNum()) ? Constants.NA :  ws_ResponseHeader.getReqRefNum();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The ESB Request Reference number is "+ws_ResponseHeader.getReqRefNum());}
			callInfo.setField(Field.ESBREQREFNUM, esbReqRefNum);
			//END
			
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}

			beanResponse.setHostResponseCode(code);

			beanResponse.setErrorDesc(ws_ResponseHeader.getEsbErrDesc());
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for getCCBalanceHostRes is : "+ws_ResponseHeader.getEsbErrDesc());}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### CCEntityInq HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}

			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_CCEntityInquiry_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_CCEntityInquiry_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_CCEntityInquiry_Succ_ErrorCode);
			}
			
			
			code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}

			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){

					//Host Response fields
					Paging paging = response.getPaging();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Got Response for the field Paging "+paging);}

					if(!util.isNullOrEmpty(paging)){
						beanResponse.setPagingKey(paging.getKey());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Paging Key is "+paging.getKey());}

						beanResponse.setPagingKey(paging.getSize() + Constants.EMPTY_STRING);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Paging Size is "+paging.getSize());}
					}


					List<Customer> customerList = response.getCustomer();
					Customer customer = null;
					List<Account> accountList = null;
					
					HashMap<String, ArrayList<String>>customerID_AcctNOMap = new HashMap<String, ArrayList<String>>();
					HashMap<String, CCEntityFields> acctNoAccountDetailMap = new HashMap<String, CCEntityFields>();
					ArrayList<String>acctNolist = new ArrayList<String>();
					
					//Bean Response fields
					CCEntityFields ccEntityFields = null;
					HashMap<String, CardDetails> cardDetailsMap = null;

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Retrieved the customer list from the host , the customer list object is "+customerList);}

					for(int i = 0; i < customerList.size(); i++ ){
						customer = customerList.get(i);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The " + i + " index of the customerList is "+ customer);}

						accountList = customer.getAccount();

						Account account = null;
						AccountData accountData = null;
						Statement statement = null;
						Overdue overdue = null;
						Payment payment = null;
						Rewards rewards = null;
						PersonEntity personEntity = null;
						Address address = null;
						ExtentionsType extentionsType = null;
						List<Card> cardDetailList = null;

						for(int count =0; count < accountList.size(); count++){
							account = accountList.get(count);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "the "+count +"Account list objec is "+ account);}
							ccEntityFields = new CCEntityFields();
							cardDetailsMap = new HashMap<String, CardDetails>();
							
							if(!util.isNullOrEmpty(account)){

								ccEntityFields.setAccountNumber(account.getNumber());
								acctNolist.add(account.getNumber());

								accountData = account.getAccountData();
								if(!util.isNullOrEmpty(accountData)){

									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Acct Product is "+accountData.getProduct());}
									ccEntityFields.setAcctProduct(accountData.getProduct());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Credit Limit is "+accountData.getCreditLimit());}
									ccEntityFields.setCreditLimit(accountData.getCreditLimit() + Constants.EMPTY_STRING);
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## St General is "+ accountData.getStGeneral());}
									ccEntityFields.setStGeneral(accountData.getStGeneral());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Currency is "+accountData.getCurrency());}
									ccEntityFields.setCurrency(accountData.getCurrency());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Balance is "+ accountData.getBalance());}
									/***
									 * 26th Feb 2017 As per BM, correct decimal upto 3 places.
									 * ***/
									if(accountData.getBalance()!=null){
										ccEntityFields.setBalance(formatter.format(accountData.getBalance()) + Constants.EMPTY_STRING);
									}else{
										ccEntityFields.setBalance(accountData.getBalance() + Constants.EMPTY_STRING);
									}
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Last Txn Date is "+ accountData.getLastTrxnDate());}
									ccEntityFields.setLastTxnDate(util.convertXMLCalendarToString(accountData.getLastTrxnDate(), Constants.DATEFORMAT_YYYYMMDD));
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Currency Min Amount is "+ accountData.getCurrentMinimumAmount());}
									ccEntityFields.setCurrencyMinAmount(accountData.getCurrentMinimumAmount() + Constants.EMPTY_STRING);
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Bank Sort Code is "+ accountData.getBankSortCode());}
									ccEntityFields.setBankSortCode(accountData.getBankSortCode());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Bank Acct name is "+ accountData.getBankAccName());}
									ccEntityFields.setBankAccName(accountData.getBankAccName());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Bank Branch is "+ accountData.getBankBranch());}
									ccEntityFields.setBankBranch(accountData.getBankBranch());
								}

								statement = account.getStatement();
								if(!util.isNullOrEmpty(statement)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Stmt Generate Date is "+statement.getGenerateDate() );}
									ccEntityFields.setStmtGenerateDate(util.convertXMLCalendarToString(statement.getGenerateDate(),Constants.DATEFORMAT_YYYYMMDD));
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Stmt Opening Balance is "+statement.getOpeningBalance());}
									ccEntityFields.setStmtOpeningDate(statement.getOpeningBalance() + Constants.EMPTY_STRING);
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## stmt Closing Balance is "+statement.getClosingBalance());}
									ccEntityFields.setStmtClosingDate(statement.getClosingBalance() + Constants.EMPTY_STRING);
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## stmt Min Date is "+statement.getMinDue());}
									/***
									 * 26th Feb 2017 As per BM, correct decimal upto 3 places.
									 * ***/
									if(statement.getMinDue()!=null){
										ccEntityFields.setStmtMinDue(formatter.format(statement.getMinDue()) + Constants.EMPTY_STRING);
									}else{
										ccEntityFields.setStmtMinDue(statement.getMinDue() + Constants.EMPTY_STRING);
									}
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Stmt Due date is "+statement.getDueDate() );}
									ccEntityFields.setStmtDueDate(util.convertXMLCalendarToString(statement.getDueDate(), Constants.DATEFORMAT_YYYYMMDD));
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  is stmt Prinit Due date "+statement.getPrintDueDate() );}
									ccEntityFields.setStmtPrintDueDate(util.convertXMLCalendarToString(statement.getPrintDueDate(), Constants.DATEFORMAT_YYYYMMDD));
								}

								overdue = account.getOverdue();
								if(!util.isNullOrEmpty(overdue)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  is Amount "+ overdue.getAmount() );}
									/***
									 * 26th Feb 2017 As per BM, correct decimal upto 3 places.
									 * ***/
									if(overdue.getAmount()!=null){
										ccEntityFields.setOverDueAmt(formatter.format(overdue.getAmount()) + Constants.EMPTY_STRING);
									}else{
										ccEntityFields.setOverDueAmt(overdue.getAmount() + Constants.EMPTY_STRING);
									}
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  is Over due cycles "+ overdue.getOverdueCycles() );}
									ccEntityFields.setOverDueCycles(overdue.getOverdueCycles() + Constants.EMPTY_STRING);
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  is Current over due amount "+ overdue.getCurrentOverdueAmount() );}
									ccEntityFields.setCurrentOverDueAmt(overdue.getCurrentOverdueAmount() + Constants.EMPTY_STRING);
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  is Current Over due cycle "+ overdue.getCurrentOverdueCycles());}
									ccEntityFields.setCurrentOverDueCycles(overdue.getCurrentOverdueCycles() + Constants.EMPTY_STRING);
								}


								payment = account.getPayment();
								if(!util.isNullOrEmpty(payment)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  is Payment date "+ payment.getPaymentDate() );}
									ccEntityFields.setPaymentDate(util.convertXMLCalendarToString(payment.getPaymentDate(),Constants.DATEFORMAT_YYYYMMDD));
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  is Payment amount "+ payment.getPaymentAmount()  );}
									ccEntityFields.setPaymentAmount(payment.getPaymentAmount() + Constants.EMPTY_STRING);

								}

								rewards = account.getRewards();
								if(!util.isNullOrEmpty(rewards)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Reward Amount is  "+ rewards.getAmount() );}
									ccEntityFields.setRewardAmount(rewards.getAmount() + Constants.EMPTY_STRING);

								}

								personEntity = account.getPerson();
								if(!util.isNullOrEmpty(personEntity)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Title name is  "+ personEntity.getTitle() );}
									ccEntityFields.setAccountLevelTitleName(personEntity.getTitle());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  First Name is  "+ personEntity.getFirstName());}
									ccEntityFields.setAccountLevelFirstName(personEntity.getFirstName());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Middle Name is  "+ personEntity.getMiddleName() );}
									ccEntityFields.setAccountLevelMiddleName(personEntity.getMiddleName());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Last Name is  "+ personEntity.getLastName());}
									ccEntityFields.setAccountLevelLastName(personEntity.getLastName());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  SSN is  "+ personEntity.getSSN() );}
									ccEntityFields.setAccountLevelSSN(personEntity.getSSN());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Cust ID is  "+ personEntity.getCustId() );}
									ccEntityFields.setAccountLevelCustID(personEntity.getCustId());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Language Code is  "+ personEntity.getLanguageCode() );}
									ccEntityFields.setAccountLevelLanguageCode(personEntity.getLanguageCode());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  VIP is  "+ personEntity.getVIP());}
									ccEntityFields.setAccountLevelVIP(personEntity.getVIP());	
								}

								address = account.getAddress();
								if(!util.isNullOrEmpty(address)){

									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Location is  "+ address.getLocation());}
									ccEntityFields.setLocation(address.getLocation());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Addr 1 is  "+ address.getAddress1());}
									ccEntityFields.setAddr1(address.getAddress1());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Addr 2 is  "+ address.getAddress2());}
									ccEntityFields.setAddr2(address.getAddress2());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Addr 3 is  "+ address.getAddress3());}
									ccEntityFields.setAddr3(address.getAddress3());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Addr 4 is  "+ address.getAddress4());}
									ccEntityFields.setAddr4(address.getAddress4());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Addr 5 is  "+ address.getAddress5());}
									ccEntityFields.setAddr5(address.getAddress5());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  City  is  "+ address.getCity());}
									ccEntityFields.setCity(address.getCity());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  State is  "+ address.getState());}
									ccEntityFields.setState(address.getState());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Country is  "+ address.getCountry());}
									ccEntityFields.setCountry(address.getCountry());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  ZIP is  "+ address.getZIP());}
									ccEntityFields.setZip(address.getZIP());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  phone 1 is  "+ address.getPhone1());}
									ccEntityFields.setPhone1(address.getPhone1());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Phone 2 is  "+ address.getPhone2());}
									ccEntityFields.setPhone2(address.getPhone2());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Fax is  "+ address.getFax());}
									ccEntityFields.setFax(address.getFax());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Mobile is  "+ address.getMobile());}
									ccEntityFields.setMobile(address.getMobile());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Email is  "+ address.getEmail());}
									ccEntityFields.setEmail(address.getEmail());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Position is  "+ address.getPosition());}
									ccEntityFields.setPosition(address.getPosition());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Type is  "+ address.getType());}
									ccEntityFields.setType(address.getType());
								}

								extentionsType = account.getExtensions();
								if(!util.isNullOrEmpty(extentionsType) && !util.isNullOrEmpty(extentionsType.getField()) && !util.isNullOrEmpty(extentionsType.getField().get(Constants.GL_ZERO))){
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Extention Number is  "+ extentionsType.getField().get(Constants.GL_ZERO).getNumber());}
									ccEntityFields.setAtNumber(extentionsType.getField().get(Constants.GL_ZERO).getNumber());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Extension Field is  "+ extentionsType.getField().get(Constants.GL_ZERO).getValue());}
									ccEntityFields.setField(extentionsType.getField().get(Constants.GL_ZERO).getValue());
								}
								cardDetailList = account.getCard();
								Card card = null;
								CardDetails cardDetails = null;
								
								CardData cardData = null;
								PersonEntity personEntity2 = null;
								Authorization authorization = null;
								Online online = null; 
								ExtentionsType extentionsType2 = null;
								
								if(!util.isNullOrEmpty(cardDetailList)){
									for(int j=0; j<cardDetailList.size(); j++){
										card = cardDetailList.get(j);
										
										cardDetails = new CardDetails();
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Card Number is  "+ util.maskCardOrAccountNumber(card.getNumber()));}
										cardDetails.setCardNumber(card.getNumber());
										
										cardData = card.getCardData();
										if(!util.isNullOrEmpty(cardData)){
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Card Product is  "+ cardData.getProduct());}
											cardDetails.setCardProduct(cardData.getProduct());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Card Embossing number is  "+ util.maskCardOrAccountNumber(cardData.getEmbossingName()));}
											cardDetails.setEmbossingNumber(cardData.getEmbossingName());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Card Exp Date  is **** ");}
											cardDetails.setExpDate(util.convertXMLCalendarToString(cardData.getExpDate(), Constants.DATEFORMAT_YYYYMMDD));
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Card St General is  "+ cardData.getStGeneral());}
											cardDetails.setStGeneral(cardData.getStGeneral());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Card Primary Card is  "+ cardData.getPrimaryCard());}
											cardDetails.setPrimaryCard(cardData.getPrimaryCard());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Card Activation Status is  "+ cardData.getCardActivationStatus());}
											cardDetails.setCardActivationStatus(cardData.getCardActivationStatus());
										}
										
										personEntity2 = card.getPerson();
										if(!util.isNullOrEmpty(personEntity2)){
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Title  is  "+ personEntity2.getTitle());}
											cardDetails.setCardTitle(personEntity2.getTitle());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## First  Name is  "+ personEntity2.getFirstName());}
											
											cardDetails.setCardFirstName(personEntity2.getFirstName());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Middle Name  is  "+ personEntity2.getMiddleName());}
											cardDetails.setCardMiddleName(personEntity2.getMiddleName());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  SSN is  "+ personEntity2.getSSN());}
											cardDetails.setCardSSN(personEntity2.getSSN());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Cust ID  is  "+ personEntity2.getCustId());}
											cardDetails.setCardCustID(personEntity2.getCustId());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Langauge Code is  "+ personEntity2.getLanguageCode());}
											cardDetails.setCardLanguageCode(personEntity2.getLanguageCode());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## VIP  is  "+ personEntity2.getVIP());}
											cardDetails.setCardVIP(personEntity2.getVIP());
										}
										
										authorization = card.getAuthorization();
										if(!util.isNullOrEmpty(authorization)){
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Auth Limit  is  "+ authorization.getAuthLimit());}
											cardDetails.setAuthLimit(authorization.getAuthLimit() + Constants.EMPTY_STRING);
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Auth Cash Limit  is  "+ authorization.getAuthCashLimit());}
											cardDetails.setAuthCashLimit(authorization.getAuthCashLimit() + Constants.EMPTY_STRING);
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Auth Tmp Bonus  is  "+ authorization.getAuthTmpBonus());}
											cardDetails.setAuthTmpBonus(authorization.getAuthTmpBonus() + Constants.EMPTY_STRING);
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Auth Tmp Bonus Date  is  "+ authorization.getAuthTmpBonusDate());}
											cardDetails.setAuthTmpBonusDate(authorization.getAuthTmpBonusDate() + Constants.EMPTY_STRING);
											
										}
										
										online = card.getOnline();
										if(!util.isNullOrEmpty(online)){
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Auth OTB  is  "+ online.getOTB());}
											cardDetails.setOtb(online.getOTB() + Constants.EMPTY_STRING);
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Auth Tmp Last Auth Date  is  "+ online.getLastAuthDate());}
											cardDetails.setLastAuthDate(util.convertXMLCalendarToString(online.getLastAuthDate(), Constants.DATEFORMAT_YYYYMMDD));
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Auth Tmp Outstanding Amt is  "+ online.getOutstandingAmount());}
											cardDetails.setOutstandingAmt(online.getOutstandingAmount() + Constants.EMPTY_STRING);
											
										}
										
										extentionsType2 = card.getExtensions();
										if(!util.isNullOrEmpty(extentionsType2) && !util.isNullOrEmpty(extentionsType2.getField()) && !util.isNullOrEmpty(extentionsType2.getField().get(Constants.GL_ZERO))){
											cardDetails.setField(extentionsType2.getField().get(Constants.GL_ZERO).getValue());
										}
										
										cardDetailsMap.put(card.getNumber(), cardDetails);
									}
								}
								
								ccEntityFields.setCardDetailsMap(cardDetailsMap);
								acctNoAccountDetailMap.put(account.getNumber(), ccEntityFields);
							}
						}

						customerID_AcctNOMap.put(customer.getNumber(), acctNolist);
					}
					
					beanResponse.setAcctNo_AccountDetailMap(acctNoAccountDetailMap);
					beanResponse.setCutomerID_AccountNumberMap(customerID_AcctNOMap);

			}
		}
		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at CCEntityInquiryDAOImpl.getCCBalanceHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CCEntityInquiryDAOImpl.getCCBalanceHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: CCEntityInquiryDAOImpl.getCCBalanceHostRes()");}
		return beanResponse;
	}

	@Override
	public CreditCardDetails_HostRes getCCPaymentIntraCardDetailHostRes(
			CallInfo callInfo, String entyityInquiryType,String inquiryReference, ArrayList<String> numberList, String returnReplacedCards, String entityEnquirySize, String requestType) throws DaoException {
		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: CCEntityInquiryDAOImpl.getCCPaymentIntraCardDetailHostRes()");}
		DecimalFormat formatter = new DecimalFormat("0.000");
		CreditCardDetails_HostRes beanResponse = new CreditCardDetails_HostRes();

		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			CCEntityInqResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callCreditCardBalanceHost");}

			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = ccEntityInquiryService.callCreditCardBalanceHost(logger, sessionID, entyityInquiryType, inquiryReference, numberList, returnReplacedCards, entityEnquirySize, requestType,str_UUI, generateXML, callInfo);

			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callCreditCardBalanceHost is : "+code);}

			//Setting the ESB request reference number for reporting
			String esbReqRefNum = util.isNullOrEmpty(ws_ResponseHeader.getReqRefNum()) ? Constants.NA :  ws_ResponseHeader.getReqRefNum();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The ESB Request Reference number is "+ws_ResponseHeader.getReqRefNum());}
			callInfo.setField(Field.ESBREQREFNUM, esbReqRefNum);
			//END
			
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}

			beanResponse.setHostResponseCode(code);

			beanResponse.setErrorDesc(ws_ResponseHeader.getEsbErrDesc());
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for getCCBalanceHostRes is : "+ws_ResponseHeader.getEsbErrDesc());}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### CCEntityInq HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
			
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_CCEntityInquiry_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_CCEntityInquiry_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_CCEntityInquiry_Succ_ErrorCode);
			}
			
			
			code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}

			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){

					//Host Response fields
					Paging paging = response.getPaging();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Got Response for the field Paging "+paging);}

					if(!util.isNullOrEmpty(paging)){
						beanResponse.setPagingKey(paging.getKey());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Paging Key is "+paging.getKey());}

						beanResponse.setPagingKey(paging.getSize() + Constants.EMPTY_STRING);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Paging Size is "+paging.getSize());}
					}


					List<Customer> customerList = response.getCustomer();
					Customer customer = null;
					List<Account> accountList = null;
					
					HashMap<String, ArrayList<String>>customerID_AcctNOMap = new HashMap<String, ArrayList<String>>();
					HashMap<String, CCP_CCEntityFields> acctNoAccountDetailMap = new HashMap<String, CCP_CCEntityFields>();
					ArrayList<String>acctNolist = new ArrayList<String>();
					
					//Bean Response fields
					CCP_CCEntityFields ccEntityFields = null;
					HashMap<String, CCP_CardDetails> cardDetailsMap = null;

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Retrieved the customer list from the host , the customer list object is "+customerList);}

					for(int i = 0; i < customerList.size(); i++ ){
						customer = customerList.get(i);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The " + i + " index of the customerList is "+ customer);}

						accountList = customer.getAccount();

						Account account = null;
						AccountData accountData = null;
						Statement statement = null;
						Overdue overdue = null;
						Payment payment = null;
						Rewards rewards = null;
						PersonEntity personEntity = null;
						Address address = null;
						ExtentionsType extentionsType = null;
						List<Card> cardDetailList = null;

						for(int count =0; count < accountList.size(); count++){
							account = accountList.get(count);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "the "+count +"Account list objec is "+ account);}
							ccEntityFields = new CCP_CCEntityFields();
							cardDetailsMap = new HashMap<String, CCP_CardDetails>();
							
							if(!util.isNullOrEmpty(account)){

								ccEntityFields.setAccountNumber(account.getNumber());
								acctNolist.add(account.getNumber());

								accountData = account.getAccountData();
								if(!util.isNullOrEmpty(accountData)){

									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Acct Product is "+accountData.getProduct());}
									ccEntityFields.setAcctProduct(accountData.getProduct());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Credit Limit is "+accountData.getCreditLimit());}
									ccEntityFields.setCreditLimit(accountData.getCreditLimit() + Constants.EMPTY_STRING);
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## St General is "+ accountData.getStGeneral());}
									ccEntityFields.setStGeneral(accountData.getStGeneral());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Currency is "+accountData.getCurrency());}
									ccEntityFields.setCurrency(accountData.getCurrency());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Balance is "+ accountData.getBalance());}
									/***
									 * 26th Feb 2017 As per BM, correct decimal upto 3 places.
									 * ***/
									if(accountData.getBalance()!=null){
										ccEntityFields.setBalance(formatter.format(accountData.getBalance()) + Constants.EMPTY_STRING);
									}else{
										ccEntityFields.setBalance(accountData.getBalance() + Constants.EMPTY_STRING);
									}
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Last Txn Date is "+ accountData.getLastTrxnDate());}
									ccEntityFields.setLastTxnDate(accountData.getLastTrxnDate() + Constants.EMPTY_STRING);
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Currency Min Amount is "+ accountData.getCurrentMinimumAmount());}
									ccEntityFields.setCurrencyMinAmount(accountData.getCurrentMinimumAmount() + Constants.EMPTY_STRING);
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Bank Sort Code is "+ accountData.getBankSortCode());}
									ccEntityFields.setBankSortCode(accountData.getBankSortCode());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Bank Acct name is "+ accountData.getBankAccName());}
									ccEntityFields.setBankAccName(accountData.getBankAccName());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Bank Branch is "+ accountData.getBankBranch());}
									ccEntityFields.setBankBranch(accountData.getBankBranch());
								}

								statement = account.getStatement();
								if(!util.isNullOrEmpty(statement)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Stmt Generate Date is "+statement.getGenerateDate() );}
									ccEntityFields.setStmtGenerateDate(statement.getGenerateDate() + Constants.EMPTY_STRING);
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Stmt Opening Date is "+statement.getOpeningBalance());}
									ccEntityFields.setStmtOpeningDate(statement.getOpeningBalance() + Constants.EMPTY_STRING);
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## stmt Closing date is "+statement.getClosingBalance());}
									ccEntityFields.setStmtClosingDate(statement.getClosingBalance() + Constants.EMPTY_STRING);
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## stmt Min Date is "+statement.getMinDue());}
									/***
									 * 26th Feb 2017 As per BM, correct decimal upto 3 places.
									 * ***/
									if(statement.getMinDue()!=null){
										ccEntityFields.setStmtMinDue(formatter.format(statement.getMinDue()) + Constants.EMPTY_STRING);
									}else{
										ccEntityFields.setStmtMinDue(statement.getMinDue() + Constants.EMPTY_STRING);
									}
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Stmt Due date is "+statement.getDueDate() );}
									ccEntityFields.setStmtDueDate(statement.getDueDate() + Constants.EMPTY_STRING);
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  is stmt Prinit Due date "+statement.getPrintDueDate() );}
									ccEntityFields.setStmtPrintDueDate(statement.getPrintDueDate() + Constants.EMPTY_STRING);
								}

								overdue = account.getOverdue();
								if(!util.isNullOrEmpty(overdue)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  is Amount "+ overdue.getAmount() );}
									/***
									 * 26th Feb 2017 As per BM, correct decimal upto 3 places.
									 * ***/
									if(overdue.getAmount() != null){
										ccEntityFields.setOverDueAmt(formatter.format(overdue.getAmount()) + Constants.EMPTY_STRING);
									}else{
										ccEntityFields.setOverDueAmt(overdue.getAmount() + Constants.EMPTY_STRING);
									}
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  is Over due cycles "+ overdue.getOverdueCycles() );}
									ccEntityFields.setOverDueCycles(overdue.getOverdueCycles() + Constants.EMPTY_STRING);
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  is Current over due amount "+ overdue.getCurrentOverdueAmount() );}
									ccEntityFields.setCurrentOverDueAmt(overdue.getCurrentOverdueAmount() + Constants.EMPTY_STRING);
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  is Current Over due cycle "+ overdue.getCurrentOverdueCycles());}
									ccEntityFields.setCurrentOverDueCycles(overdue.getCurrentOverdueCycles() + Constants.EMPTY_STRING);
								}


								payment = account.getPayment();
								if(!util.isNullOrEmpty(payment)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  is Payment date "+ payment.getPaymentDate() );}
									ccEntityFields.setPaymentDate(payment.getPaymentDate() + Constants.EMPTY_STRING);
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  is Payment amount "+ payment.getPaymentAmount()  );}
									ccEntityFields.setPaymentAmount(payment.getPaymentAmount() + Constants.EMPTY_STRING);

								}

								rewards = account.getRewards();
								if(!util.isNullOrEmpty(rewards)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Reward Amount is  "+ rewards.getAmount() );}
									ccEntityFields.setRewardAmount(rewards.getAmount() + Constants.EMPTY_STRING);

								}

								personEntity = account.getPerson();
								if(!util.isNullOrEmpty(personEntity)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Title name is  "+ personEntity.getTitle() );}
									ccEntityFields.setAccountLevelTitleName(personEntity.getTitle());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  First Name is  "+ personEntity.getFirstName());}
									ccEntityFields.setAccountLevelFirstName(personEntity.getFirstName());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Middle Name is  "+ personEntity.getMiddleName() );}
									ccEntityFields.setAccountLevelMiddleName(personEntity.getMiddleName());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Last Name is  "+ personEntity.getLastName());}
									ccEntityFields.setAccountLevelLastName(personEntity.getLastName());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  SSN is  "+ personEntity.getSSN() );}
									ccEntityFields.setAccountLevelSSN(personEntity.getSSN());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Cust ID is  "+ personEntity.getCustId() );}
									ccEntityFields.setAccountLevelCustID(personEntity.getCustId());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Language Code is  "+ personEntity.getLanguageCode() );}
									ccEntityFields.setAccountLevelLanguageCode(personEntity.getLanguageCode());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  VIP is  "+ personEntity.getVIP());}
									ccEntityFields.setAccountLevelVIP(personEntity.getVIP());	
								}

								address = account.getAddress();
								if(!util.isNullOrEmpty(address)){

									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Location is  "+ personEntity.getVIP());}
									ccEntityFields.setLocation(address.getLocation());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Addr 1 is  "+ personEntity.getVIP());}
									ccEntityFields.setAddr1(address.getAddress1());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Addr 2 is  "+ personEntity.getVIP());}
									ccEntityFields.setAddr2(address.getAddress2());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Addr 3 is  "+ personEntity.getVIP());}
									ccEntityFields.setAddr3(address.getAddress3());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Addr 4 is  "+ personEntity.getVIP());}
									ccEntityFields.setAddr4(address.getAddress4());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Addr 5 is  "+ personEntity.getVIP());}
									ccEntityFields.setAddr5(address.getAddress5());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  City  is  "+ personEntity.getVIP());}
									ccEntityFields.setCity(address.getCity());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  State is  "+ personEntity.getVIP());}
									ccEntityFields.setState(address.getState());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Country is  "+ personEntity.getVIP());}
									ccEntityFields.setCountry(address.getCountry());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  ZIP is  "+ personEntity.getVIP());}
									ccEntityFields.setZip(address.getZIP());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  phone 1 is  "+ personEntity.getVIP());}
									ccEntityFields.setPhone1(address.getPhone1());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Phone 2 is  "+ personEntity.getVIP());}
									ccEntityFields.setPhone2(address.getPhone2());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Fax is  "+ personEntity.getVIP());}
									ccEntityFields.setFax(address.getFax());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Mobile is  "+ personEntity.getVIP());}
									ccEntityFields.setMobile(address.getMobile());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Email is  "+ personEntity.getVIP());}
									ccEntityFields.setEmail(address.getEmail());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Position is  "+ personEntity.getVIP());}
									ccEntityFields.setPosition(address.getPosition());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Type is  "+ personEntity.getVIP());}
									ccEntityFields.setType(address.getType());
								}

								extentionsType = account.getExtensions();
								if(!util.isNullOrEmpty(extentionsType) && !util.isNullOrEmpty(extentionsType.getField()) && !util.isNullOrEmpty(extentionsType.getField().get(Constants.GL_ZERO))){
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Extention Number is  "+ extentionsType.getField().get(Constants.GL_ZERO).getNumber());}
									ccEntityFields.setAtNumber(extentionsType.getField().get(Constants.GL_ZERO).getNumber());
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Extension Field is  "+ extentionsType.getField().get(Constants.GL_ZERO).getValue());}
									ccEntityFields.setField(extentionsType.getField().get(Constants.GL_ZERO).getValue());
								}
								cardDetailList = account.getCard();
								Card card = null;
								CCP_CardDetails cardDetails = null;
								
								CardData cardData = null;
								PersonEntity personEntity2 = null;
								Authorization authorization = null;
								Online online = null; 
								ExtentionsType extentionsType2 = null;
								
								if(!util.isNullOrEmpty(cardDetailList)){
									for(int j=0; j<cardDetailList.size(); j++){
										card = cardDetailList.get(j);
										
										cardDetails = new CCP_CardDetails();
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Card Number is  "+ util.maskCardOrAccountNumber(card.getNumber()));}
										cardDetails.setCardNumber(card.getNumber());
										
										cardData = card.getCardData();
										if(!util.isNullOrEmpty(cardData)){
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Card Product is  "+ cardData.getProduct());}
											cardDetails.setCardProduct(cardData.getProduct());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Card Embossing number is  "+ util.maskCardOrAccountNumber(cardData.getEmbossingName()));}
											cardDetails.setEmbossingNumber(cardData.getEmbossingName());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Card Exp Date  is  **** ");}
											cardDetails.setExpDate(cardData.getExpDate() + Constants.EMPTY_STRING);
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Card St General is  "+ cardData.getStGeneral());}
											cardDetails.setStGeneral(cardData.getStGeneral());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Card Primary Card is  "+ cardData.getPrimaryCard());}
											cardDetails.setPrimaryCard(cardData.getPrimaryCard());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Card Activation Status is  "+ cardData.getCardActivationStatus());}
											cardDetails.setCardActivationStatus(cardData.getCardActivationStatus());
										}
										
										personEntity2 = card.getPerson();
										if(!util.isNullOrEmpty(personEntity2)){
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Title  is  "+ personEntity2.getTitle());}
											cardDetails.setCardTitle(personEntity2.getTitle());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## First  Name is  "+ personEntity2.getFirstName());}
											
											cardDetails.setCardFirstName(personEntity2.getFirstName());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Middle Name  is  "+ personEntity2.getMiddleName());}
											cardDetails.setCardMiddleName(personEntity2.getMiddleName());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  SSN is  "+ personEntity2.getSSN());}
											cardDetails.setCardSSN(personEntity2.getSSN());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Cust ID  is  "+ personEntity2.getCustId());}
											cardDetails.setCardCustID(personEntity2.getCustId());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Langauge Code is  "+ personEntity2.getLanguageCode());}
											cardDetails.setCardLanguageCode(personEntity2.getLanguageCode());
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## VIP  is  "+ personEntity2.getVIP());}
											cardDetails.setCardVIP(personEntity2.getVIP());
										}
										
										authorization = card.getAuthorization();
										if(!util.isNullOrEmpty(authorization)){
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Auth Limit  is  "+ authorization.getAuthLimit());}
											cardDetails.setAuthLimit(authorization.getAuthLimit() + Constants.EMPTY_STRING);
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Auth Cash Limit  is  "+ authorization.getAuthCashLimit());}
											cardDetails.setAuthCashLimit(authorization.getAuthCashLimit() + Constants.EMPTY_STRING);
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Auth Tmp Bonus  is  "+ authorization.getAuthTmpBonus());}
											cardDetails.setAuthTmpBonus(authorization.getAuthTmpBonus() + Constants.EMPTY_STRING);
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Auth Tmp Bonus Date  is  "+ authorization.getAuthTmpBonusDate());}
											cardDetails.setAuthTmpBonusDate(authorization.getAuthTmpBonusDate() + Constants.EMPTY_STRING);
											
										}
										
										online = card.getOnline();
										if(!util.isNullOrEmpty(online)){
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Auth OTB  is  "+ online.getOTB());}
											cardDetails.setOtb(online.getOTB() + Constants.EMPTY_STRING);
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Auth Tmp Last Auth Date  is  "+ online.getLastAuthDate());}
											cardDetails.setLastAuthDate(online.getLastAuthDate() + Constants.EMPTY_STRING);
											
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Auth Tmp Outstanding Amt is  "+ online.getOutstandingAmount());}
											cardDetails.setOutstandingAmt(online.getOutstandingAmount() + Constants.EMPTY_STRING);
											
										}
										
										extentionsType2 = card.getExtensions();
										if(!util.isNullOrEmpty(extentionsType2) && !util.isNullOrEmpty(extentionsType2.getField()) && !util.isNullOrEmpty(extentionsType2.getField().get(Constants.GL_ZERO))){
											cardDetails.setField(extentionsType2.getField().get(Constants.GL_ZERO).getValue());
										}
										
										cardDetailsMap.put(card.getNumber(), cardDetails);
									}
								}
								
								ccEntityFields.setCardDetailsMap(cardDetailsMap);
								acctNoAccountDetailMap.put(account.getNumber(), ccEntityFields);
							}
						}

						customerID_AcctNOMap.put(customer.getNumber(), acctNolist);
					}

					beanResponse.setAcctNo_AccountDetailMap(acctNoAccountDetailMap);
					beanResponse.setCutomerID_AccountNumberMap(customerID_AcctNOMap);

			}
		}
		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at CCEntityInquiryDAOImpl.getCCPaymentIntraCardDetailHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CCEntityInquiryDAOImpl.getCCPaymentIntraCardDetailHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: CCEntityInquiryDAOImpl.getCCPaymentIntraCardDetailHostRes()");}
		return beanResponse;
	}

}
