package dynamo.server;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;

import dynamo.core.DynamoApplication;
import dynamo.ui.servlets.DownloadDownloadableServlet;
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
				new ServletInfo("WantDownloadableServlet", WantDownloadableServlet.class).addMapping("/want")
		);
	}

	public static void main(String[] args) throws Exception {
		new Dynamo();
	}

}
