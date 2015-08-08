package dynamo.core.logging;

import java.io.Serializable;

public class LogStackTraceElement implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private LogItem logItem;

	private int id;

	private String declaringClass;

	private String methodName;

	private String fileName;

	private int lineNumber;
	
	public LogStackTraceElement() {
	}

	public LogStackTraceElement(String declaringClass, String methodName,
			String fileName, int lineNumber) {
		super();
		this.declaringClass = declaringClass;
		this.methodName = methodName;
		this.fileName = fileName;
		this.lineNumber = lineNumber;
	}

	public String getDeclaringClass() {
		return declaringClass;
	}

	public void setDeclaringClass(String declaringClass) {
		this.declaringClass = declaringClass;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public LogItem getLogItem() {
		return logItem;
	}

	public void setLogItem(LogItem logItem) {
		this.logItem = logItem;
	}

}
