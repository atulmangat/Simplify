package com.atulmangat.qrcodegenerator;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.EnumMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private final String tag = "QRCGEN";
    private final int REQUEST_PERMISSION = 0xf0;

    private MainActivity self;
    private Snackbar snackbar;
    private Bitmap qrImage;

    private TextView txtQRText;
    private TextView txtSaveHint;
    private Button btnGenerate;
    private ImageView imgResult;
    private ProgressBar loader;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        self = this;

        Intent i = getIntent();
        String textQR = i.getStringExtra("textQR");
        txtQRText   = (TextView) findViewById(R.id.txtQR);
      //  txtSaveHint = (TextView) findViewById(R.id.txtSaveHint);
        btnGenerate = (Button)findViewById(R.id.btnGenerate);

        imgResult   = (ImageView)findViewById(R.id.imgResult);
        loader      = (ProgressBar)findViewById(R.id.loader);
         txtQRText.setText(textQR);
        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                self.generateImage();
            }
        });

        txtQRText.setText(textQR);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImage();
            }
        }
    }

    private void saveImage() {
        if (qrImage == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
            return;
        }

    }

    private void alert(String message){
        AlertDialog dlg = new AlertDialog.Builder(self)
                .setTitle("QRCode Generator")
                .setMessage(message)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        dlg.show();
    }


    private void snackbar(String msg) {
        if (self.snackbar != null) {
            self.snackbar.dismiss();
        }

        self.snackbar = Snackbar.make(
                findViewById(R.id.mainBody),
                msg, Snackbar.LENGTH_SHORT);

        self.snackbar.show();
    }

    private void endEditing(){
        txtQRText.clearFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }


    private void generateImage(){
        final String text = txtQRText.getText().toString();
        if(text.trim().isEmpty()){
            return;
        }
        endEditing();
        showLoadingVisible(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                int size = imgResult.getMeasuredWidth();
                if( size > 1){
                    Log.e(tag, "size is set manually");
                    size = 260;
                }

                Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
                hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
                hintMap.put(EncodeHintType.MARGIN, 1);
                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                try {
                    BitMatrix byteMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size,
                            size, hintMap);
                    int height = byteMatrix.getHeight();
                    int width = byteMatrix.getWidth();
                    self.qrImage = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                    for (int x = 0; x < width; x++){
                        for (int y = 0; y < height; y++){
                            qrImage.setPixel(x, y, byteMatrix.get(x,y) ? Color.BLACK : Color.WHITE);
                        }
                    }

                    self.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            self.showImage(self.qrImage);
                            self.showLoadingVisible(false);
                            self.snackbar("QRCode");
                        }
                    });
                } catch (WriterException e) {
                    e.printStackTrace();
                    alert(e.getMessage());
                }
            }
        }).start();
    }

    private void showLoadingVisible(boolean visible){
        if(visible){
            showImage(null);
        }

    }


    private void showImage(Bitmap bitmap) {
        if (bitmap == null) {
            imgResult.setImageResource(android.R.color.transparent);
            qrImage = null;
        } else {
            imgResult.setImageBitmap(bitmap);

        }
    }

    public void buttonreset(View view) {

        Intent i = new Intent(this, Main2Activity.class);
        startActivity(i);

    }
}
