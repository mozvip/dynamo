package dynamo.ui.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dynamo.backlog.BackLogProcessor;
import dynamo.backlog.tasks.files.DeleteDownloadableTask;
import dynamo.core.manager.ErrorManager;

@WebServlet("/delete")
public class DeleteDownloadableServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long id = Integer.parseInt( request.getParameter("id"));
		try {
			BackLogProcessor.getInstance().schedule( new DeleteDownloadableTask(id), false );
		} catch (Exception e) {
			ErrorManager.getInstance().reportThrowable( e );
			throw new ServletException( e );
		}
	}
}
