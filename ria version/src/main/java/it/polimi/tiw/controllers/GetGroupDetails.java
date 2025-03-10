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

import it.polimi.tiw.beans.Group;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.GroupDAO;
import it.polimi.tiw.dao.ParticipationDAO;
import it.polimi.tiw.utils.ConnectionHandler;


@WebServlet("/GetGroupDetails")
@MultipartConfig
public class GetGroupDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
       
    public GetGroupDetails() {
        super();
    }
    
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
    	this.connection = ConnectionHandler.getConnection(servletContext);
    }
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();	
		User user = (User) session.getAttribute("user");
		
		Integer idGroup = null;
		
		// get and check parameter
		try {
			idGroup = Integer.parseInt(request.getParameter("idGroup"));
		} catch (NumberFormatException | NullPointerException e) {
			sendError400(response, "Incorrect param values");
			return;
		}
		
		
		GroupDAO groupDAO = new GroupDAO(connection);		
		Group group = null;
		
		//Check group existence
		try {
			group = groupDAO.findGroupById(idGroup);
		} catch (SQLException e) {
			sendError500(response);
			return;
		}
		
		//in case not exists
		if (group == null) {
			sendError404(response);
			return;
		}
		
		
		ParticipationDAO participation = new ParticipationDAO(connection);
		List<User> participants = new ArrayList<>();
		
		//Find the group's participants
		try {
			participants = participation.findParticipantsByGroupId(idGroup);
		} catch (SQLException e) {
			sendError500(response);
			return;
		}
		
		String requesterUsername = user.getUsername();
		boolean isUserAuthorized = participants.stream().anyMatch(u -> u.getUsername().equals(requesterUsername));
		
		if(isUserAuthorized) {
			// write group details
			Gson gson = new Gson();
			String jsonParticipants = gson.toJson(participants);
			//String jsonGroup = gson.toJson(group);
			//String groupDetails = "[" + jsonGroup + "," + jsonParticipants + "]";
			
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().write(jsonParticipants);
			
		}else {
			sendError400(response, "Request for group information denied, you do not participate in the requested group");
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

	
	private void sendError400(HttpServletResponse response, String errorMsg) throws IOException {
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		response.getWriter().println(errorMsg);
	}
	
	private void sendError404(HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		response.getWriter().println("Resource not found");
	}
	
	private void sendError500(HttpServletResponse response) throws IOException {
		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Execution error: group recover failed");
	}
}
