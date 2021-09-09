package com.example.smartapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;

import static android.Manifest.permission.CAMERA;

public class  ScannerActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private TextView textview;
    private Button snapBtn;
    private Button copyBtn;
    private Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        textview = (TextView) findViewById(R.id.tv);
        snapBtn = (Button) findViewById(R.id.snapimage);
        copyBtn = (Button) findViewById(R.id.copyimage);
      copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String scannedtext= textview.getText().toString();
                copytoClipboard(scannedtext);

            }
        });
        snapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    dispatchTakePictureIntent();
                } else {
                    requestPermission();
                }
            }
        });
    }

    private void copytoClipboard(String scannedtext) {
        ClipboardManager clipboard= (ClipboardManager)  getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip=ClipData.newPlainText("copied data", scannedtext);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this,"Copied to Clipboard....",Toast.LENGTH_SHORT).show();


    }

    private boolean checkPermission() {
        int camera_permission = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        return camera_permission == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            boolean cameraaccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (cameraaccepted) {
                Toast.makeText(this, "Permission granted..", Toast.LENGTH_SHORT).show();
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Permission Denined \n You cannot use app without providing permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void dispatchTakePictureIntent() {
        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(this);

    }

    private void requestPermission() {
        int PERMISSION_REQUEST_CODE = 200;
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, PERMISSION_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri resultUri = result.getUri();
                    try {
                        imageBitmap=MediaStore.Images.Media.getBitmap(this.getContentResolver(),resultUri);
                        Log.e("TAG", "after try block ");
                        detectTxt(imageBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
            }
        }


    private void detectTxt(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        Log.e("TAG", "after image in detecttext()");
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                StringBuilder result = new StringBuilder();
                                for (Text.TextBlock block : visionText.getTextBlocks()) {
                                    String blockText = block.getText();
                                    Point[] blockCornerPoints = block.getCornerPoints();
                                    Rect blockFrame = block.getBoundingBox();
                                    for (Text.Line line : block.getLines()) {
                                        String lineText = line.getText();
                                        Point[] lineCornerPoints = line.getCornerPoints();
                                        Rect lineFrame = line.getBoundingBox();
                                        for (Text.Element element : line.getElements()) {
                                            String elementText = element.getText();
                                            Point[] elementCornerPoints = element.getCornerPoints();
                                            Rect elementFrame = element.getBoundingBox();
                                            result.append(elementText);
                                        }
                                        textview.setText(blockText);
                                        snapBtn.setText("RETAKE");
                                        copyBtn.setVisibility(View.VISIBLE);

                                    }
                                }
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(ScannerActivity.this, "Fail to extract data..", Toast.LENGTH_SHORT).show();
                                        Log.e("TAG", "FAIL TO GET DATA = " + e.getMessage().toString());
                                    }
                                });
    }
}