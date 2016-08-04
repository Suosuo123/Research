package com.bx.research.net;

import com.bx.research.entity.BaseEntity;
import com.bx.research.utils.log.LogUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.List;

public class JsonUtils {

	/**
	 * 解析jsonObject类型的json
	 */
	public static BaseEntity parseMainBeanFromJson(String joStr, Class<?> class1) {
		Gson gson = new Gson();
		try {
			return (BaseEntity) gson.fromJson(joStr, class1);
		} catch (Exception e) {
			e.printStackTrace();
			LogUtils.d("将json数据直接解析为对象失败:" + class1 + joStr);
			return null;
		}
	}

	public static List<?> parseBeanFromJson(String joStr, Type class1) {
		Gson gson = new Gson();
		try {
			return gson.fromJson(joStr, class1);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 序列化方法
	 * 
	 * @param bean
	 * @param type
	 * @return
	 */
	public static String bean2json(Object bean, Type type) {
		Gson gson = new GsonBuilder().registerTypeAdapter(java.util.Date.class, new UtilDateSerializer())
				.setDateFormat(DateFormat.LONG).create();
		return gson.toJson(bean);
	}

	/**
	 * 序列化方法
	 * 
	 * @param bean
	 * @return
	 */
	public static String bean2json(Object bean) {
		Gson gson = new GsonBuilder().registerTypeAdapter(java.util.Date.class, new UtilDateSerializer())
				.setDateFormat(DateFormat.LONG).create();
		return gson.toJson(bean);
	}
}
