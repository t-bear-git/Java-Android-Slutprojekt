package com.example.slutprojekt_textrecognition2;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.nio.ByteBuffer;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    LinearLayout cameraLayout, detectLayout, launchTTS;
    ImageView imageView;
    TextView textView;
    ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get views for buttons.
        detectLayout = findViewById(R.id.detect);
        cameraLayout = findViewById(R.id.camera);
        launchTTS = findViewById(R.id.textToSpeech);

        // get imageview & textview
        imageView = findViewById(R.id.imageId);
        textView = findViewById(R.id.textId);

        // setting onClickListeners to buttons used.
        detectLayout.setOnClickListener(this);
        cameraLayout.setOnClickListener(this);
        launchTTS.setOnClickListener(this);


        // Initiate ActivityResultLauncher and handle the ActivityResult
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                // if image is successful pass it to the bundle then set the image in imageView.
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle bundle = result.getData().getExtras();
                    Bitmap bitmap = (Bitmap) bundle.get("data");
                    imageView.setImageBitmap(bitmap);
                }
            }
        });



    }



    // Method for handling the different button clicks.
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.detect:
                try {
                    runTextRecognition();
                }catch (Exception e){
                    Toast.makeText(MainActivity.this, "No image has been selected", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.camera:
                askCameraPermissions();
                break;
            case R.id.textToSpeech:
                switchActivity();
                break;
        }
    }


    // Method for starting and sending detected text to the TextToSpeech Activity.
    public void switchActivity() {

        String detectedText = textView.getText().toString();
        Intent intent = new Intent(this, TextToSpeechActivity.class);
        intent.putExtra("TEXT_TO_SEND", detectedText);

        startActivity(intent);
    }

    // Method for launching camera.
    private void dispatchTakePictureIntent(){

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        activityResultLauncher.launch(intent);
    }

    // Method for checking Camera permission before launching it.
    static final int CAMERA_PERM = 2;
    private void askCameraPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA
            },CAMERA_PERM);
        }
        else {
            dispatchTakePictureIntent();
        }
    }

    // Method for running Text Recognition.
    private void runTextRecognition(){
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        int rotationDegree = 0;

        InputImage image = InputImage.fromBitmap(bitmap, rotationDegree);

        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                processTextRecognition(visionText);
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, "Recognition failed.",Toast.LENGTH_SHORT).show();
                                    }
                                });
    }

    // Method for handling recognized text in image and saving it to a String.
    private void processTextRecognition(Text visionText) {
        List<Text.TextBlock> blocks = visionText.getTextBlocks();
        if (blocks.size() == 0){
            Toast.makeText(MainActivity.this, "No text has been found in image.",Toast.LENGTH_LONG).show();
        }

        StringBuilder text = new StringBuilder();

        for (int i = 0; i<blocks.size();i++){
            List<Text.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j<lines.size();j++){
                List<Text.Element> elements = lines.get(j).getElements();
                for (int k = 0; k<elements.size();k++){
                    text.append(elements.get(k).getText() + " ");
                }
            }
        }
        textView.setText(text);
    }
}