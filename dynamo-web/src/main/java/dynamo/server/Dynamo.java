package dynamo.server;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;

import dynamo.core.DynamoApplication;
import dynamo.ui.servlets.DeleteDownloadableServlet;
import dynamo.ui.servlets.DownloadDownloadableServlet;
import dynamo.ui.servlets.IgnoreDownloadableServlet;
import dynamo.ui.servlets.RedownloadDownloadableServlet;
import dynamo.ui.servlets.WantDownloadableServlet;
import io.undertow.servlet.api.ServletInfo;

public class Dynamo extends DynamoApplication {

	public Dynamo() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			ServletException, IOException {
		super();
	}

	@Override
	protected String getApplicationName() {
		return "Dynamo";
	}

	@Override
	protected List<ServletInfo> getCustomServletsInfo() {
		return Arrays.asList(
				new ServletInfo("DownloadDownloadableServlet", DownloadDownloadableServlet.class).addMapping("/download"),
				new ServletInfo("RedownloadDownloadableServlet", RedownloadDownloadableServlet.class).addMapping("/redownload"),
				new ServletInfo("WantDownloadableServlet", WantDownloadableServlet.class).addMapping("/want"),
				new ServletInfo("IgnoreDownloadableServlet", IgnoreDownloadableServlet.class).addMapping("/ignore"),
				new ServletInfo("DeleteDownloadableServlet", DeleteDownloadableServlet.class).addMapping("/delete")
		);
	}

	public static void main(String[] args) throws Exception {
		new Dynamo();
	}

}
