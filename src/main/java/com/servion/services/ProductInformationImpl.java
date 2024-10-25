package com.servion.services;

import java.util.ArrayList;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;
import com.servion.model.MenuIDMap;

public class ProductInformationImpl implements IProductInformation {
	
	private static Logger logger = LoggerObject.getLogger();
	
	private MessageSource messageSource;

	public MessageSource getMessageSource() {
		return messageSource;
	}

	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
	

	@Override
	public String getProductInformationMenuPhrases(CallInfo callInfo)
			throws ServiceException {
		
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: ProductInformationImpl.getProductInformationMenuPhrases()");}
		String str_GetMessage, finalResult;
		
		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();
			
			ArrayList<String> productList = null;
			productList = (ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_PRODUCTS_KEY);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The product list retrieved is :" + productList);}
			
			String moreCount = (String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);
			
			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_Str = Constants.EMPTY_STRING;
			
			/**
			 * Note temp_Str is nothing but the product name.  The wave file also should recorded in the same product name
			 * 
			 * eg Savings --> Savings.wav
			 * 
			 */
			int temp_MoreCount = int_moreCount - 1;
			
			for(int count=Constants.GL_ZERO;count<productList.size();count++){
				temp_Str = productList.get(count);
				//Adding phrase and dynamic values
//				dynamicValueArray.add(DynaPhraseConstants.PHRASE_FOR);
				dynamicValueArray.add((temp_Str+Constants.WAV_EXTENSION).trim());
//				dynamicValueArray.add((DynaPhraseConstants.PHRASE_PRESS_+count).trim());
				
				if(count == temp_MoreCount){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the more option count");}
//					dynamicValueArray.add(DynaPhraseConstants.Account_Selection_1012);
//					dynamicValueArray.add(DynaPhraseConstants.PHRASE_PRESS_6);
//					dynamicValueArray.add(Constants.PIPESEPERATOR);
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

			String menuID = MenuIDMap.getMenuID("PRODUCT_INFORMATION_SELECTION");
			//String anncID = AnncIDMap.getAnncID("Account_Balance_Message");
			String featureID = FeatureIDMap.getFeatureID("Product_Information");
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
			if(productList.size()>int_moreCount){
				totalPrompt = Constants.GL_THREE * productList.size();
				//totalPrompt = totalPrompt + Constants.GL_TWO;
				
				/**
				 * Added to fix the issue
				 */
				int temp1 = productList.size() / int_moreCount;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient value is "+temp1);}

//				int temp2 =  productList.size() % int_moreCount;
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Remainder value is "+temp2);}
//				if(temp2 > 0){
//					temp1++;
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient increased by one "+temp1);}
//				}
				totalPrompt = totalPrompt + (temp1*Constants.GL_TWO);
				//END Vinoth
				
				
			}
			else{
				totalPrompt = Constants.GL_THREE * productList.size();
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
			//"CP_1033.wav*SAVING.wav*CP_1019.wav*CP_1033.wav*CURRENT.wav*CP_1020.wav*CP_1033.wav*FIXEDDEPOSIT.wav*CP_1021.wav";
			
			if(!util.isNullOrEmpty(finalResult)){
				temp_MoreCount = int_moreCount + 1;
				if(finalResult.contains(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION).trim())){
					finalResult = finalResult.replaceAll(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION),(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION)+Constants.PIPE));
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The Final Result string is after apending pipe seperator is "+finalResult);}
				}
			}
			
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: ProductInformationImpl.getProductInformationMenuPhrases()");}
			
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at ProductInformationImpl.getProductInformationMenuPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
			}
		return finalResult;
	}

	@Override
	public String getProductInformationPhrases(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try{
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: ProductInformationImpl.getProductInformationPhrases()");}
			String finalResult = DynaPhraseConstants.SILENCE_PHRASE;

			String selectedProduct = (String) callInfo.getField(Field.SELECTEDPRODUCT);

			if(util.isNullOrEmpty(selectedProduct)){
				throw new ServiceException("the selected product is null / empthy");
			}

			finalResult = selectedProduct + Constants._INFO + Constants.WAV_EXTENSION;
			finalResult = finalResult.trim();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
			return finalResult;
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at ProductInformationImpl.getProductInformationMenuPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}

	}

}
