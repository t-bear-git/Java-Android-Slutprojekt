package com.example.slutprojekt_textrecognition2;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class TextToSpeechActivity extends AppCompatActivity implements View.OnClickListener {

    // Assigning global variables.
    private ImageView readBtn, goBackBtn;
    private EditText editRead;
    TextToSpeech toSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_to_speech);



        // Getting different element from view.
        editRead = findViewById(R.id.editRead);
        readBtn = findViewById(R.id.readText);
        goBackBtn = findViewById(R.id.goBack);

        // Setting onclicklisteners to elements used as buttons.
        readBtn.setOnClickListener(this);
        goBackBtn.setOnClickListener(this);

        // Getting string from Intent and setting it to EditText field.
        String textToRead =  getIntent().getStringExtra("TEXT_TO_SEND");
        if (getIntent() != null) {
            editRead.setText(textToRead);
        }

        // Initiating and setting up TextToSpeech engine.
        toSpeech = new TextToSpeech(this, status -> {
            try {
                toSpeech.setLanguage(new Locale("en", "US"));
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("speak", "onInit: " + TextToSpeech.ERROR);
            }
        });

    }

    // Method for handling button clicks.
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.readText:
                try {
                    String toRead = editRead.getText().toString();
                    toSpeech.speak(toRead, TextToSpeech.QUEUE_FLUSH, null);
                }catch (Exception e){
                    Toast.makeText(TextToSpeechActivity.this, "Text to speech failed. Please try again", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.goBack:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
        }

    }

    // Releasing resources used by TextToSpeech engine when onStop() is reached.
    @Override
    protected void onStop() {
        super.onStop();
        toSpeech.stop();
        toSpeech.shutdown();
    }




}