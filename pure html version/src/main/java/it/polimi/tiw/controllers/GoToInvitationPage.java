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
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.PathBook;
import it.polimi.tiw.utils.TemplateHandler;


@WebServlet("/GoToInvitationPage")
public class GoToInvitationPage extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       
	
    public GoToInvitationPage() {
        super();
    }

    
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
    	this.connection = ConnectionHandler.getConnection(servletContext);
    	this.templateEngine = TemplateHandler.getEngine(servletContext, ".html");
    }
    
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		User currentUser = (User) session.getAttribute("user");
		
		List<String> selectedUsers = new ArrayList<>();
		
		// Get already selected users, if there are any
		if (session.getAttribute("selectedUsers") == null) {
			session.setAttribute("selectedUsers", selectedUsers);
		}else {
			// cast object to list of string
			Object object = session.getAttribute("selectedUsers");
			if (object instanceof List<?>) {
				List<?> obj = (List<?>) object;
				for(Object o: obj) {
					selectedUsers.add(String.class.cast(o));
				}
			}
			//selectedUsers = (List<String>) session.getAttribute("selectedUsers");
		}
		
		
		UserDAO userDAO = new UserDAO(connection);
		List<User> users = new ArrayList<User>();
		
		// Get the list of all other users except the current one
		try {
			users = userDAO.getAllOtherUsers(currentUser.getUsername());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An execution error occured while loading the users.");
			return;
		}
		
		String invitationErrorMsg = null;
		if(session.getAttribute("invitationErrorMsg") != null) {
			invitationErrorMsg = (String) session.getAttribute("invitationErrorMsg");
		}else {
			invitationErrorMsg = "";
		}
		
		// Redirect to the page that allows to make invitations
		String path = PathBook.pathToInvitationPage;
		ServletContext servletContext = getServletContext();
		Group groupToCreate = (Group) session.getAttribute("groupToCreate");
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("groupToCreate", groupToCreate);
		ctx.setVariable("users", users);
		ctx.setVariable("selectedUsers", selectedUsers);
		ctx.setVariable("invitationErrorMsg", invitationErrorMsg);
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
