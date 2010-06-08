package org.nrg.xnat.gateway;

public final class Result implements Comparable<Result>
{
	public static final Result 
		SERVER_STARTED=new Result(0), 
		SERVER_STOPPED=new Result(1), 
		SERVER_START_FAILED=new Result(2),
//		SERVER_STOP_FAILED=new Result(3),
		PROPERTIES_FILE_ERROR=new Result(3),
		INITIALIZATION_EXCEPTION=new Result(4);		
	
	private int status=-1;
	private static String[] desc=
	{
		"Server started",
		"Server stopped",
		"Cannot start server",
//		"Cannot stop server",
		"Cannot start server: error reading properties file",
		"Cannot start server: exception during server initialization"
	};
	public String getMessage()
	{
		if(status<0 || status>=desc.length)
			return "Unknown status";
		return desc[status];
	}
	public Result(int n){status=n;}
	
	@Override
	public int compareTo(Result arg0)
	{
		if(arg0.status==status) return 0;
		return (arg0.status<status)?-1:1;
	}	
}
