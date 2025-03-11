package it.polimi.tiw.beans;

import java.sql.Date;

public class Group {
	private int id;
	private String title;
	private Date startDate;
	private int duration;
	private int minCapacity;
	private int maxCapacity;
	private String creator;
	
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}
	
	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public Date getStartDate() {
		return startDate;
	}
	
	
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	public int getDuration() {
		return duration;
	}
	
	
	public void setMinCapacity(int minCapacity) {
		this.minCapacity = minCapacity;
	}
	
	public int getMinCapacity() {
		return minCapacity;
	}
	
	
	public void setMaxCapacity(int maxCapacity) {
		this.maxCapacity = maxCapacity;
	}
	
	public int getMaxCapacity() {
		return maxCapacity;
	}
	

	public void setCreator(String creator) {
		this.creator = creator;
	}
	
	public String getCreator() {
		return creator;
	}
}
