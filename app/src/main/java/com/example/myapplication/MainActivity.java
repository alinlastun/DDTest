package com.example.myapplication;

import android.Manifest;
import android.app.DownloadManager;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.util.ProgressOutputStream;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.DownloadZipResult;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.users.FullAccount;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.example.myapplication.DropboxDownloadAndUnzipTask.DBX_DOWN_DIR;

public class MainActivity extends AppCompatActivity {
    Button button,button2,button3;
    ImageView myImageView, myImageView2;
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 0;
    private static final String ACCESS_TOKEN = "lzxGzxAfw-cAAAAAAAA0Cbh8E2Y64-f8ys7fTG2l8nfbFJA9L31Gl928LRgbUxd2";

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DbxRequestConfig config = new DbxRequestConfig("dropbox/MyThemes");
        final DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

        myImageView = findViewById(R.id.image);
        myImageView2 = findViewById(R.id.image2);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);

        button3.setOnClickListener(v -> {
            try {
                Drawable  pressedads=  getImageFromStorage3("megathemes_animals4khd_sms_plus","btn_emoji_state_pressed");
                myImageView.setImageDrawable(pressedads);
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        button.setOnClickListener(v -> {
            downloadZIp(client,"megathemes_animals4khd_sms_plus");
        });

        button2.setOnClickListener(v -> {
            unzip3("megathemes_animals4khd_sms_plus");

        });

    }
    public void getPermissionToReadExternal() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION) {
            int grantResultsLength = grantResults.length;
            if (grantResultsLength > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "You grant write external storage permission. Please click original button again to continue.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "You denied write external storage permission.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void downloadZIp(DbxClientV2 client,String nameDir) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                DbxDownloader<DownloadZipResult> downloader = null;
                try {
                    downloader = client.files().downloadZip("/" + nameDir);
                    //downloader  = client.files().download("/megathemes_animals4khd_sms_plus.zip");


                } catch (DbxException e) {
                    e.printStackTrace();
                }
                try {
                    String filePath = getFilesDir().getPath() +"/"+ nameDir +".zip";
                    File f = new File(filePath);
                    getPermissionToReadExternal();
                    FileOutputStream out = new FileOutputStream(f);
                    if (downloader != null) {
                        downloader.download(new ProgressOutputStream(out),bytesWritten -> {
                            Log.d("ASdfasdf", String.valueOf(bytesWritten));
                        });

                    }
                    out.close();
                } catch (DbxException ex) {
                    System.out.println(ex.getMessage());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private Drawable getImageFromStorage3(String nameDir, String nameImage) throws IOException {
        String separator = "/";
        String destination = getFilesDir().getPath() +separator+nameDir+separator+nameDir;
        File directory = new File(destination);
        File file = new File(directory+"/res/drawable-xhdpi", nameImage + ".png"); //or any other format supported
        FileInputStream streamIn = new FileInputStream(file.getAbsoluteFile());
        Bitmap bitmap = BitmapFactory.decodeStream(streamIn);
        streamIn.close();
        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
        return drawable;
    }

    public void unzip3(String nameZip){
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                String source = getFilesDir().getPath() + "/"+nameZip+".zip";
                String destination = getFilesDir().getPath() + "/"+nameZip;
                try {
                    ZipFile zipFile = new ZipFile(source);
                    zipFile.extractAll(destination);
                } catch (ZipException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}









