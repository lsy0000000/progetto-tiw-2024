package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.beans.Group;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.GroupDAO;
import it.polimi.tiw.dao.ParticipationDAO;
import it.polimi.tiw.utils.ConnectionHandler;


@WebServlet("/DeleteParticipation")
@MultipartConfig
public class DeleteParticipation extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;   
   
	
    public DeleteParticipation() {
        super();
    }
    
    
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
    	this.connection = ConnectionHandler.getConnection(servletContext);
    }
    

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		boolean isBadRequest = false;
		Integer idGroup = null;
		String participant = null;
		
		// Check parameters nullity
		try {
			idGroup = Integer.parseInt(request.getParameter("idGroup"));
			participant = request.getParameter("participant");
		} catch (NumberFormatException | NullPointerException e) {
			isBadRequest = true;
		}
		if (isBadRequest) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Deletion failed: \nIncorrect or missing param values");
			return;
		}
		
		HttpSession session = request.getSession();	
		User user = (User) session.getAttribute("user");
		
		// check if participant to be deleted is current user 
		if(participant.equals(user.getUsername())) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Deletion failed: \nyou can't delete yourself");
			return;
		}

		GroupDAO groupDAO = new GroupDAO(connection);
		Group group = null;
		
		// Try to find and check if there is a group with requested id
		try {
			group = groupDAO.findGroupById(idGroup);
		} catch (SQLException e) {
			sendError500(response);
			return;
		}
		if (group == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Deletion failed: \ngroup id invalid");
			return;
		}
		
		
		// Check if the requester is the creator of group
		if (!group.getCreator().equals(user.getUsername())) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Unauthorized operation: you are not the creator");
			return;
		}
		
		
		ParticipationDAO participationDAO = new ParticipationDAO(connection);
		List<User> participants = null;
		
		// Try to find group's participants
		try {
			participants = participationDAO.findParticipantsByGroupId(idGroup);
		} catch (SQLException e) {
			sendError500(response);
		}
		
		
		final String p = participant;
		boolean isParticipantIn = participants.stream().parallel().anyMatch(u -> u.getUsername().equals(p));
		
		// Check if the participant to be deleted is in that groups
		if (!isParticipantIn) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Deletion failed: \nparticipant username invalid");
			return;
		}
		
		if (participants.size() == group.getMinCapacity()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Deletion failed: \nthis cancellation will violate the minimum required number of users.");
			return;
		}
		
		
		// If none of the above
		try {
			participationDAO.deleteParticipation(idGroup, participant);
		} catch (SQLException e) {
			sendError500(response);
			return;
		}
		
		response.setStatus(HttpServletResponse.SC_OK);
		
		
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
	
	private void sendError500(HttpServletResponse response) throws IOException{
		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Execution error: operation failed");
		/*
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		response.getWriter().println("Execution error: operation failed");*/
		
	}

}
