package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.polimi.tiw.beans.Group;
import it.polimi.tiw.utils.DateHandler;

public class GroupDAO {
	private Connection connection;
	
	public GroupDAO(Connection connection) {
		this.connection = connection;
	}
	
	
	/**
	 * Store the new group in the database
	 * @throws SQLException
	 */
	public int createGroup(String title, int duration, int minCapacity, int maxCapacity, String creator) throws SQLException {
		
		String query = "INSERT INTO dbtiw.group (title, startDate, duration, minCapacity, maxCapacity, creator) VALUES (?, ?, ?, ?, ?, ?)";
		PreparedStatement pstatement = null;
		ResultSet resultSet = null;
		Date today = DateHandler.getToday();
		int idGroup = -1;
		
		try{
			pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			
			pstatement.setString(1, title);
			pstatement.setDate(2, new java.sql.Date(today.getTime()));
			pstatement.setInt(3, duration);
			pstatement.setInt(4, minCapacity);  
			pstatement.setInt(5, maxCapacity); 
			pstatement.setString(6, creator);
			
			pstatement.executeUpdate(); 
			
			resultSet = pstatement.getGeneratedKeys();
			
			// retrieve userID
			if (resultSet.next()) {
	           idGroup = resultSet.getInt(1);
			}         
	                
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException();
		} finally {
			try {
				pstatement.close();
			} catch (Exception e) {throw new SQLException();}
			try {
				resultSet.close();
			} catch (Exception e) {throw new SQLException();}
		}
		
		return idGroup;
	}
	
	/**
	 * Find groups created by the user provided as parameter
	 * @return list of groups
	 * @throws SQLException
	 */
	public List<Group> findGroupsByCreator (String creator) throws SQLException {
		
		// query to be correct
		String query = "SELECT * "
				+ "FROM dbtiw.group "
				+ "WHERE creator = ? AND (DATEDIFF(?, startDate) < duration OR DATEDIFF(?, startDate) = 0) "
				+ "ORDER BY startDate DESC, id DESC";
		PreparedStatement pstatement = null;
		ResultSet resultSet = null;
		List<Group> groups = new ArrayList<>();
		
		Date date = DateHandler.getToday();
		
		try{
			pstatement = connection.prepareStatement(query);
			
			pstatement.setString(1, creator);
			pstatement.setDate(2, new java.sql.Date(date.getTime()));
			pstatement.setDate(3, new java.sql.Date(date.getTime()));
			resultSet = pstatement.executeQuery();
			
			while (resultSet.next()) {
				Group group = new Group();
				
	        	group.setId(resultSet.getInt("id"));
	        	group.setTitle(resultSet.getString("title"));
	        	group.setStartDate(resultSet.getDate("startDate"));
	        	group.setDuration(resultSet.getInt("duration"));
	        	group.setMinCapacity(resultSet.getInt("minCapacity"));
	        	group.setMaxCapacity(resultSet.getInt("maxCapacity"));
	        	group.setCreator(resultSet.getString("creator"));
	        	
	        	groups.add(group);
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
		
		return groups;
	}
	

	
	public Group findGroupById(int idGroup) throws SQLException {
		String query = "SELECT * FROM dbtiw.group WHERE id = ?";
		PreparedStatement pstatement = null;
		ResultSet resultSet = null;
		Group group = null;
		try{
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, idGroup);
			resultSet = pstatement.executeQuery();
			if(resultSet.next()) {
				group = new Group();
				group.setId(resultSet.getInt("id"));
	        	group.setTitle(resultSet.getString("title"));
	        	group.setStartDate(resultSet.getDate("startDate"));
	        	group.setDuration(resultSet.getInt("duration"));
	        	group.setMinCapacity(resultSet.getInt("minCapacity"));
	        	group.setMaxCapacity(resultSet.getInt("maxCapacity"));
	        	group.setCreator(resultSet.getString("creator"));
			}
			
		}catch (SQLException e) {
			throw new SQLException();
		} finally {
			try {
				resultSet.close();
			} catch (Exception e) {throw new SQLException();}
			try {
				pstatement.close();
			} catch (Exception e) {throw new SQLException();}
		}
		
		return group;
	}

}
