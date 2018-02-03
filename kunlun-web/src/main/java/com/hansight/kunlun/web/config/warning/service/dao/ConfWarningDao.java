package com.hansight.kunlun.web.config.warning.service.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import com.hansight.kunlun.web.base.BaseDao;
import com.hansight.kunlun.web.config.warning.entity.ConfWarning;

@Repository
public class ConfWarningDao extends BaseDao<ConfWarning> {
	
	public List<ConfWarning> queryConfWarningAll() throws Exception{
    	 DetachedCriteria criteria= DetachedCriteria.forClass(ConfWarning.class);
    	 return query(criteria);
     }
     
     public ConfWarning getConfWarningById(String id) throws Exception{
    	 return (ConfWarning) get(ConfWarning.class,id);
     }
     
     /*public void update(ConfWarning confWarning)throws Exception{
    	 update(confWarning.getName(),confWarning);
     } 
     */
     public int getTotalPages(int pageSize,String fuzzy) throws Exception{
 		Criteria criteria = hibernateTemplate.getSessionFactory().getCurrentSession().createCriteria(ConfWarning.class);
 		if (!StringUtils.isEmpty(fuzzy)) {
 			//criteria.add(Restrictions.or(Restrictions.like("name",
 			//fuzzy, MatchMode.ANYWHERE),null));
 			
 			criteria.add(Restrictions.or(Restrictions.or(Restrictions.like("description",
 					fuzzy, MatchMode.ANYWHERE), Restrictions.like("name",
 					fuzzy, MatchMode.ANYWHERE)), Restrictions.like("id",
 					fuzzy, MatchMode.ANYWHERE)));
 			
 		}
 		return criteria.list().size() % pageSize == 0?(criteria.list().size() / pageSize):((criteria.list().size() / pageSize) + 1);
 	}
     
 	public List<ConfWarning> queryConfWarningPage(int pageNum, int pageSize	,String	fuzzy) throws Exception{
		Criteria criteria = hibernateTemplate.getSessionFactory().getCurrentSession().createCriteria(ConfWarning.class);
		criteria.setFirstResult((pageNum -1) * pageSize);
		criteria.setMaxResults(pageSize);
		if (!StringUtils.isEmpty(fuzzy)) {
			criteria.add(Restrictions.or(Restrictions.like("description",
					fuzzy, MatchMode.ANYWHERE), Restrictions.like("name",
					fuzzy, MatchMode.ANYWHERE)));
		}
		
		return criteria.list();
	}
 	public void updateReduc()throws Exception{
 		
 		hibernateTemplate.bulkUpdate("UPDATE ConfWarning SET VALUE = '0.00'");
 		/* hibernateTemplate.executeWithNativeSession(new HibernateCallback<Integer>(){
			@Override
			public Integer doInHibernate(Session session) throws HibernateException,
					SQLException {
				// TODO Auto-generated method stub
				Query query = session.createQuery("UPDATE ConfWarning SET VALUE = ?");
				query.setString(0, "未设置");
				query.executeUpdate();
				return 0;
			}
 	    	
 	    });*/
 	
 	}
 	public ConfWarning getConfWarningByName(String name) throws Exception{
 		
   	    List list = hibernateTemplate.find("from ConfWarning c  where c.name = '"+name+"'");
   	    
   	    if(list.size()>0 && list.size()==1){
		 return (ConfWarning)list.get(0);
	    }
		return null;
   	    
 		/*return hibernateTemplate.executeWithNativeSession(new HibernateCallback<ConfWarning>(){
			 @Override
			 public ConfWarning doInHibernate(Session session) throws HibernateException,
					SQLException {
				// TODO Auto-generated method stub
				Query query = session.createQuery("select * from ConfWarning where name = "+name);
			
				
				 List list = query.list();
				 if(list.size()>0 && list.size()==1){
					 return (ConfWarning)list.get(1);
				 }
				return null;
			}
 	    	
 	    });*/
    }
}
