package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.DbxClientV2Base;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.Context.MODE_PRIVATE;


/**
 * @author Shreyan Bakshi (AppyFizz)
 */

public class DropboxDownloadAndUnzipTask extends AsyncTask<String, Integer, String> {
    private static DbxClientV2Base dropboxClient;
    private final String TAG = this.getClass().getName();
    public static final String DBX_DOWN_DIR = "DbxDownload";
    public static final String DBX_ZIP_DIR = "DbxZip";
    public static final String DB_PREFS_KEY = "com.birdbraintechnologies.birdblox.DROPBOX_ACCESS_TOKEN";


    private AlertDialog.Builder builder;
    private AlertDialog downloadDialog;
    private ProgressBar progressBar;
    private Button cancelButton;
    private TextView showText;
    Context context;

    private String localName;

    public DropboxDownloadAndUnzipTask(Context context,DbxClientV2 dropboxClient) {
        super();
        this.context = context;
        this.dropboxClient = dropboxClient;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    public static String sanitizeName(String name) {
        if (name == null) return null;
        if (isNameSanitized(name)) return name;
        // else
        return name.replaceAll("[\\\\/:*?<>|.\n\r\0\"$]", "_");
    }
    static boolean isNameSanitized(String name) {
        // Illegal characters are:
        // '\', '/', ':', '*', '?', '<', '>', '|', '.', '\n', '\r', '\0', '"', '$'
        return (name != null) && !name.matches(".*[\\\\/:*?<>|.\n\r\0\"$].*");
    }

    public static JSONObject dropboxAppFolderContents(Context context) {
        if (!dropboxSignedIn(context)) return null;
        try {
            JSONArray arr = new JSONArray();
            ListFolderResult result = dropboxClient.files().listFolder("");
            while (true) {
                for (Metadata metadata : result.getEntries()) {
                    String name = metadata.getName();
                 /*   if (FilenameUtils.isExtension(name, "bbx"))
                        arr.put(FilenameUtils.getBaseName(name));*/
                }
                if (!result.getHasMore()) {
                    break;
                }
                result = dropboxClient.files().listFolderContinue(result.getCursor());
            }
            JSONObject obj = new JSONObject();
            obj.put("files", arr);
            return obj;
        } catch (DbxException | JSONException e) {
            Log.e("DropboxRequestHandler", "listFolder: " + e.getMessage());
        }
        return null;
    }

    static boolean dropboxSignedIn(Context context) {
        return context.getSharedPreferences(DB_PREFS_KEY, MODE_PRIVATE).getString("access-token", null) != null;
    }

    @Override
    protected String doInBackground(String... names) {
        /**
         * Implemented own {@link ProgressOutputStream}, since Dropbox API V2 has no built-in download progress.
         */
        try {
            if (names[0] != null && names[1] != null) {
                final String dbxName = names[0];
                localName = sanitizeName(names[1]);
                File dbxDownDir = new File(context.getFilesDir(), DBX_DOWN_DIR);
                if (!dbxDownDir.exists()) dbxDownDir.mkdirs();
                File dbxDown = new File(dbxDownDir, localName + ".bbx");
                try {
                    dropboxClient.files().getMetadata("/" + dbxName);
                } catch (GetMetadataErrorException e) {
                }
                if (!dbxDown.exists()) {
                    dbxDown.getParentFile().mkdirs();
                    dbxDown.createNewFile();
                }
                FileOutputStream fout = new FileOutputStream(dbxDown);
                FileMetadata downloadData = null;
                try {
                    DbxDownloader<FileMetadata> dbxDownloader = dropboxClient.files().download("/" + dbxName + ".bbx");
                    long size = dbxDownloader.getResult().getSize();
                    downloadData = dbxDownloader.download(new ProgressOutputStream(size, fout, new ProgressOutputStream.Listener() {
                        @Override
                        public void progress(long completed, long totalSize) {
                            if (isCancelled()) return;
                            publishProgress((int) ((completed / (double) totalSize) * 100));
                        }
                    }));
                    return localName;
                } finally {
                    fout.close();
                    if (downloadData != null)
                        Log.d(TAG, "MetadataDownload: " + downloadData);
                }
            }
        } catch (DbxException | IOException | SecurityException | IllegalStateException | ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "Unable to download file: " + e.getMessage());
           // downloadDialog.cancel();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        // update download progress in the progress bar here ...
        progressBar.setProgress(progress[0]);
    }

  /*  @Override
    protected void onPostExecute(String name) {
        if (isCancelled()) {
            try {
                if (localName != null)
                    new File(mainWebViewContext.getFilesDir() + "/" + DBX_DOWN_DIR, localName + ".bbx").delete();
            } catch (SecurityException | IllegalStateException e) {
                Log.e(TAG, "Unable to delete file: " + e.getMessage());
            } finally {
                try {
                    downloadDialog.cancel();
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Unable to close download dialog: " + e.getMessage());
                }
            }
            return;
        }
        if (localName != null) {
            super.onPostExecute(name);
            try {
                downloadDialog.cancel();
                File zip = new File(mainWebViewContext.getFilesDir() + "/" + DBX_DOWN_DIR, localName + ".bbx");
                File to = new File(getBirdbloxDir(), name);
                new UnzipTask().execute(zip, to);
            } catch (SecurityException e) {
                Log.e(TAG, "Error while unzipping project: " + name);
            }
        }
    }*/

    @Override
    protected void onCancelled(String name) {
        super.onCancelled(name);
        try {
            /*if (localName != null)
                new File(mainWebViewContext.getFilesDir() + "/" + DBX_DOWN_DIR, localName + ".bbx").delete();*/
        } catch (SecurityException | IllegalStateException e) {
            Log.e(TAG, "Unable to delete file: " + e.getMessage());
        } finally {
            try {
                downloadDialog.cancel();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Unable to close download dialog: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        try {
           /* if (localName != null)
                new File(mainWebViewContext.getFilesDir() + "/" + DBX_DOWN_DIR, localName + ".bbx").delete();*/
        } catch (SecurityException | IllegalStateException e) {
            Log.e(TAG, "Unable to delete file: " + e.getMessage());
        } finally {
            try {
                downloadDialog.cancel();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Unable to close download dialog: " + e.getMessage());
            }
        }
    }
}
