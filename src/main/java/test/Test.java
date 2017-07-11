package test;
import java.util.Calendar;
import java.util.Date;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.sojson.common.dao.OrderRepository;
import com.sojson.common.dao.ProductRepository;
import com.sojson.common.model.Order;
import com.sojson.common.model.Product;
import com.sojson.common.utils.LockUtil;
import com.sojson.permission.service.OrderService;
import com.sojson.permission.service.impl.OrderServiceImpl;


public class Test {

	private static ApplicationContext ctx;  

	public static void testpurchase(){

		//		Order order = (Order) ctx.getBean("order");
		OrderService os = new OrderServiceImpl(); 
		Order order = new Order();
		order.setProductId(1L);
		order.setCreateTime(new Date());
		order.setPnum(1);
		os.loadOrder(order);
	}

	public static void testMapper(){
		ProductRepository mapper = (ProductRepository)ctx.getBean("productMapper"); 
		//����id=1���û���ѯ���������ݿ��е���������Ըĳ����Լ���.
		System.out.println("�õ��û�id=1���û���Ϣ");
		Product product = mapper.selectProductById(1L);
		System.out.println(product.getName()); 

		OrderRepository omapper = (OrderRepository)ctx.getBean("orderMapper"); 
		Order order = new Order();
		order.setProductId(1L);
		order.setCreateTime(new Date());
		order.setPnum(1);
		omapper.saveOrder(order);   	
	}

	public static void testShardLog(int type,String identity){
		System.out.println("---------------��ʼ��ȡ��"+identity);
		LockUtil.init("192.168.1.110:2181,jfapp1:2181");
		LockUtil.getShardLock(type, identity);
		System.out.println("---------------��ȡ������"+identity);
	}
	public static void main(String[] args)  
	{  
		
		ctx = new ClassPathXmlApplicationContext("classpath:spring.xml");  
		int type = Integer.parseInt(args[0]);
		System.out.println("type="+type);
		testShardLog(type,args[1]);
		//	    	testShardLog(0,"f6");
		//	    	try {
		//	    		LockUtil.init("192.168.1.110:2181");
		//				//LockUtil.addChildWatcher("/LockService");
		//	    		LockUtil.getExclusiveLock();
		//			} catch (Exception e1) {
		//				// TODO Auto-generated catch block
		//				e1.printStackTrace();
		//			}
		long nowtime = Calendar.getInstance().getTimeInMillis();
		System.out.println("begin with "+nowtime);
		testpurchase();
		System.out.println("end with "+nowtime);
		//	String []names = ctx.getBeanDefinitionNames();
		//	    	for(String s: names){
		//	    		System.out.println(s);
		//	    	}

		//	    	try {
		//	    		Thread.sleep(20000);
		//	    		System.out.println("-------------��ʼ--�ͷ���");
		//	    		LockUtil.unlockForShardLock();
		//	    		System.out.println("-------------�ɹ�--�ͷ���");
		//				Thread.sleep(30000);
		//				System.out.println("---------------�����˳�");
		//			} catch (InterruptedException e) {
		//				// TODO Auto-generated catch block
		//				e.printStackTrace();
		//			}
	} 

}
