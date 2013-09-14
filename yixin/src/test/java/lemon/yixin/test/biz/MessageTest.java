package lemon.yixin.test.biz;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import lemon.shared.api.MmtAPI;
import lemon.shared.entity.Customer;
import lemon.shared.entity.Status;
import lemon.shared.mapper.CustomerMapper;
import lemon.yixin.YiXin;
import lemon.yixin.bean.*;
import lemon.yixin.bean.message.MusicMessage;
import lemon.yixin.bean.message.TextMessage;
import lemon.yixin.bean.message.VideoMessage;
import lemon.yixin.bean.message.AudioMessage;
import lemon.yixin.biz.YiXinMsgHelper;
import lemon.yixin.biz.parser.*;
import lemon.yixin.dao.YXConfigMapper;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@RunWith(JUnit4.class)
public class MessageTest {
	private MmtAPI api;
	private final String Subscribe_msg = "Welcome to Subscribe Lemon Test.";
	private final String TOKEN = "1230!)*!)*#)!*Q)@)!*";
	private final String MMT_TOKEN = "lemonxoewfnvowensofcewniasdmfo";
	private final String bizClass = "lemon.yixin.test.biz.LemonMessageBiz";
	private final int cust_id = 100;
	private YiXinMsgHelper msgHelper;
	private ApplicationContext acx;
	private CustomerMapper customerMapper;
	private YXConfigMapper	yxConfigMapper;
	@Before
	public void init() {
		String[] resource = { "classpath:spring-db.xml",
				"classpath:spring-dao.xml", "classpath:spring-service.xml" };
		acx = new ClassPathXmlApplicationContext(resource);
		api = acx.getBean(MmtAPI.class);
		msgHelper = acx.getBean(YiXinMsgHelper.class);
		customerMapper = acx.getBean(CustomerMapper.class);
		yxConfigMapper = acx.getBean(YXConfigMapper.class);
		assertNotNull(api);
		assertNotNull(msgHelper);
		assertNotNull(customerMapper);
		assertNotNull(yxConfigMapper);
		
		//add customer
		Customer cust = customerMapper.getCustomer(cust_id);
		if(cust == null){
			cust = new Customer();
			cust.setCust_id(cust_id);
			cust.setCust_name("Test");
			cust.setMemo("");
			cust.setStatus(Status.AVAILABLE);
			customerMapper.addCustomer(cust);
			assertNotEquals(cust.getCust_id(), 0);
		}
		
		//add YiXin configure
		YiXinConfig cfg = yxConfigMapper.get(cust_id);
		if(null == cfg){
			cfg = new YiXinConfig();
			cfg.setCust_id(cust_id);
			cfg.setToken(TOKEN);
			cfg.setApi_url(MMT_TOKEN);
			cfg.setYx_account("lemon_test");
			cfg.setAppid("");
			cfg.setSecret("");
			cfg.setBiz_class(bizClass);
			cfg.setSubscribe_msg(Subscribe_msg);
			yxConfigMapper.save(cfg);
			assertNotEquals(cfg.getCust_id(), 0);
		}
		YiXin.init();
		YiXin.setConfig(cfg);
	}
	@Test
	public void testSaveTextMsg(){
		String txtMsg = "<xml>  <ToUserName>11b09b69e7e169ed</ToUserName>  <FromUserName>eddc9f8ab0c0afc9</FromUserName>  <CreateTime>1379080088</CreateTime>  <MsgId>5</MsgId>  <MsgType>text</MsgType>  <Content>你好</Content></xml>";
		TextMessage msg = acx.getBean(TextMsgParser.class).toMsg(txtMsg);
		msgHelper.saveRecvTextMsg(msg);
	}
	@Test
	public void parserMsgType() throws JDOMException, IOException{
		String msg = "<xml>  <ToUserName>11b09b69e7e169ed</ToUserName>  <FromUserName>eddc9f8ab0c0afc9</FromUserName>  <CreateTime>1379128652</CreateTime>  <MsgId>5</MsgId>  <MsgType>text</MsgType>  <Content>你好</Content></xml>";
		InputStream is = new ByteArrayInputStream(msg.getBytes("UTF-8"));
		Document doc = new SAXBuilder().build(is);
		Element msgType = doc.getRootElement().getChild("MsgType");
		Assert.assertTrue("text".equals(msgType.getValue()));
	}
	@Test
	public void textMsgTest(){
		String txtMsg = "<xml>  <ToUserName>11b09b69e7e169ed</ToUserName>  <FromUserName>eddc9f8ab0c0afc9</FromUserName>  <CreateTime>1379128652</CreateTime>  <MsgId>5</MsgId>  <MsgType>text</MsgType>  <Content>你好</Content></xml>";
		String result = api.processMsg(MMT_TOKEN, txtMsg);
		TextMessage msg = acx.getBean(TextMsgParser.class).toMsg(result);
		assertEquals(msg.getContent(), "Lemon Text message replay.");
	}
	@Test
	public void subscribeTest(){
		String recvMsg = "<xml>  <ToUserName>11b09b69e7e169ed</ToUserName>  <FromUserName>eddc9f8ab0c0afc9</FromUserName>  <CreateTime>1379085091</CreateTime>  <MsgId>21</MsgId>  <MsgType>event</MsgType>  <Event>subscribe</Event>  <EventKey></EventKey></xml>";
		String result = api.processMsg(MMT_TOKEN, recvMsg);
		TextMessage msg = acx.getBean(TextMsgParser.class).toMsg(result);
		assertEquals(msg.getContent(), Subscribe_msg);
	}
	@Test
	public void unsubscribe(){
		String recvMsg = "<xml>  <ToUserName>11b09b69e7e169ed</ToUserName>  <FromUserName>eddc9f8ab0c0afc9</FromUserName>  <CreateTime>1379083706</CreateTime>  <MsgId>12</MsgId>  <MsgType>event</MsgType>  <Event>unsubscribe</Event>  <EventKey></EventKey></xml>";
		String result = api.processMsg(MMT_TOKEN, recvMsg);
		assertEquals(result, null);
	}
	@Test
	public void linkMsgTest(){
		String recvMsg = "<xml><ToUserName><![CDATA[weixin]]></ToUserName><FromUserName><![CDATA[lemon]]></FromUserName><CreateTime>1377753855909</CreateTime><MsgType><![CDATA[link]]></MsgType><MsgId>1024102410241024</MsgId><Title><![CDATA[Link \"TEST\" Title]]></Title><Description><![CDATA[Link DESC]]></Description><Url><![CDATA[http://www.163.com/s/a/d/f/a]]></Url></xml>";
		String result = api.processMsg(MMT_TOKEN, recvMsg);
		TextMessage msg = acx.getBean(TextMsgParser.class).toMsg(result);
		assertEquals(msg.getContent(), "Lemon Link message replay.");
	}
	@Test
	public void imageMsgTest(){
		String recvMsg = "<xml>  <ToUserName>11b09b69e7e169ed</ToUserName>  <FromUserName>eddc9f8ab0c0afc9</FromUserName>  <CreateTime>1379129715</CreateTime>  <MsgId>17</MsgId>  <MsgType>image</MsgType>  <PicUrl>http://nos.netease.com/yixinpublic/pr_FzXvFRY8nrarFbQ9AphGAQ==_1379129714_6200108</PicUrl></xml>";
		String result = api.processMsg(MMT_TOKEN, recvMsg);
		TextMessage msg = acx.getBean(TextMsgParser.class).toMsg(result);
		assertEquals(msg.getContent(), "Lemon Image message replay.");
	}
	@Test
	public void locationMsgTest(){
		String recvMsg = "<xml>  <ToUserName>11b09b69e7e169ed</ToUserName>  <FromUserName>eddc9f8ab0c0afc9</FromUserName>  <CreateTime>1379129814</CreateTime>  <MsgId>18</MsgId>  <MsgType>location</MsgType>  <Location_X>30.302664</Location_X>  <Location_Y>120.159327</Location_Y>  <Scale>15</Scale></xml>";
		String result = api.processMsg(MMT_TOKEN, recvMsg);
		TextMessage msg = acx.getBean(TextMsgParser.class).toMsg(result);
		assertEquals(msg.getContent(), "Lemon Location message replay.");
	}
	
	@Test
	public void videoMsgTest(){
		String recvMsg = "<xml>  <ToUserName>11b09b69e7e169ed</ToUserName>  <FromUserName>eddc9f8ab0c0afc9</FromUserName>  <CreateTime>1379129699</CreateTime>  <MsgId>15</MsgId>  <MsgType>video</MsgType>  <url>http://nos.netease.com/yixinpublic/pr_opNFMEeTepg0k2n3FbasyA==_1379129698_6206864</url>  <name>f4e5ce4254d188a590e31bbd0cb77fd5.mp4</name>  <mimeType>video/mp4</mimeType></xml>";
		String result = api.processMsg(MMT_TOKEN, recvMsg);
		VideoMessage msg = acx.getBean(VideoMsgParser.class).toMsg(result);
		assertEquals(msg.getUrl(), "http://nos.netease.com/yixinpublic/pr_opNFMEeTepg0k2n3FbasyA==_1379129698_6206864");
		assertEquals(msg.getName(), "f4e5ce4254d188a590e31bbd0cb77fd5.mp4");
		assertEquals(msg.getMimeType(), "video/mp4");
	}
	@Test
	public void audioMsgTest(){
		String recvMsg = "<xml>  <ToUserName>11b09b69e7e169ed</ToUserName>  <FromUserName>eddc9f8ab0c0afc9</FromUserName>  <CreateTime>1379086098</CreateTime>  <MsgId>29</MsgId>  <MsgType>audio</MsgType>  <url>http://nos.netease.com/yixinpublic/pr_B7StF30nYDDT7VWrGzQxuw==_1379086096_6169298</url>  <name>600c4c87-146c-4d62-acb1-30d3f9ee3532.aac</name>  <mimeType>audio/aac</mimeType></xml>";
		String result = api.processMsg(MMT_TOKEN, recvMsg);
		AudioMessage msg = acx.getBean(AudioMsgParser.class).toMsg(result);
		assertEquals(msg.getUrl(), "http://nos.netease.com/yixinpublic/pr_B7StF30nYDDT7VWrGzQxuw==_1379086096_6169298");
		assertEquals(msg.getName(), "600c4c87-146c-4d62-acb1-30d3f9ee3532.aac");
		assertEquals(msg.getMimeType(), "audio/aac");
	}
	
	@Test
	public void musicMsgTest(){
		String recvMsg = "<xml>  <ToUserName>11b09b69e7e169ed</ToUserName>  <FromUserName>eddc9f8ab0c0afc9</FromUserName>  <CreateTime>1379129915</CreateTime>  <MsgId>20</MsgId>  <MsgType>music</MsgType>  <url>http://p3.music.126.net/h5PuQCIHeLkrqK0x33xAHg==/76965813957105.jpg</url>  <name>36220</name>  <mimeType>audio/mpeg</mimeType>  <desc>{&quot;id&quot;:365865,&quot;artists&quot;:&quot;My Little Airport&quot;,&quot;picUrl&quot;:&quot;http://p3.music.126.net/h5PuQCIHeLkrqK0x33xAHg==/76965813957105.jpg&quot;,&quot;albumId&quot;:36220,&quot;audio&quot;:&quot;http://m1.music.126.net/8xbwk09vVjKZdfxVxDMv0A==/2094569650923372.mp3&quot;,&quot;album&quot;:&quot;在动物园散步才是正经事&quot;,&quot;name&quot;:&quot;王菲，关于你的眉&quot;}</desc></xml>";
		String result = api.processMsg(MMT_TOKEN, recvMsg);
		MusicMessage msg = acx.getBean(MusicMsgParser.class).toMsg(result);
		assertEquals(msg.getUrl(), "http://p3.music.126.net/h5PuQCIHeLkrqK0x33xAHg==/76965813957105.jpg");
		assertEquals(msg.getName(), "36220");
		assertEquals(msg.getMimeType(), "audio/mpeg");
	}
	
}
