package profil;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.borsa_app.GirisActivity;
import com.example.borsa_app.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import Hisseler.HisseMenu;

public class ProfilMenu extends AppCompatActivity {

    private TextView menuPortfoy, menuIslemGecmisi, profileName, profileEmail, balanceValue, menuSifreDegistir;
    private Button logoutButton, depositButton, withdrawButton;
    private ImageButton backButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration balanceListener;
    private DocumentReference userRef;
    private double currentBalance = 0.0;


    private ImageView profilImage;
    private ActivityResultLauncher<Intent> imagePicker;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil_menu);



        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        profileName = findViewById(R.id.profile_name);
        profileEmail = findViewById(R.id.profile_email);
        balanceValue = findViewById(R.id.balance_value);
        menuPortfoy = findViewById(R.id.menu_portfoy);
        menuIslemGecmisi = findViewById(R.id.menu_islem_gecmisi);
        menuSifreDegistir = findViewById(R.id.menu_sifre_degistir);
        logoutButton = findViewById(R.id.logout_button);

        depositButton = findViewById(R.id.deposit_button);
        withdrawButton = findViewById(R.id.withdraw_button);
        profilImage = findViewById(R.id.profile_image);

        MaterialToolbar toolbar= findViewById(R.id.backProfilToMenu);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            finish(); // geri dön
        });
        imagePicker = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        profilImage.setImageURI(imageUri);
                        saveImageToLocal(imageUri); // 👈 KAYIT BURADA
                    }
                }
        );



        profilImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePicker.launch(intent);
        });


        ImageButton btnRefresh=findViewById(R.id.btnRefresh);

        btnRefresh.setOnClickListener(view -> {
            setupListeners();
            loadUserProfile();
            loadLocalProfileImage();
        });
        setupListeners();
        loadUserProfile();
        loadLocalProfileImage();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (balanceListener != null) {
            balanceListener.remove();
        }
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> onBackPressed());
        logoutButton.setOnClickListener(v -> signOut());
        menuIslemGecmisi.setOnClickListener(v -> startActivity(new Intent(this, IslemGecmisiActivity.class)));
        menuPortfoy.setOnClickListener(v -> startActivity(new Intent(this, HisseMenu.class)));
        menuSifreDegistir.setOnClickListener(v -> startActivity(new Intent(this, SifreDegistirActivity.class)));

        depositButton.setOnClickListener(v -> showBalanceUpdateDialog(true));
        withdrawButton.setOnClickListener(v -> showBalanceUpdateDialog(false));
    }

    private void showBalanceUpdateDialog(boolean isDeposit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isDeposit ? "Para Yatır" : "Para Çek");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Miktar");
        builder.setView(input);

        builder.setPositiveButton("Onayla", (dialog, which) -> {
            String amountStr = input.getText().toString();
            if (amountStr.isEmpty()) return;

            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    Toast.makeText(this, "Geçerli bir miktar girin", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isDeposit && amount > currentBalance) {
                    Toast.makeText(this, "Yetersiz bakiye", Toast.LENGTH_SHORT).show();
                    return;
                }

                updateBalance(isDeposit ? amount : -amount, isDeposit ? "DEPOSIT" : "WITHDRAW");

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Geçersiz miktar formatı", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("İptal", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void updateBalance(double amount, String type) {
        if (userRef == null) return;

        // Use a transaction to ensure atomicity
        db.runTransaction(transaction -> {
            transaction.update(userRef, "balance", FieldValue.increment(amount));
            return null;
        }).addOnSuccessListener(aVoid -> {
            addTransactionToHistory(amount, type);
            Toast.makeText(this, "İşlem başarılı", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> Toast.makeText(this, "İşlem başarısız: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void addTransactionToHistory(double amount, String type) {
        if (userRef == null) return;

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("type", type);
        transaction.put("total", Math.abs(amount));
        transaction.put("createdAt", FieldValue.serverTimestamp());

        userRef.collection("transactions").add(transaction);
    }


    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            profileName.setText(user.getDisplayName() != null && !user.getDisplayName().isEmpty() ? user.getDisplayName() : "Kullanıcı");
            profileEmail.setText(user.getEmail());
            userRef = db.collection("users").document(user.getUid());
            listenForBalanceChanges();
        } else {
            signOut();
        }



    }

    private void listenForBalanceChanges() {
        if (userRef != null) {
            balanceListener = userRef.addSnapshotListener(this, (snapshot, e) -> {
                if (e != null) {
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    Double balance = snapshot.getDouble("balance");
                    currentBalance = (balance != null) ? balance : 0.0;
                    balanceValue.setText(String.format("%,.2f ₺", currentBalance));
                } else {
                    // Document doesn't exist, so create it with initial data.
                    createInitialUserData();
                }
            });
        }
    }

    private void createInitialUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || userRef == null) return;

        Map<String, Object> userData = new HashMap<>();
        userData.put("balance", 0.0);
        userData.put("email", user.getEmail());
        userData.put("createdAt", FieldValue.serverTimestamp());

        // Use set with merge to avoid overwriting existing data if any
        userRef.set(userData, SetOptions.merge())
                .addOnFailureListener(e -> Toast.makeText(ProfilMenu.this, "Kullanıcı profili oluşturulamadı.", Toast.LENGTH_SHORT).show());
    }

    private void signOut() {
        mAuth.signOut();
        Intent intent = new Intent(this, GirisActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void saveImageToLocal(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File file = new File(getFilesDir(), "profile.jpg");

            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Foto kaydedilemedi", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadLocalProfileImage() {
        File file = new File(getFilesDir(), "profile.jpg");
        if (file.exists()) {
            profilImage.setImageURI(Uri.fromFile(file));
        }
    }

}
