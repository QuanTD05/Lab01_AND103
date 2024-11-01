package fpoly.md19304.lab01_and103;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginPhone extends AppCompatActivity {

    private EditText edtPhoneNumber, edtOTP;
    private Button btnGetOTP, btnLogin;
    private FirebaseAuth mAuth;
    private String verificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_phone);

        mAuth = FirebaseAuth.getInstance();

        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
        edtOTP = findViewById(R.id.edtOTP);
        btnGetOTP = findViewById(R.id.btnGetOTP);
        btnLogin = findViewById(R.id.btnLogin);

        btnGetOTP.setOnClickListener(v -> getOTP());
        btnLogin.setOnClickListener(v -> verifyOTP());

        // Initialize mCallbacks inside onCreate
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                edtOTP.setText(credential.getSmsCode());
                signInWithPhoneAuthCredential(credential); // Auto sign in with the received credential
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.e("LoginPhone", "Verification failed", e);
                Toast.makeText(LoginPhone.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                LoginPhone.this.verificationId = verificationId;
                Log.d("LoginPhone", "OTP sent. Verification ID: " + verificationId);
                Toast.makeText(LoginPhone.this, "OTP Sent", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void getOTP() {
        String phoneNumber = edtPhoneNumber.getText().toString().trim();

        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "Please enter a valid phone number.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("LoginPhone", "Sending OTP to: +84" + phoneNumber);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber("+84" + phoneNumber)  // Replace "+84" with your country code
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyOTP() {
        String code = edtOTP.getText().toString().trim();

        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, "Please enter the OTP.", Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("LoginPhone", "Login successful!");
                        Toast.makeText(LoginPhone.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        FirebaseUser user = task.getResult().getUser();
                        startActivity(new Intent(LoginPhone.this, MainActivity2.class));
                        finish();
                    } else {
                        Log.e("LoginPhone", "Login failed", task.getException());
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(LoginPhone.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginPhone.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
