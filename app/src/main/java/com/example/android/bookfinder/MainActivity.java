package com.example.android.bookfinder;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // The base part of the url we will use to access google books api
    private static final String GOOGLE_BOOKS_URL = "https://www.googleapis.com/books/v1/volumes?q=";
    // The part of the url that dictates the maximum number of results we will get
    private static final String MAX_BOOKS_STRING = "&maxResults=20";
    // InputStream to store the data we get from the google books api
    private InputStream is;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView search = (ImageView) findViewById(R.id.search_image_view);
        search.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // Check that a network connection is available. If not, inform the user.
                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    ListView listView = (ListView) findViewById(R.id.list);
                    listView.setAdapter(null);
                    // get keywords and format them for use with the api
                    EditText searchField = (EditText) findViewById(R.id.search_box);
                    String keywords = searchField.getText().toString().replace(' ', '+');
                    // handle error caused by the user failing to enter input
                    if (keywords.length() == 0) {
                        Toast.makeText(MainActivity.this, R.string.input_error, Toast.LENGTH_SHORT).show();
                    } else {
                        new FetchBookTask().execute(getSearchQuery(keywords));
                    }
                } else {
                    Toast.makeText(MainActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class FetchBookTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return fetchBooks(urls[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            ArrayList<Book> books = buildBookList(result);
            // Grab the no_results_found textview so we can change it between gone and visible based
            // on whether we found any books or not
            TextView no_results = (TextView) findViewById(R.id.no_results_text_view);
            if (books != null) {
                no_results.setVisibility(TextView.GONE);
                BookAdapter adapter = new BookAdapter(MainActivity.this, books);
                ListView listView = (ListView) findViewById(R.id.list);
                listView.setAdapter(adapter);
            } else {
                no_results.setVisibility(TextView.VISIBLE);
            }
        }
    }

    /**
     * Returns the full url used to query the google books api with the keywords that the user entered.
     */
    private String getSearchQuery(String keywords) {
        return GOOGLE_BOOKS_URL + keywords + MAX_BOOKS_STRING;
    }

    /**
     * Download and fetch the book JSONObjects as a string from the Google books API.
     * Returns null if the method fails to produce a string to use.
     */
    private String fetchBooks(String query) {
        try {
            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // configure connection
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // start the query
            conn.connect();

            // check that we got a response code of 200 from the google books api
            if (conn.getResponseCode() == 200) {
                is = conn.getInputStream();
                return readResponse(is);
            }
        } catch (IOException e) {
            Log.e("fetchBooks", e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e("fetchBooks", e.getMessage());
                }
            }
        }
        return null;
    }

    /**
     * Convert the Input Stream returned by the FetchBookTask into a string.
     */
    private String readResponse(InputStream is) {
        StringBuilder builder = new StringBuilder();
        BufferedReader responseReader = new BufferedReader(new InputStreamReader(is));
        // Read the inputstream one line at a time, storing the full string in a bufferedreader
        try {
            String line = responseReader.readLine();
            while (line != null) {
                builder.append(line);
                line = responseReader.readLine();
            }
        } catch (IOException e) {
            Log.e("readResponse", e.getMessage());
        }
        return builder.toString();
    }

    /**
     * Parse the string to retrieve the JSONObjects it represents, and return an ArrayList
     * of Books constructed from the JSONObjects.
     */
    private ArrayList<Book> buildBookList(String data) {
        ArrayList<Book> books = new ArrayList<Book>();
        String title = "";
        String author = "";
        try {
            JSONObject jsonObject = new JSONObject(data);
            JSONArray jArray = jsonObject.getJSONArray("items");
            // Iterate through our JSONArray structure, working with on volumeInfo at a time
            // Each volumeInfo represents one book
            for (int i = 0; i < jArray.length(); ++i) {
                JSONObject volumeInfo = jArray.getJSONObject(i).getJSONObject("volumeInfo");
                // Grab the title
                title = volumeInfo.getString("title");
                // Grab the authors and concatenate their names into one string, if any exist
                if (volumeInfo.has("authors")) {
                    JSONArray authors = volumeInfo.getJSONArray("authors");
                    for (int j = 0; j < authors.length(); ++j) {
                        author += authors.getString(j);
                        // add a comma if we're not on the last author just to make things pretty
                        if (j < authors.length() - 1) {
                            author += ", ";
                        }
                    }
                }
                // Some results don't have authors, this helps us add some kind of indication of this
                if (author.length() == 0) {
                    author = "Author name unavailable";
                }
                books.add(new Book(author, title));
                // set both title and author back to the empty string in order to start each iteration fresh
                title = "";
                author = "";
            }
            return books;
        } catch (JSONException e) {
            Log.e("buildBookList", e.getMessage());
            return null;
        }
    }
}
