package profil;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import Hisseler.HisseMenu;
import profil.IslemGecmisiActivity; // Bu activitylerin var olduğunu varsayıyorum
import  profil.SifreDegistirActivity;// Bu activitylerin var olduğunu varsayıyorum

public class ProfilMenu extends Fragment {

    private TextView menuPortfoy, menuIslemGecmisi, profileName, profileEmail, balanceValue, menuSifreDegistir;
    private Button logoutButton, depositButton, withdrawButton;
    private MaterialToolbar backToolbar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration balanceListener;
    private DocumentReference userRef;
    private double currentBalance = 0.0;
    private static final double MAX_DEPOSIT_AMOUNT = 1_000_000_000.0; // 1 Milyar

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_profil_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        profileName = view.findViewById(R.id.profile_name);
        profileEmail = view.findViewById(R.id.profile_email);
        balanceValue = view.findViewById(R.id.balance_value);
        menuPortfoy = view.findViewById(R.id.menu_portfoy);
        menuIslemGecmisi = view.findViewById(R.id.menu_islem_gecmisi);
        menuSifreDegistir = view.findViewById(R.id.menu_sifre_degistir);
        logoutButton = view.findViewById(R.id.logout_button);
        backToolbar = view.findViewById(R.id.back_button);
        depositButton = view.findViewById(R.id.deposit_button);
        withdrawButton = view.findViewById(R.id.withdraw_button);

        setupListeners();
        loadUserProfile();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (balanceListener != null) {
            balanceListener.remove();
        }
    }

    private void setupListeners() {
        backToolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
        logoutButton.setOnClickListener(v -> signOut());

        menuIslemGecmisi.setOnClickListener(v -> startActivity(new Intent(getContext(), IslemGecmisiActivity.class)));

        // Portföy tıklandığında yine aynı activity içinde kalmak isteyebilirsin.
        // Ama şimdilik mevcut mantığı koruyorum:
        menuPortfoy.setOnClickListener(v -> {
            // Normalde BottomNav'dan geçilmeli ama intent kalsın istiyorsan:
            // startActivity(new Intent(getContext(), HisseMenu.class)); // HisseMenu artık Activity değil!
            // Bu yüzden burayı BottomNav tetiklemesi gibi düşünebilirsin ya da silebilirsin.
        });

        menuSifreDegistir.setOnClickListener(v -> startActivity(new Intent(getContext(), SifreDegistirActivity.class)));

        depositButton.setOnClickListener(v -> showBalanceUpdateDialog(true));
        withdrawButton.setOnClickListener(v -> showBalanceUpdateDialog(false));
    }

    private void showBalanceUpdateDialog(boolean isDeposit) {
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(isDeposit ? "Para Yatır" : "Para Çek");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Miktar");
        builder.setView(input);

        builder.setPositiveButton("Onayla", (dialog, which) -> {
            String amountStr = input.getText().toString();
            if (amountStr.isEmpty()) return;

            try {
                BigDecimal amount = new BigDecimal(amountStr);

                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    Toast.makeText(getContext(), "Geçerli bir miktar girin", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isDeposit && amount.compareTo(new BigDecimal(MAX_DEPOSIT_AMOUNT)) > 0) {
                    Toast.makeText(getContext(), "Tek seferde en fazla " + String.format("%,.0f", MAX_DEPOSIT_AMOUNT) + " ₺ yatırabilirsiniz.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!isDeposit && amount.compareTo(BigDecimal.valueOf(currentBalance)) > 0) {
                    Toast.makeText(getContext(), "Yetersiz bakiye", Toast.LENGTH_SHORT).show();
                    return;
                }

                updateBalance(isDeposit ? amount.doubleValue() : -amount.doubleValue(), isDeposit ? "DEPOSIT" : "WITHDRAW");

            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Geçersiz miktar formatı", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("İptal", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void updateBalance(double amount, String type) {
        if (userRef == null) return;

        db.runTransaction(transaction -> {
            transaction.update(userRef, "balance", FieldValue.increment(amount));
            return null;
        }).addOnSuccessListener(aVoid -> {
            if (isAdded()) {
                addTransactionToHistory(amount, type);
                Toast.makeText(getContext(), "İşlem başarılı", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            if (isAdded()) Toast.makeText(getContext(), "İşlem başarısız: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
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
            balanceListener = userRef.addSnapshotListener((snapshot, e) -> {
                if (!isAdded()) return;
                if (e != null) { return; }
                if (snapshot != null && snapshot.exists()) {
                    Double balance = snapshot.getDouble("balance");
                    currentBalance = (balance != null) ? balance : 0.0;
                    balanceValue.setText(String.format("%,.2f ₺", currentBalance));
                } else {
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

        userRef.set(userData, SetOptions.merge())
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(getContext(), "Kullanıcı profili oluşturulamadı.", Toast.LENGTH_SHORT).show();
                });
    }

    private void signOut() {
        mAuth.signOut();
        Intent intent = new Intent(getContext(), GirisActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) getActivity().finish();
    }
}