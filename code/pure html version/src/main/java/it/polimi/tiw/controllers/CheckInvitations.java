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
import it.polimi.tiw.utils.PathBook;


@WebServlet("/CheckInvitations")
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
		
		String contextPath = getServletContext().getContextPath();
		String cancelCreationPath = contextPath + PathBook.pathToCancellationServlet;
		String goToInvitationPath = contextPath + PathBook.pathToInvitationServlet;
		
		HttpSession session = request.getSession();
		
		// Check session attribute nullity
		if(session.getAttribute("tryTimes") == null || session.getAttribute("groupToCreate") == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal operation");
			return;
		}
		
		
		User user = (User) session.getAttribute("user");
		
		Group groupToCreate = (Group) session.getAttribute("groupToCreate");
		int tryTimes = (int) session.getAttribute("tryTimes");
		
		// Check number of tries
		if(tryTimes >= 3) {
			response.sendRedirect(cancelCreationPath);
			return;
		}
		
		
		List<String> selectedUsers = new ArrayList<>();
		String[] selectedUsersStrings = null;
		
		// Check parameter nullity
		selectedUsersStrings = request.getParameterValues("selectedUsers");
		if (selectedUsersStrings == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
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
			
		}catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Execution error");
			return;
		}catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid selection/s");
			return;
		}
		
		
		
		
		// Update session's stored informations
		tryTimes++;
		session.setAttribute("tryTimes", tryTimes);
		session.setAttribute("selectedUsers", selectedUsers);
		boolean isBadTries = false;
		
		if(selectedUsers.size() < groupToCreate.getMinCapacity() - 1) {
			int n = groupToCreate.getMinCapacity() - selectedUsers.size() - 1;
			session.setAttribute("invitationErrorMsg", "Too few selected users, add at least " + String.valueOf(n));
			isBadTries = true;
		}
		
		if(selectedUsers.size() > groupToCreate.getMaxCapacity() - 1) {
			int n = selectedUsers.size() - groupToCreate.getMaxCapacity() + 1;
			session.setAttribute("invitationErrorMsg", "Too many users selected, delete at least " + String.valueOf(n));
			isBadTries = true;
		}
		
		
		if(isBadTries) {
			if(tryTimes == 3) {
				response.sendRedirect(cancelCreationPath);
			}else {
				response.sendRedirect(goToInvitationPath);
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
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Execution error, creation failed");
				}
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Execution error, creation failed");
			}finally {
				try {
					// when the transaction is completed (successfully or not) 
					// reactive auto-commit
					connection.setAutoCommit(true);
				} catch (SQLException e) {
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Execution error, creation failed");
				}
			}
		}
		
		String path = getServletContext().getContextPath() + PathBook.pathToHomeServlet;
		response.sendRedirect(path);
		
	}
	
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
}
