package affily.id.mypreloaddata;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import affily.id.mypreloaddata.service.DataManagerService;

import static affily.id.mypreloaddata.service.DataManagerService.CANCEL_MESSAGE;
import static affily.id.mypreloaddata.service.DataManagerService.FAILED_MESSAGE;
import static affily.id.mypreloaddata.service.DataManagerService.PREPARATION_MESSAGE;
import static affily.id.mypreloaddata.service.DataManagerService.SUCCESS_MESSAGE;
import static affily.id.mypreloaddata.service.DataManagerService.UPDATE_MESSAGE;

public class MainActivity extends AppCompatActivity implements HandlerCallback {
    private static final String TAG = MainActivity.class.getSimpleName();
    ProgressBar progressBar;
    Messenger activityMessenger;

    Messenger boundService;
    boolean isServiceBound;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progress_bar);

        //ikat dan lepaskan DataManagerService
        Intent boundServiceIntent = new Intent(MainActivity.this, DataManagerService.class);
        activityMessenger = new Messenger(new IncomingHandler(this));
        boundServiceIntent.putExtra(DataManagerService.ACTIVITY_HANDLER,activityMessenger);

        bindService(boundServiceIntent,serviceConnection,BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            boundService = new Messenger(iBinder);
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isServiceBound = false;
        }
    };

    @Override
    public void preparation() {
        Toast.makeText(this,"Mulia memuat data",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void updateProgress(long progress) {
        Log.e(TAG, "updateProgress: "+ progress);
        progressBar.setProgress((int) progress);
    }

    @Override
    public void loadSuccess() {
        Toast.makeText(this,"Berhasil",Toast.LENGTH_SHORT).show();
        startActivity(new Intent(MainActivity.this,MahasiswaActivity.class));
        finish();
    }

    @Override
    public void loadFailed() {
        Toast.makeText(this,"Gagal",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void loadCancel() {
        finish();
    }

    private static class IncomingHandler extends Handler{
        WeakReference<HandlerCallback> weakReference;

        IncomingHandler(HandlerCallback callback){
            weakReference = new WeakReference<>(callback);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case PREPARATION_MESSAGE:
                    weakReference.get().preparation();
                    break;
                case UPDATE_MESSAGE:
                    Bundle bundle = msg.getData();
                    long progress = bundle.getLong("KEY_PROGRESS");
                    weakReference.get().updateProgress(progress);
                    break;
                case SUCCESS_MESSAGE:
                    weakReference.get().loadSuccess();
                    break;
                case FAILED_MESSAGE:
                    weakReference.get().loadFailed();
                    break;
                case CANCEL_MESSAGE:
                    weakReference.get().loadCancel();
                    break;
            }
        }
    }
}

interface HandlerCallback{
    void preparation();
    void updateProgress(long progress);
    void loadSuccess();
    void loadFailed();
    void loadCancel();
}
