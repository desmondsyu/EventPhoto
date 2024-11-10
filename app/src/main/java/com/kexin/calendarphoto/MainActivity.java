package com.kexin.calendarphoto;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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
import java.io.IOException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private static final int EVENT_REQUEST = 1000;
    private static final int PHOTO_REQUEST = 1100;

    EditText etEventTitle;
    TextView tvStartTime;
    TextView tvEndTime;
    EditText etDesc;
    EditText etInvitees;
    CheckBox cbAllDayEvent;
    Switch swAccessType;

    ImageView ivPhoto;
    String currentPhotoPath;

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode,@Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EVENT_REQUEST) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Event was added successfully.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Event was added failed.", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == PHOTO_REQUEST && resultCode == RESULT_OK) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            ImageView imageView = findViewById(R.id.iv_photo);
            imageView.setImageBitmap(image);
        }
    }
}