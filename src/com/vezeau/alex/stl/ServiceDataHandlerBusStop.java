package com.vezeau.alex.stl;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ServiceDataHandlerBusStop extends DefaultHandler {

	// booleans that check whether it's in a specific tag or not
	private boolean _inStop;
	private boolean _inBusPoint;

	// this holds the data
	private List<DataBusStop> _dataList;
	private DataBusStop _data;

	private List<DataBusPoint> _busPointList;
	private DataBusPoint _busPoint;

	private String maxLat;
	private String maxLon;
	private String minLat;
	private String minLon;

	private int pathNumber = 1;

	/**
	 * Returns the data object
	 * 
	 * @return
	 */
	public List<DataBusStop> getDataList() {
		return _dataList;
	}

	public List<DataBusPoint> getBusPointList() {
		return _busPointList;
	}

	/**
	 * This gets called when the xml document is first opened
	 * 
	 * @throws SAXException
	 */
	@Override
	public void startDocument() throws SAXException {
		_data = new DataBusStop();
		_dataList = new ArrayList<DataBusStop>();

		_busPoint = new DataBusPoint();
		_busPointList = new ArrayList<DataBusPoint>();
	}

	/**
	 * Called when it's finished handling the document
	 * 
	 * @throws SAXException
	 */
	@Override
	public void endDocument() throws SAXException {

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

		if (localName.equals("stop")) {
			_inStop = true;
			_data = new DataBusStop();

			_data.tag = atts.getValue("tag");
			_data.title = atts.getValue("title");
			_data.latitude = atts.getValue("lat");
			_data.longitude = atts.getValue("lon");
			_data.stopId = atts.getValue("stopId");
		} else if (localName.equals("point")) {
			_inBusPoint = true;
			_busPoint = new DataBusPoint();
			_busPoint.latitude = atts.getValue("lat");
			_busPoint.longitude = atts.getValue("lon");
			_busPoint.pathNumber = pathNumber;
		} else if (localName.equals("route")) {
			minLat = atts.getValue("latMin");
			maxLat = atts.getValue("latMax");
			minLon = atts.getValue("lonMin");
			maxLon = atts.getValue("lonMax");
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

		if (localName.equals("stop")) {
			_inStop = false;
			if(_data.title != null){
				_dataList.add(_data);
			}
		} else if (localName.equals("point")) {
			_inBusPoint = false;
			_busPointList.add(_busPoint);
		} else if (localName.equals("path")) {
			pathNumber++;
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

		if (_inStop) {
			_data.stop = chars;
		} else if (_inBusPoint) {
			_busPoint.point = chars;
		}
	}

	public String getMaxLat() {
		return maxLat;
	}

	public String getMaxLon() {
		return maxLon;
	}

	public String getMinLat() {
		return minLat;
	}

	public String getMinLon() {
		return minLon;
	}

}
