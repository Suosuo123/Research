package com.bx.research.net;

import com.bx.research.entity.BaseEntity;
import com.bx.research.utils.log.LogUtils;
import com.google.gson.Gson;

import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public abstract class CallBackForJsonArray<T extends BaseEntity> extends RequestCallBack<String> {

	private String object;

	public void setObject(String object) {
		this.object = object;
	}

	private Class<?> class1;

	public CallBackForJsonArray(Class<?> class1) {
		super();
		this.class1 = class1;
	}

	public BaseEntity getMEntity() {
		if (object != null) {
			BaseEntity entity = JsonUtils.parseMainBeanFromJson(object, BaseEntity.class);
			return entity;
		}
		return new BaseEntity();
	}

	public static BaseEntity getEntity(String jsonData, Class<?> class1) {
		if (jsonData != null) {
			BaseEntity entity = JsonUtils.parseMainBeanFromJson(jsonData, class1);
			return entity;
		}
		return new BaseEntity();
	}

	public List<T> getEntity(Class<?> class1) {
		List<T> list = new ArrayList<T>();
		if (object != null) {
			JSONArray jsonArray;
			try {
				jsonArray = new JSONArray(object);
				for (int i = 0; i < jsonArray.length(); i++) {
					Gson gson = new Gson();

					@SuppressWarnings("unchecked")
					T t = (T) gson.fromJson(jsonArray.get(i).toString(), class1);

					list.add(t);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
		return list;
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onSuccess(ResponseInfo<String> info) {
		object = info.result;// object = object.replaceAll("\\\\", "");
		LogUtils.d("onSuccess:" + object);
		List<T> list = getEntity(class1);
		// if (b.getStatus() == 1) {
		doSuccess(list);
		// }
	}

	@Override
	public void onFailure(HttpException arg0, String arg1) {
		LogUtils.d("onFailure:" + arg0.getExceptionCode() + arg1);
		// WinToast.toast(MainApplication.getInstance(), "failed:" +
		// arg0.getExceptionCode() + arg1);
	}

	public abstract void doSuccess(List<T> list);

}
