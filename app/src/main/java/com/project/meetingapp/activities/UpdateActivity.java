package com.project.meetingapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.aakira.expandablelayout.ExpandableRelativeLayout;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.meetingapp.AESHelper;
import com.project.meetingapp.R;
import com.project.meetingapp.utilities.Constants;
import com.project.meetingapp.utilities.PreferenceManager;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class UpdateActivity extends AppCompatActivity{

    private EditText inputFullName,inputMobileNum, inputEmail,
            inputPassword,inputSpecialization,inputSchedule,inputVisitFee;
    private MaterialButton buttonSignUp;
    private ProgressBar signUpProgress;
    private TextView dateView;
    private int year, month, day;
    private RadioGroup radioGroup;
    private String date = " ",user_type = " ",encryptedName = " ",user_id = " ",
            encryptedPhoneNum = "",encryptedPass="",encryptedFee="",encryptedMail = "";
    private final String key = "1122334455667700";
    SecretKey secret;
    PreferenceManager preferenceManager;
    LinearLayout linearLayout_expand;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        preferenceManager = new PreferenceManager(getApplicationContext());
        user_id = preferenceManager.getString(Constants.KEY_USER_ID);

        linearLayout_expand = findViewById(R.id.layout_expand);
        ImageButton imageButtonClick = findViewById(R.id.click);
        signUpProgress       = findViewById(R.id.progressBarSignUp);
        Button btn_update = findViewById(R.id.update);
        dateView       = findViewById(R.id.dateOfBirth);
        inputSpecialization       = findViewById(R.id.specialization);
        inputSchedule       = findViewById(R.id.schedule);
        inputVisitFee       = findViewById(R.id.visit_fee);
        inputFullName       = findViewById(R.id.inputFirstName);
        inputMobileNum       = findViewById(R.id.inputMobileNo);
        inputEmail           = findViewById(R.id.inputEmail);
        inputPassword        = findViewById(R.id.inputPassword);
        buttonSignUp         = findViewById(R.id.buttonSignUp);
        radioGroup = (RadioGroup) findViewById(R.id.radio);

        inputFullName.setText(preferenceManager.getString(Constants.KEY_FULL_NAME));
        inputMobileNum.setText(preferenceManager.getString(Constants.KEY_MOBILE_NUM));
        dateView.setText(preferenceManager.getString(Constants.KEY_DATE_OF_BIRTH));
        inputEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));
        inputPassword.setText(preferenceManager.getString(Constants.KEY_PASSWORD));

        date = preferenceManager.getString(Constants.KEY_DATE_OF_BIRTH);
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        user_type = preferenceManager.getString(Constants.KEY_USER_TYPE);
        if(user_type.equals("Doctor")){
            linearLayout_expand.setVisibility(View.VISIBLE);
            inputSchedule.setText(preferenceManager.getString(Constants.KEY_SCHEDULE));
            inputSpecialization.setText(preferenceManager.getString(Constants.KEY_SPECIALIZATION));
            inputVisitFee.setText(preferenceManager.getString(Constants.KEY_FEE));
        }
        else {
            linearLayout_expand.setVisibility(View.GONE);
        }


        imageButtonClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDate(v);
            }
        });


        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    secret = AESHelper.generateKey(key);
                    encryptedMail = AESHelper.encryptMsg(inputEmail.getText().toString(),secret);
                    encryptedName = AESHelper.encryptMsg(inputFullName.getText().toString(),secret);
                    encryptedPhoneNum = AESHelper.encryptMsg(inputMobileNum.getText().toString(),secret);
                    encryptedPass = AESHelper.encryptMsg(inputPassword.getText().toString(),secret);
                    if(!inputVisitFee.getText().toString().isEmpty()){
                        encryptedFee = AESHelper.encryptMsg(inputVisitFee.getText().toString(),secret);
                    }
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (InvalidParameterSpecException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                }
                update();
            }
        });


    }

    public void setDate(View view) {
        showDialog(999);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 999) {
            return new DatePickerDialog(this,
                    myDateListener, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0,
                                      int arg1, int arg2, int arg3) {
                    // TODO Auto-generated method stub
                    // arg1 = year
                    // arg2 = month
                    // arg3 = day
                    showDate(arg1, arg2+1, arg3);
                }
            };

    private void showDate(int year, int month, int day) {
        date = day+"/"+month+"/"+year;
        dateView.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }

    private void update(){
        int selectedId = radioGroup.getCheckedRadioButtonId();

        // find the radiobutton by returned id
        RadioButton radioButton = (RadioButton) findViewById(selectedId);

        FirebaseFirestore database    = FirebaseFirestore.getInstance();
        HashMap<String, Object> users = new HashMap<>();
        users.put(Constants.KEY_FULL_NAME,encryptedName );
        users.put(Constants.KEY_MOBILE_NUM, encryptedPhoneNum);
        users.put(Constants.KEY_DATE_OF_BIRTH, date);
        users.put(Constants.KEY_SEX,radioButton.getText().toString());
        users.put(Constants.KEY_USER_TYPE, user_type);
        users.put(Constants.KEY_EMAIL, encryptedMail);
        users.put(Constants.KEY_PASSWORD, encryptedPass);
        users.put(Constants.KEY_SCHEDULE, inputSchedule.getText().toString());
        users.put(Constants.KEY_SPECIALIZATION, inputSpecialization.getText().toString());
        users.put(Constants.KEY_FEE, encryptedFee);

        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(user_id)
                .update(users)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(UpdateActivity.this, "Data updated Successfully", Toast.LENGTH_SHORT).show();
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, user_id);
                    preferenceManager.putString(Constants.KEY_USER_TYPE, user_type);
                    preferenceManager.putString(Constants.KEY_FULL_NAME, inputFullName.getText().toString());
                    preferenceManager.putString(Constants.KEY_EMAIL, inputEmail.getText().toString());
                    preferenceManager.putString(Constants.KEY_MOBILE_NUM, inputMobileNum.getText().toString());
                    preferenceManager.putString(Constants.KEY_DATE_OF_BIRTH, date);
                    preferenceManager.putString(Constants.KEY_SEX,radioButton.getText().toString());
                    preferenceManager.putString(Constants.KEY_PASSWORD, inputPassword.getText().toString());
                    preferenceManager.putString(Constants.KEY_SCHEDULE, inputSchedule.getText().toString());
                    preferenceManager.putString(Constants.KEY_SPECIALIZATION, inputSpecialization.getText().toString());
                    preferenceManager.putString(Constants.KEY_FEE, inputVisitFee.getText().toString());
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    buttonSignUp.setVisibility(View.VISIBLE);
                    signUpProgress.setVisibility(View.INVISIBLE);
                    Toast.makeText(UpdateActivity.this, "Error woi : "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}