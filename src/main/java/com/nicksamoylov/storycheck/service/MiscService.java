package com.nicksamoylov.storycheck.service;

import java.util.List;
import java.util.Random;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.nicksamoylov.storycheck.domain.News;
import com.nicksamoylov.storycheck.utils.DisplayUtils;
import com.nicksamoylov.storycheck.utils.Logging;
import com.nicksamoylov.storycheck.utils.Utils;

@Service("miscService")
public class MiscService {
	private static final Logging log = Logging.getLog("MiscService");
	
    private static Random random = new Random();
	public String findNews(String area, boolean isRussian){
		String lang =  Utils.lang(isRussian);
	    MapSqlParameterSource params = new MapSqlParameterSource();
	    params.addValue(News.DB_AREA, area);
	    params.addValue(News.DB_LANG, lang);
		List<Integer> l = jdbcTemplate.queryForList(News.SQL_SELECT_IDS_BY_AREA_AND_LANG_ORDER_BY_ID, params, Integer.class);
		int rn =  random.nextInt(l.size());
	    params.addValue(News.DB_ID, l.get(rn));
		List<String> news = jdbcTemplate.queryForList(News.SQL_SELECT_NEWS_BY_ID, params, String.class);
		return news.get(0);
	}
	
	private NamedParameterJdbcTemplate jdbcTemplate;
	
	@Resource(name="dataSource")
	public void setDataSource(DataSource dataSource) {
	    this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	public MiscService() {
		super();
	}

}
