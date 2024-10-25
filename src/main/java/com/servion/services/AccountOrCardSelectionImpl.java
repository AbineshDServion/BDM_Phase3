package com.servion.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.ice.RuleEngine.ICERuleParam;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;
import com.servion.model.MenuIDMap;
import com.servion.model.callerIdentification.AcctInfo;

public class AccountOrCardSelectionImpl implements IAccountOrCardSelection {
	private static Logger logger = LoggerObject.getLogger();
	private MessageSource messageSource;

	public MessageSource getMessageSource() {
		return messageSource;
	}

	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	//	@Override
	//	public int getAccountLimitToCallAcctEntry(CallInfo arg0)
	//			throws ServiceException {
	//		// TODO Auto-generated method stub
	//		return 0;
	//	}

	@Override
	public String getAccountPhrases(CallInfo callInfo) throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: AccountOrCardSelectionImpl.getAccountPhrases()");}
		String str_GetMessage, finalResult;
		getConfigurationParam(callInfo);
		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalObject = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalObject)){
				throw new ServiceException("ivr_ICEGlobalObject is null / empty");
			}
			
			String customerSegment = util.isNullOrEmpty(callInfo.getField(Field.CUST_SEGMENT_TYPE)) ? Constants.CUST_SEGMENT_RETAIL : (String)callInfo.getField(Field.CUST_SEGMENT_TYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured Customer segment type is "+ customerSegment);}


			String cardVisaBrandTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_CARDBRANDTYPE_VISA+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured credit card VISA brand type is "+ cardVisaBrandTypeList);}

			String cardMasterBrandTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_CARDBRANDTYPE_MASTER+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured credit card Master brand type is "+ cardMasterBrandTypeList);}

			String cardAmexBrandTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_CARDBRANDTYPE_AMEX+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured credit card Amex brand type is "+ cardAmexBrandTypeList);}

			String currentAccoutTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_ACCOUNTTYPE_CURRENT+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured current account type list is "+ currentAccoutTypeList);}

			String savingAccountTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_ACCOUNTTYPE_SAVING+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured savings account type list is "+ savingAccountTypeList);}


			String loanAccountTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_ACCOUNTTYPE_LOAN+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured loan account type list is "+ loanAccountTypeList);}

			String depositAccountTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_ACCOUNTTYPE_DEPOSIT+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured deposit account type list is "+ depositAccountTypeList);}


			boolean calledForDestAcctSelection = false;

			if(util.isNullOrEmpty(callInfo.getField(Field.FROMDESTINATION))){
				callInfo.setField(Field.FROMDESTINATION,false);
			}else{
				calledForDestAcctSelection = (boolean)callInfo.getField(Field.FROMDESTINATION);
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "This method called for Source acct selection / for Destination account selection ? "+ calledForDestAcctSelection);}


			String featureType =callInfo.getField(Field.FEATURETYPE) == null ? Constants.EMPTY_STRING : (String)callInfo.getField(Field.FEATURETYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Feature type is "+ featureType);}


			String featureName = callInfo.getField(Field.FEATURENAME) == null ? Constants.EMPTY_STRING :(String)callInfo.getField(Field.FEATURENAME);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Feature Name is "+ featureName);}

			String srouceAcctOrCardNo = callInfo.getField(Field.SRCNO) == null ? Constants.EMPTY_STRING :(String)callInfo.getField(Field.SRCNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The source number in the SRCNO field is "+ util.maskCardOrAccountNumber(srouceAcctOrCardNo));}

			
			/**
			 * Following modification has done for Loan account selection, since the loan account selection has loan accounts of same ending digits, 
			 * handled the below condition check to resolve this
			 */
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			String strlastNdigit = Constants.EMPTY_STRING;
			strlastNdigit = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_LastNDigits)) ? null : (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_LastNDigits);
			if(util.isNullOrEmpty(strlastNdigit)){
				strlastNdigit =  callInfo.getField(Field.LastNDigits) == null ? Constants.EMPTY_STRING :(String)callInfo.getField(Field.LastNDigits);
			}
			//End
			
			int lastNdigit = util.isNullOrEmpty(strlastNdigit)?Constants.GL_FOUR:Integer.parseInt(strlastNdigit);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Card / Account Suffix's Announcement lenght is "+ lastNdigit);}

			ArrayList<String> eligibleCurrType = null;


			ArrayList<String> selectedProductAcctOrCards = null;
			ArrayList<String> filteringProductAcctOrCards = new ArrayList<String>();


			eligibleCurrType =  callInfo.getField(Field.ELIGIBLEACCTCARDCURRTYPE) == null ? new ArrayList<String>() : (ArrayList<String>) callInfo.getField(Field.ELIGIBLEACCTCARDCURRTYPE);
			if(calledForDestAcctSelection){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Since its called from Destination account selection option removing source account from the list");}
				filteringProductAcctOrCards.add(srouceAcctOrCardNo);

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Overriding the Currency type  with the destination type");}
				eligibleCurrType = callInfo.getField(Field.ELIGIBLEDESTACCTCARDCURRTYPE) == null ? new ArrayList<String>() :  (ArrayList<String>) callInfo.getField(Field.ELIGIBLEDESTACCTCARDCURRTYPE);
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Eligible currency type is "+ eligibleCurrType);}

			String selectedProductType = callInfo.getField(Field.SELECTEDTYPE) == null ? Constants.EMPTY : (String)callInfo.getField(Field.SELECTEDTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "*****Selected product type is "+ selectedProductType);}

			String moreCount =  callInfo.getField(Field.MORECOUNT) == null ? Constants.EMPTY : (String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_Str = Constants.EMPTY_STRING;

			//added on 26-MAr-2014 for Account Number entry Constarinst list
			String constrainList = Constants.EMPTY_STRING;

			int noOfEligibleAccounts = Constants.GL_ZERO;
			String accountOrCardNumber = Constants.EMPTY_STRING;
			String selectedProductPhrase = Constants.EMPTY_STRING;



			if(util.isCodePresentInTheConfigurationList(selectedProductType, depositAccountTypeList)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Retrieving the Available Deposit account list");}
				selectedProductAcctOrCards =  callInfo.getField(Field.FDACCTLIST) == null ? new ArrayList<String>() :(ArrayList<String>)callInfo.getField(Field.FDACCTLIST);
				//				selectedProductAcctOrCards = (ArrayList<String>)selectedProductAcctOrCards.clone();
				if(selectedProductAcctOrCards!=null){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The total number of Deposit Account available is " + selectedProductAcctOrCards.size());}
				}
				selectedProductPhrase = DynaPhraseConstants.Account_Selection_1015;
			}else if(util.isCodePresentInTheConfigurationList(selectedProductType, currentAccoutTypeList)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Retrieving the Available CREDIT / CURRENT account list");}
				selectedProductAcctOrCards = callInfo.getField(Field.CURRENTACCTLIST) == null ? new ArrayList<String>() :(ArrayList<String>)callInfo.getField(Field.CURRENTACCTLIST);
				//				selectedProductAcctOrCards = (ArrayList<String>)selectedProductAcctOrCards.clone();
				if(selectedProductAcctOrCards!=null){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The total number of CREDIT / CURRENT Account available is " + selectedProductAcctOrCards.size());}
				}
				selectedProductPhrase = DynaPhraseConstants.Account_Selection_1008;
			}else if(util.isCodePresentInTheConfigurationList(selectedProductType, loanAccountTypeList)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Retrieving the Available LOAN account list");}
				selectedProductAcctOrCards = callInfo.getField(Field.LOANACCTLIST) == null ? new ArrayList<String>() :(ArrayList<String>)callInfo.getField(Field.LOANACCTLIST);
				//				selectedProductAcctOrCards = (ArrayList<String>)selectedProductAcctOrCards.clone();
				if(selectedProductAcctOrCards!=null){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The total number of Deposit Account available is " + selectedProductAcctOrCards.size());}
				}
				selectedProductPhrase = DynaPhraseConstants.Account_Selection_1014;
			}else if(util.isCodePresentInTheConfigurationList(selectedProductType, savingAccountTypeList)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Retrieving the Available Savings account list");}
				selectedProductAcctOrCards =callInfo.getField(Field.SAVINGSACCTLIST) == null ? new ArrayList<String>() : (ArrayList<String>)callInfo.getField(Field.SAVINGSACCTLIST);
				//				selectedProductAcctOrCards = (ArrayList<String>)selectedProductAcctOrCards.clone();
				if(selectedProductAcctOrCards!=null){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The total number of Savings Account available is " + selectedProductAcctOrCards.size());}
				}
				selectedProductPhrase = DynaPhraseConstants.Account_Selection_1007;
			}else if(util.isCodePresentInTheConfigurationList(selectedProductType, cardVisaBrandTypeList) && Constants.FEATURE_TYPE_BANK.equalsIgnoreCase(featureType)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Retrieving the Available DR VISA CARD list");}
				selectedProductAcctOrCards = callInfo.getField(Field.DRVISACARDLIST) == null ? new ArrayList<String>() :(ArrayList<String>)callInfo.getField(Field.DRVISACARDLIST);
				//				selectedProductAcctOrCards = (ArrayList<String>)selectedProductAcctOrCards.clone();

				/**
				 * Handled by Vinoth on 07-Apr-2014 for Debit card activation flow
				 */

				if(Constants.FEATURENAME_CARDACTIVATION.equalsIgnoreCase(featureName)){
					selectedProductAcctOrCards = callInfo.getField(Field.DRVISACARDLISTINACTIVE) == null ? new ArrayList<String>() : (ArrayList<String>)callInfo.getField(Field.DRVISACARDLISTINACTIVE);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Overrriding the Selected Product Acct Or Cards with Inactive DR Visa cards size is " + selectedProductAcctOrCards.size());}
				}
				//END - Vinoth

				if(selectedProductAcctOrCards!=null){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The total number of DR VISA CARD available is " + selectedProductAcctOrCards.size());}
				}
				selectedProductPhrase = DynaPhraseConstants.Account_Selection_1009;
			}else if(util.isCodePresentInTheConfigurationList(selectedProductType, cardMasterBrandTypeList) && Constants.FEATURE_TYPE_BANK.equalsIgnoreCase(featureType)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Retrieving the Available DR MASTER CARD list");}
				selectedProductAcctOrCards = callInfo.getField(Field.DRMASTERCARDLIST) == null ? new ArrayList<String>() : (ArrayList<String>)callInfo.getField(Field.DRMASTERCARDLIST);
				//				selectedProductAcctOrCards = (ArrayList<String>)selectedProductAcctOrCards.clone();

				/**
				 * Handled by Vinoth on 07-Apr-2014 for Debit card activation flow
				 */

				if(Constants.FEATURENAME_CARDACTIVATION.equalsIgnoreCase(featureName)){
					selectedProductAcctOrCards = callInfo.getField(Field.DRMASTERCARDLISTINACTIVE) == null ? new ArrayList<String>() :(ArrayList<String>)callInfo.getField(Field.DRMASTERCARDLISTINACTIVE);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Overrriding the Selected Product Acct Or Cards with Inactive DR Master cards" + selectedProductAcctOrCards);}
				}
				//END - Vinoth


				if(selectedProductAcctOrCards!=null){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The total number of DR MASTER CARD available is " + selectedProductAcctOrCards.size());}
				}
				selectedProductPhrase = DynaPhraseConstants.Account_Selection_1010;
			}else if(util.isCodePresentInTheConfigurationList(selectedProductType, cardAmexBrandTypeList) && Constants.FEATURE_TYPE_BANK.equalsIgnoreCase(featureType)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Retrieving the Available DR AMEX CARD list");}
				selectedProductAcctOrCards = callInfo.getField(Field.DRAMEXCARDLIST) == null ? new ArrayList<String>() : (ArrayList<String>)callInfo.getField(Field.DRAMEXCARDLIST);
				//				selectedProductAcctOrCards = (ArrayList<String>)selectedProductAcctOrCards.clone();

				/**
				 * Handled by Vinoth on 07-Apr-2014 for Debit card activation flow
				 */

				if(Constants.FEATURENAME_CARDACTIVATION.equalsIgnoreCase(featureName)){
					selectedProductAcctOrCards =  callInfo.getField(Field.DRAMEXCARDLISTINACTIVE) == null ? new ArrayList<String>() :(ArrayList<String>)callInfo.getField(Field.DRAMEXCARDLISTINACTIVE);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Overrriding the Selected Product Acct Or Cards with Inactive DR Amex cards" + selectedProductAcctOrCards);}
				}
				//END - Vinoth


				if(selectedProductAcctOrCards!=null){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The total number of DR AMEX CARD available is " + selectedProductAcctOrCards.size());}
				}
				selectedProductPhrase = DynaPhraseConstants.Account_Selection_1006;
			}else if(util.isCodePresentInTheConfigurationList(selectedProductType, cardVisaBrandTypeList) && 
					Constants.FEATURE_TYPE_CARD.equalsIgnoreCase(featureType) ){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Retrieving the Available CR VISA CARD list");}
				selectedProductAcctOrCards =   callInfo.getField(Field.CRVISACARDLIST) == null ? new ArrayList<String>() :(ArrayList<String>)callInfo.getField(Field.CRVISACARDLIST);
				//				selectedProductAcctOrCards = (ArrayList<String>)selectedProductAcctOrCards.clone();
				if(selectedProductAcctOrCards!=null){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The total number of  CR VISA CARD available is " + selectedProductAcctOrCards.size());}
				}
				selectedProductPhrase = DynaPhraseConstants.Account_Selection_1009;
			}else if(util.isCodePresentInTheConfigurationList(selectedProductType, cardMasterBrandTypeList) && 
					Constants.FEATURE_TYPE_CARD.equalsIgnoreCase(featureType)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Retrieving the Available  CR MASTER CARD list");}
				selectedProductAcctOrCards =   callInfo.getField(Field.CRMASTERCARDLIST) == null ? new ArrayList<String>() :(ArrayList<String>)callInfo.getField(Field.CRMASTERCARDLIST);
				//				selectedProductAcctOrCards = (ArrayList<String>)selectedProductAcctOrCards.clone();
				if(selectedProductAcctOrCards!=null){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The total number of  CR MASTER CARD available is " + selectedProductAcctOrCards.size());}
				}
				selectedProductPhrase = DynaPhraseConstants.Account_Selection_1010;
			}else if(util.isCodePresentInTheConfigurationList(selectedProductType, cardAmexBrandTypeList) && 
					Constants.FEATURE_TYPE_CARD.equalsIgnoreCase(featureType) ){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Retrieving the Available CR AMEX CARD list");}
				selectedProductAcctOrCards = callInfo.getField(Field.CRAMEXCARDLIST) == null ? new ArrayList<String>() : (ArrayList<String>)callInfo.getField(Field.CRAMEXCARDLIST);
				//				selectedProductAcctOrCards = (ArrayList<String>)selectedProductAcctOrCards.clone();
				if(selectedProductAcctOrCards!=null){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The total number of Available CR AMEX CARD is " + selectedProductAcctOrCards.size());}
				}
				selectedProductPhrase = DynaPhraseConstants.Account_Selection_1006;
			}else if(util.isCodePresentInTheConfigurationList(selectedProductType, cardVisaBrandTypeList) && 
					Constants.FEATURE_TYPE_PREPAIDCARD.equalsIgnoreCase(featureType)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Retrieving the Available PP VISA CARD list");}
				selectedProductAcctOrCards =   callInfo.getField(Field.PPVISACARDLIST) == null ? new ArrayList<String>() :(ArrayList<String>)callInfo.getField(Field.PPVISACARDLIST);
				//				selectedProductAcctOrCards = (ArrayList<String>)selectedProductAcctOrCards.clone();
				if(selectedProductAcctOrCards!=null){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The total number of  PP VISA CARD available is " + selectedProductAcctOrCards.size());}
				}
				selectedProductPhrase = DynaPhraseConstants.Account_Selection_1009;
			}else if(util.isCodePresentInTheConfigurationList(selectedProductType, cardMasterBrandTypeList) && 
					Constants.FEATURE_TYPE_PREPAIDCARD.equalsIgnoreCase(featureType)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Retrieving the Available  PP MASTER CARD list");}
				selectedProductAcctOrCards =   callInfo.getField(Field.PPMASTERCARDLIST) == null ? new ArrayList<String>() :(ArrayList<String>)callInfo.getField(Field.PPMASTERCARDLIST);
				//				selectedProductAcctOrCards = (ArrayList<String>)selectedProductAcctOrCards.clone();
				if(selectedProductAcctOrCards!=null){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The total number of  PP MASTER CARD available is " + selectedProductAcctOrCards.size());}
				}
				selectedProductPhrase = DynaPhraseConstants.Account_Selection_1010;
			}else if(util.isCodePresentInTheConfigurationList(selectedProductType, cardAmexBrandTypeList) && 
					Constants.FEATURE_TYPE_PREPAIDCARD.equalsIgnoreCase(featureType)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Retrieving the Available PP AMEX CARD list");}
				selectedProductAcctOrCards = callInfo.getField(Field.PPAMEXCARDLIST) == null ? new ArrayList<String>() : (ArrayList<String>)callInfo.getField(Field.PPAMEXCARDLIST);
				//				selectedProductAcctOrCards = (ArrayList<String>)selectedProductAcctOrCards.clone();
				if(selectedProductAcctOrCards!=null){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The total number of Available PP AMEX CARD is " + selectedProductAcctOrCards.size());}
				}
				selectedProductPhrase = DynaPhraseConstants.Account_Selection_1006;
			}


			//			HashMap<String, CardAcctDtl> carddetailMap= new HashMap<String, CardAcctDtl>();
			//			CardAcctDtl cardAcctDtl = new CardAcctDtl();
			//			if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())){
			//				if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCardDetailMap())){
			//					carddetailMap = callInfo.getCallerIdentification_HostRes().getCardDetailMap();
			//				}
			//			}

			HashMap<String, AcctInfo> accountdetailMap= new HashMap<String, AcctInfo>();
			AcctInfo acctInfo = new AcctInfo();
			if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())){
				if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getAccountDetailMap())){
					accountdetailMap = callInfo.getCallerIdentification_HostRes().getAccountDetailMap();
				}
			}

			String currType = Constants.EMPTY_STRING;
			for(int count = Constants.GL_ZERO; count < selectedProductAcctOrCards.size();count++){
				accountOrCardNumber = selectedProductAcctOrCards.get(count);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The "+count+" Account Or Card number ending with " + util.getSubstring(accountOrCardNumber, Constants.GL_FOUR));}

				if(Constants.FEATURE_TYPE_BANK.equalsIgnoreCase(featureType)){
					acctInfo = accountdetailMap.get(accountOrCardNumber);
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved the Account Info object from the bean" + acctInfo);}

					if(!util.isNullOrEmpty(acctInfo)){
						currType = acctInfo.getAcctCurr();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Curr of the acct number ending with " + util.getSubstring(accountOrCardNumber, Constants.GL_FOUR) +" is "+ currType);}

						if(!eligibleCurrType.contains(currType)){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the account number ending with" + util.getSubstring(accountOrCardNumber, Constants.GL_FOUR) +"to filter Account number list");}
							filteringProductAcctOrCards.add(accountOrCardNumber);
						}
					}
				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "There is no currency filteration for Cards");}
				}

			}
			//TODO need to be hide this below logging lines
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Accounts to be filtered are " + filteringProductAcctOrCards );}

			//To avoid removing of accts or cards from the reference object
			ArrayList<String> selectedProductAcctOrCardsClone = new ArrayList<String>(); 
			selectedProductAcctOrCardsClone.addAll(selectedProductAcctOrCards);

			selectedProductAcctOrCardsClone.removeAll(filteringProductAcctOrCards);

			//TODO need to be hide this below logging lines
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Eligle Account numbers retrieved are :" + selectedProductAcctOrCardsClone);}


			/***
			 * Handled by Vinoth on 05Mar2013, if there is no eligible account then we should throw service exception
			 */
			if (selectedProductAcctOrCardsClone.size() == Constants.GL_ZERO){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "There is no Eligle Account numbers retrieved :");}
				throw new ServiceException("There is no eligible account");
			}
			// END 

			//Not required here we have handled this under product menu option
//			/**
//			 * Rule engine update
//			 */
//			ICERuleParam ruleParamObj = (ICERuleParam)callInfo.getICERuleParam();
//
//			if(util.isNullOrEmpty(ruleParamObj)){
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "***********Rule Engine Object is null or empty*************" + ruleParamObj);}
//			}
//
//			if(!util.isNullOrEmpty(selectedProductAcctOrCardsClone)){
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Total No Of Eligible Accounts or Cards in the Rule Engine " + selectedProductAcctOrCardsClone);}
//				ruleParamObj.setIVRParam(Constants.RULE_ENGINE_NOOFACCTORCARD, selectedProductAcctOrCardsClone.size() + Constants.EMPTY);
//				ruleParamObj.updateIVRFields();
//			}
//
//			//END Rule Engine Updation



			ArrayList<String> availableAcctOrCardNo = new ArrayList<>();

			for(int count=Constants.GL_ZERO;count<selectedProductAcctOrCardsClone.size();count++){

				accountOrCardNumber = selectedProductAcctOrCardsClone.get(count);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The "+count+" Account Or Card number ending with " + util.getSubstring(accountOrCardNumber, Constants.GL_FOUR));}
				noOfEligibleAccounts++;
				temp_Str = util.getSubstring(accountOrCardNumber, lastNdigit);
				availableAcctOrCardNo.add(accountOrCardNumber);
				dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1006);
				dynamicValueArray.add(selectedProductPhrase);
				dynamicValueArray.add(DynaPhraseConstants.PHRASE_ENDING_WITH);
				dynamicValueArray.add(temp_Str);

				if(util.isNullOrEmpty(grammar)){
					grammar = accountOrCardNumber;
				}else if(count < int_moreCount){
					grammar = grammar + Constants.COMMA + accountOrCardNumber;
				}else if(count == int_moreCount){
					grammar = grammar + Constants.COMMA + Constants.MORE;
				}
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the grammar value" + grammar);}

				/**
				 * Following has been added for next method "Account number entry constrain list of DM"
				 * on 26-Mar-2014
				 */

				if(util.isNullOrEmpty(constrainList)){
					constrainList = accountOrCardNumber;
				}else{
					constrainList = constrainList + Constants.COMMA + accountOrCardNumber;
				}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the Constrain list value for the next method account number entry ");}

				//END
			}
			
			String strAccountList = Constants.EMPTY_STRING;
			
			
			/**
			 * Added to set the account list or dynamic list for OD application.
			 */
			if(!util.isNullOrEmpty(availableAcctOrCardNo)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "settting the available acct or card no is ");}
				for(int i=Constants.GL_ZERO; i < availableAcctOrCardNo.size(); i++){
					if(util.isNullOrEmpty(strAccountList)){
						strAccountList = availableAcctOrCardNo.get(i);
					}else{
						strAccountList = strAccountList + Constants.COMMA + availableAcctOrCardNo.get(i);
					}
				}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Account list or dynamic list is created and setted ");}
			}
			
			callInfo.setField(Field.ACCOUNTLIST, strAccountList);
			//End Vinoth.
			
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}

//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed Constarined list for next method is" + constrainList);}
			callInfo.setField(Field.ACCTENTRYCONSTRAINLIST, constrainList);


			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("ACCOUNTCARD_SELECTION");
			//String anncID = AnncIDMap.getAnncID("Account_Balance_Message");
			String featureID = FeatureIDMap.getFeatureID("Account_Or_Card_Selection");
			String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+menuID;

			/**
			 * Following are the modification done for configuring the more option of menus
			 */
			combinedKey = combinedKey + Constants.UNDERSCORE + int_moreCount+Constants.UNDERSCORE + featureType;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Combined key along with more count option is "+ combinedKey);}
			//END - Vinoth
			
			
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
			if(noOfEligibleAccounts>int_moreCount){
				totalPrompt = Constants.GL_FIVE * int_moreCount;
				totalPrompt = totalPrompt + Constants.GL_TWO;
			}
			else{
				totalPrompt = Constants.GL_FIVE * noOfEligibleAccounts;
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}

			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);
			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}

			/**
			 * Sample phrase formed is 
			 * 
			 * 
			 * "Account_Selection_1006.wav*Account_Selection_1007.wav*CP_1002.wav*SS:1111:digits*CP_1019.wav*SILENCE.wav"
				+ "*Account_Selection_1006.wav*Account_Selection_1007.wav*CP_1002.wav*SS:2222:digits*CP_1020.wav*SILENCE.wav"
				+ "*Account_Selection_1006.wav*Account_Selection_1007.wav*CP_1002.wav*SS:3333:digits*CP_1021.wav*SILENCE.wav"
				+ "*Account_Selection_1006.wav*Account_Selection_1007.wav*CP_1002.wav*SS:4444:digits*CP_1022.wav*SILENCE.wav"
				+ "*Account_Selection_1006.wav*Account_Selection_1007.wav*CP_1002.wav*SS:5555:digits*CP_1023.wav*SILENCE.wav"
				+ "*Account_Selection_1012.wav*CP_1024.wav";

			 */
			int temp_MoreCount = Constants.GL_ZERO;
			if(!util.isNullOrEmpty(finalResult)){
				temp_MoreCount = int_moreCount + 1;
				if(finalResult.contains(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION).trim())){
					finalResult = finalResult.replaceAll(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION),(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION)+Constants.PIPE));
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The Final Result string is after apending pipe seperator is "+finalResult);}
				}
			}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: AccountOrCardSelectionImpl.getAccountPhrases()");}


		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at AccountOrCardSelectionImpl.getAccountPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public String getAccountTypePhrases(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: AccountOrCardSelectionImpl.getAccountTypePhrases()");}
		String str_GetMessage, finalResult;
		getConfigurationParam(callInfo);
		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalObject = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalObject)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Feature data object is null");}
				throw new ServiceException("ICE Feature Data object is null");
			}

			String customerSegment = util.isNullOrEmpty(callInfo.getField(Field.CUST_SEGMENT_TYPE)) ? Constants.CUST_SEGMENT_RETAIL : (String)callInfo.getField(Field.CUST_SEGMENT_TYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured Customer segment type is "+ customerSegment);}
			
			String featureName =  callInfo.getField(Field.FEATURENAME) == null ? Constants.EMPTY :(String)callInfo.getField(Field.FEATURENAME);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The feature name is "+ featureName);}

			String cardVisaBrandTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_CARDBRANDTYPE_VISA+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured credit card VISA brand type is "+ cardVisaBrandTypeList);}

			String cardMasterBrandTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_CARDBRANDTYPE_MASTER+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured credit card Master brand type is "+ cardMasterBrandTypeList);}

			String cardAmexBrandTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_CARDBRANDTYPE_AMEX+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured credit card Amex brand type is "+ cardAmexBrandTypeList);}


			String currentAccoutTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_ACCOUNTTYPE_CURRENT+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured current account type list is "+ currentAccoutTypeList);}


			String savingAccountTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_ACCOUNTTYPE_SAVING+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured savings account type list is "+ savingAccountTypeList);}


			String loanAccountTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_ACCOUNTTYPE_LOAN+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured loan account type list is "+ loanAccountTypeList);}

			String depositAccountTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_ACCOUNTTYPE_DEPOSIT+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured deposit account type list is "+ depositAccountTypeList);}



			ArrayList<String> eligibleProductType = null;

			boolean calledForDestAcctSelection = false;

			if(util.isNullOrEmpty(callInfo.getField(Field.FROMDESTINATION))){
				callInfo.setField(Field.FROMDESTINATION,false);
			}else{
				calledForDestAcctSelection = (boolean)callInfo.getField(Field.FROMDESTINATION);
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "This method called for Source acct selection / for Destination account selection ? "+ calledForDestAcctSelection);}

			if(calledForDestAcctSelection){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting destinaltion eligible accounts types");}
				eligibleProductType = (ArrayList<String>) callInfo.getField(Field.ELIGIBLEDESTACCTCARDTYPE);
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting destinaltion eligible accounts types");}
				eligibleProductType = (ArrayList<String>) callInfo.getField(Field.ELIGIBLEACCTCARDTYPE);
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Eligle product type list retrieved is :" + eligibleProductType);}

			String moreCount = (String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_Str = Constants.EMPTY_STRING;

			int noOfProductTypes = Constants.GL_ZERO;

			String productType = Constants.EMPTY_STRING;
			String featureType = (String)callInfo.getField(Field.FEATURETYPE);

			ArrayList<String> tempList = null;
			Boolean flagFDACCTLIST=false,flagCURRENTACCTLIST=false,flagLOANACCTLIST=false,flagSAVINGSACCTLIST=false,flagDRVISACARDLIST=false,flagDRMASTERCARDLIST=false;
			Boolean	flagDRAMEXCARDLIST=false,flagCRVISACARDLIST=false,flagCRMASTERCARDLIST=false,flagCRAMEXCARDLIST=false,flagPPVISACARDLIST=false,flagPPMASTERCARDLIST=false,flagPPAMEXCARDLIST=false;
			for(int count=Constants.GL_ZERO;count<eligibleProductType.size();count++){
				productType = eligibleProductType.get(count);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The "+count+" Product type is " + productType);}

				if(util.isCodePresentInTheConfigurationList(productType, depositAccountTypeList)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+ count + "product is of eligible product object is" + productType + "Belongs to Deposit Acct Type");}
					tempList =  callInfo.getField(Field.FDACCTLIST) == null ? new ArrayList<String>(): (ArrayList<String>)callInfo.getField(Field.FDACCTLIST);
					tempList =(ArrayList<String>)tempList.clone();
					if(true == (boolean)callInfo.getField(Field.FROMDESTINATION))
					{
						tempList.remove((String)(callInfo.getField(Field.SRCNO)));
					}
					if(flagFDACCTLIST==false){
						if(tempList!=null && tempList.size()>Constants.GL_ZERO){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"There is a linked Deposit account for this customer");}
							dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1006);
							dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1015);
							noOfProductTypes++;
							temp_Str = Constants.HOST_DEPOSIT_ACCOUNTID_TYPE;
						}
						flagFDACCTLIST=true;
					}
				}else if(util.isCodePresentInTheConfigurationList(productType, currentAccoutTypeList)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+ count + "product is of eligible product object is" + productType + "Belongs to Credit / Current Acct Type");}
					tempList = callInfo.getField(Field.CURRENTACCTLIST) == null ? new ArrayList<String>(): (ArrayList<String>)callInfo.getField(Field.CURRENTACCTLIST);
					tempList =(ArrayList<String>)tempList.clone();
					if(true == (boolean)callInfo.getField(Field.FROMDESTINATION))
					{
						tempList.remove((String)(callInfo.getField(Field.SRCNO)));
					}
					if(flagCURRENTACCTLIST==false){
						if(tempList!=null && tempList.size()>Constants.GL_ZERO){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"There is a linked CREDIT account for this customer");}
							dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1006);
							dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1008);
							noOfProductTypes++;
							temp_Str = Constants.HOST_CREDIT_ACCOUNTID_TYPE;
						}
						flagCURRENTACCTLIST=true;
					}
				}else if(util.isCodePresentInTheConfigurationList(productType, loanAccountTypeList)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+ count + "product is of eligible product object is" + productType + "Belongs to LOAN Acct Type");}
					tempList = callInfo.getField(Field.LOANACCTLIST) == null ? new ArrayList<String>():(ArrayList<String>)callInfo.getField(Field.LOANACCTLIST);
					tempList =(ArrayList<String>)tempList.clone();
					if(true == (boolean)callInfo.getField(Field.FROMDESTINATION))
					{
						tempList.remove((String)(callInfo.getField(Field.SRCNO)));
					}
					if(flagLOANACCTLIST==false){
						if(tempList!=null && tempList.size()>Constants.GL_ZERO){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"There is a linked LOAN account for this customer");}
							dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1006);
							dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1014);
							noOfProductTypes++;
							temp_Str = Constants.HOST_LOAN_ACCOUNTID_TYPE;
						}
						flagLOANACCTLIST=true;
					}
				}else if(util.isCodePresentInTheConfigurationList(productType, savingAccountTypeList)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+ count + "product is of eligible product object is" + productType + "Belongs to SAVINGS Acct Type");}
					tempList = callInfo.getField(Field.SAVINGSACCTLIST) == null ? new ArrayList<String>():(ArrayList<String>)callInfo.getField(Field.SAVINGSACCTLIST);
					tempList =(ArrayList<String>)tempList.clone();
					if(true == (boolean)callInfo.getField(Field.FROMDESTINATION))
					{
						tempList.remove((String)(callInfo.getField(Field.SRCNO)));
					}

					if(flagSAVINGSACCTLIST==false){
						if(tempList!=null && tempList.size()>Constants.GL_ZERO){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"There is a linked SAVINGS account for this customer");}
							dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1006);
							dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1007);
							noOfProductTypes++;
							temp_Str = Constants.HOST_SAVINGS_ACCOUNTID_TYPE;
						}
						flagSAVINGSACCTLIST=true;
					}
				}else if(util.isCodePresentInTheConfigurationList(productType, cardVisaBrandTypeList) && Constants.FEATURE_TYPE_BANK.equalsIgnoreCase(featureType)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+ count + "product is of eligible product object is" + productType + "Belongs to DR VISA Type");}
					tempList =  callInfo.getField(Field.DRVISACARDLIST) == null ? new ArrayList<String>():(ArrayList<String>)callInfo.getField(Field.DRVISACARDLIST);

					/**
					 * Handled for Debit card activation
					 */
					if(Constants.FEATURENAME_CARDACTIVATION.equalsIgnoreCase(featureName)){
						tempList =  callInfo.getField(Field.DRVISACARDLISTINACTIVE) == null ? new ArrayList<String>():(ArrayList<String>)callInfo.getField(Field.DRVISACARDLISTINACTIVE);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Overriding with the DR Visa Inactive card list size is " +  tempList.size());}
					}
					//END - Vinoth.

					tempList =(ArrayList<String>)tempList.clone();
					if(true == (boolean)callInfo.getField(Field.FROMDESTINATION))
					{
						tempList.remove((String)(callInfo.getField(Field.SRCNO)));
					}

					if(flagDRVISACARDLIST==false){
						if(tempList!=null && tempList.size()>Constants.GL_ZERO){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"There is a linked DR VISA card for this customer");}
							dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1006);
							dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1009);
							noOfProductTypes++;
							temp_Str = Constants.HOST_BRAND_VISA_CARD;
						}
						flagDRVISACARDLIST=true;
					}
				}else if(util.isCodePresentInTheConfigurationList(productType, cardMasterBrandTypeList) && Constants.FEATURE_TYPE_BANK.equalsIgnoreCase(featureType)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+ count + "product is of eligible product object is" + productType + "Belongs to DR Master card Type");}
					tempList =  callInfo.getField(Field.DRMASTERCARDLIST) == null ? new ArrayList<String>():(ArrayList<String>)callInfo.getField(Field.DRMASTERCARDLIST);
					/**
					 * Handled for Debit card activation
					 */
					if(Constants.FEATURENAME_CARDACTIVATION.equalsIgnoreCase(featureName)){
						tempList =  callInfo.getField(Field.DRMASTERCARDLISTINACTIVE) == null ? new ArrayList<String>():(ArrayList<String>)callInfo.getField(Field.DRMASTERCARDLISTINACTIVE);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Overriding with the DR Master Inactive card list " +  tempList.size());}
					}
					//END - Vinoth.

					tempList =(ArrayList<String>)tempList.clone();
					if(flagDRMASTERCARDLIST==false){
						if(tempList!=null && tempList.size()>Constants.GL_ZERO){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"There is a linked DR MASTER CARD for this customer");}
							dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1006);
							dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1010);
							noOfProductTypes++;
							temp_Str = Constants.HOST_BRAND_MASTER_CARD;
						}
						flagDRMASTERCARDLIST=true;
					}
				}else if(util.isCodePresentInTheConfigurationList(productType, cardAmexBrandTypeList) && Constants.FEATURE_TYPE_BANK.equalsIgnoreCase(featureType)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+ count + "product is of eligible product object is" + productType + "Belongs to DR AMEX card Type");}
					tempList = callInfo.getField(Field.DRAMEXCARDLIST) == null ? new ArrayList<String>(): (ArrayList<String>)callInfo.getField(Field.DRAMEXCARDLIST);

					/**
					 * Handled for Debit card activation
					 */
					if(Constants.FEATURENAME_CARDACTIVATION.equalsIgnoreCase(featureName)){
						tempList = callInfo.getField(Field.DRAMEXCARDLISTINACTIVE) == null ? new ArrayList<String>(): (ArrayList<String>)callInfo.getField(Field.DRAMEXCARDLISTINACTIVE);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Overriding with the DR Amex Inactive card list size is " +  tempList.size());}
					}
					//END - Vinoth.

					tempList =(ArrayList<String>)tempList.clone();

					if(flagDRAMEXCARDLIST==false){
						if(tempList!=null && tempList.size()>Constants.GL_ZERO){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"There is a linked DR AMEX CARD for this customer");}
							dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1006);
							//dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1015);
							//TODO Phrase id for AMEX need to be provided
							noOfProductTypes++;
							temp_Str = Constants.HOST_BRAND_AMEX_CARD;
						}
						flagDRAMEXCARDLIST=true;
					}
				}else if(util.isCodePresentInTheConfigurationList(productType, cardVisaBrandTypeList) && 
						(Constants.FEATURE_TYPE_CARD.equalsIgnoreCase(featureType))){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+ count + "product is of eligible product object is" + productType + "Belongs to CR VISA card Type");}
					tempList =  callInfo.getField(Field.CRVISACARDLIST) == null ? new ArrayList<String>(): (ArrayList<String>)callInfo.getField(Field.CRVISACARDLIST);
					tempList =(ArrayList<String>)tempList.clone();
					if(flagCRVISACARDLIST==false){
						if(tempList!=null && tempList.size()>Constants.GL_ZERO){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"There is a linked CR VISA card for this customer");}
							dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1006);
							dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1009);
							noOfProductTypes++;
							temp_Str = Constants.HOST_BRAND_VISA_CARD;
						}
						flagCRVISACARDLIST=true;
					}
				}else if(util.isCodePresentInTheConfigurationList(productType, cardMasterBrandTypeList) && 
						(Constants.FEATURE_TYPE_CARD.equalsIgnoreCase(featureType))){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+ count + "product is of eligible product object is" + productType + "Belongs to CR MasterCard Type");}
					tempList = callInfo.getField(Field.CRMASTERCARDLIST) == null ? new ArrayList<String>(): (ArrayList<String>)callInfo.getField(Field.CRMASTERCARDLIST);
					tempList =(ArrayList<String>)tempList.clone();

					if(flagCRMASTERCARDLIST==false){
						if(tempList!=null && tempList.size()>Constants.GL_ZERO){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"There is a linked CR MASTER CARD for this customer");}
							dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1006);
							dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1010);
							noOfProductTypes++;
							temp_Str = Constants.HOST_BRAND_MASTER_CARD;
						}
						flagCRMASTERCARDLIST=true;
					}
				}else if(util.isCodePresentInTheConfigurationList(productType, cardAmexBrandTypeList)&& 
						(Constants.FEATURE_TYPE_CARD.equalsIgnoreCase(featureType))){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+ count + "product is of eligible product object is" + productType + "Belongs to CR AMEX Card Type");}
					tempList = callInfo.getField(Field.CRAMEXCARDLIST) == null ? new ArrayList<String>(): (ArrayList<String>)callInfo.getField(Field.CRAMEXCARDLIST);
					tempList =(ArrayList<String>)tempList.clone();

					if(flagCRAMEXCARDLIST==false){
						if(tempList!=null && tempList.size()>Constants.GL_ZERO){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"There is a linked  CR AMEX CARD  for this customer");}
							dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1006);
							//dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1015);
							//Phrase id for AMEX card type need to be given
							noOfProductTypes++;
							temp_Str = Constants.HOST_BRAND_AMEX_CARD;
						}
						flagCRAMEXCARDLIST=true;
					}
				}
				//Prepaid Card
				else if(util.isCodePresentInTheConfigurationList(productType, cardVisaBrandTypeList) && 
						(Constants.FEATURE_TYPE_PREPAIDCARD.equalsIgnoreCase(featureType))){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+ count + "product is of eligible product object is" + productType + "Belongs to PP VISA card Type");}
					tempList =  callInfo.getField(Field.PPVISACARDLIST) == null ? new ArrayList<String>(): (ArrayList<String>)callInfo.getField(Field.PPVISACARDLIST);
					tempList =(ArrayList<String>)tempList.clone();
					if(flagPPVISACARDLIST==false){
						if(tempList!=null && tempList.size()>Constants.GL_ZERO){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"There is a linked PP VISA card for this customer");}
							dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1006);
							dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1009);
							noOfProductTypes++;
							temp_Str = Constants.HOST_BRAND_VISA_CARD;
						}
						flagPPVISACARDLIST=true;
					}
				}else if(util.isCodePresentInTheConfigurationList(productType, cardMasterBrandTypeList) && 
						(Constants.FEATURE_TYPE_PREPAIDCARD.equalsIgnoreCase(featureType))){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+ count + "product is of eligible product object is" + productType + "Belongs to PP MasterCard Type");}
					tempList = callInfo.getField(Field.PPMASTERCARDLIST) == null ? new ArrayList<String>(): (ArrayList<String>)callInfo.getField(Field.PPMASTERCARDLIST);
					tempList =(ArrayList<String>)tempList.clone();

					if(flagPPMASTERCARDLIST==false){
						if(tempList!=null && tempList.size()>Constants.GL_ZERO){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"There is a linked PP MASTER CARD for this customer");}
							dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1006);
							dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1010);
							noOfProductTypes++;
							temp_Str = Constants.HOST_BRAND_MASTER_CARD;
						}
						flagPPMASTERCARDLIST=true;
					}
				}else if(util.isCodePresentInTheConfigurationList(productType, cardAmexBrandTypeList)&& 
						(Constants.FEATURE_TYPE_PREPAIDCARD.equalsIgnoreCase(featureType))){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+ count + "product is of eligible product object is" + productType + "Belongs to PP AMEX Card Type");}
					tempList = callInfo.getField(Field.PPAMEXCARDLIST) == null ? new ArrayList<String>(): (ArrayList<String>)callInfo.getField(Field.PPAMEXCARDLIST);
					tempList =(ArrayList<String>)tempList.clone();

					if(flagPPAMEXCARDLIST==false){
						if(tempList!=null && tempList.size()>Constants.GL_ZERO){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"There is a linked  PP AMEX CARD  for this customer");}
							dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1006);
							//dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1015);
							//Phrase id for AMEX card type need to be given
							noOfProductTypes++;
							temp_Str = Constants.HOST_BRAND_AMEX_CARD;
						}
						flagPPMASTERCARDLIST=true;
					}
				}

				if(util.isNullOrEmpty(grammar)){
					grammar = temp_Str;
				}else{
					if(!util.isNullOrEmpty(temp_Str))
						grammar = grammar + Constants.COMMA + temp_Str;
				}
				temp_Str=Constants.EMPTY_STRING;
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the grammar value" + grammar);}
			}

			if(noOfProductTypes >= int_moreCount){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the more option count");}
				moreOption = true;
				callInfo.setField(Field.MOREOPTIONCOUNT, int_moreCount);
			}


			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("ACCOUNTCARD_SELECTION_PRODUCT_TYPE");
			//String anncID = AnncIDMap.getAnncID("Account_Balance_Message");
			String featureID = FeatureIDMap.getFeatureID("Account_Or_Card_Selection");
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
			if(noOfProductTypes>int_moreCount){
				totalPrompt = Constants.GL_THREE * int_moreCount;
				totalPrompt = totalPrompt + Constants.GL_TWO;
			}
			else{
				totalPrompt = Constants.GL_THREE * noOfProductTypes;
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}

			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);

			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}

			/**
			 * Sample Phrase formed
			 * "Account_Selection_1006.wav*Account_Selection_1007.wav*CP_1019.wav*SILENCE.wav"
			 *	+ "*Account_Selection_1006.wav*Account_Selection_1008.wav*CP_1020.wav*SILENCE.wav";
			 *
			 */
			int temp_MoreCount = Constants.GL_ZERO;
			if(!util.isNullOrEmpty(finalResult)){
				temp_MoreCount = int_moreCount + 1;
				if(finalResult.contains(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION).trim())){
					finalResult = finalResult.replaceAll(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION),(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION)+Constants.PIPE));
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The Final Result string is after apending pipe seperator is "+finalResult);}
				}
			}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: AccountOrCardSelectionImpl.getAccountTypePhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at AccountOrCardSelectionImpl.getAccountTypePhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public String getAccoutNumberEntryPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: AccountOrCardSelectionImpl.getAccoutNumberEntryPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			String moreCount = (String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;

			String totalCardOrAcctLength = Constants.EMPTY_STRING;

			String featureType = (String)callInfo.getField(Field.FEATURETYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Feature type is "+ featureType);}


			if(Constants.FEATURE_TYPE_BANK.equalsIgnoreCase(featureType)){

				totalCardOrAcctLength = (String)callInfo.getField(Field.BANKACCTLENGTH);
				totalCardOrAcctLength = util.isNullOrEmpty(totalCardOrAcctLength)?Constants.SIXTEEN:totalCardOrAcctLength;

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The total Bank account length is "+ totalCardOrAcctLength);}

				dynamicValueArray.add(totalCardOrAcctLength);
				dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1003);
			}else if(Constants.FEATURE_TYPE_PREPAIDCARD.equalsIgnoreCase(featureType)){

				totalCardOrAcctLength = (String)callInfo.getField(Field.CARDACCTLENGTH);
				totalCardOrAcctLength = util.isNullOrEmpty(totalCardOrAcctLength)?Constants.SIXTEEN:totalCardOrAcctLength;

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The total card length is "+ totalCardOrAcctLength);}

				dynamicValueArray.add(totalCardOrAcctLength);
				dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1016);
			}else{

				totalCardOrAcctLength = (String)callInfo.getField(Field.CARDACCTLENGTH);
				totalCardOrAcctLength = util.isNullOrEmpty(totalCardOrAcctLength)?Constants.SIXTEEN:totalCardOrAcctLength;

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The total card length is "+ totalCardOrAcctLength);}

				dynamicValueArray.add(totalCardOrAcctLength);
				dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1004);
			}


			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("ACCOUNT_SELECTION");
			//String anncID = AnncIDMap.getAnncID("Account_Balance_Message");
			String featureID = FeatureIDMap.getFeatureID("Account_Or_Card_Selection");
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

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}


			/**
			 * Adding the constrain list to the grammar , where the constrain list is being calculated from the Account number phrase announcements
			 * 
			 * Made on 26-Mar-2014
			 */
			String constrainList = Constants.EMPTY_STRING;
			constrainList = (String)callInfo.getField(Field.ACCTENTRYCONSTRAINLIST);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed Constarined list for next method is" + constrainList);}
			grammar = constrainList;
			//END

			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);

			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}

			/**
			 * The sample phrases would be like
			 * "Identification_1000.wav*SS:12:number*CP_1035.wav*Account_Selection_1003.wav*Account_Selection_1011.wav";
			 */

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: AccountOrCardSelectionImpl.getAccoutNumberEntryPhrases()");}


		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at AccountOrCardSelectionImpl.getAccoutNumberEntryPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;

	}

	@Override
	public String getContinueWithCurrentAcctPhrases(CallInfo callInfo)
			throws ServiceException {

		/**
		 * Sample output would be like follows
		 *
		 *"Account_Selection_1000.wav*Account_Selection_1003.wav*CP_1002.wav*SS:1234:digits*CP_1019.wav*SILENCE.wav*Account_Selection_1002.wav"
		 *	+ "Account_Selection_1003.wav*CP_1020.wav";
		 */

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: AccountOrCardSelectionImpl.getContinueWithCurrentAcctPhrases()");}
		String str_GetMessage, finalResult;
		getConfigurationParam(callInfo);
		try{

			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}

			String featurType = (String)callInfo.getField(Field.FEATURETYPE);
			String featurName = (String)callInfo.getField(Field.FEATURENAME);

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;

			String lastSelectedAcctNo = (String)callInfo.getField(Field.LASTSELECTEDACCTNO);
			String lastSelectedCardNo = (String)callInfo.getField(Field.LASTSELECTEDCARDNO);
			String lastSelectedFDAcctNo = (String)callInfo.getField(Field.LASTSELECTEDDEPOSITACCTNO);
			String lastSelectedLoanAcctNo = (String)callInfo.getField(Field.LASTSELECTEDLOANNO);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Last selected Account number is " + lastSelectedAcctNo);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Last selected card number is "+util.maskCardOrAccountNumber(lastSelectedCardNo));}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Last selected FD Account number is " + lastSelectedFDAcctNo);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Last selected Loan Account number is "+lastSelectedLoanAcctNo);}


			String lastNDigits = (String)callInfo.getField(Field.LastNDigits);

			int int_lastNDigits = util.isNullOrEmpty(lastNDigits)?Constants.GL_FOUR:Integer.parseInt(lastNDigits);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Last N digit annoucement length is "+int_lastNDigits);}


			String subString = Constants.EMPTY_STRING;
			if(Constants.FEATURE_TYPE_BANK.equalsIgnoreCase(featurType)){
				dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1003);

				if(util.isNullOrEmpty(lastSelectedAcctNo)){
					throw new ServiceException("there is no last selected account number");
				}else{
					subString = util.getSubstring(lastSelectedAcctNo, int_lastNDigits);
					dynamicValueArray.add(subString);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added last selected account number in the dynamic phrase list");}

				}
				//For other accounts phrases
				dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1003);
			}else if(Constants.FEATURE_TYPE_LOAN.equalsIgnoreCase(featurType)){
				dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1003);

				if(util.isNullOrEmpty(lastSelectedLoanAcctNo)){
					throw new ServiceException("there is no last selected account number");
				}else{
					subString = util.getSubstring(lastSelectedLoanAcctNo, int_lastNDigits);
					dynamicValueArray.add(subString);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added last selected account number in the dynamic phrase list");}

				}
				//For other accounts phrases
				dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1003);
			}else if(Constants.FEATURE_TYPE_FIXEDDEPOSIT.equalsIgnoreCase(featurType)){
				dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1003);

				if(util.isNullOrEmpty(lastSelectedFDAcctNo)){
					throw new ServiceException("there is no last selected account number");
				}else{
					subString = util.getSubstring(lastSelectedFDAcctNo, int_lastNDigits);
					dynamicValueArray.add(subString);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added last selected account number in the dynamic phrase list");}

				}
				//For other accounts phrases
				dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1003);
			}else if(Constants.FEATURE_TYPE_PREPAIDCARD.equalsIgnoreCase(featurType)){
				dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1016);

				if(util.isNullOrEmpty(lastSelectedCardNo)){
					throw new ServiceException("there is no last selected prepaid card number");
				}else{
					subString = util.getSubstring(lastSelectedCardNo, int_lastNDigits);
					dynamicValueArray.add(subString);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added last selected prepaid card number in the dynamic phrase list");}
				}

				//For other credit card phrases
				dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1016);
			}else{
				dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1004);

				if(util.isNullOrEmpty(lastSelectedCardNo)){
					throw new ServiceException("there is no last selected card number");
				}else{
					subString = util.getSubstring(lastSelectedCardNo, int_lastNDigits);
					dynamicValueArray.add(subString);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added last selected card number in the dynamic phrase list");}
				}

				//For other credit card phrases
				dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1004);
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("ACCOUNTCARD_SELECTION_PREVIOUS_FEATURE");
			//String anncID = AnncIDMap.getAnncID("Account_Balance_Message");
			String featureID = FeatureIDMap.getFeatureID("Account_Or_Card_Selection");
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

			grammar = util.getGrammar(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The Grammar retrieved is "+grammar);}

			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);

			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}

			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: AccountOrCardSelectionImpl.getContinueWithCurrentAcctPhrases()");}

		}catch(ServiceException e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at AccountOrCardSelectionImpl.getContinueWithCurrentAcctPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public int getNumberOfAccountOrCreditCards(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: AccountOrCardSelectionImpl.getNumberOfAccountOrCreditCards()");}
		int totalNoOfAccountsOrCards = Constants.GL_ZERO;
		getConfigurationParam(callInfo);
		int totalCardsOrAccts = Constants.GL_ZERO;
		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalObject = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalObject)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Feature data object is null");}
				throw new ServiceException("ivr_ICEGlobalObject object is null");
			}

			String customerSegment = util.isNullOrEmpty(callInfo.getField(Field.CUST_SEGMENT_TYPE)) ? Constants.CUST_SEGMENT_RETAIL : (String)callInfo.getField(Field.CUST_SEGMENT_TYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured Customer segment type is "+ customerSegment);}
			
			String cardVisaBrandTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_CARDBRANDTYPE_VISA+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured credit card VISA brand type is "+ cardVisaBrandTypeList);}

			String cardMasterBrandTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_CARDBRANDTYPE_MASTER+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured credit card Master brand type is "+ cardMasterBrandTypeList);}

			String cardAmexBrandTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_CARDBRANDTYPE_AMEX+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured credit card Amex brand type is "+ cardAmexBrandTypeList);}

			String currentAccoutTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_ACCOUNTTYPE_CURRENT+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured current account type list is "+ currentAccoutTypeList);}

			String savingAccountTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_ACCOUNTTYPE_SAVING+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured savings account type list is "+ savingAccountTypeList);}

			String loanAccountTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_ACCOUNTTYPE_LOAN+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured loan account type list is "+ loanAccountTypeList);}

			String depositAccountTypeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_ACCOUNTTYPE_DEPOSIT+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured deposit account type list is "+ depositAccountTypeList);}



			ArrayList<String> eligibleProductType = null;

			boolean calledFromDestAcctSel = false;

			if(util.isNullOrEmpty(callInfo.getField(Field.FROMDESTINATION))){
				callInfo.setField(Field.FROMDESTINATION,false);
			}else{
				calledFromDestAcctSel = (boolean)callInfo.getField(Field.FROMDESTINATION);
			}

			ArrayList<String> eligibleCurrType = null;
			eligibleCurrType = (ArrayList<String>) callInfo.getField(Field.ELIGIBLEACCTCARDCURRTYPE);
			HashMap<String, String> acctCurrMap = (HashMap<String, String>) callInfo.getField(Field.ACCTCURRMAP);

			if(calledFromDestAcctSel){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected Destination account selection type");}
				eligibleProductType = (ArrayList<String>) callInfo.getField(Field.ELIGIBLEDESTACCTCARDTYPE);

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Overriding the Currency type  with the destination type");}
				eligibleCurrType = (ArrayList<String>) callInfo.getField(Field.ELIGIBLEDESTACCTCARDCURRTYPE);

			}else{
				eligibleProductType = (ArrayList<String>) callInfo.getField(Field.ELIGIBLEACCTCARDTYPE);

			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Eligle product type list retrieved is :" + eligibleProductType);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Eligible currency type is "+ eligibleCurrType);}



			String productType = Constants.EMPTY_STRING;
			String featureType = (String)callInfo.getField(Field.FEATURETYPE);
			String featureName = (String)callInfo.getField(Field.FEATURENAME);

			int noOfDepositAccts = 0, noOfCreditAccts = 0, noOfLoanAccts = 0, noOfSavingsAccts = 0, noOfCRVisaCards = 0, noOfPPVisaCards = 0, noOfDRVisaCards = 0, noOfCRMasterCards = 0, noOfPPMasterCards = 0, noOfDRMasterCards = 0, noOfCRAmEx = 0, noOfPPAmEx = 0, noOfDRAmEx = 0;
			String tempProductCount = Constants.EMPTY_STRING;


			ArrayList<String> availableAcctOrCars = null;
			ArrayList<String> filteredAcctOrCars = new ArrayList<String>();
			String str_Curr = Constants.EMPTY_STRING;

			for(int count=Constants.GL_ZERO;count<eligibleProductType.size();count++){
				productType = eligibleProductType.get(count);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The "+count+" Product type is " + productType);}
				filteredAcctOrCars.clear();

				if(util.isCodePresentInTheConfigurationList(productType, depositAccountTypeList)){

					tempProductCount = Constants.EMPTY_STRING+callInfo.getField(Field.NO_OF_FD_ACCTS);
					noOfDepositAccts = util.isNullOrEmpty(tempProductCount)?Constants.GL_ZERO:Integer.parseInt(tempProductCount);
					availableAcctOrCars =  callInfo.getField(Field.FDACCTLIST)==null? new ArrayList<String>() :(ArrayList<String>)callInfo.getField(Field.FDACCTLIST);
					availableAcctOrCars =(ArrayList<String>)availableAcctOrCars.clone();
					if(!util.isNullOrEmpty(availableAcctOrCars) && !util.isNullOrEmpty(acctCurrMap)){
						for(int i=0; i<availableAcctOrCars.size();i++){
							str_Curr = acctCurrMap.get(availableAcctOrCars.get(i));
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The currency for the account ending with "+util.getSubstring(availableAcctOrCars.get(i), Constants.GL_FOUR)+"  is " + str_Curr);}

							if(!eligibleCurrType.contains(str_Curr)){
								noOfDepositAccts--;
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Currency type "+ str_Curr+" is not eligible so neglecting that corresponding account");}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Decrement the count of total FD acct number by 1");}
								filteredAcctOrCars.add(availableAcctOrCars.get(i));
							}
						}
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total number of Deposit Accounts are "+ noOfDepositAccts);}

					availableAcctOrCars.removeAll(filteredAcctOrCars);
					if(noOfDepositAccts == Constants.GL_ONE){
						callInfo.setField(Field.SELECTEDNO, availableAcctOrCars.get(Constants.GL_ZERO));
						callInfo.setField(Field.SELECTEDTYPE, Constants.HOST_DEPOSIT_ACCOUNTID_TYPE);
					}


				}else if(util.isCodePresentInTheConfigurationList(productType, currentAccoutTypeList)){
					tempProductCount = Constants.EMPTY_STRING+callInfo.getField(Field.NO_OF_CURRENT_ACCTS);
					noOfCreditAccts = util.isNullOrEmpty(tempProductCount)?Constants.GL_ZERO:Integer.parseInt(tempProductCount);

					availableAcctOrCars = callInfo.getField(Field.CURRENTACCTLIST)==null? new ArrayList<String>() :(ArrayList<String>)callInfo.getField(Field.CURRENTACCTLIST);
					availableAcctOrCars =(ArrayList<String>)availableAcctOrCars.clone();
					if(!util.isNullOrEmpty(availableAcctOrCars) && !util.isNullOrEmpty(acctCurrMap)){
						for(int i=0; i<availableAcctOrCars.size();i++){
							str_Curr = acctCurrMap.get(availableAcctOrCars.get(i));
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The currency for the account ending with "+util.getSubstring(availableAcctOrCars.get(i), Constants.GL_FOUR)+"  is " + str_Curr);}

							if(!eligibleCurrType.contains(str_Curr)){
								noOfCreditAccts--;
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Currency type "+ str_Curr+" is not eligible so neglecting that corresponding account");}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Decrement the count of total Current acct number by 1");}
								filteredAcctOrCars.add(availableAcctOrCars.get(i));
							}
						}
					}

					availableAcctOrCars.removeAll(filteredAcctOrCars);
					if(noOfCreditAccts == Constants.GL_ONE){
						callInfo.setField(Field.SELECTEDNO, availableAcctOrCars.get(Constants.GL_ZERO));
						callInfo.setField(Field.SELECTEDTYPE, Constants.HOST_CREDIT_ACCOUNTID_TYPE);
					}


					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total number of Credit Accounts are "+ noOfCreditAccts);}	
				}else if(util.isCodePresentInTheConfigurationList(productType, loanAccountTypeList)){
					tempProductCount = Constants.EMPTY_STRING+callInfo.getField(Field.NO_OF_LOAN_ACCTS);
					noOfLoanAccts = util.isNullOrEmpty(tempProductCount)?Constants.GL_ZERO:Integer.parseInt(tempProductCount);

					availableAcctOrCars = callInfo.getField(Field.LOANACCTLIST)==null? new ArrayList<String>() :(ArrayList<String>)callInfo.getField(Field.LOANACCTLIST);
					availableAcctOrCars =(ArrayList<String>)availableAcctOrCars.clone();
					if(!util.isNullOrEmpty(availableAcctOrCars) && !util.isNullOrEmpty(acctCurrMap)){
						for(int i=0; i<availableAcctOrCars.size();i++){
							str_Curr = acctCurrMap.get(availableAcctOrCars.get(i));
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The currency for the account ending with "+util.getSubstring(availableAcctOrCars.get(i), Constants.GL_FOUR)+"  is " + str_Curr);}

							if(!eligibleCurrType.contains(str_Curr)){
								noOfLoanAccts--;
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Currency type "+ str_Curr+" is not eligible so neglecting that corresponding account");}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Decrement the count of total Loan acct number by 1");}
								filteredAcctOrCars.add(availableAcctOrCars.get(i));
							}
						}
					}

					availableAcctOrCars.removeAll(filteredAcctOrCars);
					if(noOfLoanAccts == Constants.GL_ONE){
						callInfo.setField(Field.SELECTEDNO, availableAcctOrCars.get(Constants.GL_ZERO));
						callInfo.setField(Field.SELECTEDTYPE, Constants.HOST_LOAN_ACCOUNTID_TYPE);
					}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total number of Loan Accounts are "+ noOfLoanAccts);}
				}else if(util.isCodePresentInTheConfigurationList(productType, savingAccountTypeList)){
					availableAcctOrCars = new ArrayList<String>();
					tempProductCount =Constants.EMPTY_STRING+callInfo.getField(Field.NO_OF_SAVINGS_ACCTS);
					noOfSavingsAccts = util.isNullOrEmpty(tempProductCount)?Constants.GL_ZERO:Integer.parseInt(tempProductCount);

					availableAcctOrCars = callInfo.getField(Field.SAVINGSACCTLIST)==null? new ArrayList<String>() :(ArrayList<String>)callInfo.getField(Field.SAVINGSACCTLIST);
					availableAcctOrCars= (ArrayList<String>)availableAcctOrCars.clone();
					if(!util.isNullOrEmpty(availableAcctOrCars) && !util.isNullOrEmpty(acctCurrMap)){
						for(int i=0; i<availableAcctOrCars.size();i++){
							str_Curr = acctCurrMap.get(availableAcctOrCars.get(i));
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The currency for the account ending with "+util.getSubstring(availableAcctOrCars.get(i), Constants.GL_FOUR)+"  is " + str_Curr);}

							if(!eligibleCurrType.contains(str_Curr)){
								noOfSavingsAccts--;
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Currency type "+ str_Curr+" is not eligible so neglecting that corresponding account");}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Decrement the count of total Saving acct number by 1");}
								filteredAcctOrCars.add(availableAcctOrCars.get(i));
							}
						}
					}

					availableAcctOrCars.removeAll(filteredAcctOrCars);
					if(noOfSavingsAccts == Constants.GL_ONE){
						callInfo.setField(Field.SELECTEDNO, availableAcctOrCars.get(Constants.GL_ZERO));
						callInfo.setField(Field.SELECTEDTYPE, Constants.HOST_SAVINGS_ACCOUNTID_TYPE);
					}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total number of Savings Accounts are "+ noOfSavingsAccts);}
				}else if(util.isCodePresentInTheConfigurationList(productType, cardVisaBrandTypeList) && Constants.FEATURE_TYPE_BANK.equalsIgnoreCase(featureType)){
					availableAcctOrCars = new ArrayList<String>();
					tempProductCount =Constants.EMPTY_STRING+callInfo.getField(Field.NO_OF_DR_VISA_CARDS);

					/**
					 * Handled for Debit card activation flow
					 */
					if(Constants.FEATURENAME_CARDACTIVATION.equalsIgnoreCase(featureName)){
						tempProductCount =Constants.EMPTY_STRING+callInfo.getField(Field.NO_OF_DR_VISA_CARDS_INACTVE);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Overrriding the tempProductCount value with inactive Visa DR cards "+ tempProductCount);}
					}
					//END

					noOfDRVisaCards = util.isNullOrEmpty(tempProductCount)?Constants.GL_ZERO:Integer.parseInt(tempProductCount);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total number of DR VISA Cards are "+ noOfDRVisaCards);}

					availableAcctOrCars = callInfo.getField(Field.DRVISACARDLIST)==null? new ArrayList<String>() :(ArrayList<String>)callInfo.getField(Field.DRVISACARDLIST);

					/**
					 * Handled for Debit card activation flow
					 */
					if(Constants.FEATURENAME_CARDACTIVATION.equalsIgnoreCase(featureName)){
						availableAcctOrCars = callInfo.getField(Field.DRVISACARDLISTINACTIVE)==null? new ArrayList<String>() : (ArrayList<String>)callInfo.getField(Field.DRVISACARDLISTINACTIVE);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Overrriding the availableAcctOrCars value with inactive Visa DR cards "+ availableAcctOrCars.size());}
					}
					//END


					availableAcctOrCars =(ArrayList<String>)availableAcctOrCars.clone();
					if(noOfDRVisaCards == Constants.GL_ONE){
						callInfo.setField(Field.SELECTEDNO, availableAcctOrCars.get(Constants.GL_ZERO));
						callInfo.setField(Field.SELECTEDTYPE, Constants.HOST_BRAND_VISA_CARD);
					}


				}else if(util.isCodePresentInTheConfigurationList(productType, cardMasterBrandTypeList) && Constants.FEATURE_TYPE_BANK.equalsIgnoreCase(featureType)){
					availableAcctOrCars = new ArrayList<String>();
					tempProductCount = Constants.EMPTY_STRING+callInfo.getField(Field.NO_OF_DR_MASTER_CARDS);

					/**
					 * Handled for Debit card activation flow
					 */
					if(Constants.FEATURENAME_CARDACTIVATION.equalsIgnoreCase(featureName)){
						tempProductCount =Constants.EMPTY_STRING+callInfo.getField(Field.NO_OF_DR_MASTER_CARDS_INACTIVE);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Overrriding the tempProductCount value with inactive Master DR cards "+ tempProductCount);}
					}
					//END

					noOfDRMasterCards = util.isNullOrEmpty(tempProductCount)?Constants.GL_ZERO:Integer.parseInt(tempProductCount);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total number of DR Master Cards are "+ noOfDRMasterCards);}

					availableAcctOrCars =  callInfo.getField(Field.DRMASTERCARDLIST)==null? new ArrayList<String>() :(ArrayList<String>)callInfo.getField(Field.DRMASTERCARDLIST);

					/**
					 * Handled for Debit card activation flow
					 */
					if(Constants.FEATURENAME_CARDACTIVATION.equalsIgnoreCase(featureName)){
						availableAcctOrCars = callInfo.getField(Field.DRMASTERCARDLISTINACTIVE)==null? new ArrayList<String>() :(ArrayList<String>)callInfo.getField(Field.DRMASTERCARDLISTINACTIVE);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Overrriding the availableAcctOrCars value with inactive Master DR cards "+ availableAcctOrCars.size());}
					}
					//END


					availableAcctOrCars =(ArrayList<String>)availableAcctOrCars.clone();
					if(noOfDRMasterCards == Constants.GL_ONE){
						callInfo.setField(Field.SELECTEDNO, availableAcctOrCars.get(Constants.GL_ZERO));
						callInfo.setField(Field.SELECTEDTYPE, Constants.HOST_BRAND_MASTER_CARD);
					}



				}else if(util.isCodePresentInTheConfigurationList(productType, cardAmexBrandTypeList) && Constants.FEATURE_TYPE_BANK.equalsIgnoreCase(featureType)){
					tempProductCount = Constants.EMPTY_STRING+callInfo.getField(Field.NO_OF_DR_AMEX_CARDS);


					/**
					 * Handled for Debit card activation flow
					 */
					if(Constants.FEATURENAME_CARDACTIVATION.equalsIgnoreCase(featureName)){
						tempProductCount =Constants.EMPTY_STRING+callInfo.getField(Field.NO_OF_DR_AMEX_CARDS_INACTIVE);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Overrriding the tempProductCount value with inactive Amex DR cards "+ tempProductCount);}
					}
					//END

					noOfDRAmEx = util.isNullOrEmpty(tempProductCount)?Constants.GL_ZERO:Integer.parseInt(tempProductCount);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total number of DR AmEx cards are "+ noOfDRAmEx);}

					availableAcctOrCars = callInfo.getField(Field.DRAMEXCARDLIST)==null? new ArrayList<String>() : (ArrayList<String>)callInfo.getField(Field.DRAMEXCARDLIST);

					/**
					 * Handled for Debit card activation flow
					 */
					if(Constants.FEATURENAME_CARDACTIVATION.equalsIgnoreCase(featureName)){
						availableAcctOrCars = callInfo.getField(Field.DRAMEXCARDLISTINACTIVE)==null? new ArrayList<String>() : (ArrayList<String>)callInfo.getField(Field.DRAMEXCARDLISTINACTIVE);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Overrriding the availableAcctOrCars value with inactive Amex DR cards "+ availableAcctOrCars.size());}
					}
					//END


					availableAcctOrCars =(ArrayList<String>)availableAcctOrCars.clone();
					if(noOfDRAmEx == Constants.GL_ONE){
						callInfo.setField(Field.SELECTEDNO, availableAcctOrCars.get(Constants.GL_ZERO));
						callInfo.setField(Field.SELECTEDTYPE, Constants.HOST_BRAND_AMEX_CARD);
					}

				}else if(util.isCodePresentInTheConfigurationList(productType, cardVisaBrandTypeList)  && Constants.FEATURE_TYPE_CARD.equalsIgnoreCase(featureType)){
					tempProductCount = Constants.EMPTY_STRING+callInfo.getField(Field.NO_OF_CR_VISA_CARDS);
					noOfCRVisaCards = util.isNullOrEmpty(tempProductCount)?Constants.GL_ZERO:Integer.parseInt(tempProductCount);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total number of CR VISA Cards are "+ noOfCRVisaCards);}

					availableAcctOrCars = callInfo.getField(Field.CRVISACARDLIST)==null? new ArrayList<String>() : (ArrayList<String>)callInfo.getField(Field.CRVISACARDLIST);
					availableAcctOrCars =(ArrayList<String>)availableAcctOrCars.clone();
					if(noOfCRVisaCards == Constants.GL_ONE){
						callInfo.setField(Field.SELECTEDNO, availableAcctOrCars.get(Constants.GL_ZERO));
						callInfo.setField(Field.SELECTEDTYPE, Constants.HOST_BRAND_VISA_CARD);
					}

				}else if(util.isCodePresentInTheConfigurationList(productType, cardMasterBrandTypeList) && Constants.FEATURE_TYPE_CARD.equalsIgnoreCase(featureType)){
					tempProductCount = Constants.EMPTY_STRING+callInfo.getField(Field.NO_OF_CR_MASTER_CARDS);
					noOfCRMasterCards = util.isNullOrEmpty(tempProductCount)?Constants.GL_ZERO:Integer.parseInt(tempProductCount);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total number of CR Master Cards are "+ noOfCRMasterCards);}

					availableAcctOrCars = callInfo.getField(Field.CRMASTERCARDLIST)==null? new ArrayList<String>() : (ArrayList<String>)callInfo.getField(Field.CRMASTERCARDLIST);
					availableAcctOrCars =(ArrayList<String>)availableAcctOrCars.clone();
					if(noOfCRMasterCards == Constants.GL_ONE){
						callInfo.setField(Field.SELECTEDNO, availableAcctOrCars.get(Constants.GL_ZERO));
						callInfo.setField(Field.SELECTEDTYPE, Constants.HOST_BRAND_MASTER_CARD);
					}

				}else if(util.isCodePresentInTheConfigurationList(productType, cardAmexBrandTypeList) && Constants.FEATURE_TYPE_CARD.equalsIgnoreCase(featureType)){
					tempProductCount = Constants.EMPTY_STRING+callInfo.getField(Field.NO_OF_CR_AMEX_CARDS);
					noOfCRAmEx = util.isNullOrEmpty(tempProductCount)?Constants.GL_ZERO:Integer.parseInt(tempProductCount);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total number of CR AmEx Card are "+ noOfCRAmEx);}

					availableAcctOrCars =callInfo.getField(Field.CRAMEXCARDLIST)==null? new ArrayList<String>() :  (ArrayList<String>)callInfo.getField(Field.CRAMEXCARDLIST);
					availableAcctOrCars =(ArrayList<String>)availableAcctOrCars.clone();
					if(noOfCRAmEx == Constants.GL_ONE){
						callInfo.setField(Field.SELECTEDNO, availableAcctOrCars.get(Constants.GL_ZERO));
						callInfo.setField(Field.SELECTEDTYPE, Constants.HOST_BRAND_AMEX_CARD);
					}

				}else if(util.isCodePresentInTheConfigurationList(productType, cardVisaBrandTypeList)  && Constants.FEATURE_TYPE_PREPAIDCARD.equalsIgnoreCase(featureType)){
					tempProductCount = Constants.EMPTY_STRING+callInfo.getField(Field.NO_OF_PP_VISA_CARDS);
					noOfPPVisaCards = util.isNullOrEmpty(tempProductCount)?Constants.GL_ZERO:Integer.parseInt(tempProductCount);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total number of PP VISA Cards are "+ noOfPPVisaCards);}

					availableAcctOrCars = callInfo.getField(Field.PPVISACARDLIST)==null? new ArrayList<String>() : (ArrayList<String>)callInfo.getField(Field.PPVISACARDLIST);
					availableAcctOrCars =(ArrayList<String>)availableAcctOrCars.clone();
					if(noOfPPVisaCards == Constants.GL_ONE){
						callInfo.setField(Field.SELECTEDNO, availableAcctOrCars.get(Constants.GL_ZERO));
						callInfo.setField(Field.SELECTEDTYPE, Constants.HOST_BRAND_VISA_CARD);
					}

				}else if(util.isCodePresentInTheConfigurationList(productType, cardMasterBrandTypeList) && Constants.FEATURE_TYPE_PREPAIDCARD.equalsIgnoreCase(featureType)){
					tempProductCount = Constants.EMPTY_STRING+callInfo.getField(Field.NO_OF_PP_MASTER_CARDS);
					noOfPPMasterCards = util.isNullOrEmpty(tempProductCount)?Constants.GL_ZERO:Integer.parseInt(tempProductCount);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total number of PP Master Cards are "+ noOfPPMasterCards);}

					availableAcctOrCars = callInfo.getField(Field.PPMASTERCARDLIST)==null? new ArrayList<String>() : (ArrayList<String>)callInfo.getField(Field.PPMASTERCARDLIST);
					availableAcctOrCars =(ArrayList<String>)availableAcctOrCars.clone();
					if(noOfPPMasterCards == Constants.GL_ONE){
						callInfo.setField(Field.SELECTEDNO, availableAcctOrCars.get(Constants.GL_ZERO));
						callInfo.setField(Field.SELECTEDTYPE, Constants.HOST_BRAND_MASTER_CARD);
					}

				}else if(util.isCodePresentInTheConfigurationList(productType, cardAmexBrandTypeList) && Constants.FEATURE_TYPE_PREPAIDCARD.equalsIgnoreCase(featureType)){
					tempProductCount = Constants.EMPTY_STRING+callInfo.getField(Field.NO_OF_PP_AMEX_CARDS);
					noOfPPAmEx = util.isNullOrEmpty(tempProductCount)?Constants.GL_ZERO:Integer.parseInt(tempProductCount);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total number of PP AmEx Card are "+ noOfPPAmEx);}

					availableAcctOrCars =callInfo.getField(Field.PPAMEXCARDLIST)==null? new ArrayList<String>() :  (ArrayList<String>)callInfo.getField(Field.PPAMEXCARDLIST);
					availableAcctOrCars =(ArrayList<String>)availableAcctOrCars.clone();
					if(noOfPPAmEx == Constants.GL_ONE){
						callInfo.setField(Field.SELECTEDNO, availableAcctOrCars.get(Constants.GL_ZERO));
						callInfo.setField(Field.SELECTEDTYPE, Constants.HOST_BRAND_AMEX_CARD);
					}

				}
			}
			totalCardsOrAccts =  noOfDepositAccts + noOfCreditAccts+noOfLoanAccts+noOfSavingsAccts+noOfCRVisaCards+noOfPPVisaCards+noOfDRVisaCards+noOfCRMasterCards+noOfPPMasterCards+noOfDRMasterCards+noOfCRAmEx+noOfPPAmEx+noOfDRAmEx;


			if(totalCardsOrAccts > Constants.GL_ONE){
				//if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Resetting the selected number and selected type since we have >1 total no of accts / cards");}

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Assigning Last Selected Card or Account in the SELECTEDNO and SELECTED TYPE");}
                
                /**
                * Date : 26-Aug-2015 - assigning lastselectedno and lastselectedtype into the selectedno and selectedtype
                */
                
                if("AccountBalanceFD".equalsIgnoreCase(featureName))
                {
                       if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Inside Deposit");}
                       callInfo.setField(Field.SELECTEDNO, callInfo.getField(Field.LASTSELECTEDDEPOSITACCTNO));
                       callInfo.setField(Field.SELECTEDTYPE, callInfo.getField(Field.LASTSELECTEDDEPOSITTYPE));
                }
                else if("LoanBalance".equalsIgnoreCase(featureName))
                {
                       if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Inside Loan");}
                       callInfo.setField(Field.SELECTEDNO, callInfo.getField(Field.LASTSELECTEDLOANNO));
                       callInfo.setField(Field.SELECTEDTYPE, callInfo.getField(Field.LASTSELECTEDLOANTYPE));
                }
                else if("BANK".equalsIgnoreCase(featureType))
                {
                       if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Inside Bank");}
                       callInfo.setField(Field.SELECTEDNO, callInfo.getField(Field.LASTSELECTEDACCTNO));
                       callInfo.setField(Field.SELECTEDTYPE, callInfo.getField(Field.LASTSELECTEDACCTTYPE));
                }
                else if("CARD".equalsIgnoreCase(featureType))
                {
                       if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Inside Card");}
                       callInfo.setField(Field.SELECTEDNO, callInfo.getField(Field.LASTSELECTEDCARDNO));
                       callInfo.setField(Field.SELECTEDTYPE, callInfo.getField(Field.LASTSELECTEDCARDTYPE));
                }
                else if("PREPAIDCARD".equalsIgnoreCase(featureType))
                {
                       if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Inside Prepaid Card");}
                       callInfo.setField(Field.SELECTEDNO, callInfo.getField(Field.LASTSELECTEDCARDNO));
                       callInfo.setField(Field.SELECTEDTYPE, callInfo.getField(Field.LASTSELECTEDPREPAIDCARDTYPE));
                }
                
	
				/**
				 * Following lines are commented as it impacting if a customer choosed a acct at a feature and expecting the same acct at other feature acct selection option
				 */
				//				callInfo.setField(Field.SELECTEDNO, Constants.EMPTY_STRING);
//				callInfo.setField(Field.SELECTEDTYPE, Constants.EMPTY_STRING);
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total Number of Eligible accouts for the feature "+ featureName +" is "+totalCardsOrAccts);}
			callInfo.setField(Field.NOOFELIGIBLEACCTS, totalCardsOrAccts);

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at AccountOrCardSelectionImpl.getNumberOfAccountOrCreditCards() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return totalCardsOrAccts;
	}

	@Override
	public int getNumberOfProductTypes(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: AccountOrCardSelectionImpl.getNumberOfProductTypes()");}
		int noOfProductTypes = Constants.GL_ZERO;
		getConfigurationParam(callInfo);
		try{
			
			ArrayList<String> eligibleAcctCardsList = new ArrayList<String>();

			ArrayList<String> tempList = new ArrayList<String>();
			String featureType = (String)callInfo.getField(Field.FEATURETYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The Feature Type is" + featureType );}

			String featureName = (String)callInfo.getField(Field.FEATURENAME);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The Feature Name is" + featureName );}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig= (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Feature data object is null");}
				throw new ServiceException("ivr_ICEGlobalConfig object is null");
			}

			String customerSegment = util.isNullOrEmpty(callInfo.getField(Field.CUST_SEGMENT_TYPE)) ? Constants.CUST_SEGMENT_RETAIL : (String)callInfo.getField(Field.CUST_SEGMENT_TYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured Customer segment type is "+ customerSegment);}
			
			String cardVisaBrandTypeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CARDBRANDTYPE_VISA+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured credit card VISA brand type is "+ cardVisaBrandTypeList);}

			String cardMasterBrandTypeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CARDBRANDTYPE_MASTER+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured credit card Master brand type is "+ cardMasterBrandTypeList);}

			String cardAmexBrandTypeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CARDBRANDTYPE_AMEX+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured credit card Amex brand type is "+ cardAmexBrandTypeList);}

			String currentAccoutTypeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_ACCOUNTTYPE_CURRENT+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured current account type list is "+ currentAccoutTypeList);}

			String savingAccountTypeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_ACCOUNTTYPE_SAVING+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured savings account type list is "+ savingAccountTypeList);}

			String loanAccountTypeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_ACCOUNTTYPE_LOAN+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured loan account type list is "+ loanAccountTypeList);}

			String depositAccountTypeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_ACCOUNTTYPE_DEPOSIT+Constants.UNDERSCORE+customerSegment);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured deposit account type list is "+ depositAccountTypeList);}



			ArrayList<String> eligibleAcctTypes = null;

			boolean calledForDestAcctSelection = false;

			if(util.isNullOrEmpty(callInfo.getField(Field.FROMDESTINATION))){
				callInfo.setField(Field.FROMDESTINATION,false);
			}else{
				calledForDestAcctSelection = (boolean)callInfo.getField(Field.FROMDESTINATION);
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "This method called for Source acct selection / for Destination account selection ? "+ calledForDestAcctSelection);}

			if(calledForDestAcctSelection){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting destination eligible accounts types");}
				eligibleAcctTypes = (ArrayList<String>) callInfo.getField(Field.ELIGIBLEDESTACCTCARDTYPE);
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting source eligible accounts types");}
				eligibleAcctTypes = (ArrayList<String>) callInfo.getField(Field.ELIGIBLEACCTCARDTYPE);
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Eligle product type list retrieved is :" + eligibleAcctTypes);}


			//			ArrayList<String> eligibleAcctTypes = (ArrayList<String>) callInfo.getField(Field.ELIGIBLEACCTCARDTYPE);
			//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The eligible product types Object is " + eligibleAcctTypes );}

			String productType = Constants.EMPTY_STRING;
			for(int count=Constants.GL_ZERO; count<eligibleAcctTypes.size(); count++){
				productType = eligibleAcctTypes.get(count);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+ count + "product is of eligible product object is" + productType );}

				if(util.isCodePresentInTheConfigurationList(productType, depositAccountTypeList)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+ count + "product is of eligible product object is" + productType + "Belongs to Deposit Acct Type");}
					tempList = callInfo.getField(Field.FDACCTLIST)==null? new ArrayList<String>() :  (ArrayList<String>)callInfo.getField(Field.FDACCTLIST);
					
					if(tempList!=null && tempList.size() > Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Temp list has some fixed deposit account");}
						eligibleAcctCardsList.addAll(tempList);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The count of eligible account card count " + eligibleAcctCardsList.size());}
					}
					
					tempList =(ArrayList<String>)tempList.clone();
					if(tempList!=null && tempList.size()>Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"There is a linked Deposit account for this customer");}
						noOfProductTypes++;
						callInfo.setField(Field.SELECTEDTYPE, Constants.HOST_DEPOSIT_ACCOUNTID_TYPE);
					}
				}else if(util.isCodePresentInTheConfigurationList(productType, currentAccoutTypeList)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+ count + "product is of eligible product object is" + productType + "Belongs to Credit / Current Acct Type");}
					tempList = callInfo.getField(Field.CURRENTACCTLIST)==null? new ArrayList<String>() : (ArrayList<String>)callInfo.getField(Field.CURRENTACCTLIST);
					
					if(tempList!=null && tempList.size() > Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Temp list has some Current account");}
						eligibleAcctCardsList.addAll(tempList);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The count of eligible account card count " + eligibleAcctCardsList.size());}
					}
					
					tempList =(ArrayList<String>)tempList.clone();
					if(tempList!=null && tempList.size()>Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"There is a linked CREDIT account for this customer");}
						noOfProductTypes++;
						callInfo.setField(Field.SELECTEDTYPE, Constants.HOST_CREDIT_ACCOUNTID_TYPE);
					}
				}else if(util.isCodePresentInTheConfigurationList(productType, loanAccountTypeList)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+ count + "product is of eligible product object is" + productType + "Belongs to LOAN Acct Type");}
					tempList = callInfo.getField(Field.LOANACCTLIST)==null? new ArrayList<String>() : (ArrayList<String>)callInfo.getField(Field.LOANACCTLIST);
					
					if(tempList!=null && tempList.size() > Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Temp list has some Loan account");}
						eligibleAcctCardsList.addAll(tempList);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The count of eligible account card count " + eligibleAcctCardsList.size());}
					}
					
					
					tempList =(ArrayList<String>)tempList.clone();
					if(tempList!=null && tempList.size()>Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"There is a linked LOAN account for this customer");}
						noOfProductTypes++;
						callInfo.setField(Field.SELECTEDTYPE, Constants.HOST_LOAN_ACCOUNTID_TYPE);
					}
				}else if(util.isCodePresentInTheConfigurationList(productType, savingAccountTypeList)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+ count + "product is of eligible product object is" + productType + "Belongs to SAVINGS Acct Type");}
					tempList = callInfo.getField(Field.SAVINGSACCTLIST)==null? new ArrayList<String>() : (ArrayList<String>)callInfo.getField(Field.SAVINGSACCTLIST);
					
					if(tempList!=null && tempList.size() > Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Temp list has some Savings account");}
						eligibleAcctCardsList.addAll(tempList);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The count of eligible account card count " + eligibleAcctCardsList.size());}
					}
					
					tempList =(ArrayList<String>)tempList.clone();
					if(tempList!=null && tempList.size()>Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"There is a linked SAVINGS account for this customer");}
						noOfProductTypes++;
						callInfo.setField(Field.SELECTEDTYPE, Constants.HOST_SAVINGS_ACCOUNTID_TYPE);
					}
				}else if(util.isCodePresentInTheConfigurationList(productType, cardVisaBrandTypeList) && Constants.FEATURE_TYPE_BANK.equalsIgnoreCase(featureType)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+ count + "product is of eligible product object is" + productType + "Belongs to DR VISA Type");}
					
					/**
					 * Handled for Debit card activation
					 */
					if(Constants.FEATURENAME_CARDACTIVATION.equalsIgnoreCase(featureName)){
						tempList = callInfo.getField(Field.DRVISACARDLISTINACTIVE)==null? new ArrayList<String>() :(ArrayList<String>)callInfo.getField(Field.DRVISACARDLISTINACTIVE);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Overriding with inactive DR VISA card for this customer" + tempList.size());}
					}else{
						tempList = callInfo.getField(Field.DRVISACARDLIST)==null? new ArrayList<String>() : (ArrayList<String>)callInfo.getField(Field.DRVISACARDLIST);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Overriding with Active DR VISA card for this customer" + tempList.size());}
					}
					//END 
					
					if(tempList!=null && tempList.size() > Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Temp list has some DR VISA account");}
						eligibleAcctCardsList.addAll(tempList);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The count of eligible account card count " + eligibleAcctCardsList.size());}
					}
					

					tempList =(ArrayList<String>)tempList.clone();
					if(tempList!=null && tempList.size()>Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"There is a linked DR VISA card for this customer");}
						noOfProductTypes++;
						callInfo.setField(Field.SELECTEDTYPE, Constants.HOST_BRAND_VISA_CARD);
					}
				}else if(util.isCodePresentInTheConfigurationList(productType, cardMasterBrandTypeList) && Constants.FEATURE_TYPE_BANK.equalsIgnoreCase(featureType)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+ count + "product is of eligible product object is" + productType + "Belongs to DR Master card Type");}

					/**
					 * Handled for Debit card activation
					 */
					if(Constants.FEATURENAME_CARDACTIVATION.equalsIgnoreCase(featureName)){
						tempList = callInfo.getField(Field.DRMASTERCARDLISTINACTIVE)==null? new ArrayList<String>() :(ArrayList<String>)callInfo.getField(Field.DRMASTERCARDLISTINACTIVE);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Overriding with inactive DR VISA card for this customer" + tempList.size());}
					}else{
						tempList = callInfo.getField(Field.DRMASTERCARDLIST)==null? new ArrayList<String>() : (ArrayList<String>)callInfo.getField(Field.DRMASTERCARDLIST);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Overriding with Active DR VISA card for this customer" + tempList.size());}
					}
					//END 
					
					if(tempList!=null && tempList.size() > Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Temp list has some DR Master account");}
						eligibleAcctCardsList.addAll(tempList);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The count of eligible account card count " + eligibleAcctCardsList.size());}
					}
					
					

					tempList = (ArrayList<String>)tempList.clone();
					if(tempList!=null && tempList.size()>Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"There is a linked DR MASTER CARD for this customer");}
						noOfProductTypes++;
						callInfo.setField(Field.SELECTEDTYPE, Constants.HOST_BRAND_MASTER_CARD);
					}
				}else if(util.isCodePresentInTheConfigurationList(productType, cardAmexBrandTypeList)  && Constants.FEATURE_TYPE_BANK.equalsIgnoreCase(featureType)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+ count + "product is of eligible product object is" + productType + "Belongs to DR AMEX card Type");}

					/**
					 * Handled for Debit card activation
					 */
					if(Constants.FEATURENAME_CARDACTIVATION.equalsIgnoreCase(featureName)){
						tempList =  callInfo.getField(Field.DRAMEXCARDLISTINACTIVE)==null? new ArrayList<String>() :(ArrayList<String>)callInfo.getField(Field.DRAMEXCARDLISTINACTIVE);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Overriding with inactive DR Amex card for this customer" + tempList.size());}
					}else{
						tempList = callInfo.getField(Field.DRAMEXCARDLIST)==null? new ArrayList<String>() : (ArrayList<String>)callInfo.getField(Field.DRAMEXCARDLIST);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Overriding with Active DR Amex card for this customer" + tempList.size());}

					}
					//END 

					if(tempList!=null && tempList.size() > Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Temp list has some DR AMEX card account");}
						eligibleAcctCardsList.addAll(tempList);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The count of eligible account card count " + eligibleAcctCardsList.size());}
					}
					

					tempList =(ArrayList<String>)tempList.clone();
					if(tempList!=null && tempList.size()>Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"There is a linked DR AMEX CARD for this customer");}
						noOfProductTypes++;
						callInfo.setField(Field.SELECTEDTYPE, Constants.HOST_BRAND_AMEX_CARD);
					}
				}else if(util.isCodePresentInTheConfigurationList(productType, cardVisaBrandTypeList) && Constants.FEATURE_TYPE_CARD.equalsIgnoreCase(featureType)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+ count + "product is of eligible product object is" + productType + "Belongs to CR VISA card Type");}
					tempList = callInfo.getField(Field.CRVISACARDLIST)==null? new ArrayList<String>() : (ArrayList<String>)callInfo.getField(Field.CRVISACARDLIST);
					
					if(tempList!=null && tempList.size() > Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Temp list has some fixed deposit account");}
						eligibleAcctCardsList.addAll(tempList);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The count of eligible account card count " + eligibleAcctCardsList.size());}
					}
					
					
					tempList =(ArrayList<String>)tempList.clone();
					if(tempList!=null && tempList.size()>Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"There is a linked CR VISA card for this customer");}
						noOfProductTypes++;
						callInfo.setField(Field.SELECTEDTYPE, Constants.HOST_BRAND_VISA_CARD);
					}
				}else if(util.isCodePresentInTheConfigurationList(productType, cardMasterBrandTypeList) && Constants.FEATURE_TYPE_CARD.equalsIgnoreCase(featureType)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+ count + "product is of eligible product object is" + productType + "Belongs to CR MasterCard Type");}
					tempList = callInfo.getField(Field.CRMASTERCARDLIST)==null? new ArrayList<String>() : (ArrayList<String>)callInfo.getField(Field.CRMASTERCARDLIST);
					
					if(tempList!=null && tempList.size() > Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Temp list has some CR VISA account");}
						eligibleAcctCardsList.addAll(tempList);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The count of eligible account card count " + eligibleAcctCardsList.size());}
					}
					
					
					tempList =(ArrayList<String>)tempList.clone();
					if(tempList!=null && tempList.size()>Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"There is a linked CR MASTER CARD for this customer");}
						noOfProductTypes++;
						callInfo.setField(Field.SELECTEDTYPE, Constants.HOST_BRAND_MASTER_CARD);
					}
				}else if(util.isCodePresentInTheConfigurationList(productType, cardAmexBrandTypeList) && Constants.FEATURE_TYPE_CARD.equalsIgnoreCase(featureType)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+ count + "product is of eligible product object is" + productType + "Belongs to CR AMEX Card Type");}
					tempList =callInfo.getField(Field.CRAMEXCARDLIST)==null? new ArrayList<String>() :  (ArrayList<String>)callInfo.getField(Field.CRAMEXCARDLIST);
					
					if(tempList!=null && tempList.size() > Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Temp list has some CR Master account");}
						eligibleAcctCardsList.addAll(tempList);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The count of eligible account card count " + eligibleAcctCardsList.size());}
					}
					
					
					tempList =(ArrayList<String>)tempList.clone();
					if(tempList!=null && tempList.size()>Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"There is a linked  CR AMEX CARD  for this customer");}
						noOfProductTypes++;
						callInfo.setField(Field.SELECTEDTYPE, Constants.HOST_BRAND_AMEX_CARD);
					}
				}else if(util.isCodePresentInTheConfigurationList(productType, cardVisaBrandTypeList) && Constants.FEATURE_TYPE_PREPAIDCARD.equalsIgnoreCase(featureType)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+ count + "product is of eligible product object is" + productType + "Belongs to PP VISA card Type");}
					tempList = callInfo.getField(Field.PPVISACARDLIST)==null? new ArrayList<String>() : (ArrayList<String>)callInfo.getField(Field.PPVISACARDLIST);
					
					if(tempList!=null && tempList.size() > Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Temp list has some fixed deposit account");}
						eligibleAcctCardsList.addAll(tempList);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The count of eligible account card count " + eligibleAcctCardsList.size());}
					}
					
					
					tempList =(ArrayList<String>)tempList.clone();
					if(tempList!=null && tempList.size()>Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"There is a linked PP VISA card for this customer");}
						noOfProductTypes++;
						callInfo.setField(Field.SELECTEDTYPE, Constants.HOST_BRAND_VISA_CARD);
					}
				}else if(util.isCodePresentInTheConfigurationList(productType, cardMasterBrandTypeList) && Constants.FEATURE_TYPE_PREPAIDCARD.equalsIgnoreCase(featureType)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+ count + "product is of eligible product object is" + productType + "Belongs to PP MasterCard Type");}
					tempList = callInfo.getField(Field.PPMASTERCARDLIST)==null? new ArrayList<String>() : (ArrayList<String>)callInfo.getField(Field.PPMASTERCARDLIST);
					
					if(tempList!=null && tempList.size() > Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Temp list has some PP VISA account");}
						eligibleAcctCardsList.addAll(tempList);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The count of eligible account card count " + eligibleAcctCardsList.size());}
					}
					
					
					tempList =(ArrayList<String>)tempList.clone();
					if(tempList!=null && tempList.size()>Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"There is a linked PP MASTER CARD for this customer");}
						noOfProductTypes++;
						callInfo.setField(Field.SELECTEDTYPE, Constants.HOST_BRAND_MASTER_CARD);
					}
				}else if(util.isCodePresentInTheConfigurationList(productType, cardAmexBrandTypeList) && Constants.FEATURE_TYPE_PREPAIDCARD.equalsIgnoreCase(featureType)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+ count + "product is of eligible product object is" + productType + "Belongs to PP AMEX Card Type");}
					tempList =callInfo.getField(Field.PPAMEXCARDLIST)==null? new ArrayList<String>() :  (ArrayList<String>)callInfo.getField(Field.PPAMEXCARDLIST);
					
					if(tempList!=null && tempList.size() > Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Temp list has some PP Master account");}
						eligibleAcctCardsList.addAll(tempList);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The count of eligible account card count " + eligibleAcctCardsList.size());}
					}
					
					
					tempList =(ArrayList<String>)tempList.clone();
					if(tempList!=null && tempList.size()>Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"There is a linked  PP AMEX CARD  for this customer");}
						noOfProductTypes++;
						callInfo.setField(Field.SELECTEDTYPE, Constants.HOST_BRAND_AMEX_CARD);
					}
				}

			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The total no of products presents are " + noOfProductTypes );}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The total no of eligible accounts are " + eligibleAcctCardsList.size() );}
			
			if(noOfProductTypes > Constants.GL_ONE){
				callInfo.setField(Field.SELECTEDTYPE, Constants.EMPTY_STRING);
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Setting the Default product type as " + callInfo.getField(Field.SELECTEDTYPE));}
			}

			/**
			 * Added to update the total number of eligible account or card numbers in the Rule Engine
			 */

			String srouceAcctOrCardNo = callInfo.getField(Field.SRCNO) == null ? Constants.EMPTY_STRING :(String)callInfo.getField(Field.SRCNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The source number in the SRCNO field is "+ util.maskCardOrAccountNumber(srouceAcctOrCardNo));}


			ArrayList<String> eligibleCurrType = null;
			ArrayList<String> filteringProductAcctOrCards = new ArrayList<String>();

			eligibleCurrType =  callInfo.getField(Field.ELIGIBLEACCTCARDCURRTYPE) == null ? new ArrayList<String>() : (ArrayList<String>) callInfo.getField(Field.ELIGIBLEACCTCARDCURRTYPE);
			if(calledForDestAcctSelection){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Since its called from Destination account selection option removing source account from the list");}
				filteringProductAcctOrCards.add(srouceAcctOrCardNo);

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Overriding the Currency type  with the destination type");}
				eligibleCurrType = callInfo.getField(Field.ELIGIBLEDESTACCTCARDCURRTYPE) == null ? new ArrayList<String>() :  (ArrayList<String>) callInfo.getField(Field.ELIGIBLEDESTACCTCARDCURRTYPE);
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Eligible currency type is "+ eligibleCurrType);}

			String accountOrCardNumber = Constants.EMPTY_STRING;

			HashMap<String, AcctInfo> accountdetailMap= new HashMap<String, AcctInfo>();
			AcctInfo acctInfo = new AcctInfo();
			if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())){
				if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getAccountDetailMap())){
					accountdetailMap = callInfo.getCallerIdentification_HostRes().getAccountDetailMap();
				}
			}

			String currType = Constants.EMPTY_STRING;
			for(int count = Constants.GL_ZERO; count < eligibleAcctCardsList.size();count++){
				accountOrCardNumber = eligibleAcctCardsList.get(count);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The "+count+" Account Or Card number ending with " + util.getSubstring(accountOrCardNumber, Constants.GL_FOUR));}

				if(Constants.FEATURE_TYPE_BANK.equalsIgnoreCase(featureType)){
					acctInfo = accountdetailMap.get(accountOrCardNumber);
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved the Account Info object from the bean" + acctInfo);}

					if(!util.isNullOrEmpty(acctInfo)){
						currType = acctInfo.getAcctCurr();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Curr of the acct number ending with " + util.getSubstring(accountOrCardNumber, Constants.GL_FOUR) +" is "+ currType);}

						if(!eligibleCurrType.contains(currType)){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the account number ending with" + util.getSubstring(accountOrCardNumber, Constants.GL_FOUR) +"to filter Account number list");}
							filteringProductAcctOrCards.add(accountOrCardNumber);
						}
					}
				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "There is no currency filteration for Cards");}
				}

			}
			//TODO need to be hide this below logging lines
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Accounts to be filtered are " + filteringProductAcctOrCards );}	


			//To avoid removing of accts or cards from the reference object
			ArrayList<String> selectedProductAcctOrCardsClone = new ArrayList<String>(); 
			selectedProductAcctOrCardsClone.addAll(eligibleAcctCardsList);

			selectedProductAcctOrCardsClone.removeAll(filteringProductAcctOrCards);

			//TODO need to be hide this below logging lines
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Eligle Account numbers retrieved are :" + selectedProductAcctOrCardsClone);}


			/***
			 * Handled by Vinoth on 05Mar2013, if there is no eligible account then we should throw service exception
			 */
			if (selectedProductAcctOrCardsClone.size() == Constants.GL_ZERO){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "There is no Eligle Account numbers retrieved :");}
				//throw new ServiceException("There is no eligible account");
				return selectedProductAcctOrCardsClone.size();
			}
			// END 


			/**
			 * Rule engine update
			 */
			ICERuleParam ruleParamObj = (ICERuleParam)callInfo.getICERuleParam();

			if(util.isNullOrEmpty(ruleParamObj)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "***********Rule Engine Object is null or empty*************" + ruleParamObj);}
			}
			
			String customerID = Constants.EMPTY + callInfo.getField(Field.CUSTOMERID);
			ruleParamObj.setIVRParam(Constants.RULE_ENGINE_CUSTOMERID, customerID);

			if(!util.isNullOrEmpty(selectedProductAcctOrCardsClone)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Total No Of Eligible Accounts or Cards in the Rule Engine " + selectedProductAcctOrCardsClone);}
				ruleParamObj.setIVRParam(Constants.RULE_ENGINE_NOOFACCTORCARD, selectedProductAcctOrCardsClone.size() + Constants.EMPTY);
				ruleParamObj.updateIVRFields();
			}
			//END Rule Engine Updation


		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at AccountOrCardSelectionImpl.getNumberOfProductTypes() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return noOfProductTypes;
	}


	public void getConfigurationParam(CallInfo callInfo)throws ServiceException{
		//Since its a setting configuration param to call info session  variable dont throw any new exception, throw if it is mandatory
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			String featureType = Constants.EMPTY_STRING;

			featureType = (String)callInfo.getField(Field.FEATURETYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Feature Type retrieved from callinfo is " + featureType);}

			if(util.isNullOrEmpty(featureType)){
				featureType = (String) ivr_ICEFeatureData.getConfig().getParamValue(Constants.FEATURETYPE);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Feature Type is " + featureType);}
			}

			String featureName = Constants.EMPTY_STRING;
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig()) && !util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.FEATURENAME))){
				featureName = (String) ivr_ICEFeatureData.getConfig().getParamValue(Constants.FEATURENAME);
				callInfo.setField(Field.FEATURENAME, featureName);
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Feature Name is " + featureName);}

			ArrayList<String> eligibleAcctOrCardTypes = null;
			eligibleAcctOrCardTypes = (ArrayList<String>)ivr_ICEFeatureData.getConfig().getParamValue(Constants.ELIGIBLEACCTCARDTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Eligible accounts or cards type are " + eligibleAcctOrCardTypes);}

			ArrayList<String> eligibleAcctOrCardCurrTypes = null;
			eligibleAcctOrCardCurrTypes = (ArrayList<String>)ivr_ICEFeatureData.getConfig().getParamValue(Constants.ELIGIBLEACCTCARDCURRTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Eligible accounts or cards Curr type are " + eligibleAcctOrCardCurrTypes);}

			ArrayList<String> eligibleDestAcctOrCardTypes = null;
			eligibleDestAcctOrCardTypes = (ArrayList<String>)ivr_ICEFeatureData.getConfig().getParamValue(Constants.ELIGIBLEDESTACCTCARDTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Eligible Dest accounts or cards type are " + eligibleDestAcctOrCardTypes);}

			ArrayList<String> eligibleDestAcctOrCardCurrTypes = null;
			eligibleDestAcctOrCardCurrTypes = (ArrayList<String>)ivr_ICEFeatureData.getConfig().getParamValue(Constants.ELIGIBLEDESTACCTCARDCURRTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Eligible Dest accounts or cards Curr type are " + eligibleDestAcctOrCardCurrTypes);}

			String bankAccountLength = (String)callInfo.getField(Field.BANKACCTLENGTH);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The bank Account lenght is " + bankAccountLength);}

			String cardLength = (String)callInfo.getField(Field.CARDACCTLENGTH);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Card lenght is " + cardLength);}


			callInfo.setField(Field.FEATURETYPE, featureType);
			callInfo.setField(Field.ELIGIBLEACCTCARDTYPE, eligibleAcctOrCardTypes);
			callInfo.setField(Field.ELIGIBLEACCTCARDCURRTYPE, eligibleAcctOrCardCurrTypes);
			callInfo.setField(Field.ELIGIBLEDESTACCTCARDTYPE, eligibleDestAcctOrCardTypes);
			callInfo.setField(Field.ELIGIBLEDESTACCTCARDCURRTYPE, eligibleDestAcctOrCardCurrTypes);

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: AccountOrCardSelectionImpl.getConfigurationParam()");}

		}
		catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at AccountOrCardSelectionImpl.getConfigurationParam() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
	}


}
