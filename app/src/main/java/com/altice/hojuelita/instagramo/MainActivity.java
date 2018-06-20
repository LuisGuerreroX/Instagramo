package com.altice.hojuelita.instagramo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity  {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button logInButton;
    private TextView forgotPassText;
    private TextView registerText;

    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
 //   private FirebaseUser user;

    private static final String TAG = "EmailPassword";

    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        logInButton = findViewById(R.id.logInButton);
        forgotPassText = findViewById(R.id.forgotPassText);
        registerText = findViewById(R.id.registerText);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailEditText.setEnabled(false);
                passwordEditText.setEnabled(false);
                forgotPassText.setEnabled(false);
                registerText.setEnabled(false);
                logInButton.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

                signIn(emailEditText.getText().toString(),passwordEditText.getText().toString());

            }
        });

        //Texto para restablecer la contraseña.
        forgotPassText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPassText.setTextColor(Color.parseColor("#551A8B"));
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                @SuppressLint("InflateParams") View dialogLayout = inflater.inflate(R.layout.custom_resetpassdialog, null, false);
                final EditText enterEmail = dialogLayout.findViewById(R.id.enter_email);
                builder.setView(dialogLayout);

                builder.setPositiveButton(getString(R.string.reset), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String email = enterEmail.getText().toString();
                        if (TextUtils.isEmpty(email)) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    getString(R.string.empty),Toast.LENGTH_SHORT);
                            toast.show();
                        } else if (validateEmailForm(email)) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    getString(R.string.invalid_email),Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            resetPassword(email);
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    getString(R.string.reset_email_sent),Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                });

                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Do Nothing, just cancel btn.
                    }
                });

                builder.show();
            }
        });

        //Texto para registrar usuario nuevo.
        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerText.setTextColor(Color.parseColor("#551A8B"));

                //Builder para el Dialog de registro de usuario.
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                @SuppressLint("InflateParams") View dialogLayout = inflater.inflate(R.layout.custom_registerdialog, null, false);
                final EditText enterPass = dialogLayout.findViewById(R.id.enter_pass);
                final EditText enterEmail = dialogLayout.findViewById(R.id.enter_email);
                builder.setView(dialogLayout);

                builder.setPositiveButton(getString(R.string.register_now), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean emailReady = false;
                        boolean passReady = false;

                        String email = enterEmail.getText().toString();
                        if (TextUtils.isEmpty(email)) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    getString(R.string.empty),Toast.LENGTH_SHORT);
                            toast.show();
                        } else if (validateEmailForm(email)) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    getString(R.string.invalid_email),Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            emailReady = true;
                        }

                        String password = enterPass.getText().toString();
                        if (TextUtils.isEmpty(password)) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    getString(R.string.invalid_password),Toast.LENGTH_SHORT);
                            toast.show();
                        } else if (password.length() <= 5) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    getString(R.string.invalid_password),Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            passReady = true;
                        }

                        if (emailReady && passReady) {
                            createAccount(email,password);

                            Toast toast = Toast.makeText(getApplicationContext(),
                                    getString(R.string.welcome),Toast.LENGTH_SHORT);
                            toast.show();

                            Intent intent = new Intent(MainActivity.this,SocialActivity.class);
                            startActivity(intent);
                        }
                    }
                });

                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Do Nothing, just cancel btn.
                    }
                });
                builder.show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);

    }

    private void signIn(String email, final String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            progressBar.setVisibility(View.GONE);
            logInButton.setVisibility(View.VISIBLE);
            emailEditText.setEnabled(true);
            passwordEditText.setEnabled(true);
            forgotPassText.setEnabled(true);
            registerText.setEnabled(true);
            return;
        }

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                       //     FirebaseUser user = mAuth.getCurrentUser();
                            emailEditText.setEnabled(true);
                            passwordEditText.setEnabled(true);
                            forgotPassText.setEnabled(true);
                            registerText.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                            logInButton.setVisibility(View.VISIBLE);
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    getString(R.string.welcome),Toast.LENGTH_SHORT);
                            toast.show();
                            Intent intent = new Intent(MainActivity.this,SocialActivity.class);
                            startActivity(intent);
                        } else {
                            //Si falla por causa de error.
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            progressBar.setVisibility(View.GONE);
                            logInButton.setVisibility(View.VISIBLE);
                            emailEditText.setEnabled(true);
                            passwordEditText.setEnabled(true);
                            forgotPassText.setEnabled(true);
                            registerText.setEnabled(true);
                        }

                        //Si falla por mala validación.
                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    getString(R.string.invalid_user),Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
               //             FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                 //           Toast.makeText(EmailPasswordActivity.this, "Authentication failed.",
                  //                  Toast.LENGTH_SHORT).show();
                        }

                    }
                });
        // [END create_user_with_email]
    }

    private void resetPassword(String emailAddress) {
        mAuth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    getString(R.string.reset_email_sent),Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                });
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = emailEditText.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.empty));
            valid = false;
        } else if (validateEmailForm(email)) {
            emailEditText.setError(getString(R.string.invalid_email));
            valid = false;
            } else {
                emailEditText.setError(null);
            }

        String password = passwordEditText.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.empty));
            valid = false;
        } else {
            passwordEditText.setError(null);
        }

        if (password.length() <= 5) {
            passwordEditText.setError(getString(R.string.invalid_password));
            valid = false;
        } else {
            passwordEditText.setError(null);
        }

        return valid;
    }

    //Metodo para comprobar si el email tiene formato "algo@algo.com"
    private static boolean validateEmailForm(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

}
