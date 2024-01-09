console.log("this is script file")

const toggleSidebar = () => {

if($(".sidebar").is(":visible")){
 
   //true
   //band krna hai
   $(".sidebar").css("display","none");
   $(".content").css("margin-left","0%");

}else{
    //false
    //show krna hai
    $(".sidebar").css("display","block");
   $(".content").css("margin-left","15%");

}
}

const search = () => {
	
	let query = $("#search-input").val();
	
	if(query == ""){
		$(".search-result").hide();
	}else{
		//sending request to server
		let url = `http://localhost:8282/search/${query}`;
		
		fetch(url)
		 .then((response) => {
			return response.json();
		})
		.then((data) => {
			console.log(data);
			
			let text = `<div class='list-group'>`;
			
			data.forEach((contact) => {
				text += `<a href='/user/${contact.cId}/contact' class='list-group-item list-group-action'> ${contact.name} </a>`
			})
				
			text += `</div>`;
			
			 $(".search-result").html(text);
			 $(".search-result").show();
		});		
	   
	}
};