package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;


@WebServlet("/GetAllUsers")
@MultipartConfig
public class GetAllUsers extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
       
    public GetAllUsers() {
        super();
    }
    
    
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
    	this.connection = ConnectionHandler.getConnection(servletContext);
    }


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		User currentUser = (User) session.getAttribute("user");
		
		// Create the list -- ?
		if (session.getAttribute("selectedUsers") == null) {
			List<String> selectedUsers = new ArrayList<>();
			session.setAttribute("selectedUsers", selectedUsers);
		}
		
		
		UserDAO userDAO = new UserDAO(connection);
		List<User> users = new ArrayList<User>();
		
		// Get the list of all other users except the current one
		try {
			users = userDAO.getAllOtherUsers(currentUser.getUsername());
		} catch (SQLException e) {
			sendError500(response, "An execution error occured while loading the users.");
			return;
		}
		
		
		// Write response content
		Gson gson = new Gson();
		String jsonUsers = gson.toJson(users);
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonUsers);
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
	
	private void sendError500(HttpServletResponse response, String errorMsg) throws IOException {
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		response.getWriter().println(errorMsg);
	}

}
