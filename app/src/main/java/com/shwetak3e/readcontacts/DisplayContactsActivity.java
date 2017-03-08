package com.shwetak3e.readcontacts;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shwetak3e.readcontacts.adapter.ContactsAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DisplayContactsActivity extends AppCompatActivity {


    ListView contactList;
    Cursor cursor;
    ContentResolver contentResolver;
    Map<String, String> contactDetails = new HashMap<>();
    Map<String, Integer> ranks = new HashMap<>();
    ProgressDialog pDialog;
    boolean loadMore=false;
    int threshold=20;
    static int counter=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_contacts);
        contactList = (ListView) findViewById(R.id.contactList);

        contactList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastItem=firstVisibleItem+visibleItemCount;
                if((lastItem==totalItemCount )&& !(loadMore))
                {
                   loadMoreItems();

                }

            }
        });

        contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String phone=new ArrayList<String>(contactDetails.keySet()).get(position);
                if(ranks.containsKey(phone)){
                    int value=ranks.get(phone);
                    ranks.remove(phone);
                    ranks.put(phone,value+1);
                }
                contactDetails=sortbyRank(ranks);
                contactList.setAdapter(new ContactsAdapter(DisplayContactsActivity.this,contactDetails));
                openFile(ranks);
            }
        });


    }

    private void loadMoreItems() {

        loadMore=true;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                pDialog = new ProgressDialog(DisplayContactsActivity.this);
                pDialog.setCancelable(false);
                pDialog.setMessage("Loading.. Please Wait");
                showDialog();

            }

            @Override
            protected Void doInBackground(Void... params) {
                getContacts(threshold,counter);
                counter+=threshold;
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
              contactList.setAdapter( new ContactsAdapter(DisplayContactsActivity.this, contactDetails));
              loadMore=false;
              hideDialog();
            }
        }.execute();

    }


    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    void getContacts(int limit, int offset) {
        ranks=readFromJson();
        contentResolver = getContentResolver();
        String Limit=" LIMIT"+" "+limit;
        String Offset=" OFFSET"+" "+offset;
        cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null + " ASC"+Limit+Offset);

            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phonenumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                if(!contactDetails.containsKey(phonenumber)) {
                    contactDetails.put(phonenumber, name);
                    if (!ranks.containsKey(phonenumber)) {
                        ranks.put(phonenumber, 0);
                    }
                }
            }
            contactDetails = sortbyRank(ranks);
            cursor.close();
            openFile(ranks);
        }



    private void openFile(Map<String, Integer> map) {
        String FILENAME = getExternalFilesDir(null).getAbsolutePath()+"/rank.json";
        File file=new File(FILENAME);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            objectMapper.writeValue(file, map);

        } catch (IOException e) {
            e.printStackTrace();

        }

    }

    private Map<String, Integer> readFromJson() {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Integer> rankMap = new HashMap<>();
        String FILENAME = getExternalFilesDir(null).getAbsolutePath()+"/rank.json";
        File file=new File(FILENAME);
            try {
                rankMap = mapper.readValue(file, new TypeReference<Map<String, Integer>>() {
                });
            } catch (IOException e) {
                e.printStackTrace();

            }
        for(Map.Entry<String,Integer> entry: rankMap.entrySet())
        {
            Log.i("TAG6",entry.getKey()+":"+entry.getValue() );
        }
        return rankMap;
    }


    private LinkedHashMap<String, String> sortbyRank(Map<String, Integer> map) {
        List<String> mapKeys = new ArrayList<>(map.keySet());
        List<Integer> mapValues = new ArrayList<>(map.values());
        Collections.sort(mapKeys);
        Collections.reverse(mapKeys);
        Collections.sort(mapValues);
        Collections.reverse(mapValues);

        LinkedHashMap<String, Integer> sortedMap =
                new LinkedHashMap<>();
        LinkedHashMap<String, String> finalMap =
                new LinkedHashMap<>();

        Iterator<Integer> valueIt = mapValues.iterator();
        Integer val=0;
        while (valueIt.hasNext()) {
            val = valueIt.next();
            Iterator<String> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                String key = keyIt.next();
                Integer val1 = map.get(key);
                Integer val2 = val;

                if (val1.equals(val2)) {
                    keyIt.remove();
                    sortedMap.put(key,val);
                    break;
                }
            }
        }
        ranks=sortedMap;
        finalMap=sortGroupsbyKeys(ranks,contactDetails);
        return finalMap;
    }


    private LinkedHashMap<String, String> sortGroupsbyKeys(Map<String, Integer> rank,Map<String,String> contactDet ) {
        List<String> mapKeys = new ArrayList<>(rank.keySet());
        LinkedHashMap<String, String> sortedMap =
                new LinkedHashMap<>();

        Iterator<String> keyIt=mapKeys.iterator();
        String val="";
        while(keyIt.hasNext()) {
            String key = keyIt.next();
            if (contactDet.containsKey(key)){
                val = contactDet.get(key);
                keyIt.remove();
                sortedMap.put(key, val);
            }
        }
        return  sortedMap;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}





