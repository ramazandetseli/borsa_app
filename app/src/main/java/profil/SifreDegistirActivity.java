package profil;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.borsa_app.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SifreDegistirActivity extends AppCompatActivity {

    private TextInputEditText etCurrentPassword, etNewPassword, etNewPasswordConfirm;
    private Button btnChangePassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sifre_degistir);

        mAuth = FirebaseAuth.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbarSifreDegistir);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etNewPasswordConfirm = findViewById(R.id.et_new_password_confirm);
        btnChangePassword = findViewById(R.id.btn_change_password);

        btnChangePassword.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String newPasswordConfirm = etNewPasswordConfirm.getText().toString().trim();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || newPasswordConfirm.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(newPasswordConfirm)) {
            Toast.makeText(this, "Yeni şifreler uyuşmuyor.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Yeni şifre en az 6 karakter olmalıdır.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "Kullanıcı bulunamadı.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Re-authenticate the user before changing the password
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

        user.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
            if (reauthTask.isSuccessful()) {
                // If re-authentication is successful, update the password
                user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(SifreDegistirActivity.this, "Şifre başarıyla değiştirildi.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(SifreDegistirActivity.this, "Şifre değiştirilemedi: " + updateTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(SifreDegistirActivity.this, "Mevcut şifre yanlış.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
