package test;


import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.sojson.common.dao.OrderRepository;
import com.sojson.common.dao.ProductRepository;
import com.sojson.common.model.Order;

//@RunWith(SpringJUnit4ClassRunner.class)  
//@ContextConfiguration("classpath:spring-mvc.xml") 
public class UserDaoTest // extends JUnitDaoBase 
{  

//	@Autowired
//	private ProductRepository productMapper;
	@Autowired
	OrderRepository orderMapper;

	
	
	@Before
	public void before(){                                                                    
		@SuppressWarnings("resource")
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{
				"classpath:spring.xml",
				"classpath:spring-mvc.xml"
//				"classpath:spring-mybatis.xml"
				});
		orderMapper = (OrderRepository) context.getBean("orderMapper");
	}
	

//	@Test  
//	public void testproDao() {  
//
//		Product pro = productMapper.selectProductById(1L);
//		System.out.println(pro.toString());
//
//	}  


	@Test  
	public void testorderDao() {  

		Order order = new Order();
		
		order.setId(1L);
		order.setCreateTime(new Date());
		order.setPnum(1);
		order.setProductId(1L);
		
		orderMapper.saveOrder(order);

	}  





}  
