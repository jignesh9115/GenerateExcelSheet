package com.ai.excelsheetdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.ai.excelsheetdemo.databinding.ActivityMainBinding;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    File file;

    String[] permissionsRequired = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_EXTERNAL_STORAGE};

    Database db;

    String username="",password="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db=new Database(getApplicationContext());


        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                username=binding.edtUsername.getText().toString().trim();
                password=binding.edtPassword.getText().toString().trim();

                if (username.equalsIgnoreCase("")&&password.equalsIgnoreCase("")){

                    Toast.makeText(MainActivity.this, "please enter value", Toast.LENGTH_SHORT).show();

                }else {

                    db.open();
                    db.insert_user(username, password);
                    db.close();

                }

                binding.edtUsername.setText("");
                binding.edtPassword.setText("");

            }
        });

        binding.btnGenerateExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!AskPermissions(MainActivity.this, permissionsRequired)) {
                    ActivityCompat.requestPermissions(MainActivity.this, permissionsRequired, 1);
                }else {

                    db.open();
                    Cursor cur=db.get_userlist();

                    if (cur.getCount()>0) {

                        createExcelSheet();
                    }else
                    {
                        Toast.makeText(MainActivity.this, "No Data Found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public static boolean AskPermissions(MainActivity context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void createExcelSheet()
    {
        //========Create a path where we will place our List of Excel sheet on external storage

        file = new File(Environment.getExternalStorageDirectory().getPath() + "/ExcelDemo/"+ "ExcelSheet.xls");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        WorkbookSettings workbookSettings=new WorkbookSettings();
        workbookSettings.setLocale(new Locale("en","IN"));
        WritableWorkbook writableWorkbook = null;
        try {
             writableWorkbook= Workbook.createWorkbook(file,workbookSettings);
        } catch (IOException e) {
            e.printStackTrace();
        }

        WritableSheet sheet=writableWorkbook.createSheet("userlist",0);

        try {
            sheet.addCell(new Label(0,0,"username"));
            sheet.addCell(new Label(1,0,"password"));


         /*   sheet.addCell(new Label(0,1,"jignesh"));
            sheet.addCell(new Label(1,1,"jignesh@9115"));*/

        } catch (WriteException e) {
            e.printStackTrace();
        }

        //==================write data from database starts============

        db.open();
        Cursor cur=db.get_userlist();

        if (cur.getCount()>0)
        {

            if (cur.moveToFirst()){

                do {

                    try {
                        sheet.addCell(new Label(0,cur.getPosition()+1,cur.getString(1)));
                        sheet.addCell(new Label(1,cur.getPosition()+1,cur.getString(2)));
                    } catch (WriteException e) {
                        e.printStackTrace();
                    }

                }while (cur.moveToNext());
            }

        }else {

            Toast.makeText(this, "No Data Found", Toast.LENGTH_SHORT).show();

        }

        cur.close();
        db.close();


        //==================write data from database ends==============



        try {
            writableWorkbook.write();
            writableWorkbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        }

        Toast.makeText(getApplication(), "Data Exported in a Excel Sheet", Toast.LENGTH_LONG).show();

        openExcelFile();

    }

    public void openExcelFile()
    {
        if (file.exists()) {
            Uri uri;
            Intent intent = new Intent(Intent.ACTION_VIEW);

            //====================fileProvider ===========

            if (Build.VERSION.SDK_INT >= 24) {
                uri = FileProvider.getUriForFile(MainActivity.this,  BuildConfig.APPLICATION_ID + ".provider", file);
            } else {
                uri = Uri.fromFile(file);
            }

            intent.setDataAndType(uri, "application/vnd.ms-excel");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            try {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(MainActivity.this, "No Application available to view Excel Sheet", Toast.LENGTH_LONG).show();
            }
        }
    }
}