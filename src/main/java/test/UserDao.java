package test;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.sojson.common.model.UUser;
import com.sojson.core.mybatis.BaseMybatisDao;

public class UserDao {

	@Test
	public void test() {
		
		
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring-mybatis.xml");    

		BaseMybatisDao dao = (BaseMybatisDao) applicationContext.getBean("baseMybatisDao");
		
		SqlSession sqlSession = dao.getSqlSession();
		List<UUser> list = sqlSession.selectList("select * from u_user");
		
		for(UUser user : list){
			
			System.out.println(user.toString());
		}
		
		
		
		
		
		
		

	}
	
	
	
	
}
