package com.example.cipherpad;

import android.app.Activity;
import android.content.*;
import android.net.Uri;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.io.*;

public class EditNote extends AppCompatActivity {
    int noteID;
    EditText noteTextView;
    EditText titleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        noteTextView = findViewById(R.id.noteTextView);
        titleTextView = findViewById(R.id.titleTextView);

        Intent intent = getIntent();
        noteID = intent.getIntExtra("noteID", -1);

        if (noteID == -1) {
            NoteDatabase.mydb.insertNote("example title","example note","blank");
            noteID = NoteDatabase.mydb.getLastId();
        }
        else{
            titleTextView.setText(NoteDatabase.mydb.getNote(noteID).get(0));
            noteTextView.setText(NoteDatabase.mydb.getNote(noteID).get(1));
        }


        titleTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                NoteDatabase.mydb.updateNote(noteID,String.valueOf(s),NoteDatabase.mydb.getNote(noteID).get(1),NoteDatabase.mydb.getNote(noteID).get(2));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        noteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                NoteDatabase.mydb.updateNote(noteID,NoteDatabase.mydb.getNote(noteID).get(0),String.valueOf(s),NoteDatabase.mydb.getNote(noteID).get(2));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.edit_note_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.encryptDecryptText)
        {
            EditText input = new EditText(this);
            input.setHint(" ");

            new AlertDialog.Builder(this)
                    .setTitle("Please enter a key for ciphering...")
                    .setView(input)
                    .setPositiveButton(
                            NoteDatabase.mydb.getNote(noteID).get(2).equals("blank") ? "add cipher key" : "cipher",
                            new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            if (NoteDatabase.mydb.getNote(noteID).get(2).equals("blank")) {
                                if (input.getText().toString().equals("")){
                                    Toast.makeText(getApplicationContext(), "Please enter a cipher key", Toast.LENGTH_LONG).show();
                                }
                                else{
                                    noteTextView.setText(cipherText(input.getText().toString(),noteTextView.getText().toString()));
                                    NoteDatabase.mydb.updateNote(noteID,NoteDatabase.mydb.getNote(noteID).get(0),noteTextView.getText().toString(),String.valueOf(input.getText().toString().hashCode()));
                                    Toast.makeText(getApplicationContext(), "Key hash = " + String.valueOf(input.getText().toString().hashCode()), Toast.LENGTH_SHORT).show();
                                }
                            }
                            else{
                                if (NoteDatabase.mydb.getNote(noteID).get(2).equals(String.valueOf(input.getText().toString().hashCode()))){
                                    noteTextView.setText(cipherText(input.getText().toString(),noteTextView.getText().toString()));
                                    NoteDatabase.mydb.updateNote(noteID,NoteDatabase.mydb.getNote(noteID).get(0),noteTextView.getText().toString(),String.valueOf(input.getText().toString().hashCode()));
                                }

                                else{
                                    Toast.makeText(getApplicationContext(), "Incorrect cipher key", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    })
                    /*
                    .setNeutralButton(
                            "Set hint",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                //TODO implement key hint feature.
                                }
                            })
                    */
                    .setNegativeButton("Cancel", null)
                    .show();
        }
        if (item.getItemId() == R.id.deleteCipher) {
            new AlertDialog.Builder(this)
                    .setTitle("Do you want to reset the current cipher key for this note?")
                    .setPositiveButton("Reset cipher key",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            NoteDatabase.mydb.updateNote(noteID,NoteDatabase.mydb.getNote(noteID).get(0),NoteDatabase.mydb.getNote(noteID).get(1),"blank");
                            Toast.makeText(getApplicationContext(), "Cipher key reset.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        }

        if (item.getItemId() == R.id.exportNote) {
            export();
            return true;
        }

        if (item.getItemId() == R.id.shareNote) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, noteTextView.getText().toString());
            sendIntent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
            return true;
        }
        if (item.getItemId() == R.id.copyNote) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("note", noteTextView.getText().toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(), "Note copied to clipboard!", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void export() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, titleTextView.getText().toString());
        //noinspection deprecation
        startActivityForResult(intent, 1);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                try {
                    OutputStream out = getContentResolver().openOutputStream(uri);
                    String s = noteTextView.getText().toString();
                    out.write(s.getBytes());
                    out.flush();
                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private String cipherText(String keyText, String text){
        String input = text;
        String output = "";
        int keylen = keyText.length();
        int len = text.length();
        char XORkey;
        for(int j = 0; j<keylen; j++) {
            XORkey = keyText.charAt(j);
            output = "";
            for (int i = 0; i < len; i++) {
                output = output + (char) (input.charAt(i) ^ XORkey);
            }
            input = output;
        }
        return output;
    }
}
