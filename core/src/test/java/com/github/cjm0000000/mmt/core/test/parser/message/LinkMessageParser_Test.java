package com.github.cjm0000000.mmt.core.test.parser.message;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import com.github.cjm0000000.mmt.core.message.Message;
import com.github.cjm0000000.mmt.core.message.recv.LinkMessage;

/**
 * Unit test cases for link message
 * @author lemon
 * @version 2.0
 *
 */
public class LinkMessageParser_Test extends AbstractMsgParser {

	@Override
	protected void makeSpecNodesWithoutMsgId(StringBuilder sb, Message original) {
		LinkMessage msg = (LinkMessage) original;
		sb.append("<Title><![CDATA[" + msg.getTitle() + "]]></Title>");
		sb.append("<Description><![CDATA[" + msg.getDescription() + "]]></Description>");
		sb.append("<Url><![CDATA[" + msg.getUrl() + "]]></Url>");
	}

	@Override
	protected void verifySpecFieldsWithoutMsgId(Message after, Message before) {
		LinkMessage l_before = (LinkMessage) before;
		LinkMessage l_after = (LinkMessage) after;
		assertEquals(l_after.getTitle(), l_before.getTitle());
		assertEquals(l_after.getDescription(), l_before.getDescription());
		assertEquals(l_after.getUrl(), l_before.getUrl());
	}

	@Override
	protected Message getMsgInstanceWithoutMsgId() {
		LinkMessage original = new LinkMessage();
		original.setTitle(UUID.randomUUID().toString());
		original.setDescription("中文" + UUID.randomUUID().toString());
		original.setUrl("http://www.baidu.com/asd/asd/a.html");
		return original;
	}

}