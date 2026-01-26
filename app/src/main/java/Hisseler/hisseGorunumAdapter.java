package Hisseler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.borsa_app.R;

import java.util.ArrayList;

import ara.hisseAdapter;

public class hisseGorunumAdapter
        extends RecyclerView.Adapter<hisseGorunumAdapter.ViewHolder> {

    ArrayList<hisseGorunum> hisseList;
    public hisseGorunumAdapter(ArrayList<hisseGorunum> hisseList) {
        this.hisseList = hisseList;
    }

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
        holder.txtPrice.setText(hisse.guncelFiyat + " ₺");
        holder.txtTutar.setText(hisse.ortalamaFiyat+ " ₺");
        double karZarar =
                (hisse.guncelFiyat - hisse.ortalamaFiyat) * hisse.lotValue;

        holder.txtWinLose.setText(
                String.format("%.2f ₺", karZarar)
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
    }


    @Override
    public int getItemCount() {
        return hisseList.size();
    }




}


