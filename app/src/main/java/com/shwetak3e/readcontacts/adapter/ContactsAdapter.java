package com.shwetak3e.readcontacts.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.shwetak3e.readcontacts.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Pervacio on 3/7/2017.
 */

public class ContactsAdapter extends ArrayAdapter {


    Context context;
    Map<String,String> contactList=new LinkedHashMap<>();
    List<String> phonenumbers=new ArrayList<>();
    LayoutInflater inflater;

    public ContactsAdapter(Context context,Map<String,String> contactList) {
        super(context,R.layout.contact_holder);
        this.context=context;
        this.contactList=contactList;
        phonenumbers=new ArrayList<>(contactList.keySet());
        inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        if(contactList!=null){
            return contactList.size();
        }else{
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return contactList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ContactsHolder holder=new ContactsHolder();
        if (convertView == null) {
            convertView= inflater.inflate(R.layout.contact_holder, null);
            holder.contactName = (TextView) convertView.findViewById(R.id.contactName);
            holder.contactPhone= (TextView) convertView.findViewById(R.id.contactNo);
            convertView.setTag(holder);
        } else {
            holder=(ContactsHolder) convertView.getTag();
        }
        holder.contactName.setText(contactList.get(phonenumbers.get(position)));
        holder.contactPhone.setText(phonenumbers.get(position));

        return convertView;
    }



    class ContactsHolder
    {
        TextView contactName;
        TextView contactPhone;

    }
}
