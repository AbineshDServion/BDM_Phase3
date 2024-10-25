package com.servion.dao.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bankmuscat.esb.commontypes.AcctIdInfoType;
import com.bankmuscat.esb.commontypes.CardInfoType;
import com.bankmuscat.esb.commontypes.CustomerInfoType;
import com.bankmuscat.esb.customermgmtservice.CCEntityInqResType;
import com.bankmuscat.esb.customermgmtservice.CCEntityInqResType.Customer;
import com.bankmuscat.esb.customermgmtservice.CCEntityInqResType.Customer.Account;
import com.bankmuscat.esb.customermgmtservice.CCEntityInqResType.Customer.Account.AccountData;
import com.bankmuscat.esb.customermgmtservice.CCEntityInqResType.Customer.Account.Address;
import com.bankmuscat.esb.customermgmtservice.CCEntityInqResType.Customer.Account.Card;
import com.bankmuscat.esb.customermgmtservice.CCEntityInqResType.Customer.Account.Card.Authorization;
import com.bankmuscat.esb.customermgmtservice.CCEntityInqResType.Customer.Account.Card.CardData;
import com.bankmuscat.esb.customermgmtservice.CCEntityInqResType.Customer.Account.Card.Online;
import com.bankmuscat.esb.customermgmtservice.CCEntityInqResType.Customer.Account.Overdue;
import com.bankmuscat.esb.customermgmtservice.CCEntityInqResType.Customer.Account.Payment;
import com.bankmuscat.esb.customermgmtservice.CCEntityInqResType.Customer.Account.Rewards;
import com.bankmuscat.esb.customermgmtservice.CCEntityInqResType.Customer.Account.Statement;
import com.bankmuscat.esb.customermgmtservice.CustAcctListResType;
import com.bankmuscat.esb.customermgmtservice.CustShortDtlsInqResType;
import com.bankmuscat.esb.customermgmtservice.CustomerProfileAggregateResType;
import com.bankmuscat.esb.customermgmtservice.PersonEntity;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.context.Context;
import com.servion.dao.CustomerProfileAggregateDAO;
import com.servion.exception.DaoException;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.ice.RuleEngine.ICERuleParam;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.callerIdentification.AcctInfo;
import com.servion.model.callerIdentification.CallerIdentification_HostRes;
import com.servion.model.callerIdentification.CardAcctDtl;
import com.servion.model.callerIdentification.CustomerEntityAccountCardDtl;
import com.servion.model.callerIdentification.CustomerEntityAccountDtl;
import com.servion.model.callerIdentification.CustomerEntityDtl;
import com.servion.model.callerIdentification.CustomerShortDetails;
import com.servion.services.IGlobal;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.header.WS_RequestHeader;
import com.servion.ws.header.WS_ResponseHeader;
import com.servion.ws.header.impl.WS_ResponseHeaderImpl;
import com.servion.ws.service.CustomerProfileAggregateService;
import com.servion.ws.util.DAOLayerUtils;

public class CustomerProfileAggregateDAOImpl implements CustomerProfileAggregateDAO{
	private static Logger logger = LoggerObject.getLogger();
	
	@Autowired
	CustomerProfileAggregateService CustomerProfileAggregateService;

	public CustomerProfileAggregateService getCustomerProfileAggregateService() {
		return CustomerProfileAggregateService;
	}

	public void setCustomerProfileAggregateService(
			CustomerProfileAggregateService customerProfileAggregateService) {
		CustomerProfileAggregateService = customerProfileAggregateService;
	}

	//Object will initialized through Spring BEAN Injection
	private WS_RequestHeader ws_RequestHeader;

	public WS_RequestHeader getWs_RequestHeader() {
		return ws_RequestHeader;
	}

	public void setWs_RequestHeader(WS_RequestHeader ws_RequestHeader) {
		this.ws_RequestHeader = ws_RequestHeader;
	}

	//Not initializing through BEAN Injection
	private WS_ResponseHeader ws_ResponseHeader = new WS_ResponseHeaderImpl();



	@Override
	public CallerIdentification_HostRes getCallerIdentificationHostRes(CallInfo callInfo, String customerID, String requestType) throws DaoException {
		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: CustomerProfileAggregateDAOImpl.getCallerIdentificationHostRes()");}
		CallerIdentification_HostRes beanResponse = new CallerIdentification_HostRes();
		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			CustomerProfileAggregateResType response = null;

			CustAcctListResType custAcctListResType = null;
			CardInfoType cardInfoType = null;
			AcctIdInfoType acctIdInfoType = null;
			List<CardInfoType> cardInfoTypeList = null;
			List<AcctIdInfoType> acctIdInfoTypeList = null;
			CCEntityInqResType ccEntityInqResType = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Fetching the Feature Object values");}
			
			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			

			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Customer ID value is :"+customerID);}
			response = CustomerProfileAggregateService.callCustomerProfileDetailHost(logger, sessionID, customerID, requestType, str_UUI, generateXML, callInfo);

			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of Customer Profile Aggregate service is : "+code);}
			WriteLog.hostLogWrite(sessionID, (String)callInfo.getField(Field.HOST_SERVICE_NAME), code);
			
			//Setting the ESB request reference number for reporting
			String esbReqRefNum = util.isNullOrEmpty(ws_ResponseHeader.getReqRefNum()) ? Constants.NA :  ws_ResponseHeader.getReqRefNum();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The ESB Request Reference number is "+ws_ResponseHeader.getReqRefNum());}
			callInfo.setField(Field.ESBREQREFNUM, esbReqRefNum);
			//END
			
			beanResponse.setHostResponseCode(code);

			beanResponse.setErrorDesc(ws_ResponseHeader.getEsbErrDesc());
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for Customer Profile aggregate service is : "+ws_ResponseHeader.getEsbErrDesc());}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### CustomerProfileAggregate HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
			
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}

			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}

			String hostErrorCodeList = Constants.EMPTY_STRING;

			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_CustomerProfileDts_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_CustomerProfileDts_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_CustomerProfileDts_Succ_ErrorCode);
			}


			code = util.isCodePresentInTheList(code, hostErrorCodeList ,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}


			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);


			//TODO
			/**
			 * 
			 * 
			 * 
			 * AcctCur changed as AcctCcy in specification.
			 */


			if(Constants.WS_SUCCESS_CODE.equals(code)){

				if(!util.isNullOrEmpty(response)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Retrieve the respone object of Customer Profile Details host");}

					ICEGlobalConfig ivr_ICEGlobalObject = (ICEGlobalConfig)callInfo.getICEGlobalConfig();
					if(util.isNullOrEmpty(ivr_ICEGlobalObject)){
						throw new ServiceException("ivr_ICEGlobalObject object is null");
					}

					CardAcctDtl cardAcctDtl = null;
					ArrayList<CardAcctDtl> cardAcctDtlList = new ArrayList<CardAcctDtl>();
					ArrayList<CardAcctDtl> filteredCardAcctDtlList = new ArrayList<CardAcctDtl>();
					HashMap<String, CardAcctDtl> cardDetailMap = new HashMap<String, CardAcctDtl>();
					ArrayList<String> creditCardNumberList = new ArrayList<>();
					ArrayList<String> prepaidCardNumberList = new ArrayList<>();
					
					
					HashMap<String, ArrayList<String>> accountAndCreditCardMap = new HashMap<String, ArrayList<String>>();
					ArrayList<String>ccList = null;

					String cardActiveStatusCode = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_CARD_ACTIVE_STATUS);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Configured Card Active Status in the UI is "+ cardActiveStatusCode);}

					String cardInActiveStatusCode = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_CARD_IN_ACTIVE_STATUS);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Configured Card In Active Status in the UI is "+ cardInActiveStatusCode);}

					String creditCardTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_CARDTYPE_CREDITCARD);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Configured credit card type list is "+ creditCardTypeList);}
					
					String prepaidCardTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_CARDTYPE_PREPAIDCARD);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Configured prepaid card type list is "+ prepaidCardTypeList);}

					CustomerShortDetails customerShortDetails = new CustomerShortDetails();
					CustShortDtlsInqResType custShortDtlInqResType = response.getCustShortDtlInqRes();

					if(!util.isNullOrEmpty(custShortDtlInqResType)){
						CustomerInfoType customerInfoType = custShortDtlInqResType.getCustInfo();
						if(!util.isNullOrEmpty(customerInfoType)){
							/**
							 * CustType is handled wrongly - need to check with the host team regarding priority customer
							 * 
							 * as per confirmation given by Lakshmi custType should be of Retail / Islamic / Priority / IslamicPriority
							 * 
							 */

							/**
							 * Setting cust type for Cutomer segment type - As per the confirmation given by Lakshmi / Faisal
							 *
							 */
							customerShortDetails.setCustType(customerInfoType.getCustType());
							customerShortDetails.setCustSegment(customerInfoType.getCustType());

							//							callInfo.setField(Field.CUST_SEGMENT_TYPE, customerInfoType.getCustType());
							//							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Cutomer Segment type is "+  customerInfoType.getCustType());}



							/**
							 * Rule engine update
							 */
							ICERuleParam ruleParamObj = (ICERuleParam)callInfo.getICERuleParam();

							if(util.isNullOrEmpty(ruleParamObj)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "***********Rule Engine Object is null or empty*************" + ruleParamObj);}
							}

							/**
							 * No need to update the customersegmentation at the customerprofile aggregate
							 */

							//							if(!util.isNullOrEmpty(customerInfoType.getCustType())){
							//								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting Customer Segment of customer profile in the Rule Engine " +  customerInfoType.getCustType());}
							//								ruleParamObj.setIVRParam(Constants.RULE_ENGINE_CUSTOMERSEGMEMTATION,  customerInfoType.getCustType());
							//								ruleParamObj.updateIVRFields();
							//							}

							//END Rule Engine Updation


							/**
							 * TODO
							 * Need to set the customer segment value in the ruleengine method and call the update method of Rule engine
							 */
							//END 


							//TODO Once Faisal has provided with the priority customer field, we need to set that here under custInfo
							//customerShortDetails.setPriorityCustomer(priorityCustomer);

							customerShortDetails.setPriorityCustomer(customerInfoType.getBrandId());
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Priority Customer flag is "+  customerInfoType.getBrandId());}

							customerShortDetails.setPersonalInfoTypeList(customerInfoType.getPersonInfo());
							if(!util.isNullOrEmpty(customerInfoType.getPersonInfo()) && !util.isNullOrEmpty(customerInfoType.getPersonInfo().get(0))){

								/**
								 * Setting first index value of personalInfoType array list
								 */
								customerShortDetails.setFAX(customerInfoType.getPersonInfo().get(Constants.GL_ZERO).getFAX());
								customerShortDetails.setGSM(customerInfoType.getPersonInfo().get(Constants.GL_ZERO).getSMS());
								customerShortDetails.setEMAIL(customerInfoType.getPersonInfo().get(Constants.GL_ZERO).getEmail());
								customerShortDetails.setDOB(customerInfoType.getPersonInfo().get(Constants.GL_ZERO).getDateOfBirth());
								customerShortDetails.setLanguage(customerInfoType.getPersonInfo().get(Constants.GL_ZERO).getLanguage());
								customerShortDetails.setGender(customerInfoType.getPersonInfo().get(Constants.GL_ZERO).getGender());

								callInfo.setField(Field.REG_EMAIL, customerShortDetails.getEMAIL());
								callInfo.setField(Field.REG_FAXNO, customerShortDetails.getFAX());
								callInfo.setField(Field.REG_MOBILENO, customerShortDetails.getGSM());
								callInfo.setField(Field.GENDER, customerShortDetails.getGender());
								
//								callInfo.setField(Field.REG_EMAIL, "vinoths@bankmuscat.com");
								/**
								 * Rule engine update
								 */
								if(!util.isNullOrEmpty(customerShortDetails.getGender())){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting  Gender in the Rule Engine " + customerShortDetails.getGender());}
									ruleParamObj.setIVRParam(Constants.RULE_ENGINE_GENDER, customerShortDetails.getGender());
								}

								if(!util.isNullOrEmpty(customerShortDetails.getDOB())){
									String dateOfBirth = Constants.EMPTY_STRING;
									dateOfBirth = util.convertXMLCalendarToString(customerShortDetails.getDOB(), Constants.DATEFORMAT_YYYYMMDDHHMMSS);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting  DOB in the Rule Engine " );}
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting  DOB in the Rule Engine " + dateOfBirth);}
									ruleParamObj.setIVRParam(Constants.RULE_ENGINE_DATEOFBIRTH, dateOfBirth);
								}

								ICEGlobalConfig iceGlobalConfig = (ICEGlobalConfig)callInfo.getICEGlobalConfig();

								if(util.isNullOrEmpty(iceGlobalConfig)){
									throw new ServiceException("iceGlobalConfig object is null or empty");
								}
								String noOfDayForBirthAnnc = (String) iceGlobalConfig.getConfig().getParamValue(Constants.CUI_NoOfDays_DOBAnnc);
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "No of Birth Day configured greeting is " + noOfDayForBirthAnnc);}

								if(!util.isNullOrEmpty(noOfDayForBirthAnnc)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting  No Of DOB in the Rule Engine " + noOfDayForBirthAnnc);}
									ruleParamObj.setIVRParam(Constants.RULE_ENGINE_NOOFDAYS_DOBANNC, noOfDayForBirthAnnc +Constants.EMPTY);

								}

								if(!util.isNullOrEmpty(customerShortDetails.getGender())
										|| !util.isNullOrEmpty(customerShortDetails.getDOB())
										||!util.isNullOrEmpty(noOfDayForBirthAnnc)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Updating the rule engine fields" + noOfDayForBirthAnnc);}
									ruleParamObj.updateIVRFields();
								}
								//END Rule Engine Updation
							}
						}

					}
					beanResponse.setCustomerShortDetails(customerShortDetails);

					ccEntityInqResType = response.getCCEntityInqRes();

					//					String cardActiveStatusCode = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_CARD_ACTIVE_STATUS);
					//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Configured Card Active Status in the UI is "+ cardActiveStatusCode);}
					//					

					if(!util.isNullOrEmpty(ccEntityInqResType)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Going to retrieve Customer Entity Details type reponse object");}

						CustomerEntityDtl customerEntityDtl = new CustomerEntityDtl();

						List<Customer> cutomerList = ccEntityInqResType.getCustomer();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Retrieved the customer List from ccEntityInq Response" + cutomerList );}

						if(!util.isNullOrEmpty(cutomerList)){
							String customerNumber = Constants.EMPTY;
							String accountNumber = Constants.EMPTY;
							String cardNo = Constants.EMPTY;
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Customer List retrieved from entity response" + cutomerList.size());}

							HashMap<String, ArrayList<CustomerEntityAccountDtl>> customerEntityAccountDtlMap = new HashMap<String, ArrayList<CustomerEntityAccountDtl>>();
							ArrayList<CustomerEntityAccountDtl> arrayListCustomerEntityAcctDtls = new ArrayList<>();
							CustomerEntityAccountDtl customerEntityAccountDtl = null;

							HashMap<String, ArrayList<CustomerEntityAccountCardDtl>> customerEntityAccountCardDtlsMap = null;
							ArrayList<CustomerEntityAccountCardDtl> customerEntityAccountCardDtlsList = null;
							CustomerEntityAccountCardDtl customerEntityAccountCardDtl = null;


							String [] creditTypeArray = creditCardTypeList != null ? creditCardTypeList.split(Constants.COMMA) : new String[creditCardTypeList.split(Constants.COMMA).length];
							String creditCardType = creditTypeArray[Constants.GL_ZERO];
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Default Credit Card type going to have for this call flow is "+ creditCardType);}
							String [] prepaidTypeArray = prepaidCardTypeList != null ? prepaidCardTypeList.split(Constants.COMMA) : new String[prepaidCardTypeList.split(Constants.COMMA).length];
							String prepaidCardType = prepaidTypeArray[Constants.GL_ZERO];
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Default Prepaid Card type going to have for this call flow is "+ prepaidCardType);}
							List<Account> accountList = null;

							for(int i=Constants.GL_ZERO; i < cutomerList.size(); i++){
								customerNumber = cutomerList.get(i).getNumber();
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Customer " + cutomerList.get(i).getNumber());}

								if(util.isNullOrEmpty(customerEntityDtl.getFirstCustomerNo())){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting First Customer Number " + customerNumber);}
									customerEntityDtl.setFirstCustomerNo(customerNumber);
								}

								accountList = cutomerList.get(i).getAccount();
								Account account = null;
								AccountData accountData = null;
								Statement statement = null;
								Overdue overDue = null;
								Payment payment = null;
								Rewards rewards = null;
								PersonEntity personEntity = null;
								Address address = null;
								List<Card> cardList = null;
								Card card = null;
								CardData cardData = null;
								PersonEntity personEntityCard = null;
								Authorization authorization = null;
								Online online = null;


								if(!util.isNullOrEmpty(accountList)){
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Customer Account Detail List manipulation " + customerEntityAccountDtl);}
									for(int j=0; j <accountList.size(); j++){
										customerEntityAccountDtl = new CustomerEntityAccountDtl();
										customerEntityAccountCardDtlsMap = new HashMap<String, ArrayList<CustomerEntityAccountCardDtl>>();

										account = accountList.get(j);
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Account object is " + account);}

										
										if(!util.isNullOrEmpty(account)){
											customerEntityAccountDtl.setAccountNumber(account.getNumber());

											if(util.isNullOrEmpty(customerEntityDtl.getFirstCCAccountNo())){
												if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting First CC Account Number in CCEntity " + util.maskCardOrAccountNumber(account.getNumber()));}
												customerEntityDtl.setFirstCCAccountNo(account.getNumber());
											}


											accountData = account.getAccountData();
										}
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The AccountData object is " + accountData);}
										if(!util.isNullOrEmpty(accountData)){

											customerEntityAccountDtl.setAccountProduct(accountData.getProduct());
											customerEntityAccountDtl.setAccountcreditLimit(accountData.getCreditLimit() + Constants.EMPTY);
											customerEntityAccountDtl.setAccountGeneralStatus(accountData.getStGeneral());
											customerEntityAccountDtl.setAccountCurr(accountData.getCurrency());
											customerEntityAccountDtl.setAccountBalance(accountData.getBalance() + Constants.EMPTY);
											customerEntityAccountDtl.setLasttxnDate(accountData.getLastTrxnDate() + Constants.EMPTY);
											customerEntityAccountDtl.setAccountCurrentMiniAmount(accountData.getCurrentMinimumAmount() + Constants.EMPTY);
											customerEntityAccountDtl.setBankAccName(accountData.getBankAccName());
											customerEntityAccountDtl.setBankBranch(accountData.getBankBranch());
											customerEntityAccountDtl.setBankSortCode(accountData.getBankSortCode());
										}

										statement = account.getStatement();
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Account Statement object is " + statement);}
										if(!util.isNullOrEmpty(statement)){


											customerEntityAccountDtl.setStmtCloseBalance(statement.getClosingBalance() + Constants.EMPTY);
											customerEntityAccountDtl.setStmtDueDate(statement.getDueDate() + Constants.EMPTY);
											customerEntityAccountDtl.setStmtGenerationDate(statement.getGenerateDate() + Constants.EMPTY);
											customerEntityAccountDtl.setStmtMinDue(statement.getMinDue() + Constants.EMPTY);
											customerEntityAccountDtl.setStmtOpenBalance(statement.getOpeningBalance() + Constants.EMPTY);
											customerEntityAccountDtl.setStmtPrintDueDate(statement.getPrintDueDate() + Constants.EMPTY);

										}

										overDue = account.getOverdue();
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Account overDue object is " + overDue);}
										if(!util.isNullOrEmpty(statement)){

											customerEntityAccountDtl.setOverDueAmount(overDue.getAmount() + Constants.EMPTY);
											customerEntityAccountDtl.setOverDueCycles(overDue.getCurrentOverdueCycles() + Constants.EMPTY);
											customerEntityAccountDtl.setCurrentOverdueAmount(overDue.getCurrentOverdueAmount() + Constants.EMPTY);
											customerEntityAccountDtl.setCurrentOverdueCycle(overDue.getCurrentOverdueCycles()+ Constants.EMPTY);


										}

										payment = account.getPayment();
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Account payment object is " + payment);}
										if(!util.isNullOrEmpty(payment)){

											customerEntityAccountDtl.setPmtDate(payment.getPaymentDate() + Constants.EMPTY);
											customerEntityAccountDtl.setPmtAmount(payment.getPaymentAmount() + Constants.EMPTY);

										}


										rewards = account.getRewards();
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Account rewards object is " + rewards);}
										if(!util.isNullOrEmpty(rewards)){
											customerEntityAccountDtl.setRewardAmount(rewards.getAmount() + Constants.EMPTY );
										}

										personEntity = account.getPerson();
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Account personEntity object is " + personEntity);}
										if(!util.isNullOrEmpty(personEntity)){

											customerEntityAccountDtl.setTitle(personEntity.getTitle());
											customerEntityAccountDtl.setCardAcctHldrTitle(personEntity.getTitle());
											customerEntityAccountDtl.setCardAcctHldrFirstName(personEntity.getFirstName());
											customerEntityAccountDtl.setCardAcctHldrLastName(personEntity.getLastName());
											customerEntityAccountDtl.setCardAcctHldrMiddleName(personEntity.getMiddleName());
											customerEntityAccountDtl.setAcctHldrCustID(personEntity.getCustId());
											customerEntityAccountDtl.setAcctHldrLangCode(personEntity.getLanguageCode());
											customerEntityAccountDtl.setAcctHldrSSN(personEntity.getSSN());
											customerEntityAccountDtl.setAcctHldrVIPFlag(personEntity.getVIP());
										}

										address = account.getAddress();
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Account address object is " + address);}
										if(!util.isNullOrEmpty(address)){

											customerEntityAccountDtl.setAddrLocation(address.getLocation());
											customerEntityAccountDtl.setAddrOne(address.getAddress1());
											customerEntityAccountDtl.setAddrTwo(address.getAddress2());
											customerEntityAccountDtl.setAddrThree(address.getAddress3());
											customerEntityAccountDtl.setAddrFour(address.getAddress4());
											customerEntityAccountDtl.setAddrFive(address.getAddress5());
											customerEntityAccountDtl.setAddrCity(address.getCity());
											customerEntityAccountDtl.setAddrState(address.getState());
											customerEntityAccountDtl.setAddrCountry(address.getCountry());
											customerEntityAccountDtl.setAddrZip(address.getZIP());
											customerEntityAccountDtl.setAddrPhoneOne(address.getPhone1());
											customerEntityAccountDtl.setAddrPhoneTwo(address.getPhone2());
											customerEntityAccountDtl.setFax(address.getFax());
											customerEntityAccountDtl.setMobile(address.getMobile());
											customerEntityAccountDtl.setEmail(address.getEmail());
											customerEntityAccountDtl.setText(address.getText());
											customerEntityAccountDtl.setLastName(address.getLastName());
											customerEntityAccountDtl.setFirstName(address.getFirstName());
											customerEntityAccountDtl.setPosition(address.getPosition());
											customerEntityAccountDtl.setType(address.getType());
										}


										accountNumber = account.getNumber();
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The account number is " + util.maskCardOrAccountNumber(accountNumber));}

										cardList = account.getCard();

//										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Card List received are " + cardList);}
										if(!util.isNullOrEmpty(cardList)){
											customerEntityAccountCardDtlsMap = new HashMap<>();
											customerEntityAccountCardDtlsList = new ArrayList<>();
											ccList = new ArrayList<>();
											for(int k = 0; k < cardList.size(); k++){
												
												
												
												customerEntityAccountCardDtl = new CustomerEntityAccountCardDtl();
												cardAcctDtl = new CardAcctDtl();

												card = cardList.get(k);
//												if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Card Objec is " + card);}

												/**
												 * Fixes done for handling card for ccpayment on 18-06-2014
												 */
												if(!util.isNullOrEmpty(card)){
													
													cardNo = card.getNumber();
													if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Card Number is  " + util.maskCardOrAccountNumber(card.getNumber()));}
													ccList.add(cardNo);
												}
												
												customerEntityAccountCardDtl.setCardNumber(cardNo);
												cardAcctDtl.setCardNumber(cardNo);
												creditCardNumberList.add(cardNo);
												prepaidCardNumberList.add(cardNo);

												if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting the CardData object into Bean" + card.getCardData());}
												cardData = card.getCardData();
												if(!util.isNullOrEmpty(cardData)){
													customerEntityAccountCardDtl.setCardProduct(cardData.getProduct());
													customerEntityAccountCardDtl.setEmbossingName(cardData.getEmbossingName());
													customerEntityAccountCardDtl.setCardExpDate(cardData.getExpDate()+ Constants.EMPTY);
													customerEntityAccountCardDtl.setCardGeneralStatus(cardData.getStGeneral());
													customerEntityAccountCardDtl.setPrimaryCardFlag(cardData.getPrimaryCard());
													customerEntityAccountCardDtl.setCardActivationStatus(cardData.getCardActivationStatus());



													/**
													 * For Card Acct Object handling
													 */
													cardAcctDtl.setCardAccountNumber(accountNumber);
													cardAcctDtl.setCardStatus(cardData.getStGeneral());

													//As per the confirmation Faisal and business APIN status and Block codes should refer the card status
													cardAcctDtl.setApinStatus(cardData.getStGeneral());
													cardAcctDtl.setBlockCode(cardData.getStGeneral());
													cardAcctDtl.setCardActivationStatus(cardData.getStGeneral());
													if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Card's ( "+util.maskCardOrAccountNumber(cardNo)+" )Account number is " + util.maskCardOrAccountNumber(accountNumber));}
													if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "APIN status for the the card number "+util.maskCardOrAccountNumber(cardNo) +" is " + cardData.getStGeneral());}
													if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Card status for the the card number "+util.maskCardOrAccountNumber(cardNo)+" is " + cardData.getStGeneral());}
													if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Block Code status for the the card number "+util.maskCardOrAccountNumber(cardNo)+" is " + cardData.getStGeneral());}

													if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting Default Credit card type "+creditCardType);}
													cardAcctDtl.setCardType(creditCardType);
													
													String[] ccBINArray = (String[]) callInfo.getField(Field.CCBINNUMBERS);
													for(int cc_Count=0; cc_Count<ccBINArray.length; cc_Count++){
														if(ccBINArray[cc_Count].equalsIgnoreCase(cardNo.substring(Constants.GL_ZERO, ccBINArray[cc_Count].length()))){
															if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting the card type as CREDIT/CR");}
															cardAcctDtl.setCardType(creditCardType);
														}
													}
													String[] prepaidBINArray = (String[]) callInfo.getField(Field.PREPAIDBINNUMBERS);
													for(int pc_Count=0; pc_Count<prepaidBINArray.length; pc_Count++){
														if(prepaidBINArray[pc_Count].equalsIgnoreCase(cardNo.substring(Constants.GL_ZERO, prepaidBINArray[pc_Count].length()))){
															if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting the card type as PREPAID/PP");}
															cardAcctDtl.setCardType(prepaidCardType);
														}
													}
													
													cardAcctDtl.setBrand(cardData.getProduct());
													cardAcctDtl.setCardProduct(cardData.getProduct());
													cardAcctDtl.setExpDate(cardData.getExpDate()+Constants.EMPTY_STRING);

													cardAcctDtl.setPrimaryCardFlag(cardData.getPrimaryCard());
													if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Primary card flag "+cardData.getPrimaryCard());}


													if(util.isCodePresentInTheConfigurationList(cardAcctDtl.getCardStatus(), cardActiveStatusCode)){
														if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "*****The Card number ending with "+util.getSubstring(cardAcctDtl.getCardNumber(), Constants.GL_FOUR)+" Is Active / CUR");}
														cardAcctDtlList.add(cardAcctDtl);
														cardDetailMap.put(cardNo, cardAcctDtl);
													}else{

														if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "*****The Card number ending with "+util.getSubstring(cardAcctDtl.getCardNumber(), Constants.GL_FOUR)+" Is InActive / Closed / Not Available");}
														filteredCardAcctDtlList.add(cardAcctDtl);
													}


													if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total cardDetailMap Object is "+cardDetailMap.size());}
													if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Filtered CC Card Object is "+filteredCardAcctDtlList.size());}

												}

												personEntity = card.getPerson();
												if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Personal Entity object is "+personEntity);}
												if(!util.isNullOrEmpty(personEntityCard)){
													customerEntityAccountCardDtl.setCardHldrTitle(personEntityCard.getTitle());
													customerEntityAccountCardDtl.setCardHldrCustID(personEntityCard.getCustId());
													customerEntityAccountCardDtl.setCardHldrFirstName(personEntityCard.getFirstName());
													customerEntityAccountCardDtl.setCardHldrLangCode(personEntityCard.getLanguageCode());
													customerEntityAccountCardDtl.setCardHldrLastName(personEntityCard.getLastName());
													customerEntityAccountCardDtl.setCardHldrMiddleName(personEntityCard.getMiddleName());
													customerEntityAccountCardDtl.setCardSSN(personEntityCard.getSSN());
													customerEntityAccountCardDtl.setCardHldrVIPFlag(personEntityCard.getVIP());;
												}

												authorization = card.getAuthorization();
												if(!util.isNullOrEmpty(authorization)){
													customerEntityAccountCardDtl.setAuthCashLimit(authorization.getAuthCashLimit()+ Constants.EMPTY);
													customerEntityAccountCardDtl.setAuthLimit(authorization.getAuthLimit()+ Constants.EMPTY);
													customerEntityAccountCardDtl.setAuthTmpBonus(authorization.getAuthTmpBonus()+ Constants.EMPTY);
													customerEntityAccountCardDtl.setAuthTmpBonusDate(authorization.getAuthTmpBonusDate()+ Constants.EMPTY);
												}

												online = card.getOnline();
												if(!util.isNullOrEmpty(online)){
													customerEntityAccountCardDtl.setOTB(online.getOTB() + Constants.EMPTY);
													customerEntityAccountCardDtl.setLastAuthorisationDate(online.getLastAuthDate() + Constants.EMPTY);
													customerEntityAccountCardDtl.setOutstandingAmount(online.getOutstandingAmount() + Constants.EMPTY);
												}

//												if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Customer Entity Account card object" + customerEntityAccountCardDtl);}
												customerEntityAccountCardDtlsList.add(customerEntityAccountCardDtl);
												if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total number of Entity Account card Details list added is " + customerEntityAccountCardDtlsList.size());}
											}

											customerEntityAccountCardDtlsMap.put(accountNumber, customerEntityAccountCardDtlsList);
										}
										customerEntityAccountDtl.setCustomerEntityAccountCardDtlsMap(customerEntityAccountCardDtlsMap);
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Customer Cards details map count is " + customerEntityAccountCardDtlsMap.size());}

										arrayListCustomerEntityAcctDtls.add(customerEntityAccountDtl);
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Size of the customerEntityAccountDtl list is " + arrayListCustomerEntityAcctDtls.size());}
									
									
									accountAndCreditCardMap.put(account.getNumber(), ccList);
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "########### Setting the Account and Credit Card mpping hash map ###########" + accountAndCreditCardMap);}
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "########### Setting the Account and Credit Card mpping hash map Size is ###########" + accountAndCreditCardMap.size());}
									
									}

									customerEntityAccountDtlMap.put(customerNumber,arrayListCustomerEntityAcctDtls);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting the Customer Entity Details Map Object with the list of details of count " + customerEntityAccountDtlMap.size());}
								}
							}

							customerEntityDtl.setCustomerEntityAccountMap(customerEntityAccountDtlMap);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setted the customer entity account details map into customer entity detail objects" + arrayListCustomerEntityAcctDtls.size());}
						}

//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Customer Entity Detail Object is " + customerEntityDtl);}
						beanResponse.setCustomerEntityDtl(customerEntityDtl);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Total cardInfoType object list is " + cardAcctDtlList.size());}
						beanResponse.setCardAcctDtlList(cardAcctDtlList);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting the Card Details map" + cardAcctDtlList.size());}
						beanResponse.setCardDetailMap(cardDetailMap);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting the Filtered Card Details List, the total number of filtered card is " + filteredCardAcctDtlList.size());}
						beanResponse.setFilteredCardAcctDtlList(filteredCardAcctDtlList);


						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "#######Total Credit Card Manipulated are#######" + creditCardNumberList.size());}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "#######Total Prepaid Card Manipulated are#######" + prepaidCardNumberList.size());}
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "####### Credit Card Objects are #######" + creditCardNumberList);}

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "####### Setted the CC Acct Map object in the field #######" );}
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "####### Setted the CC Acct Map object in the field #######" + accountAndCreditCardMap);}
						callInfo.setField(Field.CCACCTMAP, accountAndCreditCardMap);


					}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Processing the card account list response type");}
					custAcctListResType = response.getCustAcctListRes();
					if(!util.isNullOrEmpty(custAcctListResType)){

						cardInfoTypeList = custAcctListResType.getCardAcctDtl();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Retrieved CardInfoTypeList size is "+cardInfoTypeList.size());}



						cardAcctDtlList = beanResponse.getCardAcctDtlList();
						cardDetailMap = beanResponse.getCardDetailMap();
						filteredCardAcctDtlList = beanResponse.getFilteredCardAcctDtlList();

						for(int count=0;count< cardInfoTypeList.size();count++){
							cardInfoType = cardInfoTypeList.get(count);
							cardAcctDtl = new CardAcctDtl();


							if(!util.isNullOrEmpty(cardInfoType) && creditCardNumberList!= null && 
									cardInfoType.getCardEmbossNum() != null &&!creditCardNumberList.contains(cardInfoType.getCardEmbossNum())
									&& !util.isCodePresentInTheConfigurationList(cardInfoType.getCardType(), creditCardTypeList)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Retrieved CardInfoType object from  CardInfoTypeList for the Debit card number ending with" + util.getSubstring(cardInfoType.getCardEmbossNum(), Constants.GL_FOUR));}

								cardAcctDtl.setCardNumber(cardInfoType.getCardEmbossNum());
								cardAcctDtl.setCardStatus(cardInfoType.getCardStatus());

								//As per the confirmation Faisal and business APIN status and Block codes should refer the card status
								cardAcctDtl.setApinStatus(cardInfoType.getCardStatus());
								cardAcctDtl.setBlockCode(cardInfoType.getCardStatus());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "APIN status for the the card number "+util.maskCardOrAccountNumber(cardInfoType.getCardEmbossNum()) +" is " + cardInfoType.getCardStatus());}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Card status for the the card number "+util.maskCardOrAccountNumber(cardInfoType.getCardEmbossNum())+" is " + cardInfoType.getCardStatus());}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Block Code status for the the card number "+util.maskCardOrAccountNumber(cardInfoType.getCardEmbossNum())+" is " + cardInfoType.getCardStatus());}

								cardAcctDtl.setCardType(cardInfoType.getCardType());
								cardAcctDtl.setBrand(cardInfoType.getBrand());
								cardAcctDtl.setCardProduct(cardInfoType.getProduct());
								cardAcctDtl.setExpDate(cardInfoType.getExpDate()+Constants.EMPTY_STRING);
								cardAcctDtl.setIssueDate(cardInfoType.getIssueDate()+ Constants.EMPTY_STRING);
//								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Card issue date is "+cardInfoType.getIssueDate());}

								cardAcctDtl.setPrimaryCardFlag(Constants.EMPTY_STRING + cardInfoType.isPrimaryCardFlag());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Primary card flag "+cardInfoType.isPrimaryCardFlag());}


								/**
								 * Condition handling for Card status check
								 */


								if(util.isCodePresentInTheConfigurationList(cardAcctDtl.getCardStatus(), cardActiveStatusCode)
										|| util.isCodePresentInTheConfigurationList(cardAcctDtl.getCardStatus(), cardInActiveStatusCode)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "*****The Card number ending with "+util.getSubstring(cardInfoType.getCardEmbossNum(), Constants.GL_FOUR)+" Is Active / CUR / In Active");}
									cardAcctDtlList.add(cardAcctDtl);
									cardDetailMap.put(cardInfoType.getCardEmbossNum(), cardAcctDtl);
								}else{

									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "*****The Card number ending with "+util.getSubstring(cardInfoType.getCardEmbossNum(), Constants.GL_FOUR)+" Is InActive / Closed / Not Available");}
									filteredCardAcctDtlList.add(cardAcctDtl);
								}
							}
						}

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Total cardInfoType object list is " + cardAcctDtlList.size());}
						beanResponse.setCardAcctDtlList(cardAcctDtlList);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting the Card Details map" + cardDetailMap.size());}
						beanResponse.setCardDetailMap(cardDetailMap);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting the Filtered Card Details List, the total number of filtered card is " + filteredCardAcctDtlList.size());}
						beanResponse.setFilteredCardAcctDtlList(filteredCardAcctDtlList);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Going to process the Account list response type");}

						acctIdInfoTypeList = custAcctListResType.getAcctInfo();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Retrieved AcctIdInfoTypeList size is "+acctIdInfoTypeList.size());}


						HashMap<String, String> accountCurrMap = new HashMap<String, String>();

						ArrayList<AcctInfo> acctInfoList = new ArrayList<AcctInfo>();
						ArrayList<AcctInfo> filteredAcctInfoList = new ArrayList<AcctInfo>();
						HashMap<String,AcctInfo> accountDetailMap = new HashMap<String, AcctInfo>();

						for(int count=0;count< acctIdInfoTypeList.size();count++){
							AcctInfo acctInfo = new AcctInfo();
							acctIdInfoType = acctIdInfoTypeList.get(count);

							if(!util.isNullOrEmpty(acctIdInfoType)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Retrieved acctIdInfoType object from  acctIdInfoTypeList for the Account number ending with" + util.getSubstring(acctIdInfoType.getAcctId(), Constants.GL_FOUR));}
								acctInfo.setAcctCurr(acctIdInfoType.getAcctCcy());

								acctInfo.setAcctID(acctIdInfoType.getAcctId());
								acctInfo.setAcctIDType(acctIdInfoType.getAcctIdType());

								//TODO WSDL has this method but ESB does'nt define this method in their code
								//XMLGregorianCalendar acctStartDate = acctIdInfoType.getAcctStrtDate();
								//String strAcctStartDate = util.isNullOrEmpty(acctStartDate)?null:util.convertXMLCalendarToString(acctStartDate, Constants.DATEFORMAT_yyyy_MM_ddHH_mm_ss_SSS);
								//acctInfo.setAcctStartDate(strAcctStartDate);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Account issue Date "+acctIdInfoType.getAcctStrtDate());}
								acctInfo.setAcctStartDate(acctIdInfoType.getAcctStrtDate() + Constants.EMPTY_STRING);

								XMLGregorianCalendar acctEndDate = acctIdInfoType.getAcctEndDate();
								String stracctendDate = util.isNullOrEmpty(acctEndDate)?null:util.convertXMLCalendarToString(acctEndDate, Constants.DATEFORMAT_yyyy_MM_ddHH_mm_ss_SSS);
								acctInfo.setAcctEndDate(stracctendDate);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Account end date "+acctIdInfoType.getAcctEndDate());}
								acctInfo.setAcctStatus(acctIdInfoType.getAcctStatus());
								acctInfo.setAcctType(acctIdInfoType.getAcctType());

								acctInfo.setBranchCode(acctIdInfoType.getBranchCode());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Account's Branch code is "+acctIdInfoType.getBranchCode());}

								acctInfo.setCategory(acctIdInfoType.getCategory()+"");
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Account category type is "+acctIdInfoType.getCategory());}
							}

							/**
							 * Condition handling for Account status check
							 */
							String acctActiveStatusCode = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_ACCOUNT_ACTIVE_STATUS);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Configured Account Active Status in the UI is "+ acctActiveStatusCode);}

							if(util.isCodePresentInTheConfigurationList(acctInfo.getAcctStatus(), acctActiveStatusCode)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "*****The Account number ending with "+util.getSubstring(acctInfo.getAcctID(), Constants.GL_FOUR)+" Is Active / CUR");}
								acctInfoList.add(acctInfo);
								accountDetailMap.put(acctIdInfoType.getAcctId(), acctInfo);
								accountCurrMap.put(acctIdInfoType.getAcctId(), acctIdInfoType.getAcctCcy());
							}else{

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "*****The Account number ending with "+util.getSubstring(acctInfo.getAcctID(), Constants.GL_FOUR)+"Is InActive / Closed / Not Available");}
								filteredAcctInfoList.add(acctInfo);
							}
						}

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Total AccountInfoList object list is "+acctInfoList.size());}
						beanResponse.setAcctInfoList(acctInfoList);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting up the account detail map");}
						beanResponse.setAccountDetailMap(accountDetailMap);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting the Filtered Account Details List, the total number of filtered card is " + filteredAcctInfoList.size());}
						beanResponse.setFilteredAcctInfoList(filteredAcctInfoList);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting the Account  Currency MAP Details into the callinfo field " + accountCurrMap);}
						callInfo.setField(Field.ACCTCURRMAP, accountCurrMap);


						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Going to Process the Product type segegation of Account and Cards");}


						//For Credit card we need to manipulate or use the cardDetailsMap to get the brand based account selection list
						ArrayList<String> creditCardList = new ArrayList<String>();
						ArrayList<String> prepaidCardList = new ArrayList<String>();
						ArrayList<String> debitCardList = new ArrayList<String>();
						ArrayList<String> savingsAcctList = new ArrayList<String>();
						ArrayList<String> currentAcctList = new ArrayList<String>();
						ArrayList<String> fdAcctList = new ArrayList<String>();
						ArrayList<String> loanAcctList = new ArrayList<String>();
						ArrayList<String> crVISACardList = new ArrayList<String>();
						ArrayList<String> crMasterCardList = new ArrayList<String>();
						ArrayList<String> crAmExCardList = new ArrayList<String>();
						ArrayList<String> ppVISACardList = new ArrayList<String>();
						ArrayList<String> ppMasterCardList = new ArrayList<String>();
						ArrayList<String> ppAmExCardList = new ArrayList<String>();
						ArrayList<String> drVISACardList = new ArrayList<String>();
						ArrayList<String> drMasterCardList = new ArrayList<String>();
						ArrayList<String> drAmExCardList = new ArrayList<String>();


						ArrayList<String>drVISACardListInactive = new ArrayList<String>();
						ArrayList<String>drMasterCardListInactive = new ArrayList<String>();
						ArrayList<String>drAmexCardListInactive = new ArrayList<String>();


						/**
						 * For Islamic Meethac flow
						 */
						boolean isIslamicProdAvail = false;

						ArrayList<String> islamicSavingsAcctList = new ArrayList<String>();
						ArrayList<String> islamicCurrentAcctList = new ArrayList<String>();
						ArrayList<String> islamicFDAcctList = new ArrayList<String>();
						ArrayList<String> islamicLoanAcctList = new ArrayList<String>();
						ArrayList<String> islamicVISACreditCardList = new ArrayList<String>();
						ArrayList<String> islamicMasterCreditCardList = new ArrayList<String>();
						ArrayList<String> islamicAmExCreditCardList = new ArrayList<String>();
						ArrayList<String> islamicVISAPrepaidCardList = new ArrayList<String>();
						ArrayList<String> islamicMasterPrepaidCardList = new ArrayList<String>();
						ArrayList<String> islamicAmExPrepaidCardList = new ArrayList<String>();

						/**
						 * following changes are done for islamic / retail debit card handling
						 */
						ArrayList<String> islamicAmExDebitCardList = new ArrayList<String>();
						ArrayList<String> islamicVISADebitCardList = new ArrayList<String>();
						ArrayList<String> islamicMasterDebitCardList = new ArrayList<String>();
						ArrayList<String>drIslamicVISACardListInactive = new ArrayList<String>();
						ArrayList<String>drIslamicMasterCardListInactive = new ArrayList<String>();
						ArrayList<String>drIslamicAmexCardListInactive = new ArrayList<String>();
						//END


						boolean isRetailProdAvail = false;

						ArrayList<String> retailSavingsAcctList = new ArrayList<String>();
						ArrayList<String> retailCurrentAcctList = new ArrayList<String>();
						ArrayList<String> retailFDAcctList = new ArrayList<String>();
						ArrayList<String> retailLoanAcctList = new ArrayList<String>();
						ArrayList<String> retailVISACreditCardList = new ArrayList<String>();
						ArrayList<String> retailMasterCreditCardList = new ArrayList<String>();
						ArrayList<String> retailAmExCreditCardList = new ArrayList<String>();
						ArrayList<String> retailVISAPrepaidCardList = new ArrayList<String>();
						ArrayList<String> retailMasterPrepaidCardList = new ArrayList<String>();
						ArrayList<String> retailAmExPrepaidCardList = new ArrayList<String>();

						/**
						 * following changes are done for islamic / retail debit card handling
						 */
						ArrayList<String> retailAmExDebitCardList = new ArrayList<String>();
						ArrayList<String> retailVISADebitCardList = new ArrayList<String>();
						ArrayList<String> retailMasterDebitCardList = new ArrayList<String>();
						ArrayList<String>drRetailVISACardListInactive = new ArrayList<String>();
						ArrayList<String>drRetailMasterCardListInactive = new ArrayList<String>();
						ArrayList<String>drRetailAmexCardListInactive = new ArrayList<String>();
						//END

						String customerSegment = util.isNullOrEmpty(callInfo.getField(Field.CUST_SEGMENT_TYPE)) ? Constants.CUST_SEGMENT_RETAIL : (String)callInfo.getField(Field.CUST_SEGMENT_TYPE);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Configured Customer segment type is "+ customerSegment);}

						String cardVisaBrandTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_CARDBRANDTYPE_VISA +Constants.UNDERSCORE+customerSegment);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Configured credit card VISA brand type is "+ cardVisaBrandTypeList);}

						String cardMasterBrandTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_CARDBRANDTYPE_MASTER +Constants.UNDERSCORE+customerSegment);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Configured credit card Master brand type is "+ cardMasterBrandTypeList);}

						String cardAmexBrandTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_CARDBRANDTYPE_AMEX +Constants.UNDERSCORE+customerSegment);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Configured credit card Amex brand type is "+ cardAmexBrandTypeList);}

						String debitCardVisaCodeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_BIN_FOR_VISA_DEBITCARD +Constants.UNDERSCORE+customerSegment);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Configured Debit card Visa BIN type is "+ debitCardVisaCodeList);}

						String debitCardMasterCodeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_BIN_FOR_MASTER_DEBITCARD +Constants.UNDERSCORE+customerSegment);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Configured Debit card Master BIN type is "+ debitCardMasterCodeList);}

						String debitCardAmexCodeeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_BIN_FOR_AMEX_DEBITCARD +Constants.UNDERSCORE+customerSegment);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Configured Debit card Amex BIN type is "+ debitCardAmexCodeeList);}

						String islamicCRVisaCardProdCodeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_ISLAMIC_VISA_CARD_PRODUCT_TYPE);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Configured Islamic CR Visa product code is "+ islamicCRVisaCardProdCodeList);}

						String islamicCRMasterCardProdCodeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_ISLAMIC_MASTER_CARD_PRODUCT_TYPE);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Configured Islamic CR Master product code is "+ islamicCRMasterCardProdCodeList);}

						String islamicCRAmexCardProdCodeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_ISLAMIC_AMEX_CARD_PRODUCT_TYPE);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Configured Islamic CR Amex product code is "+ islamicCRAmexCardProdCodeList);}



						/**
						 * Following are the changes for Islamic and Retail Debit card handling on 14-Jun-2014
						 */
						String islamicDebitCardVisaBinList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_BIN_FOR_ISLAMIC_VISA_DEBITCARD);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Configured Islamic Debit card Visa BIN type is "+ islamicDebitCardVisaBinList);}

						String islamicDebitCardMasterBinList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_BIN_FOR_ISLAMIC_MASTER_DEBITCARD);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Configured Islamic Debit card Master BIN type is "+ islamicDebitCardMasterBinList);}

						String islamicDebitCardAmexBinList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_BIN_FOR_ISLAMIC_AMEX_DEBITCARD);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Configured Islamic Debit card Amex BIN type is "+ islamicDebitCardAmexBinList);}
						//END


						Iterator iter = null;
						iter = cardDetailMap.keySet().iterator();
						String key = Constants.EMPTY_STRING;
						CardAcctDtl cardvalue = null;
						String cli = util.isNullOrEmpty(callInfo.getField(Field.CIN))? Constants.EMPTY_STRING : (String)callInfo.getField(Field.CIN);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The user entered CLI / Debit card number is "+ util.maskCardOrAccountNumber(cli));}

						if(!Constants.NA.equalsIgnoreCase(debitCardMasterCodeList) || !Constants.NA.equalsIgnoreCase(creditCardTypeList) || !Constants.NA.equalsIgnoreCase(prepaidCardTypeList))
						{
							while(iter.hasNext()) {
								key = (String)iter.next();
								cardvalue = cardDetailMap.get(key);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Card type is "+ cardvalue.getCardType());}

								creditCardTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_CARDTYPE_CREDITCARD +Constants.UNDERSCORE+customerSegment);
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Configured credit card type list is "+ creditCardTypeList);}
								
								prepaidCardTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_CARDTYPE_PREPAIDCARD +Constants.UNDERSCORE+customerSegment);
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Configured prepaid card type list is "+ prepaidCardTypeList);}

								String debitCardTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_CARDTYPE_DEBITCARD +Constants.UNDERSCORE+customerSegment);
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Configured debit card type list is "+ debitCardTypeList);}



								if(util.isCodePresentInTheConfigurationList(cardvalue.getCardType(), creditCardTypeList)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Credit Card number ending with "+ util.getSubstring(key.toString(), Constants.GL_FOUR) + "in Credit card acctlist");}
									creditCardList.add(key);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "This Credit Card list would be a combination of all VISA / MASTER / AMEX card types");}

									if(util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), cardVisaBrandTypeList)
											|| util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRVisaCardProdCodeList)){
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Credit card into VISA Brand list");}
										crVISACardList.add(key);


										/**
										 * Following condition handled for Islamic to retail flow handling (cross flow transformation)
										 */

										if(!util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRVisaCardProdCodeList) &&
												!util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRMasterCardProdCodeList)
												&& !util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRAmexCardProdCodeList)){
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Credit card into Retail Visa Brand list");}
											retailVISACreditCardList.add(key);
											isRetailProdAvail = true;
										}
										//END

									}
									else if(util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), cardMasterBrandTypeList)
											|| util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRMasterCardProdCodeList)){
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Credit card into Master Brand list");}
										crMasterCardList.add(key);


										/**
										 * Following condition handled for Islamic to retail flow handling (cross flow transformation)
										 */
										if(!util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRVisaCardProdCodeList) &&
												!util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRMasterCardProdCodeList)
												&& !util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRAmexCardProdCodeList)){
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Credit card into Retail Master Brand list");}
											retailMasterCreditCardList.add(key);
											isRetailProdAvail = true;
										}
										//END


									}else if(util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), cardAmexBrandTypeList)
											|| util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRAmexCardProdCodeList)){
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Credit card into AmEx Brand list");}
										crAmExCardList.add(key);

										/**
										 * Following condition handled for Islamic to retail flow handling (cross flow transformation)
										 */
										if(!util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRVisaCardProdCodeList) &&
												!util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRMasterCardProdCodeList)
												&& !util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRAmexCardProdCodeList)){
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Credit card into Retail Amex Brand list");}
											retailAmExCreditCardList.add(key);
											isRetailProdAvail = true;
										}
										//END

									}


									//Following are added for Islamic - Meethac credit card flows
									if(util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRVisaCardProdCodeList)){
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Credit card into Islamic Visa Brand list");}
										islamicVISACreditCardList.add(key);
										isIslamicProdAvail = true;
									}

									else if(util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRMasterCardProdCodeList)){
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Credit card into Islamic Master Brand list");}
										islamicMasterCreditCardList.add(key);
										isIslamicProdAvail = true;
									}

									else if(util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRAmexCardProdCodeList)){
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Credit card into Islamic Amex Brand list");}
										islamicAmExCreditCardList.add(key);
										isIslamicProdAvail = true;
									}
									//END - for islamic meethac cards

								}else if(util.isCodePresentInTheConfigurationList(cardvalue.getCardType(), prepaidCardTypeList)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Prepaid Card number ending with "+ util.getSubstring(key.toString(), Constants.GL_FOUR) + "in Prepaid card acctlist");}
									prepaidCardList.add(key);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "This Prepaid Card list would be a combination of all VISA / MASTER / AMEX card types");}

									if(util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), cardVisaBrandTypeList)
											|| util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRVisaCardProdCodeList)){
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Prepaid card into VISA Brand list");}
										ppVISACardList.add(key);


										/**
										 * Following condition handled for Islamic to retail flow handling (cross flow transformation)
										 */

										if(!util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRVisaCardProdCodeList) &&
												!util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRMasterCardProdCodeList)
												&& !util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRAmexCardProdCodeList)){
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Prepaid card into Retail Visa Brand list");}
											retailVISAPrepaidCardList.add(key);
											isRetailProdAvail = true;
										}
										//END

									}
									else if(util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), cardMasterBrandTypeList)
											|| util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRMasterCardProdCodeList)){
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Prepaid card into Master Brand list");}
										ppMasterCardList.add(key);


										/**
										 * Following condition handled for Islamic to retail flow handling (cross flow transformation)
										 */
										if(!util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRVisaCardProdCodeList) &&
												!util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRMasterCardProdCodeList)
												&& !util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRAmexCardProdCodeList)){
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Prepaid card into Retail Master Brand list");}
											retailMasterPrepaidCardList.add(key);
											isRetailProdAvail = true;
										}
										//END


									}else if(util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), cardAmexBrandTypeList)
											|| util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRAmexCardProdCodeList)){
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Prepaid card into AmEx Brand list");}
										ppAmExCardList.add(key);

										/**
										 * Following condition handled for Islamic to retail flow handling (cross flow transformation)
										 */
										if(!util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRVisaCardProdCodeList) &&
												!util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRMasterCardProdCodeList)
												&& !util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRAmexCardProdCodeList)){
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Prepaid card into Retail Amex Brand list");}
											retailAmExPrepaidCardList.add(key);
											isRetailProdAvail = true;
										}
										//END

									}


									//Following are added for Islamic - Meethac credit card flows
									if(util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRVisaCardProdCodeList)){
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Prepaid card into Islamic Visa Brand list");}
										islamicVISAPrepaidCardList.add(key);
										isIslamicProdAvail = true;
									}

									else if(util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRMasterCardProdCodeList)){
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Prepaid card into Islamic Master Brand list");}
										islamicMasterPrepaidCardList.add(key);
										isIslamicProdAvail = true;
									}

									else if(util.isCodePresentInTheConfigurationList(cardvalue.getBrand(), islamicCRAmexCardProdCodeList)){
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Prepaid card into Islamic Amex Brand list");}
										islamicAmExPrepaidCardList.add(key);
										isIslamicProdAvail = true;
									}
									//END - for islamic meethac cards

								}else if(util.isCodePresentInTheConfigurationList(cardvalue.getCardType(), debitCardTypeList)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Debit Card number ending with "+ util.getSubstring(key.toString(), Constants.GL_FOUR) + "in Debit card acctlist");}
									debitCardList.add(key);

									/**
									 * Modified by Vinoth on 07-Apr-2014 for Debit card bin identification
									 */

									/**
									 * The condition hanlding for active CIN should be filtered from the debit card activation list.
									 */

									if(util.isBinNoEligible(cardvalue.getCardNumber(), debitCardVisaCodeList)){

										if(util.isCodePresentInTheConfigurationList(cardvalue.getCardStatus(), cardInActiveStatusCode) && !cli.equalsIgnoreCase(cardvalue.getCardNumber())){
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Debit card into Inactive DR VISA BIN list");}
											drVISACardListInactive.add(key);
										}

										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Debit card into Active DR VISA BIN list");}
										drVISACardList.add(key);

										
										
										/**
										 * Following are the code to handle the bin numbers of retail / islamic
										 */
										
										if(!util.isBinNoEligible(cardvalue.getCardNumber(), islamicDebitCardVisaBinList) &&
												!util.isBinNoEligible(cardvalue.getCardNumber(), islamicDebitCardMasterBinList) &&
												!util.isBinNoEligible(cardvalue.getCardNumber(), islamicDebitCardAmexBinList)){
											
											retailVISADebitCardList.add(key);
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Debit card into common Retail DR VISA BIN list");}
										
											if(util.isCodePresentInTheConfigurationList(cardvalue.getCardStatus(), cardInActiveStatusCode) && !cli.equalsIgnoreCase(cardvalue.getCardNumber())){
												if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Retails Debit card into Inactive Retail DR VISA BIN list");}
												drRetailVISACardListInactive.add(key);
											}
											
											isRetailProdAvail = true;
										}
										
										
										if(util.isBinNoEligible(cardvalue.getCardNumber(), islamicDebitCardVisaBinList)){
											
											islamicVISADebitCardList.add(key);
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Debit card into common Islamic DR VISA BIN list");}
										
											if(util.isCodePresentInTheConfigurationList(cardvalue.getCardStatus(), cardInActiveStatusCode) && !cli.equalsIgnoreCase(cardvalue.getCardNumber())){
												if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Islamic Debit card into Inactive Islamic DR VISA BIN list");}
												drIslamicVISACardListInactive.add(key);
											}
											isIslamicProdAvail = true;
										}
										
										//END 
										

									}
									else if(util.isBinNoEligible(cardvalue.getCardNumber(), debitCardMasterCodeList)){

										if(util.isCodePresentInTheConfigurationList(cardvalue.getCardStatus(), cardInActiveStatusCode)&& !cli.equalsIgnoreCase(cardvalue.getCardNumber())){
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Debit card into Inactive DR Master BIN list");}
											drMasterCardListInactive.add(key);
										}

										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Debit card into Active DR Master BIN list");}
										drMasterCardList.add(key);
										
										
										
										/**
										 * Following are the code to handle the bin numbers of retail / islamic
										 */
										
										if(!util.isBinNoEligible(cardvalue.getCardNumber(), islamicDebitCardVisaBinList) &&
												!util.isBinNoEligible(cardvalue.getCardNumber(), islamicDebitCardMasterBinList) &&
												!util.isBinNoEligible(cardvalue.getCardNumber(), islamicDebitCardAmexBinList)){
											
											retailMasterDebitCardList.add(key);
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Debit card into common Retail DR Master BIN list");}
										
											if(util.isCodePresentInTheConfigurationList(cardvalue.getCardStatus(), cardInActiveStatusCode) && !cli.equalsIgnoreCase(cardvalue.getCardNumber())){
												if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Retails Debit card into Inactive Retail DR Master BIN list");}
												drRetailMasterCardListInactive.add(key);
											}
											isRetailProdAvail =true;
										}
										
										
										if(util.isBinNoEligible(cardvalue.getCardNumber(), islamicDebitCardVisaBinList)){
											
											islamicMasterDebitCardList.add(key);
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Debit card into common Islamic DR Master BIN list");}
										
											if(util.isCodePresentInTheConfigurationList(cardvalue.getCardStatus(), cardInActiveStatusCode) && !cli.equalsIgnoreCase(cardvalue.getCardNumber())){
												if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Islamic Debit card into Inactive Islamic DR Master BIN list");}
												drIslamicMasterCardListInactive.add(key);
											}
											isIslamicProdAvail = true;
										}
										
										//END 
										

									}else if(util.isBinNoEligible(cardvalue.getCardNumber(), debitCardAmexCodeeList)){

										if(util.isCodePresentInTheConfigurationList(cardvalue.getCardStatus(), cardInActiveStatusCode) && !cli.equalsIgnoreCase(cardvalue.getCardNumber())){
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Debit card into Inactive DR AmEx BIN list");}
											drAmexCardListInactive.add(key);
										}

										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Debit card into Active DR AmEx BIN list");}
										drAmExCardList.add(key);
										
										
										/**
										 * Following are the code to handle the bin numbers of retail / islamic
										 */
										
										if(!util.isBinNoEligible(cardvalue.getCardNumber(), islamicDebitCardVisaBinList) &&
												!util.isBinNoEligible(cardvalue.getCardNumber(), islamicDebitCardMasterBinList) &&
												!util.isBinNoEligible(cardvalue.getCardNumber(), islamicDebitCardAmexBinList)){
											
											retailAmExDebitCardList.add(key);
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Debit card into common Retail DR Amex BIN list");}
										
											if(util.isCodePresentInTheConfigurationList(cardvalue.getCardStatus(), cardInActiveStatusCode) && !cli.equalsIgnoreCase(cardvalue.getCardNumber())){
												if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Retails Debit card into Inactive Retail DR Amex BIN list");}
												drRetailAmexCardListInactive.add(key);
											}
											isRetailProdAvail = true;
										}
										
										
										if(util.isBinNoEligible(cardvalue.getCardNumber(), islamicDebitCardVisaBinList)){
											
											islamicAmExDebitCardList.add(key);
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Debit card into common Islamic DR Amex BIN list");}
										
											if(util.isCodePresentInTheConfigurationList(cardvalue.getCardStatus(), cardInActiveStatusCode) && !cli.equalsIgnoreCase(cardvalue.getCardNumber())){
												if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Islamic Debit card into Inactive Islamic DR Amex BIN list");}
												drIslamicAmexCardListInactive.add(key);
											}
											isIslamicProdAvail = true;
										}
										//END 
										
									}

								}

							}
						}


						String currentAccoutTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_ACCOUNTTYPE_CURRENT +Constants.UNDERSCORE+customerSegment);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Configured current account type list is "+ currentAccoutTypeList);}


						String savingAccountTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_ACCOUNTTYPE_SAVING +Constants.UNDERSCORE+customerSegment);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Configured savings account type list is "+ savingAccountTypeList);}


						String loanAccountTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_ACCOUNTTYPE_LOAN +Constants.UNDERSCORE+customerSegment);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Configured loan account type list is "+ loanAccountTypeList);}

						String depositAccountTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_ACCOUNTTYPE_DEPOSIT +Constants.UNDERSCORE+customerSegment);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Configured deposit account type list is "+ depositAccountTypeList);}


						/**
						 * Following for the handling of Isamic call flow
						 */
						String islamicAcctCategoryList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_ISLAMIC_ACCT_CATEGORY_TYPE);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Configured Isamic account category list is "+ islamicAcctCategoryList);}
						//END


						iter = accountDetailMap.keySet().iterator();
						AcctInfo acctValue = null;
						while(iter.hasNext()) {
							key = (String)iter.next();
							acctValue = accountDetailMap.get(key);

							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Account Type is "+ acctValue.getAcctType() );}

							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Account Category type  is "+ acctValue.getCategory() );}

							if(util.isCodePresentInTheConfigurationList(acctValue.getCategory(), depositAccountTypeList)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Deposit Account number ending with "+ util.getSubstring(key.toString(), Constants.GL_FOUR) + "in Depost acctlist");}
								fdAcctList.add(key);

								/**
								 * For Isamic flow
								 */

								if(util.isCodePresentInTheConfigurationList(acctValue.getCategory(), islamicAcctCategoryList)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Islamic Deposit Account number ending with "+ util.getSubstring(key.toString(), Constants.GL_FOUR) + "in Islamic Depost acctlist");}
									islamicFDAcctList.add(key);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Size of Islamic FD Acct List is "+ islamicFDAcctList.size());}
									isIslamicProdAvail = true;
								}
								//END
								/**
								 * Following are the handling done for Islamic to Retail (Cross flow transformation hanlding)
								 */
								else{
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Retail Deposit Account number ending with "+ util.getSubstring(key.toString(), Constants.GL_FOUR) + "in Retail Depost acctlist");}
									retailFDAcctList.add(key);
									isRetailProdAvail = true;
								}
								//END

							}else if(util.isCodePresentInTheConfigurationList(acctValue.getCategory(), currentAccoutTypeList)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the current account number ending with "+ util.getSubstring(key.toString(), Constants.GL_FOUR) + "in Current acctlist");}
								currentAcctList.add(key);


								/**
								 * For Isamic flow
								 */

								if(util.isCodePresentInTheConfigurationList(acctValue.getCategory(), islamicAcctCategoryList)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Islamic Current Account number ending with "+ util.getSubstring(key.toString(), Constants.GL_FOUR) + "in Islamic Current acctlist");}
									islamicCurrentAcctList.add(key);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Size of Islamic Current Acct List is "+ islamicCurrentAcctList.size());}
									isIslamicProdAvail = true;
								}
								//END
								/**
								 * Following are the handling done for Islamic to Retail (Cross flow transformation hanlding)
								 */
								else{
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Retail Current Account number ending with "+ util.getSubstring(key.toString(), Constants.GL_FOUR) + "in Retail current acctlist");}
									retailCurrentAcctList.add(key);
									isRetailProdAvail = true;
								}
								//END

							}else if(util.isCodePresentInTheConfigurationList(acctValue.getCategory(), loanAccountTypeList)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Loan account number ending with "+ util.getSubstring(key.toString(), Constants.GL_FOUR) + "in Loan acctlist");}
								loanAcctList.add(key);

								/**
								 * For Isamic flow
								 */

								if(util.isCodePresentInTheConfigurationList(acctValue.getCategory(), islamicAcctCategoryList)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Islamic Loan Account number ending with "+ util.getSubstring(key.toString(), Constants.GL_FOUR) + "in Islamic Loan acctlist");}
									islamicLoanAcctList.add(key);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Size of Islamic Loan Acct List is "+ islamicLoanAcctList.size());}
									isIslamicProdAvail = true;
								}
								//END
								/**
								 * Following are the handling done for Islamic to Retail (Cross flow transformation hanlding)
								 */
								else{
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Retail Loan Account number ending with "+ util.getSubstring(key.toString(), Constants.GL_FOUR) + "in Retail Loan acctlist");}
									retailLoanAcctList.add(key);
									isRetailProdAvail = true;
								}
								//END

							}else if(util.isCodePresentInTheConfigurationList(acctValue.getCategory(), savingAccountTypeList)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Saving account number ending with "+ util.getSubstring(key.toString(), Constants.GL_FOUR) + "in Savings acctlist");}
								savingsAcctList.add(key);


								/**
								 * For Isamic flow
								 */

								if(util.isCodePresentInTheConfigurationList(acctValue.getCategory(), islamicAcctCategoryList)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Islamic Savings Account number ending with "+ util.getSubstring(key.toString(), Constants.GL_FOUR) + "in Islamic Saving acctlist");}
									islamicSavingsAcctList.add(key);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Size of Islamic Savings Acct List is "+ islamicSavingsAcctList.size());}
									isIslamicProdAvail = true;
								}
								//END
								/**
								 * Following are the handling done for Islamic to Retail (Cross flow transformation handling)
								 */
								else{
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Retail Saving Account number ending with "+ util.getSubstring(key.toString(), Constants.GL_FOUR) + "in Retail Saving acctlist");}
									retailSavingsAcctList.add(key);
									isRetailProdAvail = true;
								}
								//END

							}

						}

						callInfo.setField(Field.CREDITCARDLIST, creditCardList);
						callInfo.setField(Field.PREPAIDCARDLIST, prepaidCardList);
						callInfo.setField(Field.DEBITCARDLIST, debitCardList);
						callInfo.setField(Field.SAVINGSACCTLIST, savingsAcctList);
						callInfo.setField(Field.LOANACCTLIST, loanAcctList);
						callInfo.setField(Field.CURRENTACCTLIST, currentAcctList);
						callInfo.setField(Field.FDACCTLIST, fdAcctList);
						callInfo.setField(Field.CRVISACARDLIST, crVISACardList);
						callInfo.setField(Field.CRMASTERCARDLIST, crMasterCardList);
						callInfo.setField(Field.CRAMEXCARDLIST, crAmExCardList);
						callInfo.setField(Field.PPVISACARDLIST, ppVISACardList);
						callInfo.setField(Field.PPMASTERCARDLIST, ppMasterCardList);
						callInfo.setField(Field.PPAMEXCARDLIST, ppAmExCardList);
						callInfo.setField(Field.DRVISACARDLIST, drVISACardList);
						callInfo.setField(Field.DRMASTERCARDLIST, drMasterCardList);
						callInfo.setField(Field.DRAMEXCARDLIST, drAmExCardList);

						callInfo.setField(Field.DRVISACARDLISTINACTIVE, drVISACardListInactive);
						callInfo.setField(Field.DRMASTERCARDLISTINACTIVE, drMasterCardListInactive);
						callInfo.setField(Field.DRAMEXCARDLISTINACTIVE, drAmexCardListInactive);

						callInfo.setField(Field.ISLAMICFDACCTLIST, islamicFDAcctList);
						callInfo.setField(Field.ISLAMICCURRENTACCTLIST, islamicCurrentAcctList);
						callInfo.setField(Field.ISLAMICLOANACCTLIST, islamicLoanAcctList);
						callInfo.setField(Field.ISLAMICSAVINGSACCTLIST, islamicSavingsAcctList);
						callInfo.setField(Field.ISLAMICVISACREDITCARDLIST, islamicVISACreditCardList);
						callInfo.setField(Field.ISLAMICMASTERCREDITCARDLIST, islamicMasterCreditCardList);
						callInfo.setField(Field.ISLAMICAMEXCREDITCARDLIST, islamicAmExCreditCardList);
						callInfo.setField(Field.ISLAMICVISAPREPAIDCARDLIST, islamicVISAPrepaidCardList);
						callInfo.setField(Field.ISLAMICMASTERPREPAIDCARDLIST, islamicMasterPrepaidCardList);
						callInfo.setField(Field.ISLAMICAMEXPREPAIDCARDLIST, islamicAmExPrepaidCardList);
						
						callInfo.setField(Field.ISLAMICDRVISACARDLIST, islamicVISADebitCardList);
						callInfo.setField(Field.ISLAMICDRMASTERCARDLIST, islamicMasterDebitCardList);
						callInfo.setField(Field.ISLAMICDRAMEXCARDLIST, islamicAmExDebitCardList);
						callInfo.setField(Field.DRISLAMICVISACARDLISTINACTIVE, drIslamicVISACardListInactive);
						callInfo.setField(Field.DRISLAMICMASTERCARDLISTINACTIVE, drIslamicMasterCardListInactive);
						callInfo.setField(Field.DRISLAMICAMEXCARDLISTINACTIVE, drIslamicAmexCardListInactive);


						callInfo.setField(Field.RETAILFDACCTLIST, retailFDAcctList);
						callInfo.setField(Field.RETAILCURRENTACCTLIST, retailCurrentAcctList);
						callInfo.setField(Field.RETAILLOANACCTLIST, retailLoanAcctList);
						callInfo.setField(Field.RETAILSAVINGSACCTLIST, retailSavingsAcctList);
						callInfo.setField(Field.RETAILVISACREDITCARDLIST, retailVISACreditCardList);
						callInfo.setField(Field.RETAILMASTERCREDITCARDLIST, retailMasterCreditCardList);
						callInfo.setField(Field.RETAILAMEXCREDITCARDLIST, retailAmExCreditCardList);
						callInfo.setField(Field.RETAILVISAPREPAIDCARDLIST, retailVISAPrepaidCardList);
						callInfo.setField(Field.RETAILMASTERPREPAIDCARDLIST, retailMasterPrepaidCardList);
						callInfo.setField(Field.RETAILAMEXPREPAIDCARDLIST, retailAmExPrepaidCardList);

						
						callInfo.setField(Field.RETAILDRVISACARDLIST, retailVISADebitCardList);
						callInfo.setField(Field.RETAILDRMASTERCARDLIST, retailMasterDebitCardList);
						callInfo.setField(Field.RETAILDRAMEXCARDLIST, retailAmExDebitCardList);
						callInfo.setField(Field.DRRETAILVISACARDLISTINACTIVE, drRetailVISACardListInactive);
						callInfo.setField(Field.DRRETAILMASTERCARDLISTINACTIVE, drRetailMasterCardListInactive);
						callInfo.setField(Field.DRRETAILAMEXCARDLISTINACTIVE, drRetailAmexCardListInactive);

						ArrayList<String> islamicDebitCardList = new ArrayList<>();
						
						islamicDebitCardList.addAll(islamicVISADebitCardList);
						islamicDebitCardList.addAll(islamicMasterDebitCardList);
						islamicDebitCardList.addAll(islamicAmExDebitCardList);
						callInfo.setField(Field.ISLAMICDEBITCARDLIST, islamicDebitCardList);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac / Islamic Debit card number is " + islamicDebitCardList.size());}

						ArrayList<String> retailDebitCardList = new ArrayList<>();
						retailDebitCardList.addAll(retailVISADebitCardList);
						retailDebitCardList.addAll(retailMasterDebitCardList);
						retailDebitCardList.addAll(retailAmExDebitCardList);
						callInfo.setField(Field.RETAILDEBITCARDLIST, retailDebitCardList);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac / Islamic credit card number is " + retailDebitCardList.size());}
						
						ArrayList<String> islamicCreditCardList = new ArrayList<>();

						islamicCreditCardList.addAll(islamicVISACreditCardList);
						islamicCreditCardList.addAll(islamicMasterCreditCardList);
						islamicCreditCardList.addAll(islamicAmExCreditCardList);
						callInfo.setField(Field.ISLAMICCREDITCARDLIST, islamicCreditCardList);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac / Islamic credit card number is " + islamicCreditCardList.size());}


						ArrayList<String> retailCreditCardList = new ArrayList<>();

						retailCreditCardList.addAll(retailVISACreditCardList);
						retailCreditCardList.addAll(retailMasterCreditCardList);
						retailCreditCardList.addAll(retailAmExCreditCardList);
						callInfo.setField(Field.RETAILCREDITCARDLIST, retailCreditCardList);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Retail credit card number is " + retailCreditCardList.size());}

						ArrayList<String> islamicPrepaidCardList = new ArrayList<>();

						islamicPrepaidCardList.addAll(islamicVISAPrepaidCardList);
						islamicPrepaidCardList.addAll(islamicMasterPrepaidCardList);
						islamicPrepaidCardList.addAll(islamicAmExPrepaidCardList);
						callInfo.setField(Field.ISLAMICPREPAIDCARDLIST, islamicPrepaidCardList);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac / Islamic prepaid card number is " + islamicPrepaidCardList.size());}


						ArrayList<String> retailPrepaidCardList = new ArrayList<>();

						retailPrepaidCardList.addAll(retailVISAPrepaidCardList);
						retailPrepaidCardList.addAll(retailMasterPrepaidCardList);
						retailPrepaidCardList.addAll(retailAmExPrepaidCardList);
						callInfo.setField(Field.RETAILPREPAIDCARDLIST, retailPrepaidCardList);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Retail prepaid card number is " + retailPrepaidCardList.size());}


						
						/**
						 * Following are the condition handling for Islamic - Meethac flow
						 */
						String dnisType = util.isNullOrEmpty(callInfo.getField(Field.CUST_SEGMENT_TYPE))? Constants.CUST_SEGMENT_RETAIL : (String)callInfo.getField(Field.CUST_SEGMENT_TYPE);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The DNIS / Customer Segment type is " + dnisType);}


						if(Constants.CUST_SEGMENT_ISLAMICPRIORITY.equalsIgnoreCase(dnisType)){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "************ Called from the Flow type Islamic Priority ************");}
						}

						if(Constants.CUST_SEGMENT_PRIORITY.equalsIgnoreCase(dnisType)){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "************ Called from the Flow type Pure Priority ************");}
						}

						if(Constants.CUST_SEGMENT_RETAIL.equalsIgnoreCase(dnisType) || Constants.CUST_SEGMENT_PRIORITY.equalsIgnoreCase(dnisType)){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "************ Called from the Flow type Retail ************");}

							if(!util.isNullOrEmpty(savingsAcctList)){

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Saving account list before removing meethac savings account " +savingsAcctList );}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Saving account list Count before removing meethac savings account " +savingsAcctList.size());}

								if(!util.isNullOrEmpty(islamicSavingsAcctList)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Saving account list is  " +islamicSavingsAcctList);}
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Saving account list count is  " +islamicSavingsAcctList.size());}
									savingsAcctList.removeAll(islamicSavingsAcctList);
								}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Resultant Saving account list after removing meethac savings account list is " +savingsAcctList);}

								callInfo.setField(Field.SAVINGSACCTLIST, savingsAcctList);
							}


							if(!util.isNullOrEmpty(currentAcctList)){

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Saving account list before removing meethac current account " +currentAcctList );}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Saving account list Count before removing meethac current account " +currentAcctList.size());}

								if(!util.isNullOrEmpty(islamicCurrentAcctList)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Current account list is  " +islamicCurrentAcctList);}
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Current account list count is  " +islamicCurrentAcctList.size());}
									currentAcctList.removeAll(islamicCurrentAcctList);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Resultant Current account list after removing meethac Current account list is " +currentAcctList);}
								}

								callInfo.setField(Field.CURRENTACCTLIST, currentAcctList);
							}


							if(!util.isNullOrEmpty(loanAcctList)){

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Loan account list before removing meethac loan account " +loanAcctList );}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Loan account list Count before removing meethac loan account " +loanAcctList.size());}

								if(!util.isNullOrEmpty(islamicLoanAcctList)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Loan account list is  " +islamicLoanAcctList);}
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Loan account list count is  " +islamicLoanAcctList.size());}
									loanAcctList.removeAll(islamicLoanAcctList);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Resultant Current loan list after removing meethac loan account list is " +loanAcctList);}
								}

								callInfo.setField(Field.LOANACCTLIST, loanAcctList);
							}

							if(!util.isNullOrEmpty(fdAcctList)){


								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total FD account list before removing meethac FD account " +fdAcctList );}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total FD account list Count before removing meethac FD account " +fdAcctList.size());}


								if(!util.isNullOrEmpty(islamicFDAcctList)){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's FD account list is  " +islamicFDAcctList);}
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's FD account list count is  " +islamicFDAcctList.size());}
									fdAcctList.removeAll(islamicFDAcctList);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Resultant FD account list after removing meethac FD account list is " +fdAcctList);}
								}
								callInfo.setField(Field.FDACCTLIST, fdAcctList);
							}

							if(!util.isNullOrEmpty(creditCardList)){


//								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Credit card list before removing meethac Credit cards " +creditCardList );}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Credit card list Count before removing meethac credit card " +creditCardList.size());}

								if(!util.isNullOrEmpty(islamicCreditCardList)){
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Credit card list is  " +islamicCreditCardList);}
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Credit card list count is  " +islamicCreditCardList.size());}
									creditCardList.removeAll(islamicCreditCardList);
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Resultant FD account list after removing meethac FD account list is " +creditCardList);}
								}
								callInfo.setField(Field.CREDITCARDLIST, creditCardList);
							}

							if(!util.isNullOrEmpty(crVISACardList)){


//								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Credit card VISA list before removing meethac Credit VISA cards " +crVISACardList );}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Credit card VISA list Count before removing meethac credit VISA card " +crVISACardList.size());}

								if(!util.isNullOrEmpty(islamicVISACreditCardList)){
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Credit VISA card list is  " +islamicVISACreditCardList);}
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Credit VISA card list count is  " +islamicVISACreditCardList.size());}
									crVISACardList.removeAll(islamicVISACreditCardList);
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Resultant Credit card VISA list after removing meethac Credit card VISA list is " +crVISACardList);}
								}
								callInfo.setField(Field.CRVISACARDLIST, crVISACardList);
							}


							if(!util.isNullOrEmpty(crMasterCardList)){


//								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Credit card Master list before removing meethac Credit Master cards " +crMasterCardList );}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Credit card Master list Count before removing meethac credit Master card " +crMasterCardList.size());}

								if(!util.isNullOrEmpty(islamicMasterCreditCardList)){
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Credit Master card list is  " +islamicMasterCreditCardList);}
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Credit Master card list count is  " +islamicMasterCreditCardList.size());}
									crMasterCardList.removeAll(islamicMasterCreditCardList);
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Resultant Credit card Master list after removing meethac Credit card Master list is " +crMasterCardList);}
								}
								callInfo.setField(Field.CRMASTERCARDLIST, crMasterCardList);
							}


							if(!util.isNullOrEmpty(crAmExCardList)){
//								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Credit card Amex list before removing meethac Credit Amex cards " +crAmExCardList );}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Credit card Amex list Count before removing meethac credit Amex card " +crAmExCardList.size());}

								if(!util.isNullOrEmpty(islamicAmExCreditCardList)){
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Credit Amex card list is  " +islamicAmExCreditCardList);}
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Credit Amex card list count is  " +islamicAmExCreditCardList.size());}
									crAmExCardList.removeAll(islamicAmExCreditCardList);
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Resultant Credit card Master list after removing meethac Credit card Master list is " +crAmExCardList);}
								}
								callInfo.setField(Field.CRAMEXCARDLIST, crAmExCardList);
							}
							
							if(!util.isNullOrEmpty(prepaidCardList)){


//								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Prepaid card list before removing meethac Prepaid cards " +prepaidCardList );}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Prepaid card list Count before removing meethac prepaid card " +prepaidCardList.size());}

								if(!util.isNullOrEmpty(islamicPrepaidCardList)){
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Prepaid card list is  " +islamicPrepaidCardList);}
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Prepaid card list count is  " +islamicPrepaidCardList.size());}
									prepaidCardList.removeAll(islamicPrepaidCardList);
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Resultant FD account list after removing meethac FD account list is " +prepaidCardList);}
								}
								callInfo.setField(Field.PREPAIDCARDLIST, prepaidCardList);
							}

							if(!util.isNullOrEmpty(ppVISACardList)){


//								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Prepaid card VISA list before removing meethac Prepaid VISA cards " +ppVISACardList );}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Prepaid card VISA list Count before removing meethac prepaid VISA card " +ppVISACardList.size());}

								if(!util.isNullOrEmpty(islamicVISAPrepaidCardList)){
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Prepaid VISA card list is  " +islamicVISAPrepaidCardList);}
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Prepaid VISA card list count is  " +islamicVISAPrepaidCardList.size());}
									ppVISACardList.removeAll(islamicVISAPrepaidCardList);
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Resultant Prepaid card VISA list after removing meethac Prepaid card VISA list is " +ppVISACardList);}
								}
								callInfo.setField(Field.PPVISACARDLIST, ppVISACardList);
							}


							if(!util.isNullOrEmpty(ppMasterCardList)){


//								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Prepaid card Master list before removing meethac Prepaid Master cards " +ppMasterCardList );}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Prepaid card Master list Count before removing meethac prepaid Master card " +ppMasterCardList.size());}

								if(!util.isNullOrEmpty(islamicMasterPrepaidCardList)){
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Prepaid Master card list is  " +islamicMasterPrepaidCardList);}
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Prepaid Master card list count is  " +islamicMasterPrepaidCardList.size());}
									ppMasterCardList.removeAll(islamicMasterPrepaidCardList);
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Resultant Prepaid card Master list after removing meethac Prepaid card Master list is " +ppMasterCardList);}
								}
								callInfo.setField(Field.PPMASTERCARDLIST, ppMasterCardList);
							}


							if(!util.isNullOrEmpty(ppAmExCardList)){
//								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Prepaid card Amex list before removing meethac Prepaid Amex cards " +ppAmExCardList );}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Prepaid card Amex list Count before removing meethac prepaid Amex card " +ppAmExCardList.size());}

								if(!util.isNullOrEmpty(islamicAmExPrepaidCardList)){
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Prepaid Amex card list is  " +islamicAmExPrepaidCardList);}
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Prepaid Amex card list count is  " +islamicAmExPrepaidCardList.size());}
									ppAmExCardList.removeAll(islamicAmExPrepaidCardList);
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Resultant Prepaid card Master list after removing meethac Prepaid card Master list is " +ppAmExCardList);}
								}
								callInfo.setField(Field.PPAMEXCARDLIST, ppAmExCardList);
							}


							if(!util.isNullOrEmpty(drVISACardList)){


//								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Debit card VISA list before removing meethac Debit VISA cards " +drVISACardList );}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Debit card VISA list Count before removing meethac Debit VISA card " +drVISACardList.size());}

								if(!util.isNullOrEmpty(islamicVISADebitCardList)){
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Debit VISA card list is  " +islamicVISADebitCardList);}
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Debit VISA card list count is  " +islamicVISADebitCardList.size());}
									drVISACardList.removeAll(islamicVISADebitCardList);
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Resultant Debit card VISA list after removing meethac Credit card VISA list is " +drVISACardList);}
								}
								callInfo.setField(Field.DRVISACARDLIST, drVISACardList);
							}


							if(!util.isNullOrEmpty(drMasterCardList)){


//								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Debit card Master list before removing meethac Debit Master cards " +drMasterCardList );}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Debit card Master list Count before removing meethac Debit Master card " +drMasterCardList.size());}

								if(!util.isNullOrEmpty(islamicMasterDebitCardList)){
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Credit Master card list is  " +islamicMasterDebitCardList);}
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Credit Master card list count is  " +islamicMasterDebitCardList.size());}
									drMasterCardList.removeAll(islamicMasterDebitCardList);
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Resultant Credit card Master list after removing meethac Credit card Master list is " +drMasterCardList);}
								}
								callInfo.setField(Field.DRMASTERCARDLIST, drMasterCardList);
							}


							if(!util.isNullOrEmpty(drAmExCardList)){
//								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Credit card Amex list before removing meethac Credit Amex cards " +drAmExCardList );}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Credit card Amex list Count before removing meethac credit Amex card " +drAmExCardList.size());}

								if(!util.isNullOrEmpty(islamicAmExDebitCardList)){
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Debit Amex card list is  " +islamicAmExDebitCardList);}
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Debit Amex card list count is  " +islamicAmExDebitCardList.size());}
									drAmExCardList.removeAll(islamicAmExDebitCardList);
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Resultant Credit card Master list after removing meethac Credit card Master list is " +drAmExCardList);}
								}
								callInfo.setField(Field.DRAMEXCARDLIST, crAmExCardList);
							}

							
							
							if(!util.isNullOrEmpty(drVISACardListInactive)){


//								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Debit card Inactive VISA list before removing meethac Debit VISA cards " +drVISACardListInactive );}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Debit card Inactive VISA list Count before removing meethac Debit VISA card " +drVISACardListInactive.size());}

								if(!util.isNullOrEmpty(drIslamicVISACardListInactive)){
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Inactive Debit VISA card list is  " +drIslamicVISACardListInactive);}
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Inactive Debit VISA card list count is  " +drIslamicVISACardListInactive.size());}
									drVISACardListInactive.removeAll(drIslamicVISACardListInactive);
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Resultant Debit card Inactive VISA list after removing meethac inactive debit card VISA list is " +drVISACardListInactive);}
								}
								callInfo.setField(Field.DRVISACARDLISTINACTIVE, drVISACardListInactive);
							}
							
							
							
							if(!util.isNullOrEmpty(drMasterCardListInactive)){


//								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Debit card Inactive Mater list before removing meethac Debit VISA cards " +drMasterCardListInactive );}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Debit card Inactive Master list Count before removing meethac Debit VISA card " +drMasterCardListInactive.size());}

								if(!util.isNullOrEmpty(drIslamicMasterCardListInactive)){
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Inactive Debit Master card list is  " +drIslamicMasterCardListInactive);}
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Inactive Debit Master card list count is  " +drIslamicMasterCardListInactive.size());}
									drMasterCardListInactive.removeAll(drIslamicMasterCardListInactive);
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Resultant Debit card Inactive Master list after removing meethac inactive Debit card Master list is " +drMasterCardListInactive);}
								}
								callInfo.setField(Field.DRMASTERCARDLISTINACTIVE, drMasterCardListInactive);
							}
							
							
							if(!util.isNullOrEmpty(drAmexCardListInactive)){


//								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Debit card Inactive Amex list before removing meethac Debit VISA cards " +drAmexCardListInactive );}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Debit card Inactive Amex list Count before removing meethac Debit VISA card " +drAmexCardListInactive.size());}

								if(!util.isNullOrEmpty(drIslamicAmexCardListInactive)){
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Inactive Debit Amex card list is  " +drIslamicAmexCardListInactive);}
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total Meethac's Inactive Debit Amex card list count is  " +drIslamicAmexCardListInactive.size());}
									drAmexCardListInactive.removeAll(drIslamicAmexCardListInactive);
//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Resultant Debit card Inactive Amex list after removing meethac inactive Debit card Amex list is " +drAmexCardListInactive);}
								}
								callInfo.setField(Field.DRAMEXCARDLISTINACTIVE, drAmexCardListInactive);
							}
							
							
							//END
							
						}

						/**
						 * Rule engine update
						 */
						ICERuleParam ruleParamObj = (ICERuleParam)callInfo.getICERuleParam();

						if(util.isNullOrEmpty(ruleParamObj)){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "***********Rule Engine Object is null or empty*************" + ruleParamObj);}
						}

						//Initialized the Rule Engine Object


						/**
						 * Following are for Islamic flow
						 */

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "***********Setting the rule engine IsIslamic Product Availa flag value as *************" + isIslamicProdAvail);}
						ruleParamObj.setIVRParam(Constants.RULE_ENGINE_IS_ISLAMICPRODUCT_AVAIL, isIslamicProdAvail + Constants.EMPTY);
						//						ruleParamObj.updateIVRFields();



						/**
						 * Following are for Retail flow
						 */
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "***********Setting the rule engine IsRetail Product Availa flag value as *************" + isRetailProdAvail);}
						ruleParamObj.setIVRParam(Constants.RULE_ENGINE_IS_RETAILPRODUCT_AVAIL, isRetailProdAvail + Constants.EMPTY);


						int islamicCreditCardCount = util.isNullOrEmpty(islamicCreditCardList)? Constants.GL_ZERO : islamicCreditCardList.size();

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total no of islamic Credit cards are : "+islamicCreditCardCount );}
						callInfo.setField(Field.NO_OF_ISLAMIC_CREDIT_CARDS, islamicCreditCardCount);

						/**
						 * Rule engine update
						 */
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting islamic Current AcctList count in the Rule Engine " + islamicCreditCardCount);}
						ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISLAMIC_CREDITCARDCOUNT, (islamicCreditCardCount+Constants.EMPTY));
						//END Rule Engine Updation
						
						int islamicPrepaidCardCount = util.isNullOrEmpty(islamicPrepaidCardList)? Constants.GL_ZERO : islamicPrepaidCardList.size();

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total no of islamic Prepaid cards are : "+islamicPrepaidCardCount );}
						callInfo.setField(Field.NO_OF_ISLAMIC_PREPAID_CARDS, islamicPrepaidCardCount);

						/**
						 * Rule engine update
						 */
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting islamic Current AcctList count in the Rule Engine " + islamicPrepaidCardCount);}
						ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISLAMIC_PREPAIDCARDCOUNT, (islamicPrepaidCardCount+Constants.EMPTY));
						//END Rule Engine Updation



						int islamicCurrentAcctCount = util.isNullOrEmpty(islamicCurrentAcctList)? Constants.GL_ZERO : islamicCurrentAcctList.size();

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total no of islamic Current Acct List : "+islamicCurrentAcctCount );}
						callInfo.setField(Field.NO_OF_ISLAMIC_CURRENT_ACCTS, islamicCurrentAcctCount);

						/**
						 * Rule engine update
						 */
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting islamic Current AcctList count in the Rule Engine " + islamicCurrentAcctCount);}
						ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISLAMIC_CURRENTACCTCOUNT, (islamicCurrentAcctCount+Constants.EMPTY));
						//END Rule Engine Updation



						int islamicFDAcctCount = util.isNullOrEmpty(islamicFDAcctList)? Constants.GL_ZERO : islamicFDAcctList.size();

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total no of islamic Deposit Acct List : "+islamicFDAcctCount );}
						callInfo.setField(Field.NO_OF_ISLAMIC_DEPOSIT_ACCTS, islamicFDAcctCount);

						/**
						 * Rule engine update
						 */
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting islamic Deposit AcctList count in the Rule Engine " + islamicFDAcctCount);}
						ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISLAMIC_DEPOSITACCTCOUNT, (islamicFDAcctCount+Constants.EMPTY));
						//END Rule Engine Updation


						int islamicLoanAcctCount = util.isNullOrEmpty(islamicLoanAcctList)? Constants.GL_ZERO : islamicLoanAcctList.size();

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total no of islamic Loan Acct List : "+islamicLoanAcctCount);}
						callInfo.setField(Field.NO_OF_ISLAMIC_LOAN_ACCTS, islamicLoanAcctCount);

						/**
						 * Rule engine update
						 */
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting islamic Loan AcctList count in the Rule Engine " + islamicLoanAcctCount);}
						ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISLAMIC_LOANACCTCOUNT, (islamicLoanAcctCount+Constants.EMPTY));
						//END Rule Engine Updation


						int islamicSavingAcctCount = util.isNullOrEmpty(islamicSavingsAcctList)? Constants.GL_ZERO : islamicSavingsAcctList.size();

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total no of islamic Saving Acct List : "+islamicSavingAcctCount);}
						callInfo.setField(Field.NO_OF_ISLAMIC_SAVINGS_ACCTS, islamicSavingAcctCount);

						/**
						 * Rule engine update
						 */
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting islamic Saving AcctList count in the Rule Engine " + islamicSavingAcctCount);}
						ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISLAMIC_SAVINGACCTCOUNT, (islamicSavingAcctCount+Constants.EMPTY));
						//END Rule Engine Updation




						int creditCardCount = util.isNullOrEmpty(creditCardList)? Constants.GL_ZERO : creditCardList.size();

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total no of Credit cards : "+creditCardCount);}
						callInfo.setField(Field.NO_OF_CREDIT_CARDS, creditCardCount);

						/**
						 * Rule engine update
						 */
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting Credit card count in the Rule Engine " + creditCardCount);}
						ruleParamObj.setIVRParam(Constants.RULE_ENGINE_CREDITCARDCOUNT, (creditCardCount+Constants.EMPTY));
						//END Rule Engine Updation

						
						int prepaidCardCount = util.isNullOrEmpty(prepaidCardList)? Constants.GL_ZERO : prepaidCardList.size();

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total no of Prepaid cards : "+prepaidCardCount);}
						callInfo.setField(Field.NO_OF_PREPAID_CARDS, prepaidCardCount);

						/**
						 * Rule engine update
						 */
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting Prepaid card count in the Rule Engine " + prepaidCardCount);}
						ruleParamObj.setIVRParam(Constants.RULE_ENGINE_PREPAIDCARDCOUNT, (prepaidCardCount+Constants.EMPTY));
						//END Rule Engine Updation
						

						int debitCardCount = util.isNullOrEmpty(debitCardList)? Constants.GL_ZERO : debitCardList.size();


						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total no of Debit Cards: "+debitCardCount );}
						callInfo.setField(Field.NO_OF_DEBIT_CARDS, debitCardCount);

						/**
						 * Rule engine update
						 */
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting Debit card count in the Rule Engine " + debitCardCount);}
						ruleParamObj.setIVRParam(Constants.RULE_ENGINE_DEBITCARDCOUNT, (debitCardCount+Constants.EMPTY));
						//END Rule Engine Updation



						int savingAcctCount = util.isNullOrEmpty(savingsAcctList)?Constants.GL_ZERO : savingsAcctList.size();

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total no of Saving accounts : "+savingAcctCount );}
						callInfo.setField(Field.NO_OF_SAVINGS_ACCTS, savingAcctCount);

						/**
						 * Rule engine update
						 */
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting Saving Account count in the Rule Engine " + savingAcctCount);}
						ruleParamObj.setIVRParam(Constants.RULE_ENGINE_SAVINGACCTCOUNT, (savingAcctCount+Constants.EMPTY));
						//END Rule Engine Updation

						int loanAcctCount = util.isNullOrEmpty(loanAcctList)? Constants.GL_ZERO : loanAcctList.size();

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total no of Loan accounts : "+loanAcctCount);}
						callInfo.setField(Field.NO_OF_LOAN_ACCTS, loanAcctCount);

						/**
						 * Rule engine update
						 */
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting Loan Account count in the Rule Engine " + loanAcctCount);}
						ruleParamObj.setIVRParam(Constants.RULE_ENGINE_LOANACCTCOUNT, (loanAcctCount+Constants.EMPTY));
						//END Rule Engine Updation



						int currentAcctCount = util.isNullOrEmpty(currentAcctList)? Constants.GL_ZERO : currentAcctList.size();

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total no of Current accounts : "+currentAcctCount);}
						callInfo.setField(Field.NO_OF_CURRENT_ACCTS, currentAcctCount);


						/**
						 * Rule engine update
						 */
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting Current Account count in the Rule Engine " + currentAcctCount);}
						ruleParamObj.setIVRParam(Constants.RULE_ENGINE_CURRENTACCTCOUNT, (currentAcctCount+Constants.EMPTY));
						//END Rule Engine Updation


						int fdAcctCount = util.isNullOrEmpty(fdAcctList)? Constants.GL_ZERO : fdAcctList.size();

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total no of FD accounts : "+fdAcctCount);}
						callInfo.setField(Field.NO_OF_FD_ACCTS, fdAcctCount);

						/**
						 * Rule engine update
						 */
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting Deposit Account count in the Rule Engine " + fdAcctCount);}
						ruleParamObj.setIVRParam(Constants.RULE_ENGINE_DEPOSITACCTCOUNT, (fdAcctCount+Constants.EMPTY));
						//END Rule Engine Updation

						ruleParamObj.updateIVRFields();
						//End Rule Engine Update

						/**
						 * Fixes done by Vinoth on 16-03-2014 to add the no of credit card VISA / Master / AMEX && same for Debit card details
						 */

						int crVisaCardCount = util.isNullOrEmpty(crVISACardList)? Constants.GL_ZERO : crVISACardList.size();

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total no of CR VISA Cards are : "+crVisaCardCount);}
						callInfo.setField(Field.NO_OF_CR_VISA_CARDS, crVisaCardCount);



						int crMasterCardCount = util.isNullOrEmpty(crMasterCardList)? Constants.GL_ZERO : crMasterCardList.size();

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total no of CR Master Cards are : "+crMasterCardCount);}
						callInfo.setField(Field.NO_OF_CR_MASTER_CARDS, crMasterCardCount);


						int crAmexCardCount = util.isNullOrEmpty(crAmExCardList)? Constants.GL_ZERO : crAmExCardList.size();

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total no of CR Amex Cards are : "+crAmexCardCount);}
						callInfo.setField(Field.NO_OF_CR_AMEX_CARDS, crAmexCardCount);

						int ppVisaCardCount = util.isNullOrEmpty(ppVISACardList)? Constants.GL_ZERO : ppVISACardList.size();

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total no of PP VISA Cards are : "+ppVisaCardCount);}
						callInfo.setField(Field.NO_OF_PP_VISA_CARDS, ppVisaCardCount);



						int ppMasterCardCount = util.isNullOrEmpty(ppMasterCardList)? Constants.GL_ZERO : ppMasterCardList.size();

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total no of PP Master Cards are : "+ppMasterCardCount);}
						callInfo.setField(Field.NO_OF_PP_MASTER_CARDS, ppMasterCardCount);


						int ppAmexCardCount = util.isNullOrEmpty(ppAmExCardList)? Constants.GL_ZERO : ppAmExCardList.size();

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total no of PP Amex Cards are : "+ppAmexCardCount);}
						callInfo.setField(Field.NO_OF_PP_AMEX_CARDS, ppAmexCardCount);


						int drVISACardCount = util.isNullOrEmpty(drVISACardList)? Constants.GL_ZERO : drVISACardList.size();

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total no of DR VISA Cards are : "+drVISACardCount);}
						callInfo.setField(Field.NO_OF_DR_VISA_CARDS, drVISACardCount);


						int drMasterCardCount = util.isNullOrEmpty(drMasterCardList)? Constants.GL_ZERO : drMasterCardList.size(); 

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total no of DR Master Cards are : "+drMasterCardCount);}
						callInfo.setField(Field.NO_OF_DR_MASTER_CARDS, drMasterCardCount);


						int drAmexCardCount = util.isNullOrEmpty(drAmExCardList)? Constants.GL_ZERO : drAmExCardList.size();

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total no of DR Amex Cards are : "+drAmexCardCount);}
						callInfo.setField(Field.NO_OF_DR_AMEX_CARDS, drAmexCardCount);



						int drVisaCardInactiveCount = util.isNullOrEmpty(drVISACardListInactive)? Constants.GL_ZERO : drVISACardListInactive.size();

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total no of Inactive DR VISA Cards are : "+drVisaCardInactiveCount);}
						callInfo.setField(Field.NO_OF_DR_VISA_CARDS_INACTVE, drVisaCardInactiveCount);


						int drMasterCardInActiveCount = util.isNullOrEmpty(drMasterCardListInactive)? Constants.GL_ZERO : drMasterCardListInactive.size();

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total no of Inactive DR Master Cards are : "+drMasterCardInActiveCount);}
						callInfo.setField(Field.NO_OF_DR_MASTER_CARDS_INACTIVE, drMasterCardInActiveCount);


						int drAmexCardInActiveCount = util.isNullOrEmpty(drAmexCardListInactive)? Constants.GL_ZERO : drAmexCardListInactive.size(); 

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total no of Inactive DR Amex Cards are : "+drAmexCardInActiveCount);}
						callInfo.setField(Field.NO_OF_DR_AMEX_CARDS_INACTIVE, drAmexCardInActiveCount);


						/**
						 * Rule engine update
						 */
						int totalInActiveDbtCards = Constants.GL_ZERO;
						callInfo.setField(Field.DRVISACARDLISTINACTIVE, drVISACardListInactive);
						callInfo.setField(Field.DRMASTERCARDLISTINACTIVE, drMasterCardListInactive);
						callInfo.setField(Field.DRAMEXCARDLISTINACTIVE, drAmexCardListInactive);

						if(!util.isNullOrEmpty(drVISACardListInactive)){
							totalInActiveDbtCards = totalInActiveDbtCards + drVISACardListInactive.size();
						}

						if(!util.isNullOrEmpty(drMasterCardListInactive)){
							totalInActiveDbtCards = totalInActiveDbtCards + drMasterCardListInactive.size();
						}

						if(!util.isNullOrEmpty(drAmexCardListInactive)){
							totalInActiveDbtCards = totalInActiveDbtCards + drAmexCardListInactive.size();
						}

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Total No Of Inactive cards " + totalInActiveDbtCards);}

						if(!util.isNullOrEmpty(totalInActiveDbtCards)){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting Total No Of Inactive Debit Cards in the Rule Engine " + totalInActiveDbtCards);}
							ruleParamObj.setIVRParam(Constants.RULE_ENGINE_DEBITCARDINACTIVECOUNT, totalInActiveDbtCards + Constants.EMPTY);
							ruleParamObj.updateIVRFields();
						}
						//END Rule Engine Updation


						/**
						 * Following are the hanlding done for Islamic Meethac flow - It should be hanlded above after getting confirmation from Vijay
						 */
						if(!util.isNullOrEmpty(Context.getIglobal()) && !util.isNullOrEmpty(callInfo.getField(Field.CUST_SEGMENT_TYPE)) &&
								(Constants.CUST_SEGMENT_ISLAMIC.equalsIgnoreCase((String)callInfo.getField(Field.CUST_SEGMENT_TYPE))||
										Constants.CUST_SEGMENT_ISLAMICPRIORITY.equalsIgnoreCase((String)callInfo.getField(Field.CUST_SEGMENT_TYPE)))){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Before calling the Islamic meethac callFlow configuration parameters");}
							IGlobal iglobal = Context.getIglobal();
							iglobal.getIslamicConfiguration(callInfo);		
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "After calling the Islamic meethac callFlow configuration parameters");}
						}
						//END 

						//END Vinoth on 16-MAR-2014


						/**
						 * Following are the hanlding done for Islamic Meethac flow - It should be hanlded above after getting confirmation from Vijay
						 */
						if(!util.isNullOrEmpty(Context.getIglobal()) && !util.isNullOrEmpty(callInfo.getField(Field.CUST_SEGMENT_TYPE)) &&
								(Constants.CUST_SEGMENT_RETAIL.equalsIgnoreCase((String)callInfo.getField(Field.CUST_SEGMENT_TYPE))||
										Constants.CUST_SEGMENT_PRIORITY.equalsIgnoreCase((String)callInfo.getField(Field.CUST_SEGMENT_TYPE)))){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Before calling the Retail callFlow configuration parameters");}
							IGlobal iglobal = Context.getIglobal();
							iglobal.getRetailConfiguration(callInfo);		
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "After calling the Retail callFlow configuration parameters");}
						}
						//END 

						//End Vinoth on 30-May-2014


					}else{
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received null / empty custAcctListResType response object so setting error code as 1");}
						beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
					}

				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received null / empty CustomerProfileAggregateResType response object so setting error code as 1");}
					beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
				}
			}
		}
		catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at CustomerProfileAggregateDAOImpl.getCallerIdentificationHostRes() "	+ pe.getMessage());}
//			pe.printStackTrace();
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CustomerProfileAggregateDAOImpl.getCallerIdentificationHostRes() "	+ e.getMessage());}
//			e.printStackTrace();

			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: CustomerProfileAggregateDAOImpl.getCallerIdentificationHostRes()");}
		return beanResponse;
	}

	public boolean isAcctOrCardBlocked (CallInfo callInfo, String cardOrAcctNo)throws DaoException{
		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: CustomerProfileAggregateDAOImpl.isAcctOrCardBlocked()");}
		boolean returnValue = true;
		try{
			//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Processing the Account list response type");}
			//			
			//			HashMap<String, ArrayList<String>> binTable = new HashMap<String, ArrayList<String>>();
			//			binTable = (HashMap<String, ArrayList<String>>) callInfo.getField(Field.BINAndAccountTypes);
			//			
			//			if(util.isNullOrEmpty(binTable)){
			//				throw new DaoException("BIN Number table value is null / Empty");
			//			}
			//			
			//			String binKey = Constants.EMPTY_STRING;
			//			ArrayList<String> binNoList = new ArrayList<String>();
			//			ArrayList<String> tempList = new ArrayList<>();
			//			
			//			String strBinValue = Constants.EMPTY_STRING;
			//			int count = Constants.GL_ZERO;
			//			Iterator entries = binTable.entrySet().iterator();
			//			while (entries.hasNext()) {
			//			    Map.Entry entry = (Map.Entry) entries.next();
			//			    binKey = (String)entry.getKey();
			//			    if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"The "+count+ "The bin table product Key is "+ binKey);}
			//			    binNoList = (ArrayList<String>)entry.getValue();
			//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.INFO, "The retireved bin no list for the key"+binKey+" is " +binNoList);}
			//				
			//				for(int index=0; index < binNoList.size(); index++){
			//					strBinValue = binNoList.get(index);
			//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.INFO, "Its a valid bin number ");}
			//					
			//					if(strBinValue.equalsIgnoreCase(cardOrAcctNo.substring(Constants.GL_ZERO, strBinValue.length()))){
			//						returnValue = true;
			//						break;
			//					}
			//				}
			//				
			//				if(returnValue){
			//					break;
			//				}
			//				
			//			}
			//			
			//			callInfo.setField(Field.BINAndAccountTypes, binTable);
			//			
			//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.INFO, "Total Number of bin product types presents are "+binTable.size());}
			//			//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: CustomerProfileAggregateDAOImpl.isAValidAcctOrCardNo()");}
			return returnValue;

		}catch(Exception pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at CustomerProfileAggregateDAOImpl.isAcctOrCardBlocked() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}

	}


}