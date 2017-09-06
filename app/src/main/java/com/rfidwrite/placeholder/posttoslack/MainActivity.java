package com.rfidwrite.placeholder.rfidwrite;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Parcelable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends Activity {

    public static final String ERROR_DETECTED = "No NFC tag detected!";
    public static final String WRITE_SUCCESS = "Text written to the NFC tag successfully!";
    public static final String WRITE_ERROR = "Error during writing, is the NFC tag close enough to your device?";
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    Tag myTag;
    Context context;

    TextView tvNFCContent;
    TextView message;
    Button btnWrite, btnDropDb;

    ImageView tagDetectionCheckMark;
    TextView detectionStatus;
    TextView detectedTagDetails;

    private TagDatabase tagDatabase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        //tvNFCContent = (TextView) findViewById(R.id.title);
       // message = (TextView) findViewById(R.id.dataInput);
       // btnWrite = (Button) findViewById(R.id.writeDataBtn);
       // btnDropDb = (Button) findViewById(R.id.dropDbBtn);

        detectionStatus = (TextView) findViewById(R.id.processStatus);
        tagDetectionCheckMark = (ImageView) findViewById(R.id.tagReadCompleteMark);
        detectedTagDetails = (TextView) findViewById(R.id.tagDetails);

        tagDatabase = new TagDatabase(this.context);
/*
        btnWrite.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                try {
                    if(myTag ==null) {
                        Toast.makeText(context, ERROR_DETECTED, Toast.LENGTH_LONG).show();
                    } else {
                        write(message.getText().toString(), myTag);
                        Toast.makeText(context, WRITE_SUCCESS, Toast.LENGTH_LONG ).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(context, WRITE_ERROR, Toast.LENGTH_LONG ).show();
                    e.printStackTrace();
                } catch (FormatException e) {
                    Toast.makeText(context, WRITE_ERROR, Toast.LENGTH_LONG ).show();
                    e.printStackTrace();
                }
            }
        });

        btnDropDb.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
               tagDatabase.clearDatabase();
            }
        });
*/
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
        }
        readFromIntent(getIntent());

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };
    }


    /**************Read From NFC Tag**********************************/
    private void readFromIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            buildTagViews(msgs);
        }
    }
    private void buildTagViews(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) return;

        String text = "";
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
        int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"

        try {
            // Get the Text
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);

            // Check if the tag name is already in the database.
            EntranceTag detectedTag = tagDatabase.findTag(text);

            // If tag has been detected before
            if(detectedTag != null){

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy-HH:mm:ss");
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+9:00"));
                Date currentLocalTime = cal.getTime();

                // Check if it is in use
                if(detectedTag.GetTagUsedFlag() == 0) {
                    // not in use, so update database
                    String detectedDateTime = dateFormat.format(currentLocalTime);
                    detectedTag.SetUsedFlag(1);
                    detectedTag.SetDateTimeDetected(detectedDateTime);
                    tagDatabase.updateTag(detectedTag);
                }else{ // in use, so we need to calculate elapsed time and additional charge
                    Date initialDetectionDate = currentLocalTime; // just to initialize initialDetectionDate.

                    try {
                        initialDetectionDate = dateFormat.parse(detectedTag.GetTagDateTime());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    String readableElapsedTime = ConvertMilliSecToReadableTime(GetElapsedTime(initialDetectionDate, currentLocalTime));
                    int additionalCharge = CalculateAdditionalCharge(initialDetectionDate,currentLocalTime);

                    String detectionResult = readableElapsedTime   + " 超過しました。" + "追加料金：" + additionalCharge + "円 です。";

                    detectedTagDetails.setText(detectionResult);
                    detectedTagDetails.setVisibility(View.VISIBLE);

                    detectedTag.SetUsedFlag(0);
                    tagDatabase.updateTag(detectedTag);
                }
                String tagDetectionStatus = "タグ認識しました";
                detectionStatus.setText(tagDetectionStatus);
                tagDetectionCheckMark.setVisibility(View.VISIBLE);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ResetViews();
                    }
                }, 10000);

            }else{
                // If tag is not in the database, add it
                Toast.makeText(this, "タグを記憶します... " , Toast.LENGTH_LONG).show();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy-HH:mm:ss");
                // Get current date
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+9:00"));
                Date currentLocalTime = cal.getTime();
                String detectedDateTime = dateFormat.format(currentLocalTime);

                // Finally add the new tag to database
                EntranceTag newDetectedTag = new EntranceTag(text, detectedDateTime, 1);
                tagDatabase.addTag(newDetectedTag);
            }

        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncoding", e.toString());
        }

        //tvNFCContent.setText("NFC Content: " + text);

    }

    private void ResetViews(){
        detectedTagDetails.setText("");
        detectionStatus.setText(R.string.waiting_for_tag);
        tagDetectionCheckMark.setVisibility(View.INVISIBLE);
    }


    /****Write to NFC Tag**************************************************/
    private void write(String text, Tag tag) throws IOException, FormatException {
        NdefRecord[] records = { createRecord(text) };
        NdefMessage message = new NdefMessage(records);
        // Get an instance of Ndef for the tag.
        Ndef ndef = Ndef.get(tag);
        // Enable I/O
        ndef.connect();
        // Write the message
        ndef.writeNdefMessage(message);
        // Close the connection
        ndef.close();
    }
    private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
        String lang       = "en";
        byte[] textBytes  = text.getBytes();
        byte[] langBytes  = lang.getBytes("US-ASCII");
        int    langLength = langBytes.length;
        int    textLength = textBytes.length;
        byte[] payload    = new byte[1 + langLength + textLength];

        // set status byte (see NDEF spec for actual bits)
        payload[0] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1,              langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  new byte[0], payload);

        return recordNFC;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        readFromIntent(intent);
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        WriteModeOff();
    }

    @Override
    public void onResume(){
        super.onResume();
        WriteModeOn();
    }

    /********Enable Write**********************/
    private void WriteModeOn(){
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }
    /* *****Disable Write**********************************************/
    private void WriteModeOff(){
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
    }

    private long GetElapsedTime(Date start, Date end){

        long elapsedTimeInMilliSec = end.getTime() - start.getTime();
        return elapsedTimeInMilliSec;
    }

    private String ConvertMilliSecToReadableTime(long millisec){
        int hour, minute, sec;
        sec = (int) millisec/1000;
        hour = sec/3600;
        minute = (sec%3600) / 60;
        sec = (sec%3600) % 60;
        return (Integer.toString(hour)+":"+Integer.toString(minute)+":"+Integer.toString(sec));
    }

    private int CalculateAdditionalCharge(Date entry, Date exit){
        int multiplier = 0;
        long elapsedTime = GetElapsedTime(entry, exit) - 1800000; // Substract 30 mins from elapsed time.

        if(elapsedTime <= 0){
            return 0;
        }

        multiplier = (int) elapsedTime / 1800000;
        if(elapsedTime % 1800000 > 600000){
            multiplier++;
        }
        return multiplier * 500; // 500 is the additional charge per 30 mins
    }

    private String DateToString(Date inputDate){
        String output = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy-HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+9:00"));
        output = dateFormat.format(inputDate);
        return output;
    }

}