package it.polimi.tiw.dao;

import java.sql.Connection;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.beans.Group;
import it.polimi.tiw.beans.User;

public class ParticipationDAO {
	private Connection connection;
	
	public ParticipationDAO(Connection connection) {
		this.connection = connection;
	}
	
	
	/**
	 * Store the new participation in the database
	 * @throws SQLException
	 */
	public void addParticipation(int idGroup, String participant) throws SQLException {
		
		String query = "INSERT INTO participation (idGroup, participant) VALUES (?, ?)";
		PreparedStatement pstatement = null;
		
		try {
			
			pstatement = connection.prepareStatement(query);

			pstatement.setInt(1, idGroup);
			pstatement.setString(2, participant);
			
			pstatement.executeUpdate();
			
		}catch (SQLException e) {
			throw new SQLException();
		} finally {
			try {
				pstatement.close();
			} catch (Exception e) {throw new SQLException();}
		}
	}
	
	
	/**
	 * Find groups the user has been invited to
	 * @return list of groups
	 * @throws SQLException
	 */
	public List<Group> findGroupsByInvitee (String username) throws SQLException {
		
		String query = "SELECT G.* "
				     + "FROM dbtiw.participation AS P "
				     + "JOIN dbtiw.group AS G ON P.idGroup = G.id "
				     + "WHERE P.participant = ? AND creator != ? "
				              + "AND (DATEDIFF(?, startDate) < (duration-1) OR DATEDIFF(?, startDate) = 0)"
				     + "ORDER BY startDate DESC, id DESC";
		
		PreparedStatement pstatement = null;
		ResultSet resultSet = null;
		Date date = getCurrentDate();
		List<Group> groups = new ArrayList<>();
		
		try{
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, username);
			pstatement.setString(2, username);
			pstatement.setDate(3, new java.sql.Date(date.getTime()));
			pstatement.setDate(4, new java.sql.Date(date.getTime()));
			
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
		}catch (SQLException e) {
			e.printStackTrace();
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
	
	
	/**
	 * Find users who participate in a specific group
	 * @throws SQLException
	 */
	public List<User> findParticipantsByGroupId(int idGroup) throws SQLException{
		
		String query = "SELECT U.* "
			     + "FROM dbtiw.participation AS P JOIN dbtiw.user AS U ON P.participant = U.username "
			     + "WHERE P.idGroup = ? "
			     + "ORDER BY U.surname ";
		
		PreparedStatement pstatement = null;
		ResultSet resultSet = null;
		List<User> users = new ArrayList<>();
			
		try{
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, idGroup);
			resultSet = pstatement.executeQuery();
			
			while (resultSet.next()) {
				
				User user = new User();
                user.setUsername(resultSet.getString("username"));
                user.setEmail(resultSet.getString("email"));
				user.setName(resultSet.getString("name"));
				user.setSurname(resultSet.getString("surname"));
				
                users.add(user);  
                
			}
		}catch (SQLException e) {
			e.printStackTrace();
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
	
	/**
	 * @return the current timeStamp without considering the time
	 */
	private Timestamp getCurrentDate() {
		// get current time-stamp
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		// convert to localDate
	    LocalDate ld = ts.toLocalDateTime().toLocalDate(); 
	    // reconvert to Timestamp and return
		return Timestamp.valueOf(ld.atStartOfDay());
	}
	
}
