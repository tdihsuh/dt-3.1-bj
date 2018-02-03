define(['app'],function(app){
	app.controller("informationCtrl",["$scope","$http","$timeout",function($scope,$http,$timeout){
		var hide;
		var timeId;
		$scope.close = function(){
			$("#winpop").css("display","none");
		} 
		query=function(){
			var url = "warning/getInformation.hs";
			/*			var changH = function(str){
				var msgPop = $("#winpop");
				var me = this ;
				//var popH = parseInt(MsgPop.style.height);
				var popH = $("#winpop").height();
				if(str=="up"){
					if(popH<=160){
					   $("#winpop").css("height",(popH+8)+"px");
					  me.interval = setInterval(function(){changH("up")},30);
					}else{
						alert(me.interval);
						clearInterval(me.interval);
						
					}
				}if(str=="down"){
					if(popH>=8){
						$("#winpop").css("height",(popH-8).toString()+"px");
						hide = setInterval(changH("down"),50);
						
					}else{
						clearInterval(hide);
						$("#winpop").css("display","none");
						$("#winpop").css("height","4px");
						console.log($("#winpop").css("height"));
					}
				}
			}*/
			$http.post(url).success(function(data){
				var information = data.model.information;
				if(information!=null){
					$("#myorder").text(information);
					$("#winpop").height(160).slideDown(2000);
					$("#winpop").delay(5000);
					$("#winpop").height(0).slideUp(2000);
					//$("#winpop").delay(40000);
					//$("#winpop").height(160).slideDown(2000);
					//setInterval(query,10000);
					//$("#winpop").css("display","none");
					//setTimeout($("#winpop").height(0).slideUp("slow"),3000);
				   // $("#winpop").height(0).slideUp("slow");
					
//					var msgPop = $("#winpop");
//					var popH = $("#winpop").css("height");
//					if(popH=="4px"){
//						alert("showUp");
////						$("#winpop").css("display","block");
////						changH("up");
//						$("#winpop").height(164).slideUp();
//					}else{
//						alert("down");
////						changH("down");
//						$("#winpop").height(0).slideDown();
//					}
				}
				else{
					//alert(information+"@@@");
					$("#winpop").height(0).slideUp(600);
					//setInterval(query,30000);
				}
			});
		};
		setInterval(query,60000);
		
	}])
});