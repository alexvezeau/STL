package com.vezeau.alex.stl;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ServiceDataHandlerVehicle extends DefaultHandler {

	// booleans that check whether it's in a specific tag or not
	private boolean _inVehicle;

	// this holds the data
	private List<DataVehicle> _vehicleList;
	private DataVehicle _vehicle;

	/**
	 * Returns the data object
	 * 
	 * @return
	 */
	public List<DataVehicle> getVehicleList() {
		return _vehicleList;
	}

	/**
	 * This gets called when the xml document is first opened
	 * 
	 * @throws SAXException
	 */
	@Override
	public void startDocument() throws SAXException {
		_vehicle = new DataVehicle();
		_vehicleList = new ArrayList<DataVehicle>();
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

		if (localName.equals("vehicle")) {
			_inVehicle = true;
			_vehicle = new DataVehicle();

			_vehicle.heading = atts.getValue("heading");
			_vehicle.latitude = atts.getValue("lat");
			_vehicle.longitude = atts.getValue("lon");
			_vehicle.vehicleId = atts.getValue("id");
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

		if (localName.equals("vehicle")) {
			_inVehicle = false;
			_vehicleList.add(_vehicle);
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

		if (_inVehicle) {
			_vehicle.vehicle = chars;
		}
	}
}
