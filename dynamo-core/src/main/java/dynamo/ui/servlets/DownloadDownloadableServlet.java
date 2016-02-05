package dynamo.ui.servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dynamo.core.model.DownloadableFile;
import dynamo.manager.DownloadableManager;

@WebServlet("/download")
public class DownloadDownloadableServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		long id = Integer.parseInt( request.getParameter("id"));
		
		Optional<DownloadableFile> firstFile = DownloadableManager.getInstance().getAllFiles( id ).findFirst();

		if (!firstFile.isPresent()) {
			throw new ServletException( "File not found");
		}

		DownloadableFile downloadableFile = firstFile.get();
		
		response.setContentLength( (int) downloadableFile.getSize() );
		response.setContentType( Files.probeContentType( downloadableFile.getFilePath() ) );
		
		response.setHeader("Content-Disposition", "filename='" + downloadableFile.getFilePath().getFileName().toString()+"'");
		
		try (OutputStream output = response.getOutputStream()) {
			Files.copy( downloadableFile.getFilePath(), output);
			output.flush();
		}
		
	}
}
