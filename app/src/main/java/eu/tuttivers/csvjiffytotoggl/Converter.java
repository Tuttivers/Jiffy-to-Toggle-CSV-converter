package eu.tuttivers.csvjiffytotoggl;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import com.snatik.storage.Storage;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static eu.tuttivers.csvjiffytotoggl.FileUtils.getPath;

public class Converter {

    private static final String header = "Email,Project,Start date,Start time,duration\n";
    private static final String email = "tuttivers@gmail.com";

    Context context;
    ConvertListener listener;

    public Converter(ConvertListener listener) {
        this.context = listener.getContext();
        this.listener = listener;
    }

    public void convert(final Uri uri) {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = null;
                try {
                    inputStream = context.getContentResolver().openInputStream(uri);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    boolean isHeader = true;
                    while ((line = reader.readLine()) != null) {
                        if (isHeader) {
                            stringBuilder.append(header);
                            isHeader = false;
                        } else {
                            stringBuilder.append(email).append(',');
                            String[] columns = StringUtils.split(line, ',');
                            for (int i = 0; i < columns.length; i++) {
                                switch (i) {
                                    case 0: //project
                                        stringBuilder.append(columns[i]);
                                        break;
                                    case 1: // start date and time
                                        stringBuilder.append(columns[i].replace(" ", ","));
                                        break;
                                    case 3: // duration
                                        int minutesRaw = Integer.parseInt(columns[i]);
                                        int hours = minutesRaw / 60;
                                        int minutes = minutesRaw % 60;
                                        stringBuilder.append(String.format("%d:%02d:00", hours, minutes));
                                        break;
                                }

                                if (i < columns.length - 2) {
                                    stringBuilder.append(',');
                                }
                            }
                            stringBuilder.append("\n");
                        }
                    }
                    inputStream.close();
                    final String text = stringBuilder.toString();

                    Storage storage = new Storage(context);
                    String jiffyCSVpath = getPath(context, uri);
                    String togglCSVpath;
                    if (uri.getScheme().equals("content")) {
                        togglCSVpath = storage.getExternalStorageDirectory() + File.separator + uri.getLastPathSegment();
                        togglCSVpath = togglCSVpath.replace(".", "_converted_for_toggl.");
                    } else {
                        togglCSVpath = jiffyCSVpath.replace(".", "_converted_for_toggl.");
                        togglCSVpath = togglCSVpath.replace(' ', '_');
                    }
                    if (storage.isFileExist(togglCSVpath)) {
                        storage.deleteFile(togglCSVpath);
                    }
                    storage.createFile(togglCSVpath, text);
                    final String finalTogglCSVpath = togglCSVpath;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.succesfullyConverted(finalTogglCSVpath);
                        }
                    });
                } catch (final IOException e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onErrorConverted(e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }
}
