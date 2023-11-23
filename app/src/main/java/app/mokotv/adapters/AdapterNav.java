package app.mokotv.adapters;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import app.mokotv.R;
import app.mokotv.utils.ItemNav;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class AdapterNav extends RecyclerView.Adapter<AdapterNav.ViewHolder> {

    private Activity activity;
    private int row_index = 0;
    private ArrayList<ItemNav> itemNavs;

    public AdapterNav(Activity activity, ArrayList<ItemNav> itemNavs) {
        this.activity = activity;
        this.itemNavs = itemNavs;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.row_navigation_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        ItemNav itemNav = itemNavs.get(position);
        Picasso.get().load(itemNav.getIconRes()).into(holder.imageView);
        holder.textView_Name.setText(itemNav.getName());

        holder.linearLayout.setOnClickListener(view -> {
            row_index = position;
            notifyDataSetChanged();
        });

        if (row_index == position) {
            holder.linearLayout.setBackgroundColor(activity.getResources().getColor(R.color.colorPrimary));
            holder.textView_Name.setTextColor(activity.getResources().getColor(R.color.white));
            holder.imageView.setColorFilter(activity.getResources().getColor(R.color.white));
        } else {
            holder.linearLayout.setBackgroundColor(activity.getResources().getColor(R.color.white));
            holder.textView_Name.setTextColor(activity.getResources().getColor(R.color.navigation_text));
            holder.imageView.setColorFilter(activity.getResources().getColor(R.color.colorPrimary));
        }
    }

    @Override
    public int getItemCount() {
        return itemNavs.size();
    }

    public void setSelected(int index) {
        row_index = index;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView textView_Name;
        private LinearLayout linearLayout;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_nav_icon);
            textView_Name = itemView.findViewById(R.id.text_nav_title);
            linearLayout = itemView.findViewById(R.id.linearLayout_main_adapter);
        }
    }
}
