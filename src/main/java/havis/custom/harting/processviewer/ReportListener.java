package havis.custom.harting.processviewer;

import havis.middleware.ale.service.ec.ECReport;

public interface ReportListener {
	public void fire(ECReport ecReport) throws Exception;

}
