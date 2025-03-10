package it.polimi.tiw.controllers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.beans.Group;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.PathBook;


@WebServlet("/CreateGroup")
public class CreateGroup extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
       
    public CreateGroup() {
        super();
    }
    

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		
		boolean isBadRequest = false;
		String title = null;
		int duration = 0;
		int minCapacity = 0;
		int maxCapacity = 0;
		String creator = user.getUsername();
		
		try {
			// Get and parse all parameters from request
			title = request.getParameter("title");
			duration = Integer.parseInt(request.getParameter("duration"));
			minCapacity = Integer.parseInt(request.getParameter("minCapacity"));
			maxCapacity = Integer.parseInt(request.getParameter("maxCapacity"));
					
		} catch (NumberFormatException | NullPointerException e) {
			isBadRequest = true;
		}
		
		
		// If exists invalid parameter/s
		if (isBadRequest) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect type or missing param values");
			return;
		}
		
		
		String checkError = getCheckError(title, duration, minCapacity, maxCapacity);
		if (checkError != null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, checkError);
			return;
		}
		
		
		
		// Store the currently creating group in the session
		Group groupToCreate = new Group();
		groupToCreate.setTitle(title);
		groupToCreate.setDuration(duration);
		groupToCreate.setMinCapacity(minCapacity);
		groupToCreate.setMaxCapacity(maxCapacity);
		groupToCreate.setCreator(creator);
		session.setAttribute("groupToCreate", groupToCreate);
		session.setAttribute("tryTimes", 0);

		
		String path = getServletContext().getContextPath() + PathBook.pathToInvitationServlet;
		response.sendRedirect(path);
	}
	
	private String getCheckError(String title, int duration, int minCapacity, int maxCapacity) {
		if(title.isEmpty())
			return "Title cannot be empty";
		if(duration <= 0 )
			return "Duration must be at least 1";
		if(minCapacity < 2)
			return "Minimum number of users must be at least 2";
		if(maxCapacity < minCapacity)
			return "Maximum number of users must be more than minimun number of users";
		return null;
	}
	
}
