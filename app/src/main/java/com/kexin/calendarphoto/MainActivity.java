package com.kexin.calendarphoto;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int EVENT_REQUEST = 1000;
    private static final int PHOTO_REQUEST = 1100;
    private static final int NOTE_REQUEST = 1200;

    EditText etEventTitle;
    TextView tvStartTime;
    TextView tvEndTime;
    EditText etDesc;
    EditText etInvitees;
    CheckBox cbAllDayEvent;
    Switch swAccessType;

    ImageView ivPhoto;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEventTitle = findViewById(R.id.et_event_title);
        tvStartTime = findViewById(R.id.tv_start_time);
        tvEndTime = findViewById(R.id.tv_end_time);
        etDesc = findViewById(R.id.et_desc);
        etInvitees = findViewById(R.id.et_invitees);
        cbAllDayEvent = findViewById(R.id.cb_all_day_event);
        swAccessType = findViewById(R.id.sw_access);

        cbAllDayEvent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            handleAllDayEvent(isChecked);
        });

        swAccessType.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String accessType = isChecked ? "Public" : "Private";
            Log.d("AccessType", "Event access: " + accessType);
        });

        ivPhoto = findViewById(R.id.iv_photo);
    }

    private void handleAllDayEvent(boolean isChecked) {
        tvStartTime.setEnabled(!isChecked);
        tvEndTime.setEnabled(!isChecked);
    }

    public void selectStartTimeClicked(View view) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (viewNew, hourOfDay, minuteOfHour) -> {
                    tvStartTime.setText(String.format("%02d:%02d", hourOfDay, minuteOfHour));
                },
                hour, minute, false
        );
        timePickerDialog.show();
    }

    public void selectEndTimeClicked(View view) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (viewNew, hourOfDay, minuteOfHour) -> {
                    tvEndTime.setText(String.format("%02d:%02d", hourOfDay, minuteOfHour));
                },
                hour, minute, false
        );
        timePickerDialog.show();
    }

    public void addEventClicked(View view) {
        String title = etEventTitle.getText().toString();
        String description = etDesc.getText().toString();
        boolean allDayEvent = cbAllDayEvent.isChecked();
        boolean isPublic = swAccessType.isChecked();
        String invitees = etInvitees.getText().toString();

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, title)
                .putExtra(CalendarContract.Events.DESCRIPTION, description)
                .putExtra(CalendarContract.Events.ALL_DAY, allDayEvent)
                .putExtra(CalendarContract.Events.ACCESS_LEVEL, isPublic ? CalendarContract.Events.ACCESS_PUBLIC : CalendarContract.Events.ACCESS_PRIVATE);

        if (!allDayEvent) {
            String startTime = tvStartTime.getText().toString();
            String endTime = tvEndTime.getText().toString();

            Calendar startCal = Calendar.getInstance();
            Calendar endCal = Calendar.getInstance();

            String[] startParts = startTime.split(":");
            String[] endParts = endTime.split(":");

            startCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startParts[0]));
            startCal.set(Calendar.MINUTE, Integer.parseInt(startParts[1]));

            endCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endParts[0]));
            endCal.set(Calendar.MINUTE, Integer.parseInt(endParts[1]));

            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startCal.getTimeInMillis());
            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endCal.getTimeInMillis());
        }

        if (!invitees.isEmpty()) {
            intent.putExtra(Intent.EXTRA_EMAIL, invitees);
        }

        startActivityForResult(intent, EVENT_REQUEST);
    }

    public void takePhotoClicked(View view) {
        Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (photoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(photoIntent, PHOTO_REQUEST);
        }
    }

    private void storeImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Toast.makeText(this, "Failed to create file", Toast.LENGTH_LONG).show();
            return;
        }

        try (FileOutputStream fos = new FileOutputStream(pictureFile)) {
            image.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            Log.d("MainActivity", "Image saved successfully.");
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "File not found", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to save", Toast.LENGTH_LONG).show();
        }
    }

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(getFilesDir(), "CalendarPhotos");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e("MainActivity", "Failed to create directory for storing images.");
                return null;
            } else {
                Log.d("MainActivity", "Directory created successfully.");
            }
        }

        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm", Locale.getDefault()).format(new Date());
        String mImageName = "IMG_" + timeStamp + ".jpeg";
        Log.d("MainActivity", "Image created successfully.");
        return new File(mediaStorageDir, mImageName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EVENT_REQUEST) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Event was added successfully.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Event was added failed.", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == PHOTO_REQUEST && resultCode == RESULT_OK) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            if (image == null) {
                Log.e("MainActivity", "Captured image is null.");
                return;
            }
            storeImage(image);
            ivPhoto.setImageBitmap(image);
        } else if (requestCode == NOTE_REQUEST && resultCode == RESULT_OK) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Email was added successfully.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Email was added failed.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void emailClicked(View view) {
        File imageFile = getOutputMediaFile();
        if (imageFile == null || !imageFile.exists()) {
            Toast.makeText(this, "No image to share", Toast.LENGTH_SHORT).show();
            return;
        }

        imageUri = FileProvider.getUriForFile(this,
                "com.kexin.calendarphoto.fileprovider", imageFile);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Photo");
        intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}