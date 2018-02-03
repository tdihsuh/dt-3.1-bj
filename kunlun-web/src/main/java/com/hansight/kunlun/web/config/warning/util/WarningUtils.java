package com.hansight.kunlun.web.config.warning.util;

import com.hansight.kunlun.web.config.warning.framework.controllers.WarningController;
import com.hansight.kunlun.web.config.warning.service.EsMonitorJob;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class WarningUtils {
	private static final Logger LOG = LoggerFactory.getLogger(EsMonitorJob.class);
	public WarningController wc = new WarningController();
	private static String  name = "alert.es.rate";
	/*static{
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://172.16.219.20:3306/hansight", "root", "123");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(SQLException e){
			e.printStackTrace();
		}
	        //wc = new WarningController();
		
	}*/
	/*public static  ConfWarning getConf() {
		//ConfWarning cw = warningDao.getConfWarningByName(name);
		/*ConfWarning cw = null;
		try {
			ConfWarning l = wc.getConf("2c905b9b487c07ec01487c767fc800022");
			System.out.println(l.getEmail()+"@@@@@@@@@@@@@@@@@@@@@22s");
			System.out.println(l.getValue());
			//cw = (ConfWarning)l.get(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(cw);
			e.printStackTrace();
		}
		String email = cw.getEmail();
		String value = cw.getValue();
		System.out.println(email+"@@@@@@@@@"+value);
		return cw;
		
		Statement stm = null;
		ResultSet query = null;
		String email = null;
		ConfWarning cw = new ConfWarning();
		try {
			Class.forName("com.mysql.jdbc.Driver");
			try {
				//con=DriverManager.getConnection("jdbc:mysql://172.16.219.135:3306/hansight","root","admin");
				stm=connection.createStatement();
				//stm.addBatch("select EMALL from T_USER");
				query = stm.executeQuery("select  * from CONF_WARNING where name = 'alert.es.rate'");
				if(query.next()){
					 	email = query.getString("EMAIL");
					 	String value = query.getString("VALUE");
					 	System.out.println(email);
					 	
					 	cw.setEmail(email);
					 	cw.setValue(value);
				}
				return cw;
			} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}finally{
				try{
					if(query != null){
						query.close();
					}
					if(stm != null){
						stm.close();
					}
				}catch(SQLException e){
					e.printStackTrace();
				}
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cw;
	}*/
	
	/*public static void sendJavaEmail(String email,String m){
		Properties props = new Properties();
		props.put("mail.host", "smtp.163.com");
		//props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth","true");
		//MailAuthenticator ma=new MailAuthenticator("baichaolumeng1@163.com","yaya123.");
		Session session = Session.getInstance(props,null);
		Message message = new MimeMessage(session);
		
		try {
			message.setFrom(new InternetAddress("baiclm@163.com"));
			System.out.println(email);
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
			message.setSubject("异常报警");
			message.setText(m);
			message.saveChanges();
			
			Transport transport = session.getTransport("smtp");
			transport.connect("baiclm@163.com", "yaya123.");
			//Transport.send(message);
			transport.send(message);
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}*/
	public  void warning(String email,String msg) throws Exception{
		//ConfWarning conf = getConf();
		//sendJavaEmail(email,msg);
		sendSimpleEmail(email,msg);
	}
	public static void sendSimpleEmail(String email,String m){
		SimpleEmail sm = new SimpleEmail();
		sm.setHostName("smtp.163.com");
		sm.setAuthentication("batubate1@163.com", "batu123.");
		sm.setCharset("UTF-8");
		try {
			sm.setFrom("batubate1@163.com");
			sm.addTo(email);
			sm.setSubject("异常报警");
			sm.setMsg(m);
			sm.setSentDate(new Date());
			sm.send();
		} catch (EmailException e) {
			// TODO Auto-generated catch block
			LOG.debug("请查看邮件地址是否正确");
		}
	}

}
