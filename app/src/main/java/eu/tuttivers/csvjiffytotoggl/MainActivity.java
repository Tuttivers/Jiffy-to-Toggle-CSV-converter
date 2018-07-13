package eu.tuttivers.csvjiffytotoggl;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static eu.tuttivers.csvjiffytotoggl.FileUtils.getPath;

public class MainActivity extends AppCompatActivity implements ConvertListener {

    private static final String importPageUrl = "https://www.toggl.com/app/import";

    @BindView(R.id.name_file)
    TextView fileNameTv;

    @BindView(R.id.status)
    TextView statusTv;

    @BindView(R.id.converted_file_path)
    TextView convertedFilePathTv;

    @BindView(R.id.sucess_layout)
    LinearLayout successLayout;

    @OnClick(R.id.choose_file)
    public void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/comma-separated-values");
        startActivityForResult(intent, 0);
    }

    @OnClick(R.id.open_converted_csv)
    public void openConvertedCsv() {
        File file = new File(togglCSVpath);
        Uri fileUri = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "text/csv");
        startActivity(intent);
    }

    @OnClick(R.id.import_to_toggl)
    public void openImportPage() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(importPageUrl));
        startActivity(i);
    }

    private ProgressDialog progressDialog;
    private String togglCSVpath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_SEND.equals(intent.getAction())) {
            handleIntent(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (intent != null && resultCode ==  RESULT_OK) {
            handleIntent(intent);
        }

    }

    private void handleIntent(Intent intent) {
        showProgress();
        Uri uri;
        if (intent.hasExtra(Intent.EXTRA_STREAM)) {
            uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        } else {
            uri = intent.getData();
        }
        if (uri == null) {
            onErrorConverted("uri is null");
            return;
        }
        fileNameTv.setText(getPath(MainActivity.this, uri));
        new Converter( this).convert(uri);
    }

    @Override
    public void succesfullyConverted(String convertedFilePath) {
        togglCSVpath = convertedFilePath;
        statusTv.setText("Successfully converted!");
        convertedFilePathTv.setText(convertedFilePath);
        successLayout.setVisibility(View.VISIBLE);
        hideProgress();
    }

    @Override
    public void onErrorConverted(String errorMessage) {
        statusTv.setText(errorMessage);
        convertedFilePathTv.setText("");
        successLayout.setVisibility(View.GONE);
        hideProgress();
    }

    @Override
    public Context getContext() {
        return this;
    }

    private void showProgress() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Converting");
        }
        progressDialog.show();
    }

    private void hideProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
