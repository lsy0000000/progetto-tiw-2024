package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.beans.Group;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.GroupDAO;
import it.polimi.tiw.dao.ParticipationDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.PathBook;
import it.polimi.tiw.utils.TemplateHandler;


@WebServlet("/GetGroupDetails")
public class GetGroupDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection;
       
    public GetGroupDetails() {
        super();
    }
    
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
    	this.connection = ConnectionHandler.getConnection(servletContext);
    	this.templateEngine = TemplateHandler.getEngine(servletContext, ".html");
    }
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();	
		User user = (User) session.getAttribute("user");
		
		Integer idGroup = null;
		
		// get and check parameter
		try {
			idGroup = Integer.parseInt(request.getParameter("idGroup"));
		} catch (NumberFormatException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect param values");
			return;
		}
		
		
		GroupDAO groupDAO = new GroupDAO(connection);		
		Group group = null;
		
		//Check group existence
		try {
			group = groupDAO.findGroupById(idGroup);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover group");
			return;
		}
		
		//in case not exists
		if (group == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found");
			return;
		}
		
		
		ParticipationDAO participation = new ParticipationDAO(connection);
		List<User> participants = new ArrayList<>();
		
		//Find the group's participants
		try {
			participants = participation.findParticipantsByGroupId(idGroup);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover group");
			return;
		}
		
		String requesterUsername = user.getUsername();
		boolean isUserAuthorized = participants.stream().anyMatch(u -> u.getUsername().equals(requesterUsername));
		
		if(isUserAuthorized) {
			// Redirect to the group details page and add the parameters
			String path = PathBook.pathToGroupDetailsPage;
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("group", group);
			ctx.setVariable("participants", participants);
			templateEngine.process(path, ctx, response.getWriter());
			
		}else {
			
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request for group information denied, you do not participate in the requested group");
		}
		
		
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
