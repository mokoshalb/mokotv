package app.mokotv.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.balysv.materialripple.MaterialRippleLayout;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import app.mokotv.Config;
import app.mokotv.R;
import app.mokotv.utils.Constant;
import app.mokotv.utils.NetworkCheck;
import cz.msebera.android.httpclient.Header;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ActivityRequest extends AppCompatActivity {
    private EditText et_name, et_email, et_comment;
    private TextView tv_message;
    private TextInputLayout input_et_name, input_et_email, input_et_comment;
    private MaterialRippleLayout bt_submit_comment;
    private boolean task_running = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        input_et_name = findViewById(R.id.input_et_name);
        input_et_email = findViewById(R.id.input_et_email);
        input_et_comment = findViewById(R.id.input_et_comment);
        tv_message = findViewById(R.id.tv_message);
        et_name = findViewById(R.id.et_name);
        et_email = findViewById(R.id.et_email);
        et_comment = findViewById(R.id.et_comment);
        bt_submit_comment = findViewById(R.id.bt_submit_comment);
        bt_submit_comment.setOnClickListener(view -> validateAllField());
        (findViewById(R.id.bt_close)).setOnClickListener(view -> {
            if (task_running) {
                Toast.makeText(getApplicationContext(), R.string.task_running_msg, Toast.LENGTH_LONG).show();
            }else{
                onBackPressed();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void submitCommentToApi() {
        setEnableEditText(false);
        bt_submit_comment.setVisibility(View.GONE);
        task_running = true;
        String name = et_name.getText().toString().trim();
        String email = et_email.getText().toString().trim();
        String comment = et_comment.getText().toString().trim();
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("Name", name);
        params.put("Email", email);
        params.put("Stream", comment);
        client.post(Config.ADMIN_PANEL_URL +"/api/stream_request/?api_key=" + Config.API_KEY, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                bt_submit_comment.setVisibility(View.VISIBLE);
                setEnableEditText(true);
                task_running = false;

                et_comment.setText("");
                String result = new String(responseBody);
                String reportMsg;
                boolean reportResp;
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Config.ARRAY_NAME);
                    JSONObject objJson = jsonArray.getJSONObject(0);
                    reportResp = objJson.getBoolean("success");
                    reportMsg = objJson.getString("msg");
                    if(reportResp){
                        String str_msg = getString(R.string.send_report_success);
                        showMessageLayout(true, false, str_msg);
                        Toast.makeText(ActivityRequest.this, reportMsg, Toast.LENGTH_SHORT).show();
                    } else {
                        setEnableEditText(true);
                        String str_msg_fail = getString(R.string.send_report_failure);
                        showMessageLayout(true, true, str_msg_fail);
                        Toast.makeText(ActivityRequest.this, reportMsg, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                bt_submit_comment.setVisibility(View.VISIBLE);
                setEnableEditText(true);
                task_running = false;
                onFailRequest();
            }
        });
    }

    private void onFailRequest() {
        setEnableEditText(true);
        if (NetworkCheck.isConnect(this)) {
            showMessageLayout(true, true, getString(R.string.failed_text_comment));
        } else {
            showMessageLayout(true, true, getString(R.string.no_internet_text_comment));
        }
    }

    private void validateAllField() {
        showMessageLayout(false, false, "");
        input_et_name.setEnabled(false);
        input_et_email.setEnabled(false);
        input_et_comment.setEnabled(false);
        if (!validateName()) return;
        if (!validateEmail()) return;
        if (!validateComment()) return;
        hideKeyboard();
        new Handler().postDelayed(this::submitCommentToApi, Constant.DELAY_TIME_MEDIUM);
    }

    private boolean validateName() {
        if (et_name.getText().toString().trim().isEmpty()) {
            input_et_name.setEnabled(true);
            input_et_name.setError(getString(R.string.invalid_name));
            requestFocus(et_name);
            return false;
        }
        input_et_name.setError(null);
        return true;
    }

    private boolean validateEmail() {
        String email = et_email.getText().toString().trim();
        if (email.isEmpty() || !isValidEmail(email)) {
            input_et_email.setEnabled(true);
            input_et_email.setError(getString(R.string.invalid_email));
            requestFocus(et_email);
            return false;
        }
        input_et_email.setError(null);
        return true;
    }

    private boolean validateComment() {
        if (et_comment.getText().toString().trim().isEmpty()) {
            input_et_comment.setEnabled(true);
            input_et_comment.setError(getString(R.string.invalid_report));
            requestFocus(et_comment);
            return false;
        }
        input_et_comment.setError(null);
        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void setEnableEditText(boolean flag) {
        et_name.setEnabled(flag);
        et_email.setEnabled(flag);
        et_comment.setEnabled(flag);
    }

    private void showMessageLayout(boolean visible, boolean isError, String msg) {
        tv_message.setText(msg);
        tv_message.setVisibility(View.GONE);
        tv_message.setBackgroundColor(getResources().getColor(R.color.green_color));
        if (visible) tv_message.setVisibility(View.VISIBLE);
        if (isError) tv_message.setBackgroundColor(getResources().getColor(R.color.red_color));
    }

    @Override
    public void onBackPressed() {
        if (task_running) {
            Toast.makeText(getApplicationContext(), R.string.task_running_msg, Toast.LENGTH_LONG).show();
        }else{
            super.onBackPressed();
        }
    }

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
