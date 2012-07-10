package com.vezeau.alex.stl;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ArrayAdapterBusStop extends ArrayAdapter<String>{
	private final Context context;
	private final String[] values;

	public ArrayAdapterBusStop(Context context, String[] values) {
		super(context, R.layout.rowlayout, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.rowlayout, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.listLabel);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.listIcon);
		textView.setText(values[position]);
		// Change the icon for Windows and iPhone
		String s = values[position];
		
		if ((s != null) && (s.startsWith("Gare") || s.startsWith("Métro"))) {
			imageView.setImageResource(R.drawable.start);
		} else {
			imageView.setImageResource(R.drawable.stop);
		}

		return rowView;
	}
}
