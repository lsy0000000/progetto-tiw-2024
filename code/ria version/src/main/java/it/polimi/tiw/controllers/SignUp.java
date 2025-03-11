package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;


@WebServlet("/SignUp")
@MultipartConfig
public class SignUp extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    public SignUp() {
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
		
		String username   = request.getParameter("username");
		String email      = request.getParameter("email");
		String name       = request.getParameter("name");
		String surname    = request.getParameter("surname");
		String password   = request.getParameter("password");
		String repeat_pwd = request.getParameter("repeat_pwd");
		
		//Check if sign up data nullify
		try {
			if(username == null || email == null || name == null || surname == null || password == null || repeat_pwd == null) {
				throw new Exception("Missing or empty credential value");
			}
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println( "Missing credential value");
			return;
		}
		
		//error indicates the cause of sign up failure
		String error = null;
		
		//Check sign up data format  
		error = checkDataValidity(username, email, name, surname, password, repeat_pwd);
		try {
			if(error != null) {
				throw new Exception("Invalid data format");
			}
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(error);
			return;
		}
		
		
		
		UserDAO userDAO = new UserDAO(connection);		
		
		//Check if the username is already in use
		//in case not, register the new user in the database
		try {
			if(userDAO.isUsernameUsable(username)) {
				userDAO.registerUser(username, password, email, name, surname);
			}else {
				error = "Username already in use";
				throw new Exception();
			}
		} catch (SQLException e) {	
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not Possible to execute sign up");
			return;	
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println(error);
			return;
		}
		
		
		// If if everything is in order, auto-login after registration 
		User user = new User();
		user.setUsername(username);
        user.setEmail(email);
		user.setName(name);
		user.setSurname(surname);
		
		request.getSession().setAttribute("user", user);
		
		Gson gson = new GsonBuilder().create();
		String userInfo = gson.toJson(user);
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.setStatus(HttpServletResponse.SC_OK);
		
		response.getWriter().write(userInfo);
		
	}
	
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * @return a error message if invalid specific data format exists, null otherwise
	 */
	private String checkDataValidity(String username, String email, String name, String surname, String password, String repeat_pwd) {
		
		if (isFormatInvalid(username)) {
			return "Username format invalid";
		}
		
		if(!isEmailValid(email)) {
			return "E-mail format invalid";
		}
		
		if (isFormatInvalid(name)) {
			return "Name format invalid";
		}
		
		if (isFormatInvalid(surname)) {
			return "Surname format invalid";
		}
		
		if (isFormatInvalid(password)) {
			return "Password format invalid";
		}
		
		if(!password.equals(repeat_pwd)) {
			return "Password and repeat password field not equal";
		}
		
		return null;
	}
	
	
	/**
	 * @return true if the string passed as parameter respect the specific format, false otherwise
	 */
	private boolean isFormatInvalid(String string) {
		return (string.length() <= 0 || string.length() > 64 || string.matches(".*\\s+.*"));
	}
	
	
	/**
	 * @return true if the String email is valid, false otherwise
	 */
	private boolean isEmailValid(String email) {
		String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z]{2,6}$";
		Pattern pattern = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}
	

}
