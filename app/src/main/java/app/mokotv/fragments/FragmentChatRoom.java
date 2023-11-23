package app.mokotv.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import app.mokotv.Config;
import app.mokotv.R;
import app.mokotv.activities.MainActivity;
import app.mokotv.adapters.AdapterChatRoom;
import app.mokotv.models.Chat;
import app.mokotv.utils.Tools;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class FragmentChatRoom extends Fragment {

    private static final int ANTI_FLOOD_SECONDS = 1;
    private Random rand = new Random();
    private int chatID = rand.nextInt(100000);
    private String username = "Anonymous"+chatID;
    private RecyclerView main_recycler_view;
    private String userID;
    private AdapterChatRoom adapter;
    private DatabaseReference databaseRef;
    private ImageButton imageButton_send;
    private EditText editText_message;
    private ArrayList<Chat> messageArrayList = new ArrayList<>();
    private ProgressBar progressBar;
    private long last_message_timestamp = 0;
    private Toolbar toolbar;
    private MainActivity mainActivity;
    private InterstitialAd interstitialAd;
    private int counter = 1;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = (MainActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public FragmentChatRoom() {
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View root_view = inflater.inflate(R.layout.fragment_chat_room, null);
        main_recycler_view = root_view.findViewById(R.id.main_recycler_view);
        main_recycler_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        main_recycler_view.setHasFixedSize(true);
        toolbar = root_view.findViewById(R.id.toolbar);
        progressBar = root_view.findViewById(R.id.progressBar);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseRef = database.getReference();
        loadInterstitialAd();
        progressBar.setVisibility(View.VISIBLE);
        adapter = new AdapterChatRoom(messageArrayList);
        main_recycler_view.setAdapter(adapter);
        imageButton_send = root_view.findViewById(R.id.imageButton_send);
        editText_message = root_view.findViewById(R.id.editText_message);
        showInterstitialAd();
        try {
            databaseRef.child("messages").limitToLast(50).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NotNull DataSnapshot dataSnapshot, String s) {
                    progressBar.setVisibility(View.GONE);
                    Chat new_message = dataSnapshot.getValue(Chat.class);
                    messageArrayList.add(new_message);
                    adapter.notifyDataSetChanged();
                    main_recycler_view.scrollToPosition(adapter.getItemCount() - 1);
                }

                @Override
                public void onChildChanged(@NotNull DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(@NotNull DataSnapshot dataSnapshot) {
                    Log.d("REMOVED", Objects.requireNonNull(dataSnapshot.getValue(Chat.class)).toString());
                    messageArrayList.remove(dataSnapshot.getValue(Chat.class));
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onChildMoved(@NotNull DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(@NotNull DatabaseError databaseError) {

                }
            });
        }catch (Exception e){
            Toast.makeText(mainActivity, "Fail to connect to Chat Room", Toast.LENGTH_SHORT).show();
        }
        imageButton_send.setOnClickListener(view -> newMessage(editText_message.getText().toString().trim(), false));
        editText_message.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_SEND)) {
                imageButton_send.performClick();
            }
            return false;
        });
        setupToolbar();
        setUsername();
        return root_view;
    }

    private void newMessage(String new_message, boolean isNotification) {
        if (new_message.isEmpty()) {
            return;
        }
        if ((System.currentTimeMillis() / 1000L - last_message_timestamp) < ANTI_FLOOD_SECONDS) {
            Tools.showErrorSnackBar(getActivity(), Objects.requireNonNull(getActivity()).findViewById(android.R.id.content), "You cannot send messages so fast.").show();
            return;
        }
        editText_message.setText("");
        Chat xmessage = new Chat(userID, username, new_message, System.currentTimeMillis() / 1000L, false, isNotification, Config.COLOUR);
        String key = databaseRef.child("messages").push().getKey();
        assert key != null;
        databaseRef.child("messages").child(key).setValue(xmessage);
        last_message_timestamp = System.currentTimeMillis() / 1000L;
    }

    //Popup message with your username if none found. Change it to your liking
    private void setUsername() {
        userID = Tools.getUniqueID(Objects.requireNonNull(getActivity()).getApplicationContext());
        databaseRef.child("users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                if (!dataSnapshot.exists()) {
                    showUsername();
                } else {
                    username = dataSnapshot.getValue(String.class);
                    Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content), "Logged in as " + username, Snackbar.LENGTH_SHORT).show();
                }
            }
            @Override public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.w("Error", "Get Username Failed", databaseError.toException());
            }
        });
    }

    private void showUsername() {
        AlertDialog.Builder alertDialogUsername = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        alertDialogUsername.setMessage("Enter A Username");
        final EditText input = new EditText(getActivity());
        input.setText(username);
        alertDialogUsername.setView(input);
        alertDialogUsername.setPositiveButton("SAVE", (dialog, id) -> {
            String new_username = input.getText().toString().trim();
            if ((!new_username.toLowerCase().equals(username.toLowerCase())) && (!username.toLowerCase().equals("anonymous"))) {
                newMessage(username + " changed username to " + new_username, true);
            } else {
                if ((!new_username.toLowerCase().equals(username.toLowerCase()))) {
                    username = username.toLowerCase();
                }
            }
            username = new_username;
            databaseRef.child("users").child(userID).setValue(username);
        }).setNegativeButton("CANCEL", (dialog, id) -> dialog.dismiss());
        alertDialogUsername.show();
    }
    private void setupToolbar() {
        toolbar.setTitle(getString(R.string.app_name));
        toolbar.setSubtitle(getString(R.string.drawer_chatroom));
        mainActivity.setSupportActionBar(toolbar);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity.setupNavigationDrawer(toolbar);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        menu.removeItem(R.id.search);
        menu.removeItem(R.id.iptv_search);
        menu.findItem(R.id.edit_username).setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.search).setVisible(false);
        menu.removeItem(R.id.search);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.edit_username) {
            showUsername();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS) {
            MobileAds.initialize(getActivity(), getResources().getString(R.string.admob_app_id));
            interstitialAd = new InterstitialAd(Objects.requireNonNull(getActivity()));
            interstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstitial_unit_id));
            interstitialAd.loadAd(Tools.getAdRequest(getActivity()));
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    interstitialAd.loadAd(Tools.getAdRequest(getActivity()));
                }
            });
        } else {
            Log.d("AdMob", "AdMob Interstitial is Disabled");
        }
    }

    private void showInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS) {
            if (interstitialAd != null && interstitialAd.isLoaded()) {
                if (counter == Config.ADMOB_INTERSTITIAL_ADS_INTERVAL) {
                    interstitialAd.show();
                    counter = 1;
                } else {
                    counter++;
                }
            } else {
                Log.d("AdMob", "Interstitial Ad is Disabled");
            }
        } else {
            Log.d("AdMob", "AdMob Interstitial is Disabled");
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}