package com.vkstech.guessinggame;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebUrls = new ArrayList<>();
    ArrayList<String> celebNames = new ArrayList<>();

    ImageView imageView;
    Button button0, button1, button2, button3;

    int chosenCeleb = 0, locationOfCorrectAnswer = 0;

    String[] answers = new String[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        button0 = findViewById(R.id.button0);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);

        DownloadTask downloadTask = new DownloadTask();
        String result;

        try {
            result = downloadTask.execute("http://www.posh24.se/kandisar").get();

            String[] splitResult = result.split("<div class=\"listedArticles\">");

            Pattern p = Pattern.compile("img src=\"(.*?)\"");
            Matcher m = p.matcher(splitResult[0]);

            while (m.find()) {
                celebUrls.add(m.group(1));
            }

            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(splitResult[0]);

            while (m.find()) {
                celebNames.add(m.group(1));
            }

            newQuestion();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);

                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result = result += current;
                    data = reader.read();
                }

                return result;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public class ImageDownloadTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {

                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream in = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                return bitmap;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public void newQuestion() {
        try {
            Random random = new Random();

            chosenCeleb = random.nextInt(celebUrls.size());

            ImageDownloadTask imageDownloadTask = new ImageDownloadTask();

            Bitmap celebImage = imageDownloadTask.execute(celebUrls.get(chosenCeleb)).get();

            if (celebImage == null)
                newQuestion();


            imageView.setImageBitmap(celebImage);

            locationOfCorrectAnswer = random.nextInt(4);

            int incorrectAnswerLocation;

            for (int i = 0; i < 4; i++) {
                if (i == locationOfCorrectAnswer) {
                    answers[i] = celebNames.get(chosenCeleb);
                } else {
                    incorrectAnswerLocation = random.nextInt(celebUrls.size());

                    while (incorrectAnswerLocation == chosenCeleb) {
                        incorrectAnswerLocation = random.nextInt(celebUrls.size());

                    }

                    answers[i] = celebNames.get(incorrectAnswerLocation);
                }
            }

            button0.setText(answers[0]);
            button1.setText(answers[1]);
            button2.setText(answers[2]);
            button3.setText(answers[3]);
        } catch (Exception e) {
            newQuestion();
        }
    }

    public void celebChosen(View view) {
        if (view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))) {
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Wrong! It was " + celebNames.get(chosenCeleb), Toast.LENGTH_SHORT).show();
        }
        newQuestion();
    }
}
