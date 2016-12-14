package com.wxxiaomi.ebs.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.springframework.stereotype.Service;

import com.wxxiaomi.ebs.dao.bean.Comment;
import com.wxxiaomi.ebs.dao.bean.Option;
import com.wxxiaomi.ebs.dao.bean.Topic;
import com.wxxiaomi.ebs.dao.bean.User;
import com.wxxiaomi.ebs.dao.bean.UserCommonInfo;
import com.wxxiaomi.ebs.dao.bean.constant.OptionType;
import com.wxxiaomi.ebs.dao.bean.constant.Result;
import com.wxxiaomi.ebs.dao.inter.CommentDao;
import com.wxxiaomi.ebs.dao.inter.OptionDao;
import com.wxxiaomi.ebs.dao.inter.TopicDao;
import com.wxxiaomi.ebs.dao.inter.UserDao;
import com.wxxiaomi.ebs.module.jwt.Jwt;
import com.wxxiaomi.ebs.module.jwt.TokenState;
import com.wxxiaomi.ebs.service.UserService;
import com.wxxiaomi.ebs.util.JsonDateValueProcessor;

@Service
public class UserServiceImpl implements UserService {

	@Resource
	UserDao userDao;
	@Resource
	OptionDao optionDao;
	@Resource
	TopicDao topicDao;
	@Resource
	CommentDao commentDao;
	
	@Override
	public Result Login(String username, String password,String uniqueNum) {
		User user = userDao.getUser(username, password);
		Result result;
		if(user!=null){
			Map<String, Object> payload = new HashMap<String, Object>();
			Date date = new Date();
			payload.put("uid", user.getUserCommonInfo().id+"");// 用户id
			payload.put("iat", date.getTime());// 生成时间:当前
			payload.put("ext", date.getTime() +  60 * 60);// 过期时间2小时(60*60*2000 2小时)
			String token = Jwt.createToken(payload);
			Map<String, Object> longMap = new HashMap<String, Object>();
			longMap.put("uid", user.getUserCommonInfo().id+"");// 用户id
			longMap.put("iat", date.getTime());// 生成时间:当前
			longMap.put("ext", date.getTime() + 1000 * 60 * 60 * 24 * 15);// 过期时间15天
			longMap.put("phoneNum", uniqueNum);
			String long_token = Jwt.createToken(longMap);
			
			result = new Result(200,"",user);
			result.putHeader("token", token);
			result.putHeader("long_token", long_token);
		}else{
			result = new Result(300, "账号或者密码错误", "");
		}
		
		return result;

	}


	@Override
	public Result Register(String username, String passwrod) {
//		userDao.insertUser(user)
		return null;
	}


	@Override
	public Result getUserInfosByEms(List<String> ems) {
		List<UserCommonInfo> userInfosByEms = userDao.getUserInfosByEms(ems);
		return new Result(200, "", userInfosByEms);
	}


	@Override
	public Result getUserInfosByName(String name) {
		return new Result(200, "", userDao.getUserInfosByNames(name));
	}


	@Override
	public Result UserOptionLog(int userid) {
		List<Option> options = optionDao.getUserOptions(userid);
		for(Option option : options){
			JsonConfig jsonConfig = new JsonConfig();  
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor());  
			int type = option.getObj_type();
			switch (type) {
			case OptionType.FOOT_PRINT:
				break;
			case OptionType.PHOTO_PUBLISH: //更新相册
				break;
			case OptionType.TOPIC_COMMENT://话题评论
				//取出评论
				Comment comment = commentDao.getCommentById(option.getObj_id());
				//取出话题
				Topic topic = topicDao.getTopicById(option.getParent_id());
				System.out.println(topic.toString());
				option.setJson_obj(JSONObject.fromObject(comment,jsonConfig).toString());
				option.setJson_parent(JSONObject.fromObject(topic,jsonConfig).toString());
				break;
			case OptionType.TOPIC_PUBLISH://话题发布
				Topic t = topicDao.getTopicById(option.getObj_id());
				option.setJson_obj(JSONObject.fromObject(t,jsonConfig).toString());
				break;
			}
		}
		return new Result(200, "", options);
	}


	@Override
	public Result LongToken(String ltoken, String phoneId) {
		System.out.println("LongToken,ltoken:"+ltoken);
		Result result = null;
		//先验证longToken的正确性，再返回短token
		Map<String, Object> resultMap = Jwt.validToken(ltoken);
		TokenState tokenState = TokenState.getTokenState((String) resultMap.get("state"));
		switch (tokenState) {
		case VALID:
			//通过
			@SuppressWarnings("unchecked")
			HashMap<String, String> dataobj = ((HashMap<String, String>) resultMap
					.get("data"));
			System.out.println(dataobj);
			String userid = dataobj.get("uid");
			
			Map<String, Object> payload = new HashMap<String, Object>();
			Date date = new Date();
			payload.put("uid",userid);// 用户id
			payload.put("iat", date.getTime());// 生成时间:当前
			payload.put("ext", date.getTime() + 2000 * 60 * 60);// 过期时间2小时
			String stoken = Jwt.createToken(payload);
			
			Map<String, Object> longMap = new HashMap<String, Object>();
			longMap.put("uid", userid);// 用户id
			longMap.put("iat", date.getTime());// 生成时间:当前
			longMap.put("ext", date.getTime() + 1000 * 60 * 60 * 24 * 15);// 过期时间15小时
			longMap.put("phoneNum", dataobj.get("phoneNum"));
			String long_token = Jwt.createToken(longMap);
			
			result = new Result(200,"",userid);
			result.putHeader("long_token", long_token);
			result.putHeader("token", stoken);
			break;
		case EXPIRED:
			//无效
		case INVALID:
			//过期
			result = new Result(402,"登陆凭证过期，请重新登陆","");
			break;

		default:
			break;
		}
		return result;
	}


	@Override
	public Result updateUserInfo(int id,String name, String head,
			String emname) {
		try{
		UserCommonInfo info = new UserCommonInfo();
		info.name = name;
		info.head = head;
		info.id = id;
		info.emname = emname;
		userDao.updateUser(info);
		return new Result(200,"","success");
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	return new Result(404,"出错","success");
	}
	
	
	
	
	
	
	
	
	
	
	
	
	//	@SuppressWarnings("unchecked")
//	@Override
//	public List<UserCommonInfo> getUserListByEMUsername(List<String> list) {
//		String queryString = "from UserCommonInfo u where u.emname in(:list)";
//		Query queryObject = factory.getCurrentSession()
//				.createQuery(queryString);
//		queryObject.setParameterList("list", list);
//		return (List<UserCommonInfo>) queryObject.list();
//	}

//	@Override
//	public boolean checkPhoneExists(String phone) {
//		// String queryString = "from UserCommonInfo u where u.phone=?";
//		return false;
//
//	}
//
//	@Override
//	public User registerUser(String username, String password) {
//		try {
//			// 新建user
//			User user = new User();
//			user.setUsername(username);
//			user.setPassword(password);
//
//			// 设置CommonInfo
//			UserCommonInfo info = new UserCommonInfo();
//			info.setName(username);
//			info.setHead("default.jpg");
//
//			// 申请IM注册
//			ObjectNode createNewIMUserSingle = EasemobIMUsers
//					.createNewIMUserSingle(username, password);
//			int statusCode = Integer.valueOf(createNewIMUserSingle.get(
//					"statusCode").toString());
//
//			if (statusCode == 200) {
//				info.setEmname(username);
//				factory.getCurrentSession().persist(info);
//				user.setUserCommonInfo(info);
//				factory.getCurrentSession().persist(user);
//			} else {
//				throw new Exception("IM注册失败，错误码：" + statusCode + "(错误信息):"
//						+ createNewIMUserSingle.get("error"));
//			}
//
//			return user;
//		} catch (Exception e) {
//			return null;
//		}
//	}
//
//	@Override
//	public UserCommonInfo getUserCommonInfoById(int userid) {
//		UserCommonInfo result = (UserCommonInfo) factory.getCurrentSession()
//				.get(UserCommonInfo.class, userid);
//		return result;
//	}
//
//	@Override
//	public boolean improveUserInfo(int userid, String emname, String name,
//			String description) {
//		try {
//			UserCommonInfo user = new UserCommonInfo();
//			user.setId(userid);
//			user.setEmname(emname);
//			user.setHead("");
//			user.setName(name);
//			factory.getCurrentSession().update(user);
//			return true;
//		} catch (Exception e) {
//			return false;
//		}
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public List<UserCommonInfo> getUserInfoByName(String name) {
//		String queryString = "from UserCommonInfo u where u.name=?";
//		Query queryObject = factory.getCurrentSession()
//				.createQuery(queryString);
//		queryObject.setParameter(0, name);
//		return queryObject.list();
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public List<UserCommonInfo> getUserInfoByEmname(String emname) {
//		String queryString = "from UserCommonInfo u where u.emname=?";
//		Query queryObject = factory.getCurrentSession()
//				.createQuery(queryString);
//		queryObject.setParameter(0, emname);
//		return queryObject.list();
//	}
//
//	@Override
//	public boolean updateUserHead(int userid, String path) {
//		try {
//			String queryString = "update UserCommonInfo u set u.head=? where u.id=?";
//			Query queryObject = factory.getCurrentSession().createQuery(
//					queryString);
//			queryObject.setParameter(0, path);
//			queryObject.setParameter(1, userid);
//			queryObject.executeUpdate();
//			return true;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//	}
//
//	
//	@Override
//	public boolean insertUserPhoto(List<Photo> photos) {
//		for(Photo item : photos){
//			factory.getCurrentSession().persist(item);
//		}
//		
//		return true;
//	}
//
//	@Override
//	public List<String> getUserPhoto(int album_id, int size) {
//		String queryString = "select url from Photo p where p.album_id=?";
//		Query queryObject = factory.getCurrentSession()
//				.createQuery(queryString);
//		queryObject.setParameter(0, album_id);
//		return queryObject.list();
//	}

	

	

//	@Override
//	public boolean insertUserPhoto(int userid, int album_id, String[] imgUrl,
//			String create_time) {
//		//更新user的相片数量
//		//向数据库插入photo
//		return false;
//	}

}
