package com.xitu.app.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.xitu.app.common.R;
import com.xitu.app.common.SystemConstant;
import com.xitu.app.common.request.LoginRequest;
import com.xitu.app.common.request.RegisterRequest;
import com.xitu.app.common.request.UpdateUserRequest;
import com.xitu.app.mapper.ItemMapper;
import com.xitu.app.mapper.UserMapper;
import com.xitu.app.model.Item;
import com.xitu.app.model.User;
import com.xitu.app.service.Cache;
import com.xitu.app.utils.BeanUtil;
import com.xitu.app.utils.JwtUtils;



@CrossOrigin(origins = "*", maxAge = 3600, allowCredentials = "true")
@Controller
public class UserController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	
	@Autowired
    private UserMapper userMapper;

	@Autowired
	private ItemMapper itemMapper;
	
	@Autowired
    private Cache cache;
	
	
	@RequestMapping("/")
	public String index(Model model){
		User user = new User();
		model.addAttribute("user", user);
		return "index";
	}
	
	@ResponseBody
	@RequestMapping(value = "user/bind", method = RequestMethod.POST,consumes = "application/json")
	public R bind(@RequestBody RegisterRequest request) {
		System.out.println("entering binding");
//		String openId = request.getOpenId();
		String account = request.getAccount();
//		System.out.println("openId is " + openId);
		String password = request.getPassword();
		System.out.println("password is " + password);
		String email = request.getEmail();
		System.out.println("email is " + email);
		String nickName = request.getNickName();
		System.out.println("nickName is " + nickName);
		
		User user = userMapper.getUserByAccount(account);
		if(user != null && user.getId() != null) {
			return R.error().put("code", "401").put("msg", "该用户名已被注册，请换一个用户名");
		}
		
//		user = userMapper.getUserByOpenId(openId);
		user = new User();
		user.setAccount(account);
		user.setPassword(password);
		user.setEmail(email);
		user.setRole("普通用户");
//		if(user != null && user.getId() != null) {
//			userMapper.updateByOpenId(user);
//			System.out.println("bind successfully");
//		}else {
//			logger.info("微信注册绑定失败，没有找到相应openId的人");
//			return R.error();
//		}
		userMapper.insertUser(user);
		
		return R.ok().put("loginName", account).put("role", "普通用户");
	}
	
	@ResponseBody
	@RequestMapping(value = "user/login", method = RequestMethod.POST,consumes = "application/json")
	public R login(@RequestBody LoginRequest request) {
		String account = request.getAccount();
		String password = request.getPassword();
		User user = userMapper.getUserByAccount(account);
		if (!user.getPassword().equals(password)) {
			return R.error();
		}
		String role = user.getRole();
		return R.ok().put("loginName", account).put("role", role);
	}
	
	
	@PostMapping(value = "user/update")
	public String updateUser(UpdateUserRequest request,Model model, RedirectAttributes redirectAttributes) {
		User user = new User();
		BeanUtil.copyBean(request, user);
		userMapper.updateById(user);
		redirectAttributes.addAttribute("token", request.getToken());
		return "redirect:/user/list";
	}
	
	@GetMapping(value = "user/list")
	public String users(@RequestParam(required=false, value="token") String token, 
			@RequestParam(required=false,value="pageSize") Integer pageSize, 
			@RequestParam(required=false, value="pageIndex") Integer pageIndex, 
			Model model) {
		if(pageSize == null) {
			pageSize = 10;
		}
		if(pageIndex == null) {
			pageIndex = 0;
		}
		if("-1".equals(String.valueOf(pageIndex))) {
			pageIndex = 0;
		}
		int start = Integer.valueOf(pageIndex) * Integer.valueOf(pageSize);
		int totalCount = userMapper.getUserCount();
		int end = totalCount-1;
		int totalPages = totalCount/Integer.valueOf(pageSize) + 1;
		if(pageIndex.equals(String.valueOf(totalPages))) {
			pageIndex = pageIndex - 1;
		}else {
			end = pageSize * (pageIndex+1);
		}
		List<User> userList = userMapper.getUsers(start, end);
		model.addAttribute("userList", userList);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("pageIndex", pageIndex);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("totalPages", totalPages);
			
		return "manage";
	}
	
	@GetMapping(value = "user/getUser")
	public String getUser(@RequestParam("account") String account, Model model) {
		User user = userMapper.getUserByAccount(account);
		model.addAttribute("user", user);

		Item yhsfitem = itemMapper.selectItemByService("yhsf");
		List<String> yhsfitemitems = new ArrayList<String>();
		if(yhsfitem != null && yhsfitem.getItem() != null) {
			for(String s: yhsfitem.getItem().split(";")) {
				yhsfitemitems.add(s);
			}
		}
		model.addAttribute("yhsfitems", yhsfitemitems);

		return "manage_mess";
	}
	
	
	
}
