package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;


@WebServlet("/CheckInvitations")
@MultipartConfig
public class CheckInvitations extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    
    public CheckInvitations() {
        super();
        
    }

    
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
    	this.connection = ConnectionHandler.getConnection(servletContext);
    }
    
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		
		HttpSession session = request.getSession();
		
		// Check session attribute existence
		if(session.getAttribute("tryTimes") == null || session.getAttribute("groupToCreate") == null) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.getWriter().println("Illegal operation: please follow the normal creation steps");
			return;
		}
		
		
		User user = (User) session.getAttribute("user");
		
		Group groupToCreate = (Group) session.getAttribute("groupToCreate");
		int tryTimes = (int) session.getAttribute("tryTimes");
		
		// Check number of tries
		if(tryTimes >= 3) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			session.removeAttribute("tryTimes");
			response.getWriter().println("Illegal operation: the number of attempts has already been exceeded");
			return;
		}
		
		
		List<String> selectedUsers = new ArrayList<>();
		String[] selectedUsersStrings = null;
		
		// Check parameter nullity
		selectedUsersStrings = request.getParameterValues("selectedUsers");
		if (selectedUsersStrings == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Please select your group member/s");
			return;
		}
		
		
		UserDAO userDAO = new UserDAO(connection);
		
		selectedUsers = Arrays.asList(selectedUsersStrings);
		Set<String> noDuplication = new HashSet<>(selectedUsers);
		
		// Check selection validity
		try {
			
			//Check if there are duplicate selections
			if(noDuplication.size() != selectedUsers.size())
				throw new Exception();
			
			List<User> users = userDAO.getAllOtherUsers(user.getUsername());
			List<String> usernames = users.stream().parallel().map(u-> u.getUsername()).collect(Collectors.toList());
			
			//Check if all selected users exist and none of they are the current user 
			for (String s : selectedUsers) {
				if(!usernames.contains(s) || s.equals(user.getUsername())) {
					throw new Exception();
				}
			} 
			/*
			for (String s : selectedUsers) {
				if(!userDAO.isUserExists(s) || s.equals(user.getUsername())) {
					throw new Exception();
				}
			} */
			
		}catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Execution error: operation failed");
			return;
		}catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid selection/s");
			return;
		}
		
		
		
		
		// Update session's stored informations
		tryTimes++;
		session.setAttribute("tryTimes", tryTimes);
		session.setAttribute("selectedUsers", selectedUsers);
		boolean isBadTries = false;
		String errorMsg = null;
		
		if(selectedUsers.size() < groupToCreate.getMinCapacity() - 1) {
			errorMsg = "The selected number of people does not meet the minimum number requirement";
			isBadTries = true;
		}
		
		if(selectedUsers.size() > groupToCreate.getMaxCapacity() - 1) {
			errorMsg = "The selected number of people does not meet the maximum number requirement";
			isBadTries = true;
		}
		
		
		if(isBadTries) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(errorMsg);
			if(tryTimes == 3) {
				session.removeAttribute("tryTimes");
				session.removeAttribute("selectedUsers");
				session.removeAttribute("groupToCreate");		
			}
			return;
		}else {
			GroupDAO groupDAO = new GroupDAO(connection);
			ParticipationDAO participationDAO = new ParticipationDAO(connection);
			
			try {
				
				// Deactivate auto-commit to ensure an atomic transaction
				connection.setAutoCommit(false);
				
				// Create new group and get its id
				int idGroup = groupDAO.createGroup(groupToCreate.getTitle(), groupToCreate.getDuration(), 
						groupToCreate.getMinCapacity(), groupToCreate.getMaxCapacity(), groupToCreate.getCreator());
				
				// Check group id, if it's -1, probably an execution error occurred
				if(idGroup == -1)
					throw new SQLException();
				
				// Create participation for the creator
				participationDAO.addParticipation(idGroup, user.getUsername());
				
				// Create participation for each user selected by the creator
				for(String username: selectedUsers) {
					participationDAO.addParticipation(idGroup, username);
				}
				
				// Commit the whole transaction
				connection.commit();
				
			}catch (SQLException e) {
				try {
					// If an SQLException is raised, undoes all the changes done by the current transaction
					connection.rollback();
				} catch (SQLException e1) {
					sendServerError(response);
				}
				sendServerError(response);
			}finally {
				try {
					// when the transaction is completed (successfully or not) 
					// reactive auto-commit
					connection.setAutoCommit(true);
				} catch (SQLException e) {
					sendServerError(response);
				}
			}
			
			// If the new creation is successful·
			response.setStatus(HttpServletResponse.SC_OK);
			
			/*
			 * unresolved problem: this didn't send to user the number of attempts
			 */
		}
		
		
		
	}
	
	private void sendServerError(HttpServletResponse response) throws IOException{
		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Execution error: creation failed");
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
}
