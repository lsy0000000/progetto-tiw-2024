package it.polimi.tiw.controllers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;



@WebServlet("/CancelCreation")
@MultipartConfig
public class CancelCreation extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
       
   
    public CancelCreation() {
        super();
    }

    
    public void init() throws ServletException {
    }
    
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession();
		
		//Remove attributes relating last failed group creation
		session.removeAttribute("tryTimes");
		session.removeAttribute("selectedUsers");
		session.removeAttribute("groupToCreate");		
		
		response.setStatus(HttpServletResponse.SC_OK);
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		doGet(request, response);
	}

}
