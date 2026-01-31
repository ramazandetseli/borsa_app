package Hisseler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.borsa_app.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class hisseGorunumAdapter
        extends RecyclerView.Adapter<hisseGorunumAdapter.ViewHolder> {

    ArrayList<hisseGorunum> hisseList;
    NumberFormat format;

    public hisseGorunumAdapter(ArrayList<hisseGorunum> hisseList) {
        this.hisseList = hisseList;
    }

    public interface OnHisseClickListener {
        void onHisseClick(hisseGorunum hisse);
    }

    private OnHisseClickListener listener;

    public void setOnHisseClickListener(OnHisseClickListener listener) { this.listener = listener; }
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtSymbol, txtPrice,txtTutar,txtWinLose;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtSymbol = itemView.findViewById(R.id.tvSymbol);
            txtPrice  = itemView.findViewById(R.id.tvcurrentPrice);
            txtTutar=itemView.findViewById(R.id.tvTutar);
            txtWinLose=itemView.findViewById(R.id.tvLoseWin);

        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hisse, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        hisseGorunum hisse = hisseList.get(position);

        holder.txtSymbol.setText(hisse.symbol);
        holder.txtPrice.setText("Fiyat:"+hisse.guncelFiyat + " ₺");
        holder.txtTutar.setText("Ort. Maliyet:"+hisse.ortalamaFiyat+ " ₺");
        double karZarar =
                (hisse.guncelFiyat - hisse.ortalamaFiyat) * hisse.lotValue;
        format = NumberFormat.getNumberInstance(new Locale("tr", "TR"));
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);

        holder.txtWinLose.setText(
                "Top. Getiri: " + format.format(karZarar) + " ₺"

        );
        if (karZarar > 0) {
            holder.txtWinLose.setTextColor(
                    holder.itemView.getContext().getColor(android.R.color.holo_green_dark)

            );
        } else if (karZarar < 0) {
            holder.txtWinLose.setTextColor(
                    holder.itemView.getContext().getColor(android.R.color.holo_red_dark)
            );
        } else {
            holder.txtWinLose.setTextColor(
                    holder.itemView.getContext().getColor(android.R.color.darker_gray)
            );
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHisseClick(hisse);
            }
        });
    }


    @Override
    public int getItemCount() {
        return hisseList.size();
    }




}


