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
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DownloadZipResult;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.users.FullAccount;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends AppCompatActivity {
    Button button;
    ImageView myImageView,myImageView2;
    TextView textDemo;
    DownloadManager downloadManager;
    Drawable ae3ad;
    Drawable ae3ad2;
    private long downloadID;
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 0;
    private static final String ACCESS_TOKEN = "lzxGzxAfw-cAAAAAAAAz_PxAiEG7ec__CTV-BUsL_OdbzBD2cSoc5w8wZe5bXT8h";
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial");
        final DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

        // Get current account info


            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        FullAccount account = client.users().getCurrentAccount();
                        Log.d("af43faewf","1 "+account.getName().getDisplayName());
                    } catch (DbxException e) {
                        e.printStackTrace();
                    }

                }
            });

        myImageView = findViewById(R.id.image);
        myImageView2 = findViewById(R.id.image2);
        button = findViewById(R.id.button);

        // Drawable unpressed =  getImageFromStorage("megathemes_animals4khd_sms_plus","btn_drawer_state_unpressed");
        // Drawable pressedads =  getImageFromStorage("megathemes_animals4khd_sms_plus","btn_menu_state_unpressed");
        //  ae3ad =  setState(unpressed,pressedads);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String separator = "/";
                String sdcardPath = Environment.getExternalStorageDirectory().getPath() + separator;
                unpackZip(getFilesDir().getPath(),"AlinTest");
                /*try {
                    myImageView.setImageDrawable(getImageFromStorage("com.megathemes_animals4khd.sms.plus","background_rate_sms"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                myImageView2.setImageDrawable(ae3ad2);*/
            }
        });



        // Get files and folder metadata from Dropbox root directory
        final ListFolderResult[] result = {null};
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    result[0] = client.files().listFolder("");
                } catch (DbxException e) {
                    e.printStackTrace();
                }
                while (true) {
                    for (Metadata metadata : result[0].getEntries()) {
                        Log.d("af43faewf","2 "+metadata.getPathLower());
                    }

                    if (!result[0].getHasMore()) {
                        break;
                    }

                    try {
                        result[0] = client.files().listFolderContinue(result[0].getCursor());
                    } catch (DbxException e) {
                        e.printStackTrace();
                    }
                }

                // Upload "test.txt" to Dropbox
             /*   try (InputStream in = new FileInputStream("megathemes_animals4khd_sms_plus")) {
                    FileMetadata metadata = client.files().uploadBuilder("/test.txt")
                            .uploadAndFinish(in);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (UploadErrorException e) {
                    e.printStackTrace();
                } catch (DbxException e) {
                    e.printStackTrace();
                }*/

                DbxDownloader<DownloadZipResult> downloader = null;
                try {
                    downloader = client.files().downloadZip("/megathemes_animals4khd_sms_plus");
                    // = client.files().download("/megathemes_animals4khd_sms_plus.zip");
                } catch (DbxException e) {
                    e.printStackTrace();
                }
                try {
                    String separator = "/";
                    String sdcardPath = Environment.getExternalStorageDirectory().getPath() + separator;
                    String filePath = getFilesDir().getPath() + "/AlinTest";
                    Log.d("Asdfa4fa",sdcardPath);
                    File f = new File(filePath);
                    getPermissionToReadExternal();
                    FileOutputStream out = new FileOutputStream(f);
                    if (downloader != null) {
                        downloader.download(out);
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

    private Drawable getImageFromStorage(String nameDir, String nameImage) throws IOException {
        String separator = "/";
        String sdcardPath = Environment.getExternalStorageDirectory().getPath() + separator;
        File directory = new File(sdcardPath + nameDir + "/res/drawable-xhdpi");
        File file = new File(directory, nameImage + ".png"); //or any other format supported
        FileInputStream streamIn = new FileInputStream(file);
        Bitmap bitmap = BitmapFactory.decodeStream(streamIn);
        streamIn.close();
        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
        return drawable;
    }


    private boolean unpackZip(String path, String zipname)
    {
        InputStream is;
        ZipInputStream zis;
        try
        {
            String filename;
            is = new FileInputStream(path + zipname);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null)
            {
                filename = ze.getName();

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(path + filename);
                    fmd.mkdirs();
                    continue;
                }

                FileOutputStream fout = new FileOutputStream(path + filename);

                while ((count = zis.read(buffer)) != -1)
                {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}