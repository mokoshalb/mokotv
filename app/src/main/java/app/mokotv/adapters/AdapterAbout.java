package app.mokotv.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.jetbrains.annotations.NotNull;
import app.loveplusplus.update.UpdateChecker;
import app.mokotv.R;
import app.mokotv.fragments.FragmentAbout;

import java.util.List;

public class AdapterAbout extends RecyclerView.Adapter<AdapterAbout.UserViewHolder> {

    private List<FragmentAbout.Data> dataList;
    private Context context;

    public AdapterAbout(List<FragmentAbout.Data> dataList, Context context) {
        this.dataList = dataList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.lsv_item_about, null);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, final int position) {

        FragmentAbout.Data data = dataList.get(position);
        holder.image.setImageResource(data.getImage());
        holder.title.setText(data.getTitle());
        holder.sub_title.setText(data.getSub_title());

        holder.relativeLayout.setOnClickListener(view -> {
            if (position == 1) {
                UpdateChecker.checkForUpdate(context);
            } else if (position == 2) {
                try {
                    Intent emailIntent = new Intent (Intent.ACTION_VIEW , Uri.parse("mailto:" + context.getResources().getString(R.string.sub_about_app_email)));
                    emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Moko TV - Contact");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                    context.startActivity(Intent.createChooser(emailIntent, "Send Mail Using:"));
                } catch(Exception e) {
                    Toast.makeText(context, "\"Unable to find any emailing app", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else if (position == 3) {
                try{
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(context.getResources().getString(R.string.sub_about_app_url)));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (Exception e){
                    Toast.makeText(context, "Unable to find any browser app", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

            /*
            } else if (position == 4) {
                try {
                    Intent telegram = new Intent(Intent.ACTION_VIEW , Uri.parse("https://t.me/mokolivestream"));
                    telegram.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(telegram);
                } catch (Exception e){
                    Toast.makeText(context, "Unable to find Telegram app", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            */

            } else if (position == 5) {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                @SuppressLint("InflateParams") final View mView = layoutInflater.inflate(R.layout.lyt_dialog, null);
                final AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setView(mView);

                final TextView textView = mView.findViewById(R.id.privacy_policy);
                textView.setText(Html.fromHtml(context.getResources().getString(R.string.privacy_policy_content)));

                final AlertDialog alertDialog = alert.create();
                alertDialog.show();

                final ImageView imageView = mView.findViewById(R.id.img_dialog_fullscreen_close);
                imageView.setOnClickListener(view1 -> alertDialog.dismiss());
            } else {
                Log.d("Log", "Do Nothing!");
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView sub_title;
        RelativeLayout relativeLayout;
        UserViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            title = itemView.findViewById(R.id.title);
            sub_title = itemView.findViewById(R.id.sub_title);
            relativeLayout = itemView.findViewById(R.id.lyt_parent);
        }
    }
}