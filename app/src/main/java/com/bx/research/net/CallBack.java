package com.bx.research.net;

import android.content.Context;


import com.bx.research.entity.BaseEntity;
import com.bx.research.utils.CommonUtils;
import com.bx.research.utils.SharedPreferenceUtils;
import com.bx.research.utils.log.LogUtils;
import com.bx.research.widget.CustomProgressDialog;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

public abstract class CallBack<T extends BaseEntity> extends RequestCallBack<String> {

	private String object;
	private String cacheKey;
	private CustomProgressDialog mDialog;
	private boolean mIsShow = false;
	private boolean saveCache = false;

	private Context mContext;

	public void setObject(String object) {
		this.object = object;
	}

	public void setCacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
		saveCache = true;
		getCache();
	}

	public void saveCache() {
		saveCache = true;
	}

	private Class<?> class1;

	/**
	 * 普通构造
	 * 
	 * @param class1
	 *            返回结果的实体泛型
	 */
	public CallBack(Class<?> class1) {
		super();
		this.class1 = class1;
	}

	/**
	 * 带缓存的构造
	 * 
	 * @param class1
	 * @param cacheKey
	 */
	public CallBack(Class<?> class1, String cacheKey) {
		super();
		this.class1 = class1;
		setCacheKey(cacheKey);
	}

	/**
	 * 带加载对话框及加载提示信息的构造
	 * 
	 * @param class1
	 * @param context
	 * @param dialogMsg
	 */
	public CallBack(Class<?> class1, Context context, String dialogMsg) {
		super();
		this.class1 = class1;

		mContext = context;

		mDialog = CustomProgressDialog.createDialog(mContext);
		mDialog.setMessage(dialogMsg);
		mDialog.setCanceledOnTouchOutside(false);
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

	public BaseEntity getEntity(Class<?> class1) {
		if (object != null) {
			BaseEntity entity = JsonUtils.parseMainBeanFromJson(object, class1);
			if (entity != null) {
				return entity;
			}
		}
		return new BaseEntity();
	}

	@Override
	public void onStart() {
		super.onStart();
		showProgress();
	}

	@Override
	public void onSuccess(ResponseInfo<String> info) {
		dismissProgress();
		System.out.println("=========result===============" + info.result);
		object = info.result;

		// LogUtils.d("onSuccess:==result==>>>" + object);
		// LogUtils.json(object);

		@SuppressWarnings("unchecked")
		T b = (T) getEntity(class1);
		// if (returnFlag == 0) {// 接口正常返回时，解析数据
		doSuccess((T) b);
		// }

		if (saveCache && !CommonUtils.isBlank(cacheKey)) {
			LogUtils.d("do cache:====>>>");
			SharedPreferenceUtils.putString(cacheKey, object);
			saveCache = false;
		}
	}

	@Override
	public void onFailure(HttpException arg0, String arg1) {
		LogUtils.d("onFailure:====>>>" + arg0.getExceptionCode() + "---" + arg1);
		dismissProgress();
	}

	private void showProgress() {
		if (mDialog != null && !mIsShow) {
			mDialog.show();
		}
	}

	public void showProgress(boolean show) {
		mIsShow = show;
	}

	private void dismissProgress() {
		if (mDialog != null) {
			mDialog.dismiss();
			mIsShow = false;
		}
	}

	public abstract void doSuccess(T entity);

	public void doCache(T entity) {
	}

	private void getCache() {
		if (!CommonUtils.isBlank(cacheKey)) {
			object = SharedPreferenceUtils.getString(cacheKey);
			if (!CommonUtils.isBlank(object)) {
				LogUtils.d("Cache:====>>>" + object);
				@SuppressWarnings("unchecked")
				T b = (T) getEntity(class1);
				if (b != null) {
					doCache(b);
				}
			}
		}
	}
}
