package it.polimi.tiw.controllers;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/IncrementTryTimes")
@MultipartConfig
public class IncrementTryTimes extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public IncrementTryTimes() {
        super();
    }

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		int temp;
		
		if (session.getAttribute("tryTimes") == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid operation, please follow the normal creation procedure");
			return;
		}else {
			temp = (int) session.getAttribute("tryTimes");
			temp = temp + 1;
		}
		
		if (temp == 3) {
			session.removeAttribute("tryTimes");
			session.removeAttribute("selectedUsers");
			session.removeAttribute("groupToCreate");
		} else {
			session.setAttribute("tryTimes", temp);
		}
		response.setStatus(HttpServletResponse.SC_OK);
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
