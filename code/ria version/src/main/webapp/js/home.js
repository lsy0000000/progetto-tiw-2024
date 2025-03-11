// Script for home page

// block scope
{  
	// page components
	let createdGroups, invitedToGroups, groupDetails, creation_form, selection_modal;
	
	window.addEventListener("load", () => {
	    if (sessionStorage.getItem("user") == null) {
			window.location.href = "./index.html";
	    }else{
			
			fillProfile();

			createdGroups = new GroupsList(
				document.getElementById("createdGroups_container"),
				document.getElementById("createdGroups_body"),
				"GetCreatedGroups"
			)
			createdGroups.show();
			
			invitedToGroups = new GroupsList(
				document.getElementById("invitedToGroups_container"),
				document.getElementById("invitedToGroups_body"),
				"GetInvitedToGroups"
			)
			invitedToGroups.show();
			
			groupDetails = new GroupDetails(
				document.getElementById("details_container"),
				document.getElementById("groupInfo_body"),
				document.getElementById("participantsInfo_body"),
				document.getElementById("details_errorMsg")
			)
			groupDetails.reset();
			
			creation_form = new CreationForm(
				document.getElementById("creation_form"),
				document.getElementById("creation_form_button"),
				document.getElementById("creation_form_errorMsg")
			)
			creation_form.init();
			
			
			selection_modal = new SelectionModal(
				document.getElementById("modal_container"),
				document.getElementById("modal_form"),
				document.getElementById("modal_form_button"),
				document.getElementById("modal_groupInfo_body"),
				document.getElementById("modal_usersInfo_body"),
				document.getElementById("modal_errorMsg_div"),
				document.getElementById("modal_cancellation_button"),
				document.getElementById("modal_times_div")
			)
			
			selection_modal.reset();
			
			
			
		}
	});
	
	
	let current_user = JSON.parse(sessionStorage.getItem("user"));
	
	function fillProfile(){
		document.getElementById("profile_username").textContent = current_user.username;
		document.getElementById("profile_email").textContent = current_user.email;
		document.getElementById("profile_name").textContent = current_user.name;
	    document.getElementById("profile_surname").textContent = current_user.surname;
	}
	
	 
	
	function GroupsList (list_container, list_body, request_url) {	
		this.list_container = list_container;
		this.list_body = list_body;
		
	    this.reset = function () {
			this.list_body.innerHTML = "";
	    }
	
	    this.show = function() {
	        let self = this;
	        makeCall("GET", request_url, null, 
				function(req) {
		            if (req.readyState == XMLHttpRequest.DONE) {
		                let message = req.responseText;
		                if (req.status == 200) {
		                    let groups = JSON.parse(message);
		                    if (groups.length == 0) {
		                        //alert("No groups are present.");
		                        return;
		                    } else { 
		                        self.update(groups);
		                    }
		                } else { 
		                    self.list_container.style.visibility = "hidden";
		                    alert("Unable to retriver data.");
		                }
		            }
	        })
	    };
	
	    this.update = function (groups) {
	        this.reset();
	        let self = this;

	        groups.forEach(function (group) { //Self is visible here, not this.
				
				let new_tr,id_td, title_td, date_td, duration_td, capacity_td, details_td, link_a;
				
	            new_tr = document.createElement("tr");
	
				id_td = document.createElement("td");
	            id_td.textContent = group.id;
	            new_tr.appendChild(id_td);
				
	            title_td = document.createElement("td");
	            title_td.textContent = group.title;
	            new_tr.appendChild(title_td);
	
	            date_td = document.createElement("td");
	            date_td.textContent = group.startDate;
	            new_tr.appendChild(date_td);
	
	            duration_td = document.createElement("td");
	            duration_td.textContent = group.duration;
	            new_tr.appendChild(duration_td);
	
	            capacity_td = document.createElement("td");
	            capacity_td.textContent = group.minCapacity + " - " + group.maxCapacity;
	            new_tr.appendChild(capacity_td);
				
				details_td = document.createElement("td");
				link_a = document.createElement("a");
				link_a.textContent = ">>";
				link_a.style.cursor = "pointer";
				link_a.addEventListener("click", (e) => {
					groupDetails.reset();
					groupDetails.show(group);
				}, false);
	            details_td.appendChild(link_a);
	            new_tr.appendChild(details_td);
				
	            self.list_body.appendChild(new_tr);
	        });
	
	        this.list_body.style.visibility = "visibile";
	    }
	}
	
	
	function GroupDetails(details_container, groupInfo_body, participantsInfo_body, details_errorMsg){
		
		this.details_container = details_container;
		this.groupInfo_body = groupInfo_body;
		this.participantsInfo_body = participantsInfo_body;
		this.details_errorMsg = details_errorMsg;
		
		
		this.reset = function(){
			//this.details_container.style.visibility = "hidden";
			this.groupInfo_body.innerHTML = "";
			this.participantsInfo_body.innerHTML = "";
			this.details_errorMsg.textContent = "";
		}
		
		this.show = function(group){
			let self = this;
			makeCall("GET", "GetGroupDetails?idGroup=" + group.id, null, 
				function(req) {
		            if (req.readyState == XMLHttpRequest.DONE) {
		                let message = req.responseText;
						switch (req.status) {
							case 200:
								//let group = JSON.parse(message)[0];
							    let participants = JSON.parse(message);
								self.update(group, participants);
							    break;
							case 400: // bad request
							    details_errorMsg.textContent = message;
							    break;
							case 404: // not found
							    details_errorMsg.textContent = message;
							    break;
		            	}
	        		}
				}
			)
		}
		
		this.update = function(group, participants){
			this.reset();
			let self = this;
			
			let new_groupInfo_tr = getGroupInfoTR_img(group);
			self.groupInfo_body.appendChild(new_groupInfo_tr);
			
			participants.forEach(function (participant) { //Self is visible here, not this.
				let new_tr = getUserInfoTR(participant);

				
				if(group.creator === current_user.username && participant.username !== current_user.username){
					new_tr.draggable = true;
					new_tr.style.cursor="grab";
					new_tr.ondragstart = function(e){
						e.dataTransfer.setData('participant', participant.username)
					}
				}
				
				
	            self.participantsInfo_body.appendChild(new_tr);
	        });
			this.details_container.style.visibility = "visible";
	    }
			
			
	}
	
	
	function CreationForm(form, form_button, form_errorMsg){
			this.form = form;
			this.form_button = form_button;
			this.form_errorMsg = form_errorMsg;
			
			this.reset = function(){
				this.form.reset();
				this.form_errorMsg.textContent = "";
			}
			
			this.init = function(){
				
	            let self = this;
				
	            self.form_button.addEventListener('click', (e) => {
	                e.preventDefault();
					let form = self.form;
					
	                if (form.checkValidity()) {
						
	                    makeCall("POST", 'CreateGroup', form,
	                        function(req) {
	                            if (req.readyState === XMLHttpRequest.DONE) {
	                                let message = req.responseText;
	                                switch (req.status) {
	                                    case 200:
											
											let groupToCreate = JSON.parse(message);
											
	                                        selection_modal.show(groupToCreate);
											
	                                        // clean error message
	                                        form_errorMsg.innerHTML = "";
											
	                                        // reset form
	                                        self.reset();

	                                        break;
	                                    case 400: // bad request
	                                        form_errorMsg.textContent = message;
	                  
	                                        break;
	                                }
	                            }
	                        }
	                    );
	                } else {
	                    form.reportValidity();
	                }
	            });
				
			}

		}
		
		
	function SelectionModal(modal_container, form, form_button, groupInfo_body, usersInfo_body, errorMsg_div, cancel_button, times_div){
		this.modal_container = modal_container;
		this.form = form;
		this.form_button = form_button;
		this.groupInfo_body = groupInfo_body;
		this.usersInfo_body = usersInfo_body;
		this.errorMsg_div = errorMsg_div;
		this.cancel_button = cancel_button;
		this.times_div = times_div;
		var tryTimes;
		
		this.init = function(){}
		
		this.reset = function(){
			tryTimes = 0;
			this.groupInfo_body.innerHTML = "";
			this.usersInfo_body.innerHTML = "";
			this.modal_container.style.display = "none";
			this.times_div.textContent = tryTimes;
			
			// Replace with new button because the old one will activate also the previous event 
			// (probabilmente non è una soluzione ideale)
			let new_button = getNewButton("SUBMIT");
			this.form_button.parentNode.replaceChild(new_button, this.form_button);
			this.form_button = new_button;
			this.form_button.id = "modal_form_button";
			
			
			this.cancel_button.removeEventListener('click', reqCancellation);
		}
		
		this.show = function(group){
			
			let self = this;
			makeCall("GET", "GetAllUsers" , null, 
				function(req) {
		            if (req.readyState == XMLHttpRequest.DONE) {
		                let message = req.responseText;
						if(req.status === 200) {
							
						    let users = JSON.parse(message);
							self.update(group, users);
		            	}
	        		}
				}
			)				
		}
		
		this.update = function(group, users){
			let self = this;
			
			let new_groupInfo_tr = getGroupInfoTR(group, true);
		    self.groupInfo_body.appendChild(new_groupInfo_tr);
			
			users.forEach(function (user) { //Self is visible here, not this.
				let new_tr = getUserInfoTR(user);
				let checkbox_td = document.createElement("td");
				let checkbox = document.createElement("input");
				checkbox.type = "checkbox";
				checkbox.name = "selectedUsers";
				checkbox.value = user.username;
				checkbox_td.appendChild(checkbox);
				new_tr.appendChild(checkbox_td);
	            self.usersInfo_body.appendChild(new_tr);
	        });
			
			
			self.form_button.addEventListener('click', (e) => {
                e.preventDefault();
				
				let items = document.getElementsByName("selectedUsers");
				let count = 0;
				
				for (var i = 0; i < items.length; i++) { 
					if (items[i].checked == true) 
						count++; 
				}
				
				let isBad = false;
				
				if(count < group.minCapacity-1){
					errorMsg_div.textContent = "Too few users selected. Add at least "+(group.minCapacity-1-count);
		            isBad = true;
				}else if(count > group.maxCapacity-1){
					errorMsg_div.textContent = "Too much users selected. Remove at least "+ (count-group.maxCapacity+1);
					isBad = true;
				}
				
				if(isBad)
					reqIncrement();
				else
					reqInvitation(self.form, self.errorMsg_div);
				
		    });
			
			self.cancel_button.addEventListener('click', reqCancellation);
		
			modal_container.style.display = "block";
		}
		
		this.incrementTimes = function(){
			tryTimes = tryTimes + 1;
			this.times_div.textContent = tryTimes;
			if(tryTimes === 3){
				alert("3 attempts to define a group with an incorrect number of participants, the group will not be created");
				this.reset();
			}
		}
		
		
	}
	
	
	function getGroupInfoTR(group, isCreation){
		let new_tr, id_td, title_td, date_td, duration_td, minCapacity_td, maxCapacity_td, creator_td;
											
        new_tr = document.createElement("tr");
		
		if(!isCreation){
			id_td = document.createElement("td");
			id_td.textContent = group.id;
	        new_tr.appendChild(id_td);
		}
	
        title_td = document.createElement("td");
        title_td.textContent = group.title;
        new_tr.appendChild(title_td);
		
		if(!isCreation){
		    date_td = document.createElement("td");
		    date_td.textContent = group.startDate;
		    new_tr.appendChild(date_td);
		}	

        duration_td = document.createElement("td");
        duration_td.textContent = group.duration;
        new_tr.appendChild(duration_td);

        minCapacity_td = document.createElement("td");
        minCapacity_td.textContent = group.minCapacity;
        new_tr.appendChild(minCapacity_td);
		
		maxCapacity_td = document.createElement("td");
        maxCapacity_td.textContent = group.maxCapacity;
        new_tr.appendChild(maxCapacity_td);
		
		creator_td = document.createElement("td");
        creator_td.textContent = group.creator;
        new_tr.appendChild(creator_td);
		
		return new_tr;
	}
	
	
	function getGroupInfoTR_img(group){
		
		let new_tr = getGroupInfoTR(group, false);
		let bin_td, bin_img;
		
		bin_td = document.createElement("td");
		if(group.creator === current_user.username){
			
			bin_img = document.createElement("img");
			bin_img.src = "img/bin.jpeg";
			bin_img.id = "bin_img";
			
			bin_img.ondragover = function(e){
				e.preventDefault();
			}
			
			bin_img.ondrop = function(e){
				let participant = e.dataTransfer.getData('participant');
				makeCall("GET", "DeleteParticipation?idGroup="+group.id+"&participant="+participant, null, 
					function(req){
						if (req.readyState == XMLHttpRequest.DONE) {
			                let message = req.responseText;
							switch (req.status) {
								case 200:
									alert("DELETION SUCCESSFUL!")
								    groupDetails.show(group);
								    break;
								case 400: // bad request
								    alert(message)
								    break;
								case 401: // unauthorized
								    alert(message)
								    break;
			            	}
		        		}
					}
				)
			}
	        bin_td.appendChild(bin_img);
		}
		
        new_tr.appendChild(bin_td);
		return new_tr;
	}
	
	
	function getUserInfoTR(user){
		let new_tr, username_td, email_td, name_td, surname_td;
						
        new_tr = document.createElement("tr");
		
		surname_td = document.createElement("td");
        surname_td.textContent = user.surname;
        new_tr.appendChild(surname_td);
		
		name_td = document.createElement("td");
        name_td.textContent = user.name;
        new_tr.appendChild(name_td);
		
		email_td = document.createElement("td");
        email_td.textContent = user.email;
        new_tr.appendChild(email_td);
		
        username_td = document.createElement("td");
        username_td.textContent = user.username;
        new_tr.appendChild(username_td); 
		
		return new_tr;
	}
	

	function getNewButton(content){
		let button = document.createElement("button");
		button.textContent = content;
		return button;
	}
	
	function reqIncrement(){
		makeCall("POST", 'IncrementTryTimes', null,
            function(req) {
                if (req.readyState === XMLHttpRequest.DONE) {
					if(req.status == 200){
						selection_modal.incrementTimes();
					}else if(req.status == 400){
						alert(req.responseText);
					}else{
						alert("Submitting failed")
					}
                }
            }
        );
	}
	
	function reqInvitation(form, errorMsg){
		makeCall("POST", 'CheckInvitations', form,
            function(req) {
                if (req.readyState === XMLHttpRequest.DONE) {
                    let message = req.responseText;
                    switch (req.status) {
                        case 200:
							selection_modal.reset();
							createdGroups.show();
                            break;
                        case 400: // bad request
                            errorMsg.textContent = message;
							selection_modal.incrementTimes;
                            break;
                        case 403: // forbidden
                            errorMsg.textContent = message;
                            break;
                    }
                }
            }
        );
	}
	
	function reqCancellation(){
			makeCall("GET", 'CancelCreation', null,
	            function(req) {
	                if (req.readyState === XMLHttpRequest.DONE) {
						if(req.status === 200){
							selection_modal.reset();
							creation_form.reset();
						}else{
							allert("Cancellation failed")
						}
	                }
	            }
	        );
	}
}