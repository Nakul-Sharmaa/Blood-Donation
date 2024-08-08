package com.example.blooddonation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class ViewParticipants extends AppCompatActivity {

    private ListView listView;
    private TextView tvHeader;
    private TextView tvNextCampDate;
    private Button btnGeneratePDF;
    private DatabaseHelper databaseHelper;

    private static final int DIRECTORY_REQUEST_CODE = 201;
    private Uri pickedDirectoryUri;
    private Uri generatedFileUri;

    private int pageHeight = 1120;
    private int pageWidth = 792;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_participants);

        listView = findViewById(R.id.listView);
        tvHeader = findViewById(R.id.tvParticipantDetails);
        tvNextCampDate = findViewById(R.id.tvNextCampDate);
        btnGeneratePDF = findViewById(R.id.btnGeneratePDF);
        databaseHelper = new DatabaseHelper(this);

        tvHeader.setText("Participants List");

        ArrayList<String> participantsList = databaseHelper.getAllData();
        CustomAdapter adapter = new CustomAdapter(this, R.layout.list_item, participantsList);
        listView.setAdapter(adapter);

        String nextCampDate = getNextCampDate();
        tvNextCampDate.setText("Next Blood Donation Camp: " + nextCampDate);

        btnGeneratePDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pickedDirectoryUri != null) {
                    generatePDF(participantsList);
                } else {
                    requestDirectoryPermission();
                }
            }
        });
    }

    private String getNextCampDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 15);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());

        String date = sdf.format(calendar.getTime());
        String dayOfWeek = dayFormat.format(calendar.getTime());

        return date + " (" + dayOfWeek + ")";
    }

    private void generatePDF(ArrayList<String> participantsList) {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint title = new Paint();
        Paint datePaint = new Paint();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
        Bitmap scaledBmp = Bitmap.createScaledBitmap(bmp, 140, 140, false);
        if (scaledBmp != null) {
            float bmpX = (pageWidth - scaledBmp.getWidth()) / 2;
            canvas.drawBitmap(scaledBmp, bmpX, 40, paint);
        }

        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        title.setTextSize(15);
        title.setColor(ContextCompat.getColor(this, R.color.black));
        title.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Participants List", 56, 200, title);
        canvas.drawText("Next Blood Donation Camp: " + getNextCampDate(), 56, 220, title);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String currentDateTime = dateFormat.format(Calendar.getInstance().getTime());
        datePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        datePaint.setTextSize(12);
        datePaint.setColor(ContextCompat.getColor(this, R.color.black));
        datePaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(currentDateTime, pageWidth - 56, 40, datePaint);

        int yPosition = 240;
        for (String participant : participantsList) {
            canvas.drawText(participant, 56, yPosition, title);
            yPosition += 20;
        }

        pdfDocument.finishPage(page);

        String uniqueFileName = "ParticipantsList_" + UUID.randomUUID().toString() + ".pdf";
        try {
            Uri fileUri = createFileInPickedDirectory(uniqueFileName);
            if (fileUri != null) {
                try (FileOutputStream out = (FileOutputStream) getContentResolver().openOutputStream(fileUri)) {
                    pdfDocument.writeTo(out);
                    generatedFileUri = fileUri;
                    Toast.makeText(this, "PDF file generated successfully.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to generate PDF file.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate PDF file.", Toast.LENGTH_SHORT).show();
        }

        pdfDocument.close();
    }

    private Uri createFileInPickedDirectory(String fileName) {
        if (pickedDirectoryUri == null) {
            return null;
        }
        Uri newFileUri = null;
        try {
            Uri treeUri = DocumentsContract.buildDocumentUriUsingTree(pickedDirectoryUri, DocumentsContract.getTreeDocumentId(pickedDirectoryUri));
            newFileUri = DocumentsContract.createDocument(getContentResolver(), treeUri, "application/pdf", fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newFileUri;
    }

    private void requestDirectoryPermission() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, DIRECTORY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DIRECTORY_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                pickedDirectoryUri = data.getData();
                getContentResolver().takePersistableUriPermission(pickedDirectoryUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                Toast.makeText(this, "Permission Granted for the selected directory.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class CustomAdapter extends ArrayAdapter<String> {
        private int resourceLayout;
        private ArrayList<String> participants;

        public CustomAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String> participants) {
            super(context, resource, participants);
            this.resourceLayout = resource;
            this.participants = participants;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(resourceLayout, parent, false);
            }

            String participant = participants.get(position);
            TextView tvParticipantDetails = convertView.findViewById(R.id.tvParticipantDetails);
            tvParticipantDetails.setText(participant);

            return convertView;
        }
    }
}
