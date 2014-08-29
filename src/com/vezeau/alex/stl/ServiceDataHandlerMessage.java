package com.vezeau.alex.stl;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;


public class ServiceDataHandlerMessage extends DefaultHandler {

	private static final String tag = "Message Parser";
	// booleans that check whether it's in a specific tag or not
	private boolean _inText;
	private boolean _inTextEN;

	// this holds the data
	private List<DataMessage> _messageList;
	private DataMessage _message;

	/**
	 * Returns the data object
	 * 
	 * @return
	 */
	public List<DataMessage> getMessageList() {
		return _messageList;
	}

	/**
	 * This gets called when the xml document is first opened
	 * 
	 * @throws SAXException
	 */
	@Override
	public void startDocument() throws SAXException {
		Log.d(tag, "[startDocument()]--- START DOC");
		_message = new DataMessage();
		_messageList = new ArrayList<DataMessage>();
	}

	/**
	 * Called when it's finished handling the document
	 * 
	 * @throws SAXException
	 */
	@Override
	public void endDocument() throws SAXException {
		Log.d(tag, "[endDocument()]--- END DOC");
	}

	/**
	 * This gets called at the start of an element. Here we're also setting the
	 * booleans to true if it's at that specific tag. (so we know where we are)
	 * 
	 * @param namespaceURI
	 * @param localName
	 * @param qName
	 * @param atts
	 * @throws SAXException
	 */
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {

		if (localName.equals("route")) {
			_message = new DataMessage();
			_message.tag = atts.getValue("tag");
			
		}else if (localName.equals("text")) {
			_inText = true;
		}else if (localName.equals("textSecondaryLanguage")) {
			_inTextEN = true;
		}
	}

	/**
	 * Called at the end of the element. Setting the booleans to false, so we
	 * know that we've just left that tag.
	 * 
	 * @param namespaceURI
	 * @param localName
	 * @param qName
	 * @throws SAXException
	 */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {

		if (localName.equals("route")) {
			_messageList.add(_message);
		}else if (localName.equals("text")) {
			_inText = false;
		}else if (localName.equals("textSecondaryLanguage")) {
			_inTextEN = false;
		}
	}
	
	

	/**
	 * Calling when we're within an element. Here we're checking to see if there
	 * is any content in the tags that we're interested in and populating it in
	 * the Config object.
	 * 
	 * @param ch
	 * @param start
	 * @param length
	 */
	@Override
	public void characters(char ch[], int start, int length) {
		String chars = new String(ch, start, length);
		chars = chars.trim();

		if (ch != null && _inText){
			_message.text = _message.text + chars;
		}else if (ch != null && _inTextEN){
			_message.textEn = _message.textEn + chars;
		}
	}
}
