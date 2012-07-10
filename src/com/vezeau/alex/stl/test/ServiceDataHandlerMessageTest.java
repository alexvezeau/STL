package com.vezeau.alex.stl.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.Assert;

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.vezeau.alex.stl.DataMessage;
import com.vezeau.alex.stl.ServiceDataHandlerMessage;

public class ServiceDataHandlerMessageTest {

	@Test
	public void testRequestMessage() throws ParserConfigurationException, SAXException,
			IOException {
		DefaultHandler serviceDataHandler = new ServiceDataHandlerMessage();
		List<DataMessage> dataList = null;

		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();

		XMLReader xr = sp.getXMLReader();

		xr.setContentHandler(serviceDataHandler);

		InputStream resourceAsStream = getClass().getClassLoader()
				.getResourceAsStream(
						"com/vezeau/alex/stl/messageserviceresponsemock.xml");

		xr.parse(new InputSource(resourceAsStream));

		ServiceDataHandlerMessage messageDataHandler = (ServiceDataHandlerMessage) serviceDataHandler;
		dataList = messageDataHandler.getMessageList();
		
		Assert.assertTrue("Message list is empty", dataList.size() > 0);

	}
	
}
