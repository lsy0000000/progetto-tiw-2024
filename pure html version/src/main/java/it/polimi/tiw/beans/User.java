package it.polimi.tiw.beans;

public class User {
	private String username;
	private String email;
	private String name;
	private String surname;
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}


	public void setEmail(String mail) {
		this.email = mail;
	}

	public String getEmail() {
		return email;
	}

	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	
	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getSurname() {
		return surname;
	}
	
}
