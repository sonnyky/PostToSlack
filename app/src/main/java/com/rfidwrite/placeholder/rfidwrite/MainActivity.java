package com.rfidwrite.placeholder.rfidwrite;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private NfcAdapter NfcAndroidAdapter;
    private PendingIntent NfcIntent;
    private Tag currentDetectedTag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NfcAndroidAdapter = NfcAdapter.getDefaultAdapter(this);

        if (NfcAndroidAdapter == null) {
            // Device does not support NFC
            Toast.makeText(this,
                    "Device does not support NFC!",
                    Toast.LENGTH_LONG).show();
            this.finish();
        } else {
            if (!NfcAndroidAdapter.isEnabled()) {
                // NFC is disabled
                Toast.makeText(this, "Enable NFC!",
                        Toast.LENGTH_LONG).show();
            } else {
                NfcIntent = PendingIntent.getActivity(MainActivity.this,
                        0, new Intent(MainActivity.this, MainActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            }
        }

        final Button button = (Button) findViewById(R.id.writeDataBtn);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    ndefWrite(currentDetectedTag, "MyFirstTag");
                }
                catch(java.io.IOException e){

                }
                catch(android.nfc.FormatException nfe){
                    
                }
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        NfcAndroidAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        NfcAndroidAdapter.enableForegroundDispatch(this, NfcIntent, null,
                null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String action = intent.getAction();
        if(action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED) || action.equals(NfcAdapter.ACTION_TECH_DISCOVERED)){
            String tagContent = "";
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            if(action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED))
            {
                tagContent = ndefReadTag(tag);
                currentDetectedTag = tag;
            }

        }

    }

    protected String ndefReadTag(Tag tag)
    {
        String contentString = "";
        NdefRecord[] records;
        try {
            Ndef ndef = Ndef.get(tag);

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            records = ndefMessage.getRecords();

            if (records.length == 0)
            {

            } else {
                for (NdefRecord Record : records) {
                    if (Record.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(Record.getType(), NdefRecord.RTD_TEXT)) {
                        byte[] contentpayload = Record.getPayload();
                        String Encoding = ((contentpayload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
                        int languageCodeLength = contentpayload[0] & 0063;
                        contentString = new String(contentpayload, languageCodeLength + 1, contentpayload.length - languageCodeLength - 1, Encoding);
                    }
                }
            }
        }
        catch (UnsupportedEncodingException e) {
            Toast.makeText(this, e.getMessage(),
                    Toast.LENGTH_LONG).show();
            return contentString;
        }
        return contentString;
    }

    private void ndefWrite(Tag tag, String textString) throws IOException, FormatException {

        String language       = "en";
        byte[] stringBytes  = textString.getBytes();
        byte[] languageBytes  = language .getBytes("US-ASCII");
        int    languageLength = languageBytes.length;
        int    stringLength = stringBytes.length;
        byte[] payload    = new byte[1 + languageLength + stringLength];
        payload[0] = (byte) languageLength;

        NdefRecord[] ndefRecords = { new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload)};
        NdefMessage  message = new NdefMessage(ndefRecords);

        Ndef ndef = Ndef.get(tag);
        ndef.connect();
        ndef.writeNdefMessage(message);
        ndef.close();

    }


}
