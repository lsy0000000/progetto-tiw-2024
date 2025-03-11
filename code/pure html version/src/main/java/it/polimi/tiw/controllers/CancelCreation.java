package it.polimi.tiw.controllers;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.utils.PathBook;
import it.polimi.tiw.utils.TemplateHandler;


@WebServlet("/CancelCreation")
public class CancelCreation extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
       
   
    public CancelCreation() {
        super();
    }

    
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
    	this.templateEngine = TemplateHandler.getEngine(servletContext, ".html");
    }
    
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		
		//Remove attributes relating last failed group creation
		session.removeAttribute("tryTimes");
		session.removeAttribute("selectedUsers");
		session.removeAttribute("invitationErrorMsg");
		session.removeAttribute("groupToCreate");		
		
		
		String errorMsg = "3 attempts to define a group with an incorrect number of participants, the group will not be created.";
		String selfCancellation = request.getParameter("selfCancellation");
		
		if(selfCancellation != null && selfCancellation.equals("true"))
			errorMsg = "The group being created was successfully deleted, it  will not be created";
		
		String path = PathBook.pathToCreationFailedPage;
		WebContext ctx = new WebContext(request, response, getServletContext(), request.getLocale());
		ctx.setVariable("creationErrorMsg", errorMsg );
		templateEngine.process(path, ctx, response.getWriter());
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		doGet(request, response);
	}

}
