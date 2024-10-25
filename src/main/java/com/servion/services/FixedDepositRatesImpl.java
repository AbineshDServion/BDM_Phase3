package com.servion.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.db.beans.TblFDRates;
import com.db.dataservices.DataServices;
import com.db.resource.DBConstants;
import com.db.resource.VRUDBDataServicesInstance;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.DepositDtlsInqDAO;
import com.servion.exception.ServiceException;
import com.servion.model.AnncIDMap;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;
import com.servion.model.MenuIDMap;
import com.servion.model.fixedDepositRates.FixedDepositsRateDetails_DBRes;

public class FixedDepositRatesImpl implements IFixedDepositRates {
	private static Logger logger = LoggerObject.getLogger();

	private DepositDtlsInqDAO depositDtlsInqDAO;
	private MessageSource messageSource;


	public DepositDtlsInqDAO getDepositDtlsInqDAO() {
		return depositDtlsInqDAO;
	}

	public void setDepositDtlsInqDAO(DepositDtlsInqDAO depositDtlsInqDAO) {
		this.depositDtlsInqDAO = depositDtlsInqDAO;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}


	@Override
	public String getFDRatesDetails(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try {
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: FixedDepositRatesImpl. getFDRatesDetails()");}

			String code = Constants.ONE;
			String sessionId = (String)callInfo.getField(Field.SESSIONID);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method ");}
			HashMap<String, Object> configMap = new HashMap<String, Object>();

			String uui = (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UUI of the call is " + uui);}
			
			
			DataServices dataServices = VRUDBDataServicesInstance.getInstance();
			try {
				code = dataServices.getFDRates(logger, sessionId, uui, configMap);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The response code for GetFDRates db access is " + code );}

				List<TblFDRates> tblFDRatesList = null;

				FixedDepositsRateDetails_DBRes fixedDepositsRateDetails_DBRes = new FixedDepositsRateDetails_DBRes();
				fixedDepositsRateDetails_DBRes.setErrorDesc(Constants.HOST_FAILURE);
				fixedDepositsRateDetails_DBRes.setErrorCode(code);

				if(Constants.ZERO.equalsIgnoreCase(code)){
					tblFDRatesList = (List<TblFDRates>)configMap.get(DBConstants.FDRATESLIST);

					callInfo.setField(Field.TenureDetailsList, tblFDRatesList);
					fixedDepositsRateDetails_DBRes.setTblFDRatesList(tblFDRatesList);
					fixedDepositsRateDetails_DBRes.setErrorDesc(Constants.HOST_SUCCESS);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setted the Tenure deatail list in the callInfo" + tblFDRatesList );}

					Map<String, TblFDRates> tblFDRatestMap = new LinkedHashMap<String, TblFDRates>();
					for(int i =0; i < tblFDRatesList.size(); i++){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the "+ i + "result " + tblFDRatesList.get(i));}
						tblFDRatestMap.put(tblFDRatesList.get(i).getDnSCHPhrase(), tblFDRatesList.get(i));
					}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed the FD Rate Map bean "+ tblFDRatestMap);}
					fixedDepositsRateDetails_DBRes.setTblFDRatesMap(tblFDRatestMap);
				}

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the DB response details for fixedDepositsRateDetails_DBRes" + fixedDepositsRateDetails_DBRes );}
				callInfo.setFixedDepositsRateDetails_DBRes(fixedDepositsRateDetails_DBRes);
			} catch (com.db.exception.ServiceException e) {
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR:  FixedDepositRatesImpl. getFDRatesDetails ()");}
				code = Constants.ONE;
				//e.printStackTrace();
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Result of the DB method call is " + code );}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Exit:  FixedDepositRatesImpl. getFDRatesDetails()");}

			return code;
		} catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR, e,   "EXCEPTION at:  FixedDepositRatesImpl. getFDRatesDetails ()" + e.getMessage());
			throw new ServiceException(e);
		}

	}

	@Override
	public String getFDRatesMenuPhrases(CallInfo callInfo) throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: FixedDepositRatesImpl.getFDRatesMenuPhrases()");}
		String str_GetMessage, finalResult,dbResult;

		try{
			
			String code = getFDRatesDetails(callInfo);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Results of Fixed Deposit rate DB method is "+ code );}
			
			if(Constants.ONE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Returning empty string to OD");}
				return Constants.EMPTY_STRING;
			}
			
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();

			//Need to get the FeatureConfig Data
			ArrayList<TblFDRates> tblFDRateList = new ArrayList<TblFDRates>();
			FixedDepositsRateDetails_DBRes fixedDepositsRateDetails_DBRes = callInfo.getFixedDepositsRateDetails_DBRes();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved fixedDeposit Rate Details is "+ fixedDepositsRateDetails_DBRes);}


			LinkedHashMap<String, TblFDRates> tblFDRatestMap =  null;
			if(!util.isNullOrEmpty(fixedDepositsRateDetails_DBRes)){
				tblFDRatestMap = (LinkedHashMap<String, TblFDRates>)fixedDepositsRateDetails_DBRes.getTblFDRatesMap();
				tblFDRateList = (ArrayList<TblFDRates>)fixedDepositsRateDetails_DBRes.getTblFDRatesList();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "TBL FD rates list is "+ tblFDRateList);}
			}

			String moreCount = (String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			TblFDRates temp_Str = null;

			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			int temp_MoreCount = int_moreCount - 1;

			Iterator iter = tblFDRatestMap.keySet().iterator();
			String key = Constants.EMPTY_STRING;
			int i = Constants.GL_ZERO;

			while(iter.hasNext()) {
				i++;
				key = (String)iter.next();
				temp_Str = (TblFDRates)tblFDRatestMap.get(key);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved the TblFDRate object from the MAP"+ temp_Str);}

				dynamicValueArray.add((temp_Str.getDnSCHPhrase()).trim());

				if(i == temp_MoreCount){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the more option count");}
					moreOption = true;
					callInfo.setField(Field.MOREOPTIONCOUNT, int_moreCount);
				}

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added Tenure type "+temp_Str.getDnSCHPhrase());}

				if(util.isNullOrEmpty(grammar)){
					grammar = temp_Str.getDnSCHPhrase();
				}else{
					grammar = grammar + Constants.COMMA + temp_Str.getDnSCHPhrase();
				}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the grammar value" + grammar);}

			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("FIXED_DEPOSIT_TENURE_SELECTION");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("Fixed_Deposit_Rate");
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
			totalPrompt = Constants.GL_THREE * tblFDRateList.size();
			totalPrompt = totalPrompt + Constants.GL_ONE;

			/**
			 * Added to fix the issue
			 */
			int temp1 = tblFDRateList.size() / int_moreCount;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient value is "+temp1);}

//			int temp2 =  tblFDRateList.size() % int_moreCount;
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Remainder value is "+temp2);}
//			if(temp2 > 0){
//				temp1++;
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient increased by one "+temp1);}
//			}
			totalPrompt = totalPrompt + (temp1*Constants.GL_TWO);
			//END Vinoth
			
			
			
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
			if(!util.isNullOrEmpty(finalResult)){
				temp_MoreCount = int_moreCount + 1;
				if(finalResult.contains(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION).trim())){
					finalResult = finalResult.replaceAll(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION),(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION)+Constants.PIPE));
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The Final Result string is after apending pipe seperator is "+finalResult);}
				}
			}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: FixedDepositRatesImpl.getFDRatesMenuPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at FixedDepositRatesImpl.getFDRatesMenuPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public String getFDRatesPhrase(CallInfo callInfo) throws ServiceException {
		
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: FixedDepositRatesImpl.getFDRatesPhrase()");}
		String str_GetMessage, finalResult,tenureInterestPhrase=Constants.EMPTY_STRING;
		
		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			//Need to get the FeatureConfig Data
			String tenurType = (String)callInfo.getField(Field.SELECTEDTENURETYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Selected tenure type is "+tenurType);}
			
			String tenurePhrase = Constants.EMPTY_STRING;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Initializing the tenure phrase");}
			
			
			HashMap<String, TblFDRates> tblFDRatestMap = null;
			TblFDRates tblFDRates = null;
			
			if(!util.isNullOrEmpty(callInfo.getFixedDepositsRateDetails_DBRes())){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Fixed Deposit Rate Details retrieved");}
				tblFDRatestMap = (HashMap<String, TblFDRates>) callInfo.getFixedDepositsRateDetails_DBRes().getTblFDRatesMap();
				
				tblFDRates = tblFDRatestMap.get(tenurType);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The TblFDRates object is " + tblFDRates);}
				
				tenurePhrase = tblFDRates.getDnSCHPhrase();
				tenureInterestPhrase = tblFDRates.getDcDecimal();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The Tenure phrase " + tenurePhrase);}
			}
			
			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			
			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			//dynamicValueArray.add(tenurType+Constants.WAV_EXTENSION);
			dynamicValueArray.add(tenurePhrase);
			dynamicValueArray.add(tenureInterestPhrase);
			
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}
			
			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			//String menuID = MenuIDMap.getMenuID("Tenure_Message");
			String anncID = AnncIDMap.getAnncID("Tenure_Message");
			
			String featureID = FeatureIDMap.getFeatureID("Fixed_Deposit_Rate");
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
			//No Need

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
			
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: ProductInformationImpl.getProductInformationMenuPhrases()");}
			
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at ReportLossCardImpl.getReportLostCardMenuPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
			}
		return finalResult;
	}

}
