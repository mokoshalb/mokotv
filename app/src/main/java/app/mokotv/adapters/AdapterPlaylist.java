package app.mokotv.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import org.jetbrains.annotations.NotNull;
import app.mokotv.R;
import app.mokotv.activities.ActivityStreaming;
import app.mokotv.utils.M3UItem;
import app.mokotv.utils.NetworkCheck;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdapterPlaylist extends RecyclerView.Adapter<AdapterPlaylist.ItemHolder> implements Filterable {

    private final Context mContext;
    private final LayoutInflater mInflater;
    private List<M3UItem> mItem = new ArrayList<>();
    private List<M3UItem> mItemCopy;
    private ColorGenerator generator = ColorGenerator.MATERIAL;

    public AdapterPlaylist(Context c) {
        mContext = c;
        mInflater = LayoutInflater.from(mContext);
    }

    @NotNull
    @Override
    public ItemHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        final View sView = mInflater.inflate(R.layout.item_playlist, parent, false);
        mItemCopy = new ArrayList<>(mItem);
        return new ItemHolder(sView);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public void onBindViewHolder(@NotNull final ItemHolder holder, final int position) {
        final M3UItem item = mItem.get(position);
        if (item != null) {
            holder.update(item);
        }
    }

    @Override
    public int getItemCount() {
        return mItem.size();
    }

    public void update(List<M3UItem> _list) {
        this.mItem = _list;
        notifyDataSetChanged();
    }
    @SuppressWarnings("unchecked")
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<M3UItem> fList = new ArrayList<>();
                if (!(constraint.length() == 0)){
                    final String pat = constraint.toString().toLowerCase().trim();
                    for (final M3UItem item : mItemCopy){
                        if (Objects.requireNonNull(item.getItemName()).trim().toLowerCase().contains(pat)){
                            fList.add(item);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = fList;
                mItem.clear();
                mItem.addAll((ArrayList<M3UItem>) results.values);
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                notifyDataSetChanged();
            }
        };
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        ImageView cImg;
        ItemHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            name = view.findViewById(R.id.item_name);
            cImg = view.findViewById(R.id.cimg);
        }

        void update(final M3UItem item) {
            try {
            name.setText(item.getItemName());
                int color = generator.getRandomColor();
                TextDrawable textDrawable;
                textDrawable = TextDrawable.builder().buildRoundRect(String.valueOf(Objects.requireNonNull(item.getItemName()).charAt(0)), color, 100);
                cImg.setImageDrawable(textDrawable);
            } catch (Exception ignored) { }
        }

        public void onClick(View v) {
            try {
                int position = getLayoutPosition();
                final M3UItem imm = mItem.get(position);
                if (NetworkCheck.isNetworkAvailable((Activity) mContext)) {
                        Intent intent = new Intent(mContext, ActivityStreaming.class);
                        intent.putExtra("url", imm.getItemUrl());
                        mContext.startActivity(intent);
                } else {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.network_required), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception ignored) {}
        }
    }
}
