$("#quit").click(function(){
	$.cookie("openId", "", {expires: -1});
	$.cookie("nickName", "", {expires: -1});
	$.cookie("headImg", "", {expires: -1});
	$.cookie("role", "", {expires: -1});
	window.location.href = "/";
});

function showLoginInfo(){
	//$.cookie("openId","2");
	//$.cookie("role","管理员");
	//$.cookie("role","操作员");
	//$.cookie("nickName","测试人");
	if($.cookie("openId")!=null) {
		$("#headImg").attr("src", $.cookie("headImg"));
		$("#logoinName").html($.cookie("nickName"));
		$("#logoinNames").html($.cookie("nickName"));
		$("#logoinRole").html($.cookie("role"));
		
		if($.cookie("role")!=null && $.cookie("role")==="操作员"){
			document.getElementById("fenleishezhi").removeAttribute("href");
			document.getElementById("hangyerencai").removeAttribute("href");
			document.getElementById("zhogndianjigou").removeAttribute("href");
			document.getElementById("fenxibaogao").removeAttribute("href");
			document.getElementById("jieguoliebiao").removeAttribute("href");
			document.getElementById("xiangmuxinxi").removeAttribute("href");
		}
	}
}