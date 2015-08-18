package dynamo.ui.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import dynamo.manager.LocalImageCache;

/**
 * Servlet implementation class ExternalDataServlet
 */
@WebServlet("/data/*")
public class ExternalDataServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String uri = request.getRequestURI();
		uri = URLDecoder.decode(uri, "UTF-8");
		Path path = LocalImageCache.getInstance().resolveLocal( uri );
		if (!Files.isReadable( path )) {
			response.sendError( 404 );
		} else {
			
			Calendar calendar = Calendar.getInstance();
			calendar.add( Calendar.DAY_OF_MONTH, 7);
			
			response.addDateHeader("Expires", calendar.getTimeInMillis());

			try (InputStream input = Files.newInputStream( path )) {
				IOUtils.copy( input, response.getOutputStream() );
			}
		}
	}

}
