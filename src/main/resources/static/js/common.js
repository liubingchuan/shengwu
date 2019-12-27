$("#registry").click(function(){
	$("#alertZh").css('display','none');
	$("#alertbd").css('display','block');
});

$("#login").click(function(){
	$("#alertbd").css('display','none');
	$("#alertZh").css('display','block');
});
$("#quit").click(function(){
	$.cookie("role", "", {expires: -1});
	$.cookie("loginName", "", {expires: -1});
	window.location.href = "/";
});

$("#close").click(function(){
	$("#alertZh").css('display','none');
	
});
$("#closezhuce").click(function(){
	$("#alertbd").css('display','none');
	
});

$("#chanyejiance").click(function(){
	if($.cookie("role")!=null) {
		window.location.href = "/jiance/jiancelist";
	}else {
		alert("请先登录")
		$("#login").click()
	}
});
$("#xituzhiku").click(function(){
	if($.cookie("role")!=null) {
		window.location.href = "/org/list?pageSize=10&pageIndex=0&front=0";
	}else {
		alert("请先登录")
		$("#login").click()
	}
});
$("#xituzhiku2").click(function(){
	if($.cookie("role")!=null) {
		window.location.href = "/org/list?pageSize=10&pageIndex=0&front=0";
	}else {
		alert("请先登录")
		$("#login").click()
	}
});
$("#zhuanlifenxi").click(function(){
	if($.cookie("role")!=null) {
		window.location.href = "/patent/agmount?q=";
	}else {
		alert("请先登录")
		$("#login").click()
	}
});
$("#zhuanlifenxi2").click(function(){
	if($.cookie("role")!=null) {
		window.location.href = "/patent/agmount?q=";
	}else {
		alert("请先登录")
		$("#login").click()
	}
});
$("#zhishifuwu").click(function(){
	if($.cookie("role")!=null) {
		window.location.href = "/zhishifuwu/zhishifuwu";
	}else {
		alert("请先登录")
		$("#login").click()
	}
});
$("#zhishifuwu2").click(function(){
	if($.cookie("role")!=null) {
		window.location.href = "/zhishifuwu/zhishifuwu";
	}else {
		alert("请先登录")
		$("#login").click()
	}
});
$("#zhuanlifenxi").click(function(){
	if($.cookie("role")!=null) {
		window.location.href = "/patent/agmount?q=";
	}else {
		alert("请先登录")
		$("#login").click()
	}
});
$("#zhuanlifenxi2").click(function(){
	if($.cookie("role")!=null) {
		window.location.href = "/patent/agmount?q=";
	}else {
		alert("请先登录")
		$("#login").click()
	}
});




$("#bindButtion").click(function(){
	if($("#bdaccount").val() == ""){
		alert('请输入用户名','提示')
		return;
	}
	if($("#bdpassword").val() == ""){
		alert('请输入密码','提示')
		return;
	}
	if($("#bdpassword2").val() == ""){
		alert('请确认密码','提示')
		return;
	}
	if($("#bdpassword").val() !== $("#bdpassword2").val()){
		alert('两次输入密码不一致','提示')
		return;
	}
	
	if($("#bdemail").val() == ""){
		alert('请输入电子邮箱','提示')
		return;
	}
	if(!$("#bdemail").val().match(/^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+((\.[a-zA-Z0-9_-]{2,3}){1,2})$/)){
		alert('请输入有效邮箱','提示')
		return;
	}
	
	
	var formObject = {};
    var formArray =$("#bindForm").serializeArray();
    $.each(formArray,function(i,item){
        formObject[item.name] = item.value;
    });
    //formObject["nickName"] = $.cookie("nickName")
    //formObject["openId"] = $.cookie("openId")
    $.ajax({
        url:"user/bind",
        type:"post",
        contentType: "application/json; charset=utf-8",
        data: JSON.stringify(formObject),
        dataType: "json",
        success:function(data){
        	$("#alertbd").css('display','none'); 
            if(data.code == "200")
            {
            	$.cookie("role",data.role);
            	$.cookie("loginName",data.loginName);
			    //$("#headImg").attr("src", $.cookie("headImg"));
				$("#loginBefore").css('display','none');
			    $("#loginAfter").css('display','block');
				window.location.href = "/";
            }
            else
            {
            	console.log("binding error" + data);
            	if(data.code=="401") {
            		alert(data.msg);
            	}else {
            	 $("#quit").click();
            	}
            	
            }
        },
        error:function(e){
            alert("错误！！");
            $("#quit").click();
        }
    });
});

$("#loginButtion").click(function(){
	if($("#lnaccount").val() == ""){
		alert('请输入用户名','提示')
		return;
	}
	if($("#lnpassword").val() == ""){
		alert('请输入密码','提示')
		return;
	}
	
	
	var formObject = {};
	var formArray =$("#loginForm").serializeArray();
	$.each(formArray,function(i,item){
		formObject[item.name] = item.value;
	});
	$.ajax({
		url:"user/login",
		type:"post",
		contentType: "application/json; charset=utf-8",
		data: JSON.stringify(formObject),
		dataType: "json",
		success:function(data){
			$("#alertZh").css('display','none'); 
			if(data.code == "200")
			{
				$.cookie("role",data.role);
				$.cookie("loginName",data.loginName);
				//$("#headImg").attr("src", $.cookie("headImg"));
//				$("#loginBefore").css('display','none');
//				$("#loginAfter").css('display','block');
				window.location.href = "/";
			}
			else
			{
				console.log("login error" + data);
				if(data.code=="401") {
					alert(data.msg);
				}else {
					$("#quit").click();
				}
				
			}
		},
		error:function(e){
			alert("错误！！");
			$("#quit").click();
		}
	});
});

function showLoginInfoFrontend(){
	$("#alertbd").css('display','none');
    if($.cookie("loginName")!=null) {
		$("#loginBefore").css('display','none'); 
	    //$("#headImg").attr("src", $.cookie("headImg"));
		$("#loginAfter").css('display','block');
		if($.cookie("role")!=null && $.cookie("role")==="普通用户"){
			$(".vip").html("普通用户"); 
			$("#backgroud").css('display','none');
			/* document.getElementById("xituzhiku").removeAttribute("href");
			document.getElementById("xituzhiku1").removeAttribute("href");
			document.getElementById("xituzhiku2").removeAttribute("href");
			document.getElementById("xituzhiku3").removeAttribute("href");
			document.getElementById("xituzhiku4").removeAttribute("href");
			document.getElementById("xituzhiku5").removeAttribute("href");
			document.getElementById("xituzhiku6").removeAttribute("href");
			document.getElementById("xituzhiku11").removeAttribute("href");
			
			document.getElementById("cailiaojisuan").removeAttribute("href");
			document.getElementById("cailiaojisuan1").removeAttribute("href");
			document.getElementById("cailiaojisuan2").removeAttribute("href");
			document.getElementById("cailiaojisuan11").removeAttribute("href");
			
			document.getElementById("chanyejiance").removeAttribute("href");
			document.getElementById("chanyejiance1").removeAttribute("href");
			document.getElementById("chanyejiance2").removeAttribute("href");
			document.getElementById("chanyejiance3").removeAttribute("href");
			document.getElementById("chanyejiance11").removeAttribute("href");
			
			document.getElementById("zhishifuwu").removeAttribute("href");
			document.getElementById("zhishifuwu1").removeAttribute("href");
			document.getElementById("zhishifuwu2").removeAttribute("href");
			document.getElementById("zhishifuwu3").removeAttribute("href");
			document.getElementById("zhishifuwu4").removeAttribute("href");
			document.getElementById("zhishifuwu11").removeAttribute("href"); */
		}
		if($.cookie("role")!=null && $.cookie("role")==="admin"){
			$(".vip").html("admin"); 
			$("#backgroud").css('display','block'); 
		}
		$(".vip").html($.cookie("role"));
		$("#logoinName").html($.cookie("loginName"));
	}else {
		$("#loginBefore").css('display','block'); 
		$("#loginAfter").css('display','none'); 
	}
}

function showLoginInfoBackend(){
    if($.cookie("role")!=null) {
		if($.cookie("role")!=null){
			$("#logoinNames").html($.cookie("loginName")); 
			$("#logoinRole").html($.cookie("role")); 
		}
	}else {
		alert("请登录")
		window.location.href = "/";
	}
}