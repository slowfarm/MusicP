package eva.android.com.musicp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener{

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private FragmentAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private DataBase mDatabase;
    private SQLiteDatabase mSqLiteDatabase;
    private ArrayList<String> label = new ArrayList<>();
    private ArrayList<String> author = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        try { // проверка на наличие списка в БД
            if(getDatabase() == null) {
                String text = new ParseTask().execute().get();
                setDatabase(text);
                parser(getDatabase());
            }
            parser(getDatabase());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        setAdapter(this);
    }

    @Override
    public void onRefresh() {
        try { // Проверка наличия изменений и обновление списка
            String text = new ParseTask().execute().get();
            if(!getDatabase().equals(text) && text.length() != 0) {
                updateDatabase(text);
                parser(getDatabase());
                Toast toast = Toast.makeText(this,
                        "Список обновлен", Toast.LENGTH_SHORT);
                toast.show();
                setAdapter(this);
            }
            else {
                Toast toast = Toast.makeText(this,
                        "Список без изменения", Toast.LENGTH_SHORT);
                toast.show();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }
    // Получение JSON строки
    private class ParseTask extends AsyncTask<Void, Void, String> {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        @Override
        protected String doInBackground(Void... params) {
            try {
                URL url = new URL("http://tomcat.kilograpp.com/songs/api/songs");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                resultJson = buffer.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultJson;
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);
        }
    }
    // Добавление JSON строки в БД
    public void setDatabase(String strJson) {
        mDatabase = new DataBase(this, "database.db", null, 1);
        mSqLiteDatabase = mDatabase.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DataBase.MUSIC_COLUMN, strJson);
        mSqLiteDatabase.insert("music", null, values);
    }
    // Получение JSON строки из БД
    public String getDatabase() {
        String data = null;
        mDatabase = new DataBase(this, "database.db", null, 1);
        mSqLiteDatabase = mDatabase.getWritableDatabase();
        Cursor cursor = mSqLiteDatabase.query("music", new String[]{DataBase.MUSIC_COLUMN}, null, null, null, null, null);
        cursor.moveToFirst();
        if (cursor!=null && cursor.getCount() > 0)
            data = cursor.getString(cursor.getColumnIndex(DataBase.MUSIC_COLUMN));
        cursor.close();
        mSqLiteDatabase.close();
        return data;
    }
    // Обновление БД при изменении
    public void updateDatabase(String data) {
        mDatabase = new DataBase(this, "database.db", null, 1);
        mSqLiteDatabase = mDatabase.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DataBase.MUSIC_COLUMN, data);
        mSqLiteDatabase.update("music", values, null, null);
    }
    // JSON парсер
    public void parser(String strJson) {
        label.clear();
        author.clear();
        try {
            JSONArray music = new JSONArray(strJson);
            for (int i = 0; i < music.length(); i++) {
                JSONObject song = music.getJSONObject(i);
                label.add(song.getString("label"));
                author.add(song.getString("author"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    // Установка адаптера
    public void setAdapter(Context ctx) {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(ctx);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FragmentAdapter(label,author);
        mRecyclerView.setAdapter(mAdapter);
    }
}