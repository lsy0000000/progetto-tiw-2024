// Script for login and sign up

(function() {  // avoid variables ending up in the global scope 
	
	//-------- LOGIN --------
	
	document.getElementById("login_title").addEventListener("click", () => {
	    showLogin();
	});
	
	document.getElementById("login_button").addEventListener("click", (e) => {
		e.preventDefault();
		let form = e.target.closest("form");
	    if (form.checkValidity()) {
			toServer("Login",form, "login_error_msg");
	    } else {
			form.reportValidity();
	    }
	});
	
	
	//-------- SIGN UP --------
	
	document.getElementById("signup_title").addEventListener("click", (e) => {
	    showSignUp();
	});
	
	document.getElementById("signup_button").addEventListener("click", (e) => {
		
		e.preventDefault()	
						       								
		let form = e.target.closest("form");
		let msg_id = "signup_error_msg";
		
	    if (form.checkValidity()) {
			if(!isEmailValid()){
				document.getElementById(msg_id).textContent = "Email format invalid";
                //document.getElementById("signup_error_msg").display = block;
                return false;
			}
			if(!isPwdsEqual()){
				document.getElementById(msg_id).textContent = "Passwords do not match (or are missing)";
                //document.getElementById("signup_error_msg").display = block;
                return false;			
			}
			toServer("SignUp",form, msg_id); 	   
	    } else {
	    	form.reportValidity();
	    }
	});
	
})();




function toServer(url, form, elem_id) { 
	makeCall("POST", url, form, 
	 	function(req){
			if (req.readyState === XMLHttpRequest.DONE) {
			    let message = req.responseText;
			    switch (req.status) {
			      case 200:
					
			        sessionStorage.setItem('user',message);
			        window.location.href = "home.html";
			        break;
			      case 400: // bad request
			        document.getElementById(elem_id).textContent = message;
			        document.getElementById(elem_id).style.display="block";
			        break;
			      case 401: // unauthorized
			        document.getElementById(elem_id).textContent = message;
			        document.getElementById(elem_id).style.display="block";
			        break;
			      case 500: // server error
			    	document.getElementById(elem_id).textContent = message;
			        document.getElementById(elem_id).style.display="block";
			        break;
			    }
			}	
	});		
}

function isEmailValid(){
	var pattern = /^([A-Za-z0-9_\-\.])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,4})$/;
    if (pattern.test(document.getElementById("email").value)) {
        return true;
    }    
    return false;
}


function isPwdsEqual(){
	var pwd1 = document.getElementById("pwd1").value;
	var pwd2 = document.getElementById("pwd2").value;
	if(pwd1 === "" || pwd2 === ""){
		return false;
	}
	if(pwd1 === pwd2)
		return true;	
	return false;
};


function showLogin() {
    document.getElementById("login_container").style.display="block";
	document.getElementById("login_title").style.backgroundColor = "white";
    document.getElementById("signup_container").style.display="none";
	document.getElementById("signup_title").style.backgroundColor = "lightgray";
	document.getElementById("login_error_msg").textContent = "";
}


function showSignUp() {
    document.getElementById("login_container").style.display="none";
	document.getElementById("login_title").style.backgroundColor = "lightgray";
    document.getElementById("signup_container").style.display="block";
	document.getElementById("signup_title").style.backgroundColor = "white";
	document.getElementById("signup_error_msg").textContent = "";
}



