package com.wxxiaomi.ebs.service;


import com.wxxiaomi.ebs.dao.bean.constant.Result;


public interface MapService {

	Result getNearByPerson(int userid, String geo);
	
	Result savaLocation(int userid, String geo);
	
	Result collectLocat(int userid,String geo,String locat_tag,String content,String picture);
	
//	List<Locat> createNearByPeople(double latitude,
//			double longitude);
	
	Result listFootPrint(int target_id);
}
