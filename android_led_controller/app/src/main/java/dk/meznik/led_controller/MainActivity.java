package dk.meznik.led_controller;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

// TODO: Better exception handling

public class MainActivity extends FragmentActivity implements OnAmbilWarnaListener {
    static final String TAG = "LED_CONTROLLER";
    static final int INTENSITY_MIN = 20; // Minimum value for LED intensity

    // Connection
    int port = 31337;
    String ip = "192.168.1.198";
    Socket socket;
    InputStream inputStream;

    // Views
    Button buttonSelectColor;
    Button buttonSend;
    SeekBar seekBarIntensity;
    SeekBar seekBarFlashing;
    TextView textViewStatus;

    // Colors
    private int mColor = 0; // Packed color
    int red = 0;
    int green = 0;
    int blue = 0;
    int intensity = 100;                // Intensity of the color. To be implemented
    int flashing = 0;                   // Speed of the flash. To be implemented

    // For saving settings
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        loadSettings();

        seekBarIntensity = (SeekBar) findViewById(R.id.seekbar_intensity);
        seekBarFlashing = (SeekBar) findViewById(R.id.seekbar_flashing);
        textViewStatus = (TextView) findViewById(R.id.textview_status);
        buttonSelectColor = (Button) findViewById(R.id.button_select_color);
        buttonSend = (Button)findViewById(R.id.button_send);
        seekBarFlashing.setProgress(0);
        seekBarIntensity.setProgress(100);
        textViewStatus.setMovementMethod(new ScrollingMovementMethod());

        buttonSelectColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorPicker();
            }
        });
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update();
            }
        });

        // when activity is re-created, we need to set OnAmbilWarnaListener in order to get callbacks.
        if (savedInstanceState != null) {
            AmbilWarnaDialogFragment fragment = (AmbilWarnaDialogFragment) getSupportFragmentManager().findFragmentByTag("color_picker_dialog");
            if (fragment != null) {
                fragment.setOnAmbilWarnaListener(this);
            }
        }

        // Connect to arduino
        connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_item_connection_options:
                showSettingsDialog();
                return true;
            case R.id.menu_item_about:
                Toast.makeText(getApplicationContext(), "Not implemented yet", Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Connects to Arduino and starts a listening thread
    private void connect() {
       new Thread(new Runnable() {
           @Override
           public void run() {
               //if(socket != null && socket.isConnected()) return; // Nothing to do
               try {

                   log("Connecting to IP: " + ip + ":" + port);
                   InetAddress inetaddr = InetAddress.getByName(ip);
                   socket = new Socket(inetaddr, port);
                   if(socket.isConnected() == false) {
                       log("Could not connect!");
                       return;
                   }

                   log("Connected");

                   // Open the stream and listen to it. Not needed, but good for debugging
                   inputStream = socket.getInputStream();
                   while(socket.isConnected()) {
                       String buffer = "";

                       while(inputStream.available() > 0) {
                            buffer += (char)inputStream.read();
                       }

                       if(buffer.length() > 0) {
                           buffer = buffer.replace('\n',(char)0);
                           log(buffer, "< ");
                           buffer = "";
                       }
                       Thread.sleep(800);
                   }
               } catch(Exception e) {
                   log(e.getMessage());
                   e.printStackTrace();
               }
           }
       }).start();
    }

    // Sends data to Arduino
    private void update() {
        intensity = seekBarIntensity.getProgress() + INTENSITY_MIN;
        flashing = seekBarFlashing.getProgress();

        if(socket == null || socket.isConnected() == false) {
            log("update() failed: Not connected to arduino!");
            connect();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                StringBuffer buffer = new StringBuffer(20); // approx
                buffer.append("r"+red);
                buffer.append("g"+green);
                buffer.append("b"+blue);
                buffer.append("i"+intensity);
                buffer.append("f"+flashing);

                try {
                    socket.getOutputStream().write(buffer.toString().getBytes());
                    log("Sent: " + "red = " + red + ", green = " + green + ", blue = " + blue);
                    log("    intensity = " + intensity + ", flashing = " + flashing,"");
                } catch(Exception e) {
                    e.printStackTrace();
                    log("update() failed: Could not send to arduino:");
                    log(e.getMessage());
                    log("Trying to reconnect");
                    connect();
                }
            }
        }).start();
    }

    /**
     * Shows Color Picker dialog fragment. If color wasn't set previously, use BLUE by default.
     */
    private void showColorPicker() {
        int thisColor = mColor == 0 ? Color.WHITE : mColor;

        // create new instance of AmbilWarnaDialogFragment and set OnAmbilWarnaListener listener to it
        // show dialog fragment with some tag value
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        AmbilWarnaDialogFragment fragment = AmbilWarnaDialogFragment.newInstance(thisColor, android.R.style.Theme_Dialog);
        fragment.setOnAmbilWarnaListener(this);

        fragment.show(ft, "color_picker_dialog");
    }

    @Override
    public void onCancel(AmbilWarnaDialogFragment dialogFragment) {
        Log.d(TAG, "onCancel()");
    }

    @Override
    public void onOk(AmbilWarnaDialogFragment dialogFragment, int color) {
        mColor = color;
        red = Color.red(color);
        green = Color.green(color);
        blue = Color.blue(color);
        Log.d(TAG, "red = " + red + ", green = " + green + ", blue = " + blue);
        buttonSelectColor.setBackgroundColor(color);
    }

    private void log(final String msg) {
        log(msg, "> ");
    }
    private void log(final String msg, final String prefix)
    {
        // Im sure there is a better way to do this ...
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewStatus.append(prefix + msg +"\n");
            }
        });
    }


    private void showSettingsDialog()
    {
        ConnectionSettingsDialog connectionSettingsDialog = new ConnectionSettingsDialog(this);
        connectionSettingsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                ConnectionSettingsDialog csd = (ConnectionSettingsDialog)dialog;

                if(csd.changed) {

                    port = csd.port == 0 ? port : csd.port;
                    ip = csd.ip.length() == 0 ? ip : csd.ip;

                    saveSettings();
                    log("Settings changed and saved");

                    // close current session and reopen
                    try {
                        if (socket != null) {
                            socket.close();
                        }
                    } catch (IOException e) {
                        log(e.getMessage());
                        socket = null;
                    }
                }
            }
        });
       connectionSettingsDialog.show();
    }

    public void saveSettings()
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getString(R.string.settings_port), port);
        editor.putString(getString(R.string.settings_ip), ip);
        editor.apply();
        //editor.commit();
    }

    public void loadSettings()
    {
        port = getResources().getInteger(R.integer.default_port);
        ip = getResources().getString(R.string.default_ip);

        port = sharedPreferences.getInt(getString(R.string.settings_port),port);
        ip = sharedPreferences.getString(getString(R.string.settings_ip),ip);
    }

    public String getIp() {return this.ip;}
    public int getPort(){return this.port;}

}
