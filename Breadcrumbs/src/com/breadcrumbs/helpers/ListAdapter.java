package com.breadcrumbs.helpers;

import java.util.List;

import com.breadcrumbs.R;
import com.breadcrumbs.R.id;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class ListAdapter extends ArrayAdapter<AdapterItem> {

	protected static final String LOG_TAG = ListAdapter.class.getSimpleName();
	
	private List<AdapterItem> items;
	private int layoutResourceId;
	private Context context;

	public ListAdapter(Context context, int layoutResourceId, List<AdapterItem> items) {
		super(context, layoutResourceId, items);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.items = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		AdapterItemHolder holder = null;

		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		row = inflater.inflate(layoutResourceId, parent, false);

		holder = new AdapterItemHolder();
		holder.adapterItem = items.get(position);
		holder.removePaymentButton = (ImageButton)row.findViewById(R.id.delete);
		holder.removePaymentButton.setTag(holder.adapterItem);

		holder.name = (TextView)row.findViewById(android.R.id.text1);
		setNameTextChangeListener(holder);
//		holder.value = (TextView)row.findViewById(R.id.atomPay_value);
//		setValueTextListeners(holder);

		row.setTag(holder);

		setupItem(holder);
		return row;
	}

	private void setupItem(AdapterItemHolder holder) {
		holder.name.setText(holder.adapterItem.getName());
//		holder.value.setText(String.valueOf(holder.adapterItem.getValue()));
	}

	public static class AdapterItemHolder {
		AdapterItem adapterItem;
		TextView name;
//		TextView value;
		ImageButton removePaymentButton;
	}
	
	private void setNameTextChangeListener(final AdapterItemHolder holder) {
		holder.name.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				holder.adapterItem.setName(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

			@Override
			public void afterTextChanged(Editable s) { }
		});
	}

//	private void setValueTextListeners(final AdapterItemHolder holder) {
//		holder.value.addTextChangedListener(new TextWatcher() {
//
//			@Override
//			public void onTextChanged(CharSequence s, int start, int before, int count) {
//				try{
//					holder.adapterItem.setValue(Double.parseDouble(s.toString()));
//				}catch (NumberFormatException e) {
//					Log.e(LOG_TAG, "error reading double value: " + s.toString());
//				}
//			}
//
//			@Override
//			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
//
//			@Override
//			public void afterTextChanged(Editable s) { }
//		});
//	}
}
