package com.wxxiaomi.ebs.dao.inter.impl;

import java.util.List;

import javax.annotation.Resource;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.wxxiaomi.ebs.dao.bean.User;
import com.wxxiaomi.ebs.dao.bean.UserCommonInfo;
import com.wxxiaomi.ebs.dao.inter.UserDao;

@Repository
@Transactional
public class UserDaoImpl implements UserDao{

	@Resource
	SessionFactory factory;
	
	@Override
	public User getUser(String username, String password) {
		String queryString = "from User u where u.username=? and u.password=?";
		Query queryObject = factory.getCurrentSession()
				.createQuery(queryString);
		queryObject.setParameter(0, username);
		queryObject.setParameter(1, password);
		if (queryObject.list().size() > 0) {
			return (User) queryObject.list().get(0);
		} else
			return null;
	}

	@Override
	public int insertUser(User user) {
		factory.getCurrentSession().persist(user.getUserCommonInfo());
		factory.getCurrentSession().persist(user);
		return 1;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserCommonInfo> getUserInfosByEms(List<String> names) {
		String queryString = "from UserCommonInfo u where u.emname in(:list)";
		Query queryObject = factory.getCurrentSession()
				.createQuery(queryString);
		queryObject.setParameterList("list", names);
		return (List<UserCommonInfo>) queryObject.list();
	}

	@Override
	public UserCommonInfo getUserInfoByEm(String emname) {
		String queryString = "from UserCommonInfo u where u.emname=?";
		Query queryObject = factory.getCurrentSession()
				.createQuery(queryString);
		queryObject.setParameter(0, emname);
		return (UserCommonInfo) (queryObject.list().get(0));
	}

	@Override
	public UserCommonInfo getUserInfoById(int userid) {
		UserCommonInfo result = (UserCommonInfo) factory.getCurrentSession()
				.get(UserCommonInfo.class, userid);
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserCommonInfo> getUserInfosByNames(String name) {
		String queryString = "from UserCommonInfo u where u.name=?";
		Query queryObject = factory.getCurrentSession()
				.createQuery(queryString);
		queryObject.setParameter(0, name);
		return queryObject.list();
	}

	@Override
	public int updateUser(UserCommonInfo userInfo) {
		factory.getCurrentSession().update(userInfo);
		return 1;
	}

	@Override
	public int updateUserHead(int userid, String imgPath) {
		try {
			String queryString = "update UserCommonInfo u set u.head=? where u.id=?";
			Query queryObject = factory.getCurrentSession().createQuery(
					queryString);
			queryObject.setParameter(0, imgPath);
			queryObject.setParameter(1, userid);
			queryObject.executeUpdate();
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

}