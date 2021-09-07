package com.example.smartapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptions;

import java.util.BitSet;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.PACKAGE_USAGE_STATS;

public class ScannerActivity extends AppCompatActivity {
    private ImageView captureIV;
    private TextView resulttv;
    private Bitmap imageBitmap;
    private Button msnap;
    static final int REQUEST_IMAGE_CAPTURE=1;
    private Button mdetect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        captureIV=findViewById(R.id.captureIv);
        resulttv=findViewById(R.id.tv);
        msnap=findViewById(R.id.snapimage);
        mdetect=findViewById(R.id.detect);
        msnap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             if(checkpermission()){
                 captureImage();
             }
             else
                 requestpermission();
            }
        });
     mdetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            detectText();
            }


     });
    }
    private boolean checkpermission(){
        int cameraPermission= ContextCompat.checkSelfPermission(getApplicationContext(),CAMERA);
        return cameraPermission== PackageManager.PERMISSION_GRANTED;
    }
    private void requestpermission(){
        int PERMISSION_CODE=200;
        ActivityCompat.requestPermissions(this,new String[]{CAMERA},PERMISSION_CODE);

    }
private void captureImage(){
        Intent takepic =new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takepic.resolveActivity(getPackageManager())!=null){
            startActivityForResult(takepic,REQUEST_IMAGE_CAPTURE);
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0)
        {
            boolean cameraPermissions=grantResults[0]==PackageManager.PERMISSION_GRANTED;
            if(cameraPermissions)
            {
                Toast.makeText(this,"Permissions granted ...",Toast.LENGTH_SHORT).show();
                captureImage();
            }
            else {
                Toast.makeText(this,"Permissions denied...",Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_IMAGE_CAPTURE && resultCode==RESULT_OK)
        {
            Bundle extras= data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            captureIV.setImageBitmap(imageBitmap);
        }
    }

    private void detectText() {
     InputImage image=InputImage.fromBitmap(imageBitmap,0);

     TextRecognizer recognizer= TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
     Task<Text> result=recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
         @Override
         public void onSuccess(@NonNull  Text text) {
StringBuilder result=new StringBuilder();
for(Text.TextBlock block: text.getTextBlocks()){
    String blockText=block.getText();
    Point[] blockcornerpoints=block.getCornerPoints();
    Rect blockFrame=block.getBoundingBox();
    for(Text.Line line:block.getLines()){
        String linetext= line.getText();
        Point[] linecornerpoints=line.getCornerPoints();
        Rect lineFrame=line.getBoundingBox();
        for(Text.Element element:line.getElements()){
            String elementtext=element.getText();
            result.append(elementtext);
        }
        resulttv.setText(blockText);
                 }
            }
         }

     }).addOnFailureListener(new OnFailureListener() {
         @Override
         public void onFailure(@NonNull Exception e) {
             Toast.makeText(ScannerActivity.this,"Failed to detect text...",Toast.LENGTH_SHORT).show();
         }
     });

    }
}