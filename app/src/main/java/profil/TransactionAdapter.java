package profil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.borsa_app.R;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final List<Transaction> transactionList;
    private final Context context;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public TransactionAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvType, tvDetail, tvTotal, tvDate;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_transaction_icon);
            tvType = itemView.findViewById(R.id.tv_transaction_type);
            tvDetail = itemView.findViewById(R.id.tv_transaction_detail);
            tvTotal = itemView.findViewById(R.id.tv_transaction_total);
            tvDate = itemView.findViewById(R.id.tv_transaction_date);
        }

        void bind(Transaction transaction) {
            tvDetail.setVisibility(View.VISIBLE);

            String formattedTotal = String.format("%,.2f ₺", transaction.getTotal());

            switch (transaction.getType()) {
                case "BUY":
                    tvType.setText(transaction.getSymbol() + " Alış");
                    tvDetail.setText(transaction.getLot() + " LOT @ " + String.format("%,.2f ₺", transaction.getPrice()));
                    tvTotal.setText("- " + formattedTotal);
                    tvTotal.setTextColor(ContextCompat.getColor(context, R.color.red));
                    ivIcon.setImageResource(R.drawable.ic_arrow_upward);
                    break;
                case "SELL":
                    tvType.setText(transaction.getSymbol() + " Satış");
                    tvDetail.setText(transaction.getLot() + " LOT @ " + String.format("%,.2f ₺", transaction.getPrice()));
                    tvTotal.setText("+ " + formattedTotal);
                    tvTotal.setTextColor(ContextCompat.getColor(context, R.color.green));
                    ivIcon.setImageResource(R.drawable.ic_arrow_downward);
                    break;
                case "DEPOSIT":
                    tvType.setText("Para Yatırma");
                    tvDetail.setVisibility(View.GONE);
                    tvTotal.setText("+ " + formattedTotal);
                    tvTotal.setTextColor(ContextCompat.getColor(context, R.color.green));
                    ivIcon.setImageResource(R.drawable.ic_add);
                    break;
                case "WITHDRAW":
                    tvType.setText("Para Çekme");
                    tvDetail.setVisibility(View.GONE);
                    tvTotal.setText("- " + formattedTotal);
                    tvTotal.setTextColor(ContextCompat.getColor(context, R.color.red));
                    ivIcon.setImageResource(R.drawable.ic_remove);
                    break;
                default:
                    tvType.setText(transaction.getType());
                    tvDetail.setVisibility(View.GONE);
                    tvTotal.setText(formattedTotal);
                    break;
            }

            Timestamp createdAt = transaction.getCreatedAt();
            if (createdAt != null) {
                tvDate.setText(sdf.format(createdAt.toDate()));
            }
        }
    }
}
