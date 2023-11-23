package app.mokotv.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import app.mokotv.Config;
import app.mokotv.R;
import app.mokotv.models.Category;
import com.squareup.picasso.Picasso;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.ViewHolder> {

    private List<Category> items;
    private OnItemClickListener mOnItemClickListener;
    public interface OnItemClickListener {
        void onItemClick(View view, Category obj, int position);
    }
    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterCategory(List<Category> items) {
        this.items = items;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView category_name;
        ImageView category_image;
        LinearLayout lyt_parent;

        ViewHolder(View v) {
            super(v);
            category_name = v.findViewById(R.id.category_name);
            category_image = v.findViewById(R.id.category_image);
            lyt_parent = v.findViewById(R.id.lyt_parent);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lsv_item_category, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NotNull ViewHolder holder, final int position) {
        final Category c = items.get(position);
        holder.category_name.setText(c.category_name);
        Picasso.get()
                .load(Config.ADMIN_PANEL_URL + "/app/upload/category/" + c.category_image)
                .placeholder(R.drawable.ic_thumbnail)
                .into(holder.category_image);
        holder.lyt_parent.setOnClickListener(view -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, c, position);
            }
        });
    }

    public void setListData(List<Category> items){
        this.items = items;
        notifyDataSetChanged();
    }

    public void resetListData() {
        this.items = new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}