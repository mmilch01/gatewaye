package org.nrg.xnat.gateway;

import java.text.DateFormat;
import java.util.Date;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

public final class Tools
{
	public static Logger logger = Logger.getRootLogger();
	public static void LogMessage(int priority, String msg)
	{
		String s = getMsg(msg);
		if (priority >= Priority.ERROR_INT)
		{
			StackTraceElement[] st = Thread.currentThread().getStackTrace();
			if (st.length > 1)
				for (int i = 1; i < st.length; i++)
				{
					s += "\n" + st[i].toString();
				}
		}
		Thread.currentThread().getStackTrace();
		logger.log(Level.toLevel(priority), s);
		System.err.println(msg);
	}
	public static void LogException(Priority p, String msg, Throwable t)
	{
		logger.log(p, getMsg(msg), t);
		System.err.println(msg + "exception: ");
		t.printStackTrace(System.err);
	}
	private static String getMsg(String msg)
	{
		return DateFormat.getDateTimeInstance().format(new Date()) + ": " + msg;
	}
}