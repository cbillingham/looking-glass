package cameron.lookingglass;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MenuActivity extends AppCompatActivity {

    static final int CAMERA_INTENT = 1;

    private static int requestConnects = 0;
    private Socket mSocket;

    private Spinner cameraSelector;
    private Button newCamera;
    private ImageButton go;

    private ArrayList<String> mayaCameras;

    private Boolean initialDisp = true;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_menu);

        SocketClient app = new SocketClient();
        mSocket = app.getSocket();
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT, connect);
        mSocket.on("new camera list", newCameraList);
        mSocket.connect();

        cameraSelector = (Spinner) findViewById(R.id.camera_selector);
        newCamera = (Button) findViewById(R.id.create_camera);
        go = (ImageButton) findViewById(R.id.go_button);


        mayaCameras = new ArrayList<String>();

        ArrayAdapter<String> cameraAdapter = new ArrayAdapter<String>(MenuActivity.this, android.R.layout.simple_spinner_dropdown_item, mayaCameras);
        cameraAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cameraSelector.setAdapter(cameraAdapter);

        cameraSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (initialDisp) {
                    initialDisp = false;
                    return;
                } else {
                    JSONObject data = new JSONObject();
                    try {
                        data.put("name", adapterView.getItemAtPosition(i).toString());
                        mSocket.emit("choose camera", data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        newCamera.setOnClickListener(new OnClickListener(){
            @Override
            //On click function
            public void onClick(View view) {
                promptForName();
            }
        });

        go.setOnClickListener(new OnClickListener(){
            @Override
            //On click function
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), CameraActivity.class);
                startActivityForResult(intent, CAMERA_INTENT);
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == CAMERA_INTENT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                //Do nothing in our case
            }
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        mSocket.disconnect();
        mSocket.off("connect");
        mSocket.off("new camera list", newCameraList);
    }

    private void promptForName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Camera Name");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newCamera(input.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void newCamera(String name) {
        JSONObject data = new JSONObject();
        try {
            data.put("name", name);
            mSocket.emit("make camera", data);
            ((ArrayAdapter) cameraSelector.getAdapter()).add(name);
            cameraSelector.setSelection(((ArrayAdapter) cameraSelector.getAdapter()).getPosition(name));
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private Emitter.Listener newCameraList = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject data = (JSONObject) args[0];
                JSONArray cameras = data.getJSONArray("cameras");
                ArrayList<String> listdata = new ArrayList<String>();
                if (cameras != null) {
                    for (int i = 0; i < cameras.length(); i++) {
                        listdata.add(cameras.get(i).toString());
                    }
                }
                mayaCameras = listdata;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<String> cameraAdapter = new ArrayAdapter<String>(MenuActivity.this, android.R.layout.simple_spinner_item, mayaCameras);
                        cameraAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        cameraSelector.setAdapter(cameraAdapter);
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener connect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = new JSONObject();
            try {
                data.put("name", "tango");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mSocket.emit("connection name", data);

            mSocket.emit("camera list");
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {

        @Override
        public void call(Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MenuActivity.this.getApplicationContext(),
                            "Connection Failed... trying again", Toast.LENGTH_SHORT).show();
                }
            });
            if (requestConnects > 3) {
                Intent goBackToLoginScreen = new Intent(MenuActivity.this, LoginActivity.class);
                startActivity(goBackToLoginScreen);
                finish();
            }
            requestConnects += 1;
        }
    };


    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Menu Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://cameron.lookingglass/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Menu Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://cameron.lookingglass/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
