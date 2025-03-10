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


@WebServlet("/GoToHomePage")
public class GoToHomePage extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection;
       
    public GoToHomePage() {
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
		
		List<Group> createdGroups = new ArrayList<Group>();
		List<Group> invitedToGroups = new ArrayList<Group>();
		
		GroupDAO groupDAO = new GroupDAO(connection);
		ParticipationDAO invitationDAO = new ParticipationDAO(connection);
		
		//Extract user created groups data from db
		try {
			createdGroups = groupDAO.findGroupsByCreator(user.getUsername());
		} catch (SQLException e) {
			String errorMsg = "An execution error occured when loading user created groups";
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMsg);
			return;
		}
		
		//Extract groups data that user was invited to  from db
		try {
			invitedToGroups = invitationDAO.findGroupsByInvitee(user.getUsername());
		} catch (SQLException e) {
			String errorMsg = "An execution error occured when loading user partecipate as invitee groups";
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMsg);
			return;
		}
		
		//Remove attributes relating last failed group creation
		session.removeAttribute("tryTimes");
		session.removeAttribute("selectedUsers");
		session.removeAttribute("invitationErrorMsg");
		session.removeAttribute("groupToCreate");

		
		//Redirect to the Home page and add two lists of groups to the parameters
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		String path = PathBook.pathToHomePage;
		ctx.setVariable("user", user);
		ctx.setVariable("createdGroups", createdGroups);
		ctx.setVariable("invitedToGroups", invitedToGroups);
		templateEngine.process(path, ctx, response.getWriter());
	
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
