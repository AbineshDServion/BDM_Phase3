package com.servion.services;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.bm.malaa.service.MalaaAPI;
import com.db.dataservices.DataServices;
import com.db.resource.DBConstants;
import com.db.resource.VRUDBDataServicesInstance;
import com.reportbean.IvrData;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.ChqBookOrderDAO;
import com.servion.dao.CustDtlsDAO;
import com.servion.dao.impl.CustDtlsDAOImpl;
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
import com.servion.model.CustDtls.CustDtls_HostRes;
import com.servion.model.chequeBookRequest.UpdateChequeBookOrder_HostRes;
import com.servion.model.reporting.HostReportDetails;

public class ChequeBookRequestImpl implements IChequeBookRequest {

	
	private static Logger logger = LoggerObject.getLogger();
	
	private ChqBookOrderDAO chqBookOrderDAO;
	private CustDtlsDAO custDtlsDAO;
	
	public CustDtlsDAO getCustDtlsDAO() {
		return custDtlsDAO;
	}

	public void setCustDtlsDAO(CustDtlsDAO custDtlsDAO) {
		this.custDtlsDAO = custDtlsDAO;
	}

	public ChqBookOrderDAO getChqBookOrderDAO() {
		return chqBookOrderDAO;
	}

	public void setChqBookOrderDAO(ChqBookOrderDAO chqBookOrderDAO) {
		this.chqBookOrderDAO = chqBookOrderDAO;
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
	public String getChequeBookAlreadyOrdered(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try {
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: ChequeBookRequestImpl. getChequeBookAlreadyOrdered()");}

			String code = Constants.ONE;
			String sessionId = (String)callInfo.getField(Field.SESSIONID);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method ");}
			HashMap<String, Object> configMap = new HashMap<String, Object>();

			String currentDate = util.getTodayDateOrTime(Constants.DATEFORMAT_YYYYMMDD);
			
			configMap.put(DBConstants.CUSTOMERID, callInfo.getField(Field.CUSTOMERID));
			configMap.put(DBConstants.DATETIME, currentDate);
			
			String uui = (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UUI of the call is " + uui);}
			
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer id " + configMap.get(Constants.CUSTOMERID));}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current date is " + configMap.get(Constants.DATETIME) );}

			DataServices dataServices = VRUDBDataServicesInstance.getInstance();
			try {
				code = dataServices.getChequeBookOrdered(logger, sessionId, uui, configMap);
				
				if(Constants.ZERO.equalsIgnoreCase(code)){
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Check book orderd : " + configMap.get(DBConstants.CHEQUEBOOKORDER));}
					String result = Constants.EMPTY_STRING+configMap.get(DBConstants.CHEQUEBOOKORDER);
					
					if(DBConstants.YES.equalsIgnoreCase(result)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Has the cheque book already ordered ? " + result );}
						callInfo.setField(Field.ISCHEQUEBOOKALREADYDONE, true);
					}
					else
					{
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Has the cheque book already ordered ? " + result );}
						callInfo.setField(Field.ISCHEQUEBOOKALREADYDONE, false);
					}
				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Result of the DB method call is " + code );}
					if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Got Failure at getting the statu of the cheque book has already ordered db method");}
				}
				
			}catch (com.db.exception.ServiceException e) {
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR: ChequeBookRequestImpl. getChequeBookAlreadyOrdered()");}
				code = Constants.ONE;
				//e.printStackTrace();
			}
			
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Exit: ChequeBookRequestImpl. getChequeBookAlreadyOrdered()");}
			return code;
		} catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR, e,   "EXCEPTION at: ChequeBookRequestImpl. getChequeBookAlreadyOrdered ()" + e.getMessage());
			throw new ServiceException(e);
		}
	}

	@Override
	public String getChequeBookFeePhrases(CallInfo callInfo)
			throws ServiceException {
		
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: ChequeBookRequestImpl.getChequeBookFeePhrases()");}
		String str_GetMessage, finalResult;
		
		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();
			
			String selectedLeafType = (String)callInfo.getField(Field.SELECTEDLEAFTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected Leaf type is "+ selectedLeafType);}
			
			//Need to get the FeatureConfig Data
			String chequeLeafFee = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CHEQUELEAF_FEE+Constants.UNDERSCORE+selectedLeafType);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Cheque leaf fee is "+ chequeLeafFee);}
			
//			double double_fee = util.isNullOrEmpty(chequeLeafFee)?Constants.GL_ZERO : Double.parseDouble(chequeLeafFee);
//			double totalFeeAmt = Constants.GL_ZERO;
//			totalFeeAmt=double_fee*Double.parseDouble(selectedLeafType);
					
			String lastNNum = (String)callInfo.getField(Field.LastNDigits);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Last N Digit is "+ lastNNum);}
			int int_LastNNum = util.isNullOrEmpty(lastNNum)?Constants.GL_FOUR : Integer.parseInt(lastNNum);
			
			String srcAcct = (String)callInfo.getField(Field.SRCNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Source Acct No ending with "+ util.getSubstring(srcAcct, int_LastNNum));}
			String finalAcctForAnnc = util.getSubstring(srcAcct, int_LastNNum);
		
			
			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			chequeLeafFee = util.isNullOrEmpty(chequeLeafFee)?Constants.ZERO : chequeLeafFee;
			dynamicValueArray.add(chequeLeafFee);
			dynamicValueArray.add(selectedLeafType + Constants.WAV_EXTENSION);
			dynamicValueArray.add(finalAcctForAnnc);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}
			
			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("CHEQUE_BOOK_REQUEST_CONFIRMATION");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("Cheque_Book_Request");
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
			//No Need
			//To have the property file grammar, need to call that util method here
			//No Need

			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);
			
			
			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}
			
			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
			
			
			//Need to handle if we want to append pipe seperator sign
			//No Need
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: ChequeBookRequestImpl.getChequeBookFeePhrases()");}
			
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at ChequeBookRequestImpl.getChequeBookFeePhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
			}
		return finalResult;
	}

	@Override
	public String getChequeBookRequestSuccPhrases(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: ChequeBookRequestImpl.getChequeBookRequestSuccPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();
			
			String noOfDays = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CHEQUE_BOOK_RECEIVE_NOOF_DAYS);
			int int_NoOfDays = util.isNullOrEmpty(noOfDays)? Constants.GL_FOUR : Integer.parseInt(noOfDays);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total No Of days for cheque book retrieval is " + int_NoOfDays);}
			
			 SimpleDateFormat spdf =new SimpleDateFormat(Constants.DATEFORMAT_YYYYMMDD);
			 Calendar cc = Calendar.getInstance();
		     int day_of_week = cc.get(Calendar.DAY_OF_WEEK);
//		     day_of_week =day_of_week +1;
		     if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Today's day of the week is " + day_of_week);}
		     
		     for(int i=0;i<int_NoOfDays;){
		    	 day_of_week = cc.get(Calendar.DAY_OF_WEEK);
		    	 if(day_of_week!=6 && day_of_week!=7)
		    		 i++;
		    	 cc.add(Calendar.DATE, 1);
		    	 
		     }
		     String strDate =spdf.format(cc.getTime());
		     if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Date after adding : " + strDate);}
		     
		     if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Resulted day after adding of the week is " + day_of_week);}
		     
			
			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;

			dynamicValueArray.add(strDate);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			//String menuID = MenuIDMap.getMenuID("");
			String anncID = AnncIDMap.getAnncID("Cheque_Book_Request_Success_Message");
			String featureID = FeatureIDMap.getFeatureID("Cheque_Book_Request");
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
			//No Need
			
			//To have the property file grammar, need to call that util method here
			//No need

			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);

			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}


			//Need to handle if we want to append pipeseperator sign
			//No Need

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: ChequeBookRequestImpl.getChequeBookRequestSuccPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at  ChequeBookRequestImpl.getChequeBookRequestSuccPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public String getNoOfChequeLeafPhrases(CallInfo callInfo)
			throws ServiceException {
		
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: ChequeBookRequestImpl.getNoOfChequeLeafPhrases()");}
		String str_GetMessage, finalResult;
		
		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();
			
			//Need to get the FeatureConfig Data
			String customerSegment = (String)callInfo.getField(Field.CUST_SEGMENT_TYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer segment is " + customerSegment);}
			
			if(customerSegment == null || Constants.EMPTY_STRING.equalsIgnoreCase(customerSegment)){
				customerSegment = Constants.DEFAULT;
			}
			
			ArrayList<String> chequeLeafList = null;
			chequeLeafList = (ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(customerSegment + Constants.CUI_NO_OF_CHEQUELEAF);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The chequeLeafList retrieved is :" + chequeLeafList);}
			
			String moreCount = (String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);
			
			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_Str = Constants.EMPTY_STRING;
			
			/**
			 * Note temp_Str is nothing but the product name.  The wave file also should recorded in the same product name
			 * 
			 * eg 10Leaf --> 10Leaf.wav
			 * 
			 */
			int temp_MoreCount = int_moreCount - 1;
			
			for(int count=Constants.GL_ZERO;count<chequeLeafList.size();count++){
				temp_Str = chequeLeafList.get(count);
				dynamicValueArray.add((temp_Str+Constants.WAV_EXTENSION).trim());
				
				if(count == temp_MoreCount){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the more option count");}
					moreOption = true;
					callInfo.setField(Field.MOREOPTIONCOUNT, int_moreCount);
				}
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added Product type "+temp_Str);}
				
				if(util.isNullOrEmpty(grammar)){
					grammar = temp_Str;
				}else{
					grammar = grammar + Constants.COMMA + temp_Str;
				}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the grammar value" + grammar);}

			}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}
			
			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("CHEQUE_BOOK_REQUEST_LEAFS");
			//String anncID = AnncIDMap.getAnncID("Account_Balance_Message");
			String featureID = FeatureIDMap.getFeatureID("Cheque_Book_Request");
			String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+menuID;
			
			/**
			 * Following are the modification done for configuring the more option of menus
			 */
			combinedKey = combinedKey + Constants.UNDERSCORE + int_moreCount;
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
			if(chequeLeafList.size()>int_moreCount){
				totalPrompt = Constants.GL_FOUR * chequeLeafList.size();
				totalPrompt = totalPrompt + Constants.GL_ONE;
				
				
				/**
				 * Added to fix the issue
				 */
				int temp1 = chequeLeafList.size() / int_moreCount;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient value is "+temp1);}

//				int temp2 =  chequeLeafList.size() % int_moreCount;
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Remainder value is "+temp2);}
//				if(temp2 > 0){
//					temp1++;
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient increased by one "+temp1);}
//				}
				totalPrompt = totalPrompt + (temp1*Constants.GL_TWO);
				//END Vinoth
				
				
			}
			else{
				totalPrompt = Constants.GL_FOUR * chequeLeafList.size();
				totalPrompt = totalPrompt + Constants.GL_ONE;
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
			//"CP_1033.wav*10Leaf.wav*CP_1019.wav*CP_1033.wav*15Leaf.wav*CP_1020.wav*CP_1033.wav*20Leaf.wav*CP_1021.wav";
			
			if(!util.isNullOrEmpty(finalResult)){
				temp_MoreCount = int_moreCount + 1;
				if(finalResult.contains(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION).trim())){
					finalResult = finalResult.replaceAll(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION),(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION)+Constants.PIPE));
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The Final Result string is after apending pipe seperator is "+finalResult);}
				}
			}
			
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: ChequeBookRequestImpl.getNoOfChequeLeafPhrases()");}
			
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at ChequeBookRequestImpl.getNoOfChequeLeafPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
			}
		return finalResult;
	}

	@Override
	public String updateChequeBookRequest(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: ChequeBookRequestImpl.updateChequeBookRequest()");}
		String code = Constants.EMPTY_STRING;
//		getConfigurationParam(callInfo);
		try{
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}
			
			/**
			 * Changed on 04Mar2013 as we need to pass the SRCNO instead of ENTERCINNUMBER
			 */
			
			String acctID = (String)callInfo.getField(Field.SRCNO);
			if(util.isNullOrEmpty(acctID)){
				throw new ServiceException("Selected Card OR Acct No is empty or null");
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Account ID ending with" + util.getSubstring(acctID, Constants.GL_FOUR));}
			
			/**
			 * Changed as need to pass the cheque leaf count 
			 */
			
			String str_LeafCount = (String)callInfo.getField(Field.SELECTEDLEAFTYPE);
//			int leafCount = util.isNullOrEmpty(str_LeafCount)?Constants.GL_ZERO:Integer.parseInt(str_LeafCount);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Leaf Count Ordered was "+ str_LeafCount);}
			
			/**
			 * For Reporting Purpose
			 */
			HostReportDetails hostReportDetails = new HostReportDetails();
			
			String featureId = (String)callInfo.getField(Field.FEATUREID);
			hostReportDetails.setHostActiveMenu(featureId);
			//hostReportDetails.setHostCounter(hostCounter);
			//hostReportDetails.setHostEndTime(hostEndTime);
			String customerIDObbj = util.isNullOrEmpty(callInfo.getField(Field.CUSTOMERID))?Constants.NA : (String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID going to insert in host is " + customerIDObbj);}


			String strHostInParam =	Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA +  Constants.HOST_INPUT_PARAM_SOURCE_NO +  Constants.EQUALTO + util.maskCardOrAccountNumber(acctID)
					+Constants.COMMA + Constants.HOST_INPUT_PARAM_NUMBER_OF_LEAVES +  Constants.EQUALTO + str_LeafCount
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_CHQBOOKORDER);
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


			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("iceFeatureData object is null or empty");
			}

			String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_CHQBOOKORDER_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_CHQBOOKORDER_REQUESTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}

			
			UpdateChequeBookOrder_HostRes updateChequeBookOrder_HostRes = chqBookOrderDAO.getChequeBookOrderUpdHostRes(callInfo, acctID, str_LeafCount, requestType);
			
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UpdateChequeBookOrder_HostRes Object is :"+ updateChequeBookOrder_HostRes);}
			callInfo.setUpdateChequeBookOrder_HostRes(updateChequeBookOrder_HostRes);

			code = updateChequeBookOrder_HostRes.getErrorCode();
			 
			/*
			 * For Reporting Start
			 */
			
			String hostEndTime = updateChequeBookOrder_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);
			
			String hostResCode = updateChequeBookOrder_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);
			
			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;
			}
			
			/****Duplicate RRN Fix 25012016 *****/
			strHostInParam =	Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA +  Constants.HOST_INPUT_PARAM_SOURCE_NO +  Constants.EQUALTO + util.maskCardOrAccountNumber(acctID)
					+Constants.COMMA + Constants.HOST_INPUT_PARAM_NUMBER_OF_LEAVES +  Constants.EQUALTO + str_LeafCount
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			/************************************/
			
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode + Constants.COMMA + Constants.HOST_OUTPUT_PARAM_TRANSREFNO + Constants.EQUALTO + updateChequeBookOrder_HostRes.getRefID()
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(updateChequeBookOrder_HostRes.getErrorDesc()) ?"NA" :updateChequeBookOrder_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);
			
			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
			
			callInfo.updateHostDetails(ivrdata);
			//End Reporting
			
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got success response for Cheque book order request service");}
			}else{

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for Update Cheque book order host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + updateChequeBookOrder_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_CHQBOOKORDER, updateChequeBookOrder_HostRes.getHostResponseCode());
			
				/**
				 * Following will be called only if there occured account selection before this host access
				 */
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
				util.setEligibleAccountCounts(callInfo, hostResCode);
			
			}
			
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at ChequeBookRequestImpl.updateChequeBookRequest() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;
	}

	@Override
	public String updateChequeBookRuleEngineDB(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try {
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: ChequeBookRequestImpl. updateChequeBookRuleEngineDB()");}

			String code = Constants.ONE;
			String sessionId = (String)callInfo.getField(Field.SESSIONID);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method ");}
			HashMap<String, Object> configMap = new HashMap<String, Object>();

			
			String currentDate = util.getTodayDateOrTime(Constants.DATEFORMAT_YYYYMMDD);
			configMap.put(DBConstants.CUSTOMERID, callInfo.getField(Field.CUSTOMERID));
			configMap.put(DBConstants.DATETIME, currentDate);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID is " + configMap.get(Constants.CUSTOMERID));}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Date is " + configMap.get(Constants.DATETIME) );}

			String uui = (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UUI of the call is " + uui);}
			
			
			DataServices dataServices = VRUDBDataServicesInstance.getInstance();
			try {
				code = dataServices.insertChequeBookOrdered(logger, sessionId, uui, configMap);
				
			} catch (com.db.exception.ServiceException e) {
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR: ChequeBookRequestImpl. updateChequeBookRuleEngineDB ()");}
				code = Constants.ONE;
				//e.printStackTrace();
			}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Result of the DB method call is " + code );}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Exit: ChequeBookRequestImpl. updateChequeBookRuleEngineDB ()");}

			return code;
		} catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR, e,   "EXCEPTION at:  ChequeBookRequestImpl. updateChequeBookRuleEngineDB ()" + e.getMessage());
			throw new ServiceException(e);
		}

	}
	//Method Added to getCaution Summary from Malaa API --Yuvaraj
	public String getCautionSummary(CallInfo callInfo) throws ServiceException
	{
		String result;
		String idType=null,idNum=null,reqRefNo,reqTimeStamp,code;
		try {
			String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
			
			if(logger.isDebugEnabled()) {
		        WriteLog.write(WriteLog.DEBUG, session_ID_, "Enter::ChequeBookRequestImpl::getCautionSummary");
		    }
			MalaaAPI malaaApi=new MalaaAPI();
		
			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if (callInfo.custDtls_HostRes() == null) {
		        if(logger.isDebugEnabled()) {
		            WriteLog.write(WriteLog.DEBUG, session_ID_, "Cust Dtls Object is null");
		        }
		        code = processCustDtlsHostRes(callInfo);
		        if(logger.isDebugEnabled()) {
		            WriteLog.write(WriteLog.DEBUG, session_ID_, "Process CustDtlsHostRes is completed");
		        }
		    }

		    // Check again if custDtls_HostRes is not null before accessing its methods
		    if (callInfo.custDtls_HostRes() != null) {
		        idType = callInfo.custDtls_HostRes().getBmLgDocName();
		        idNum = callInfo.custDtls_HostRes().getLegalDocId();
		        if(logger.isDebugEnabled()) {
		            WriteLog.write(WriteLog.DEBUG, session_ID_, "From the Cust-Dtls Host the values are-" + "idType-" + idType + "idNum-" + idNum);
		        }
		    } else {
		        if(logger.isDebugEnabled()) {
		            WriteLog.write(WriteLog.DEBUG, session_ID_, "Cust Dtls Object is still null after processing");
		        }
		    }

		    // Use hardcoded values if either idType or idNum is null or empty
		    if (idType == null || idType.isEmpty() || idNum == null || idNum.isEmpty()) {
		        if(logger.isDebugEnabled()) {
		            WriteLog.write(WriteLog.DEBUG, session_ID_, "Id Type or IdNum is null or empty");
		        }
		        idType = "NID";
		        idNum = "86141351";
		    }

		    if(logger.isDebugEnabled()) {
		        WriteLog.write(WriteLog.DEBUG, session_ID_, "Final Values for processing--" + "idType-" + idType + "idNum-" + idNum);
		    }

		     reqRefNo = (String) callInfo.getField(Field.ESBREQREFNUM);
		    if(logger.isDebugEnabled()) {
		        WriteLog.write(WriteLog.DEBUG, session_ID_, "reqrefNum-" + reqRefNo);
		    }

		    reqTimeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
		    if(logger.isDebugEnabled()) {
		        WriteLog.write(WriteLog.DEBUG, session_ID_, "reqTimeStamp-" + reqTimeStamp);
		    }

		    String malaaUrl=util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue("MalaaUrl"))? null : (String)iceFeatureData.getConfig().getParamValue("MalaaUrl");
		    if(logger.isDebugEnabled()) {
		        WriteLog.write(WriteLog.DEBUG, session_ID_, "Malaa Url from Servintuit-" + malaaUrl);
		    }
		   String malaaTokenUrl=util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue("malaaTokenUrl"))? null : (String)iceFeatureData.getConfig().getParamValue("malaaTokenUrl");
		   if(logger.isDebugEnabled()) {
		        WriteLog.write(WriteLog.DEBUG, session_ID_, "malaaTokenUrl from Servintuit" + malaaTokenUrl);
		    }
		  result = malaaApi.MalaaApiService(idNum, idType, reqRefNo, reqTimeStamp,malaaUrl,malaaTokenUrl, logger);
		  if(logger.isDebugEnabled()) {
		        WriteLog.write(WriteLog.DEBUG, session_ID_, "Host Response captured from Malaa API" + result);
		        WriteLog.write(WriteLog.DEBUG, session_ID_, "--Exit::ChequeBookRequestImpl::getCautionSummary--");
		    }
			return result;
		} catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR, e,   "EXCEPTION at:  ChequeBookRequestImpl. getCautionSummary()" + e.getMessage());
			return Constants.HOST_FAILURE;
		}
		
	}	
		
		public String processCustDtlsHostRes(CallInfo callInfo)

		{    
			String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
			String code="1";
			try {
			
				if(logger.isDebugEnabled()) {
			        WriteLog.write(WriteLog.DEBUG, session_ID_, "Enter::ChequeBookRequestImpl::processCustDtlsHostRes");
			    }
				String customerId = util.isNullOrEmpty(callInfo.getField(Field.CUSTOMERID))?Constants.NA : (String)callInfo.getField(Field.CUSTOMERID);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID going to insert in host is " + customerId);}
				String SelectedCardOrAcctNo = (String)callInfo.getField(Field.ENTEREDCINNUMBER);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected Card or Acct No-" + SelectedCardOrAcctNo);}
				String requestType =  "CQIREQS";
				//util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_CustDtls_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_CustDtls_REQUESTTYPE);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}
				if(SelectedCardOrAcctNo==null || SelectedCardOrAcctNo.isEmpty())
				{   
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected Card or Acct No is null");}
					SelectedCardOrAcctNo="4228205514593766 ";
				}
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
				hostReportDetails.setHostInParams(Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerId
						+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + Constants.NA
						+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)));
				hostReportDetails.setHostMethod(Constants.HOST_METHOD_CustDtls);
				//hostReportDetails.setHostOutParams(hostOutParams);
				hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
				hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
				hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
				hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);

				String startTime = util.getCurrentDateTime();
				hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It should be in the formate of 31/07/2013 18:11:11
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
				//CustDtlsDAOImpl custDaoImpl=new CustDtlsDAOImpl();
				//if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CustDtlsDAOImpl Object" + custDaoImpl);}
				
				if(getCustDtlsDAO()==null)
				{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "getCustDtlsDAO Object is null");}
				}
				
			//	CustDtls_HostRes custDtls_HostRes = custDaoImpl.getCustDtlsHostRes(callInfo, SelectedCardOrAcctNo, customerId, requestType);
				CustDtls_HostRes custDtls_HostRes = getCustDtlsDAO().getCustDtlsHostRes(callInfo, SelectedCardOrAcctNo, customerId, requestType);
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "custDtls_HostRes Object is :"+ custDtls_HostRes);}
				callInfo.setCustDtls_HostRes(custDtls_HostRes);
             
				code = custDtls_HostRes.getErrorCode();
				
				/*
				 * For Reporting Start
				 */

				String hostEndTime = custDtls_HostRes.getHostEndTime();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
				hostReportDetails.setHostEndTime(hostEndTime);

				String hostResCode = custDtls_HostRes.getHostResponseCode();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
				hostReportDetails.setHostResponse(hostResCode);

				String durationTime = util.hostServiceTimeDuration(startTime, hostEndTime, Constants.DATEFORMAT_dd_MM_yyyyHH_mm_ss);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Access duration is " + durationTime);}
				
				String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerId 
						+ Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_TYPE + Constants.EQUALTO + Constants.NA
						+ Constants.COMMA + Constants.HOST_INPUT_PARAM_REQUEST_TYPE + Constants.EQUALTO + Constants.NA
						+ Constants.COMMA + Constants.HOST_INPUT_PARAM_DURATION +Constants.EQUALTO + durationTime 
						+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
				hostReportDetails.setHostInParams(strHostInParam);


				String responseDesc = Constants.HOST_FAILURE;
				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
					responseDesc = Constants.HOST_SUCCESS;
				}
				String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
						+ Constants.EQUALTO + hostResCode
				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(custDtls_HostRes.getErrorDesc()) ?"NA" :custDtls_HostRes.getErrorDesc());
				hostReportDetails.setHostOutParams(hostOutputParam);

				callInfo.setHostReportDetails(hostReportDetails);
				ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

				callInfo.updateHostDetails(ivrdata);
				//End Reporting
				
				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response from CustDtlsService Host");}
				}else{

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for Customer Short Update host service");}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + custDtls_HostRes.getHostResponseCode());}

					util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_CustDtls, custDtls_HostRes.getHostResponseCode());
				}
				if(logger.isDebugEnabled()) {
					WriteLog.write(WriteLog.DEBUG, session_ID_, "Error Response Code from Cust_Dtls Host-"+code);
			        WriteLog.write(WriteLog.DEBUG, session_ID_, "Exit::ChequeBookRequestImpl::processCustDtlsHostRes");
			    }
				return code;
				
			} catch (Exception e) {
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Error while processing Cust Dtls request");}
				code="0";
				return code;
			}
			

		}
		
	}
	
	
	
	
	
	

