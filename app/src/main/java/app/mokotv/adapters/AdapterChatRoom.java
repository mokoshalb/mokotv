package app.mokotv.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.jetbrains.annotations.NotNull;
import app.mokotv.R;
import app.mokotv.models.Chat;
import app.mokotv.utils.Tools;
import java.util.ArrayList;

public class AdapterChatRoom extends RecyclerView.Adapter<AdapterChatRoom.MyViewHolder> {
  private ArrayList<Chat> data;

  public AdapterChatRoom(ArrayList<Chat> data) {
    this.data = data;
  }

  @NotNull
  @Override public MyViewHolder onCreateViewHolder(@NotNull ViewGroup viewGroup, int i) {
    @SuppressLint("InflateParams") View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_chat_room, null);
    return new MyViewHolder(view);
  }

  @Override public void onBindViewHolder(@NotNull MyViewHolder myViewHolder, int i) {
    Chat chat = data.get(i);
    String formatted_date = Tools.formatted_date(chat.getTimestamp());
    if(chat.isNotification()) {
      myViewHolder.textView_message.setText(Html.fromHtml("<small><i><font color=\"#FFBB33\">" + " " + chat.getMessage() + "</font></i></small>"));
    }else{
      myViewHolder.textView_message.setText(
          Html.fromHtml("<b><font color=\""+ chat.getColor() +"\">&#x3C;" + chat.getUsername() + "&#x3E;</font><font color=\"#ffffff\">" + " " + chat.getMessage() + " </font><font color=\"#999999\">" + formatted_date + "</font></b>"));
    }
  }

  @Override public int getItemCount() {
    return (null != data ? data.size() : 0);
  }

  class MyViewHolder extends RecyclerView.ViewHolder {
    TextView textView_message;
    MyViewHolder(View view) {
      super(view);
      this.textView_message = view.findViewById(R.id.textView_message);
    }
  }
}