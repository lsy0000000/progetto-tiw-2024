package it.polimi.tiw.utils;

import java.sql.Timestamp;
import java.time.LocalDate;

public class DateHandler {
	/**
	 * @return the current timeStamp without considering the time
	 */
	static public Timestamp getToday() {
		// get current time-stamp
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		// convert to localDate
	    LocalDate ld = ts.toLocalDateTime().toLocalDate(); 
	    // reconvert to Timestamp and return
		return Timestamp.valueOf(ld.atStartOfDay());
	}
}
