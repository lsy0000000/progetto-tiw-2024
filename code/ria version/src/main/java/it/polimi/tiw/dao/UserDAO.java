package it.polimi.tiw.dao;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import it.polimi.tiw.beans.User;

public class UserDAO {
	private Connection connection;
	
	public UserDAO(Connection connection) {
		this.connection = connection;
	}
	
	
	/**
	 * Finds and returns a user from the DB taking their username and password as input
	 * @return the user
	 * @throws SQLException in case of error
	 */
	public User findUser(String username, String password) throws SQLException {

		String query = "SELECT username, email, name, surname FROM User WHERE username = ? AND hashed_pwd = ?";
		PreparedStatement pstatement = null;
		ResultSet resultSet = null;
		User user = null;
		
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, username);
			pstatement.setString(2, getHash(password));
			
			resultSet = pstatement.executeQuery();

			while (resultSet.next()) {
				user = new User();
				user.setUsername(resultSet.getString("username"));
				user.setEmail(resultSet.getString("email"));
				user.setName(resultSet.getString("name"));
				user.setSurname(resultSet.getString("surname"));	
			}
			
		} catch (SQLException e) {
			throw new SQLException();
		} finally {
			try {
				resultSet.close();
			} catch (Exception e) {throw new SQLException();}

			try {
				pstatement.close();
			} catch (Exception e) {throw new SQLException();}
	    }
		return user;
	}
	
	
	/**
	 * @return true when the username is usable, false when it is already in use by someone
	 * @throws SQLException
	 */
	public boolean isUsernameUsable(String username) throws SQLException {
		
		String query = "SELECT username FROM User WHERE username = ?";
		PreparedStatement pstatement = null;
		ResultSet resultSet = null;

		try{
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, username);
			resultSet = pstatement.executeQuery();
			
			if (resultSet.next()) return false; //Username already in use
			
		} catch (SQLException e) {
			throw new SQLException();
		} finally {
			try {
				resultSet.close();
			} catch (Exception e) {throw new SQLException();}
			try {
				pstatement.close();
			} catch (Exception e) {throw new SQLException();}
		}
		return true;
	}
	
	
	/**
	 * Store the new user in the database
	 * @throws SQLException
	 */
	public void registerUser (String username, String password, String email, String name, String surname) throws SQLException {
			
		String query = "INSERT INTO user (username, hashed_pwd, email, name, surname) VALUES (?, ?, ?, ?, ?)";
		PreparedStatement pstatement = null;
		
		try{
			pstatement = connection.prepareStatement(query);
			
			pstatement.setString(1, username);
			pstatement.setString(2, getHash(password));
			pstatement.setString(3, email);
			pstatement.setString(4, name);
			pstatement.setString(5, surname);
    
			pstatement.executeUpdate(); 
			
		} catch (SQLException e) {
			throw new SQLException();
		} finally {
			try {
				pstatement.close();
			} catch (Exception e) {throw new SQLException();}
		}

	} 
	
	
	/**
	 * @return the list of users except the user with username provided as parameter
	 * @throws SQLException
	 */
	public List<User> getAllOtherUsers(String username) throws SQLException{
		
		String query = "SELECT username, email, name, surname FROM User WHERE username != ? ORDER BY surname";
		PreparedStatement pstatement = null;
		ResultSet resultSet = null;
		List<User> users = new ArrayList<>();
		
		try{
			
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, username);
			resultSet = pstatement.executeQuery();
			
			while (resultSet.next()) {
				
            	User user = new User();
            	
                user.setUsername(resultSet.getString("username"));
                user.setEmail(resultSet.getString("email"));
				user.setName(resultSet.getString("name"));
				user.setSurname(resultSet.getString("surname"));
				
                users.add(user);  
            }
			
		} catch (SQLException e) {
			throw new SQLException();
		} finally {
			try {
				resultSet.close();
			} catch (Exception e) {throw new SQLException();}
			try {
				pstatement.close();
			} catch (Exception e) {throw new SQLException();}
		}
		
		return users;
	}
	
	@Deprecated
	public boolean isUserExists(String username) throws SQLException {
		String query = "SELECT username FROM User WHERE username = ?";
		PreparedStatement pstatement = null;
		ResultSet resultSet = null;
		
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, username);
			resultSet = pstatement.executeQuery();

			if (resultSet.next()) {
				return true;
			}
			
		} catch (SQLException e) {
			throw new SQLException();
		} finally {
			try {
				resultSet.close();
			} catch (Exception e) {throw new SQLException();}

			try {
				pstatement.close();
			} catch (Exception e) {throw new SQLException();}
	    }
		return false;
	}
	
	
	/**
	 * @return hashed password
	 * @throws NoSuchAlgorithmException 
	 */
	private String getHash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
        	e.printStackTrace();
        	return password;
        }	
	}
		
}
