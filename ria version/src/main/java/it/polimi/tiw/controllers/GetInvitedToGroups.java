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
import it.polimi.tiw.dao.ParticipationDAO;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/GetInvitedToGroups")
@MultipartConfig
public class GetInvitedToGroups extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection;
	
    public GetInvitedToGroups() {
        super();
    }

    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
    	this.connection = ConnectionHandler.getConnection(servletContext);
    }
    
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();	
		User user = (User) session.getAttribute("user");
		
		List<Group> invitedToGroups = new ArrayList<Group>();
		ParticipationDAO invitationDAO = new ParticipationDAO(connection);
		
		//Extract groups data that user was invited to  from db
		try {
			invitedToGroups = invitationDAO.findGroupsByInvitee(user.getUsername());
		} catch (SQLException e) {
			response.sendError(0);
			String errorMsg = "An execution error occured when loading user partecipate as invitee groups";
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMsg);
			return;
		}
		
		// Write response content
		Gson gson = new Gson();
		String jsonInvitedToGroups = gson.toJson(invitedToGroups);

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().write(jsonInvitedToGroups);
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
