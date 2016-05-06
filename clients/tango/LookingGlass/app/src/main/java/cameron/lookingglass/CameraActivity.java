package cameron.lookingglass;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.Image;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;


import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.Tango.OnTangoUpdateListener;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import com.projecttango.rajawali.Pose;
import com.projecttango.rajawali.ScenePoseCalculator;

import java.util.ArrayList;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class CameraActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String sTranslationFormat = "Translation: %f, %f, %f";
    private static final String sRotationFormat = "Rotation: %f, %f, %f, %f";

    private static final int SECS_TO_MILLISECS = 1000;
    private static final double UPDATE_INTERVAL_MS = 100.0;
    private double mPreviousTimeStamp;
    private double mTimeToNextUpdate = UPDATE_INTERVAL_MS;

    private TextView mTranslationTextView;
    private TextView mRotationTextView;

    private ImageButton select;
    private ImageButton toggleTranslate;
    private ImageButton toggleRotate;
    private ImageButton backButton;
    private ImageButton lockButton;
    private ImageButton recButton;

    private Tango mTango;
    private TangoConfig mConfig;
    private boolean mIsTangoServiceConnected;

    private boolean recording;
    private boolean active;
    private boolean sendTranslate;
    private boolean sendRotate;

    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera);

        Intent i = getIntent();
        mSocket = SocketClient.getGLobalSocket();

        active = false;
        recording = false;
        sendTranslate = false;
        sendRotate = false;

        recButton = (ImageButton) findViewById(R.id.rec_button);
        select = (ImageButton) findViewById(R.id.select_area);
        toggleRotate = (ImageButton) findViewById(R.id.select_rotate);
        toggleTranslate = (ImageButton) findViewById(R.id.select_translate);
        backButton = (ImageButton) findViewById(R.id.back_button);
        lockButton = (ImageButton) findViewById(R.id.lock_button);

        backButton.setOnClickListener(new OnClickListener(){
            @Override
            //On click function
            public void onClick(View view) {
                setResult(Activity.RESULT_OK);
                finish();
            }
        });

        recButton.setOnClickListener(new OnClickListener(){
            @Override
            //On click function
            public void onClick(View view) {
                record();
            }
        });

        lockButton.setOnClickListener(new OnClickListener(){
            @Override
            //On click function
            public void onClick(View view) {
                activate();
            }
        });

        recButton.setOnClickListener(new OnClickListener(){
            @Override
            //On click function
            public void onClick(View view) {
                record();
            }
        });

        recButton.setOnClickListener(new OnClickListener(){
            @Override
            //On click function
            public void onClick(View view) {
                record();
            }
        });

        mTranslationTextView = (TextView) findViewById(R.id.translation_textview);
        mRotationTextView = (TextView) findViewById(R.id.rotation_textview);

        // Instantiate Tango client
        mTango = new Tango(this);

        // Set up Tango configuration for motion tracking
        // If you want to use other APIs, add more appropriate to the config
        // like: mConfig.putBoolean(TangoConfig.KEY_BOOLEAN_DEPTH, true)
        mConfig = mTango.getConfig(TangoConfig.CONFIG_TYPE_CURRENT);
        mConfig.putBoolean(TangoConfig.KEY_BOOLEAN_MOTIONTRACKING, true);

    }

    private void activate() {
        if (recording) {
            mSocket.emit("stop playback");
            recording = false;
        }
        if (active) {
            active = false;
        }
        else {
            mSocket.emit("store");
            active = true;
        }
    }

    private void record() {
        if (recording) {
            mSocket.emit("stop playback");
            recording = false;
        }
        else if (active) {
            mSocket.emit("record");
            recording = true;
        }
        else {
            mSocket.emit("playback");
            recording = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Lock the Tango configuration and reconnect to the service each time
        // the app is brought to the foreground.
        super.onResume();
        if (!mIsTangoServiceConnected) {
            try {
                setTangoListeners();
            } catch (TangoErrorException e) {
                Toast.makeText(this, "Tango Error! Restart the app!",
                        Toast.LENGTH_SHORT).show();
            }
            try {
                mTango.connect(mConfig);
                mIsTangoServiceConnected = true;
            } catch (TangoOutOfDateException e) {
                Toast.makeText(getApplicationContext(),
                        "Tango Service out of date!", Toast.LENGTH_SHORT)
                        .show();
            } catch (TangoErrorException e) {
                Toast.makeText(getApplicationContext(),
                        "Tango Error! Restart the app!", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // When the app is pushed to the background, unlock the Tango
        // configuration and disconnect
        // from the service so that other apps will behave properly.
        try {
            mTango.disconnect();
            mIsTangoServiceConnected = false;
        } catch (TangoErrorException e) {
            Toast.makeText(getApplicationContext(), "Tango Error!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setTangoListeners() {
        // Select coordinate frame pairs
        ArrayList<TangoCoordinateFramePair> framePairs = new ArrayList<TangoCoordinateFramePair>();
        framePairs.add(new TangoCoordinateFramePair(
                TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                TangoPoseData.COORDINATE_FRAME_DEVICE));

        // Add a listener for Tango pose data
        mTango.connectListener(framePairs, new OnTangoUpdateListener() {

            @Override
            public void onPoseAvailable(TangoPoseData pose) {
                // Format Translation and Rotation data
                final String translationMsg = String.format(sTranslationFormat,
                        pose.translation[0], pose.translation[1],
                        pose.translation[2]);
                final String rotationMsg = String.format(sRotationFormat,
                        pose.rotation[0], pose.rotation[1], pose.rotation[2],
                        pose.rotation[3]);

                // Output to LogCat
                String logMsg = translationMsg + " | " + rotationMsg;
                Log.i(TAG, logMsg);

                final double deltaTime = (pose.timestamp - mPreviousTimeStamp)
                        * SECS_TO_MILLISECS;
                mPreviousTimeStamp = pose.timestamp;
                mTimeToNextUpdate -= deltaTime;

                // Throttle updates to the UI based on UPDATE_INTERVAL_MS.
                if (mTimeToNextUpdate < 0.0) {
                    mTimeToNextUpdate = UPDATE_INTERVAL_MS;

                    if (active) {
                        try {
                            attemptSend("updateCameraPose",pose);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    // Display data in TextViews. This must be done inside a
                    // runOnUiThread call because
                    // it affects the UI, which will cause an error if performed
                    // from the Tango
                    // service thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRotationTextView.setText(rotationMsg);
                            mTranslationTextView.setText(translationMsg);
                        }
                    });
                }
            }

            @Override
            public void onXyzIjAvailable(TangoXyzIjData arg0) {
                // Ignoring XyzIj data
            }

            @Override
            public void onTangoEvent(TangoEvent arg0) {
                // Ignoring TangoEvents
            }

            @Override
            public void onFrameAvailable(int arg0) {
                // Ignoring onFrameAvailable Events
            }
        });
    }

    private void attemptSend(String call, TangoPoseData pose) throws JSONException {
        if (!mSocket.connected()) return;

        Pose openGLPose = ScenePoseCalculator.toOpenGLPose(pose);
        Vector3 t = openGLPose.getPosition();
        Quaternion r = openGLPose.getOrientation();

        JSONObject data = new JSONObject();
        data.put("translation", new JSONArray(new double[]{t.x, t.y, t.z}));
        data.put("rotation", new JSONArray(new double[]{r.x, r.y, r.z, r.w}));

        // perform the sending message attempt.
        mSocket.emit(call, data);
    }

}
