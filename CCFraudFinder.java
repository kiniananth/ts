package hash;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class CCFraudFinder {
	/**
	 * Get list of fraudulent CCs for a given day, exceeding a threshold
	 * @param txnCSV , expected in "10d7ce2f43e35fa57d1bbf8b1e2, 2014-04-29T13:15:54, 10.00"
	 * @param selectedDay , expected in "2014-04-29" or "yyyy-MM-dd" format
	 * @param thresholdAmount
	 * @return
	 * @throws ParseException
	 */
	List<String> findFraudCCs(List<String> txnCSV, String selectedDay, Double thresholdAmount) throws ParseException{

		List<String> retFraudCCList = null; //instantiate if needed later
		
		//validate selected date
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setLenient(false);
		Date selectedDate = sdf.parse(selectedDay);
		
		//map tracking amounts for cc
		Map<String,Double> ccAmtMap = new HashMap<String,Double>();
		
		for(String txnString : txnCSV) {
			String[] parts = txnString.split(",");
			
			//parse, validate curr date. ignoring time parts as we need day only
			String[] dateParts = parts[1].trim().split("T");
			Date currDate =  sdf.parse(dateParts[0].trim());
			
			//if selected date matches current txn
			if(currDate.equals(selectedDate)) {
				String cc = parts[0].trim();
				Double amountSoFar = ccAmtMap.getOrDefault(cc, 0.0);
				Double amount = Double.valueOf(parts[2].trim());
				amountSoFar += amount;
				ccAmtMap.put(cc, amountSoFar);
			}			
		}
		
		//threshold check done only after examining all txns for the day for all CCs
		//as perhaps cc transactions can be negative as well
		for(Map.Entry<String,Double> entry : ccAmtMap.entrySet()) {
			if(entry.getValue() > thresholdAmount) {
				if(retFraudCCList == null) {
					retFraudCCList = new ArrayList<String>();
				}
				retFraudCCList.add(entry.getKey());
			}
		}
		
		return retFraudCCList;
	}
	
	public static void main(String[] args) throws ParseException {
		//Format "10d7ce2f43e35fa57d1bbf8b1e2, 2014-04-29T13:15:54, 10.00"
		List<String> txns = new ArrayList<String>();
		txns.add("10d7ce2f43e35fa57d1bbf8b1e2, 2014-04-29T13:15:54, 10.00");
		txns.add("10d7ce2f43e35fa57d1bbf8b1e2, 2014-04-29T13:15:54, 10.00");
		txns.add("10d7ce2f43e35fa57d1bbf8b1e3, 2014-04-29T13:15:54, 10.00");
		txns.add("10d7ce2f43e35fa57d1bbf8b1e3, 2014-04-29T13:15:54, 10.00");
		txns.add("10d7ce2f43e35fa57d1bbf8b1e2, 2014-04-29T13:15:54, 10.00");
		txns.add("10d7ce2f43e35fa57d1bbf8b1e1, 2014-04-29T13:15:54, 10.00");
		txns.add("10d7ce2f43e35fa57d1bbf8b1e3, 2014-04-29T13:15:54, -10.00");
		txns.add("10d7ce2f43e35fa57d1bbf8b1e4, 2014-04-30T13:15:54, 20.00");
		txns.add("10d7ce2f43e35fa57d1bbf8b1e5, 2014-04-30T13:15:54, 10.00");
		txns.add("10d7ce2f43e35fa57d1bbf8b1e6, 2014-01-30T13:15:54, 10.00");
		txns.add("10d7ce2f43e35fa57d1bbf8b1e6, 2014-01-30T13:15:54, 30.00");
		
		List<String> fraudCCs = new CCFraudFinder().findFraudCCs(txns,"2014-04-29",15.0);
		System.out.println(fraudCCs); // contains xxx2
		
		/**
		 * Explanation for transactions above and expected output
		 * card xxx1 same day as selected | no exceed | N in output
		 * card xxx2 same day as selected | exceeds | Y in output list
		 * card xxx3 same day as selected | exceeds once, but has negative later in the day | N in output
		 * card xxx4 later day than selected | exceeds | N in output 
		 * card xxx5 later day than selected | does not exceed | N in output
		 * card xxx6 earlier day than selected | does not exceed | N in output
		 * card xxx7 earlier day than selected | exceeds | N in output
		 */
		
	}
}
