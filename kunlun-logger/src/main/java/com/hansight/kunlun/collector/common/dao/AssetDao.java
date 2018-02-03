package com.hansight.kunlun.collector.common.dao;

import com.hansight.kunlun.base.dao.BaseDao;
import com.hansight.kunlun.collector.common.model.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Author:chao_bai DateTime:2014/7/29 16:43.
 */
public class AssetDao extends ESDao<DataSource> implements BaseDao<String, DataSource> {
	private static final Logger LOG = LoggerFactory.getLogger(AssetDao.class);

	@Override
	protected DataSource toObject(Map<String, Object> map, String id) {
		return toObject(DataSource.class.getDeclaredFields(), new DataSource(), map, id);
	}

	@Override
	Map<String, Object> toMap(DataSource t) {
		return toMap(DataSource.class.getDeclaredFields(), t);
	}

}
