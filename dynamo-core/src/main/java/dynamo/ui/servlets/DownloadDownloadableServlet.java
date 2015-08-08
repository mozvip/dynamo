package dynamo.ui.servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dynamo.core.manager.DownloadableFactory;
import dynamo.model.DownloadInfo;

@WebServlet("/download")
public class DownloadDownloadableServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		long id = Integer.parseInt( request.getParameter("id"));
		DownloadInfo info = DownloadableFactory.getInstance().getDownloadInfo(id);

		response.setContentLength( (int) Files.size( info.getPath() ));
		response.setContentType( Files.probeContentType( info.getPath() ) );
		
		response.setHeader("Content-Disposition", "filename='" + info.getPath().getFileName().toString()+"'");
		
		try (OutputStream output = response.getOutputStream()) {
			Files.copy( info.getPath(), output);
			output.flush();
		}
		
	}
}
