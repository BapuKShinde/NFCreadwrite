package com.example.nfcrwapp;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements DialogInterface.OnClickListener
{
    static MainActivity startActivity = null;
    NfcAdapter nfcAdapter;
    boolean nfc_enabled = false;
    static boolean read_tag = false;
    static boolean read_uid_tag = false;
    static boolean write_tag = false;
    static String str = "";
    EditText editText;
    PendingIntent pendingIntent;
    IntentFilter mifare;
    IntentFilter[] intentFiltersArray;
    String[][] techListsArray;
    /*
     * NFC KeySet
     */


	/*
	 * General KeySet
	 */
      byte[] ki0 = { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };



    int sectorNo = 0;
    int blockNo = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startActivity = this;
        try
        {
            nfcAdapter = NfcAdapter.getDefaultAdapter(MainActivity.this);
            if (nfcAdapter == null)
            {
                nfc_enabled = false;
                // showDialog("Message", "NFC is not supporting.");
            }
            else
            {
                nfc_enabled = true;
                // showDialog("Message", "NFC is supporting.");
            }
        }
        catch (Exception e)
        {
            showDialog("Message", e.toString());
        }

        try
        {
            this.nfcAdapter = NfcAdapter.getDefaultAdapter(this);

            this.pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            if (pendingIntent == null)
            {
                showDialog("Message", "null");
            }

            IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

            this.intentFiltersArray = new IntentFilter[] { ndef };
            this.techListsArray = new String[][] { new String[] { NfcA.class.getName() }, new String[] { MifareClassic.class.getName() } };
        }
        catch (Exception e)
        {
            showDialog("Message", e.toString());
        }
    }

    public void onPause()
    {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    public void onResume()
    {
        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    private void getSectorAndBlockNo()
    {
        sectorNo = Integer.parseInt(((EditText)findViewById(R.id.editText_sector_no)).getText().toString());
        blockNo = Integer.parseInt(((EditText)findViewById(R.id.editText_block_no)).getText().toString());
    }

    public void readFromCard(View view)
    {
        read_tag = true;
        write_tag = false;
        getSectorAndBlockNo();
        Toast.makeText(this, "Show tag to read data", Toast.LENGTH_LONG).show();
    }


    public void readUIDFromCard(View view)
    {
        read_uid_tag = true;
        read_tag = false;
        write_tag = false;
        Toast.makeText(this, "Show tag to read data", Toast.LENGTH_LONG).show();
    }

    public void writeOnCard(View view)
    {
        getSectorAndBlockNo();

        editText = (EditText) findViewById(R.id.editText_data);
        str = editText.getText().toString().trim();
        if (str.trim().length() > 0)
        {
            read_tag = false;
            read_uid_tag = false;
            write_tag = true;
            Toast.makeText(this, "Show tag to write data", Toast.LENGTH_LONG).show();
        }
        else
        {
            read_uid_tag = false;
            read_tag = false;
            write_tag = false;
            showDialog("Message", "Please enter the data in text field.");
        }
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch (item.getItemId())
        {
            case R.id.menu_exit:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Message");
                alertDialogBuilder.setMessage("Do you want to exit?").setCancelable(false).setPositiveButton("Yes", this).setNegativeButton("No", this);
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return true;
            case R.id.menu_app_info:
                LayoutInflater inflater = getLayoutInflater();
                View dialoglayout = inflater.inflate(R.layout.dialog_appinfo_layout, null);
                AlertDialog.Builder alertDialogBuilder1 = new AlertDialog.Builder(this);
                alertDialogBuilder1.setView(dialoglayout).setTitle("Application Information")
                .setCancelable(false).
                 setPositiveButton("OK", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         dialogInterface.dismiss();
                     }
                 });
                AlertDialog alertDialog1 = alertDialogBuilder1.create();
                alertDialog1.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onClick(DialogInterface dialog, int which)
    {
        if (which == -1)
        {
            this.finish();
        }
        else
        {
            dialog.cancel();
        }
    }

    public void showDialog(String title, String message)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message).setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    protected void onNewIntent(Intent intent)
    {
        String action = intent.getAction().trim();
        if (MainActivity.read_tag)
        {
            if (action.equals(NfcAdapter.ACTION_TECH_DISCOVERED))
            {
                readTag(intent);
            }
            else
            {
                // this.finish();
            }
        }
        else if (MainActivity.write_tag)
        {
            if (MainActivity.str.trim().length() > 0)
            {
                if (action.equals(NfcAdapter.ACTION_TECH_DISCOVERED))
                {
                    writeTag(intent);
                }
                else
                {
                    // this.finish();
                }
            }
            else
            {
                // this.finish();
            }
        }else if (MainActivity.read_uid_tag)
        {
            if (action.equals(NfcAdapter.ACTION_TECH_DISCOVERED))
            {
                readTagUID(intent);
            }
            else
            {
                // this.finish();
            }
        }
        else
        {
            showDialog("Message", "Please select activity.");
        }
    }

    void readTag(Intent intent)
    {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action))
        {
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            MifareClassic mfc = MifareClassic.get(tagFromIntent);
            byte[] data = null;

            try
            {
                mfc.connect();
                boolean auth = mfc.authenticateSectorWithKeyA(sectorNo, ki0);
               // boolean auth = mfc.authenticateSectorWithKeyB(sectorNo, MifareClassic.KEY_DEFAULT);
                if (auth)
                {
                    data = mfc.readBlock( blockNo ); //(mfc.sectorToBlock(15)));
                    String s = new String(data).trim();
                    MainActivity.read_tag = false;
                    MainActivity.write_tag = false;
                    showDialog("Message", "Data on card " + "\n\n" + s + "\n");
                    mfc.close();
                    // this.finish();
                }
                else
                {
                    showDialog("INFO", "Auth failed");
                }
            }
            catch (Exception e)
            {
                showDialog("INFO", e.toString());
            }
        }
    }


    void readTagUID(Intent intent)
    {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action))
        {
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            byte[] bt = tagFromIntent.getId();
            String UID = bytesToHex(bt).toLowerCase();
            showDialog("Message", "UID on card is " + "\n" + UID);
        }
    }

    public String bytesToHex(byte[] bytes)
    {
        final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++)
        {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    void writeTag(Intent intent)
    {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action))
        {
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            MifareClassic mfc = MifareClassic.get(tagFromIntent);

            try
            {
                mfc.connect();
                boolean auth = mfc.authenticateSectorWithKeyA(sectorNo, ki0);
                if (auth)
                {
                    String s = addSpacesTo16(MainActivity.str);
                    mfc.writeBlock(blockNo, s.getBytes());
                    MainActivity.read_tag = false;
                    MainActivity.write_tag = false;
                    showDialog("INFO", "Data written successfully on card.");
                    mfc.close();
                    // this.finish();
                }
                else
                {
                    showDialog("INFO", "Auth failed");
                }
            }
            catch (Exception e)
            {
                showDialog("INFO", e.toString());
            }
        }
    }

    public String addSpacesTo16(String data)
    {
        int spaces = 16 - data.length();
        for (int i = 0; i < spaces; i++)
        {
            data = data + " ";
        }
        return data;
    }

    @Override
    public void onBackPressed() {

    }
}
