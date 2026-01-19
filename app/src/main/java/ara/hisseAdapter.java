package ara;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.borsa_app.R;

import java.util.ArrayList;
import java.util.List;

public class hisseAdapter extends RecyclerView.Adapter<hisseAdapter.HisseVH> {
    public interface OnHisseClickListener {
        void onHisseClick(hisseModel hisse);
    }

    private OnHisseClickListener listener;

    public void setOnHisseClickListener(OnHisseClickListener listener) {
        this.listener = listener;
    }
    private final List<hisseModel> tumListe;
    private final List<hisseModel> gosterilenListe;

    public hisseAdapter(List<hisseModel> liste) {
        this.tumListe = new ArrayList<>(liste);
        this.gosterilenListe = new ArrayList<>(liste);
    }

    @NonNull
    @Override
    public HisseVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hisse, parent, false);
        return new HisseVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HisseVH holder, int position) {

        hisseModel h = gosterilenListe.get(position);
        if (h != null && h.symbol != null) {
            holder.tvSymbol.setText(h.symbol);
            holder.tvName.setText(h.name != null ? h.name : h.symbol);
        } else {
            holder.tvSymbol.setText("N/A");
            holder.tvName.setText("Bilinmeyen");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHisseClick(h);
            }
        });
    }

    @Override
    public int getItemCount() {
        return gosterilenListe.size();
    }

    static class HisseVH extends RecyclerView.ViewHolder {
        TextView tvSymbol, tvName;

        public HisseVH(@NonNull View itemView) {
            super(itemView);
            tvSymbol = itemView.findViewById(R.id.tvSymbol);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }

    // 🔍 FİLTRE (CRASH SAFE)
    public void filter(String text) {
        gosterilenListe.clear();

        if (text == null || text.trim().isEmpty()) {
            gosterilenListe.addAll(tumListe);
        } else {
            text = text.toLowerCase();
            for (hisseModel h : tumListe) {
                if ((h.symbol != null && h.symbol.toLowerCase().contains(text)) ||
                        (h.name != null && h.name.toLowerCase().contains(text))) {
                    gosterilenListe.add(h);
                }
            }
        }
        notifyDataSetChanged();
    }
}
