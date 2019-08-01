package affily.id.mypreloaddata.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import affily.id.mypreloaddata.database.MahasiswaHelper;
import affily.id.mypreloaddata.pref.AppPreference;

public class DataManagerService extends Service {
    public static final int PREPARATION_MESSAGE = 0;
    public static final int UPDATE_MESSAGE = 1;
    public static final int SUCCESS_MESSAGE = 2;
    public static final int FAILED_MESSAGE = 3;
    public static final int CANCEL_MESSAGE = 4;
    public static final String ACTIVITY_HANDLER = "activity handler";

    private String TAG = DataManagerService.class.getSimpleName();
    private LoadDataAsync loadData;
    private Messenger activityMessenger;

    public DataManagerService() {
    }

    LoadDataCallback myCallback = new LoadDataCallback() {
        @Override
        public void onPreLoad() {
            Message message = Message.obtain(null,PREPARATION_MESSAGE);
            try {
                activityMessenger.send(message);
            }catch (RemoteException e){
                e.printStackTrace();
            }
        }

        @Override
        public void onProgressUpdate(long progress) {
            try {
                Message message = Message.obtain(null,UPDATE_MESSAGE);
                Bundle bundle = new Bundle();
                bundle.putLong("KEY_PROGRESS",progress);
                message.setData(bundle);
                activityMessenger.send(message);
            }catch (RemoteException e){
                e.printStackTrace();
            }
        }

        @Override
        public void onLoadSuccess() {
            Message message = Message.obtain(null,SUCCESS_MESSAGE);
            try {
                activityMessenger.send(message);
            }catch (RemoteException e){
                e.printStackTrace();
            }
        }

        @Override
        public void onLoadFailed() {
            Message message = Message.obtain(null,FAILED_MESSAGE);
            try {
                activityMessenger.send(message);
            }catch (RemoteException e){
                e.printStackTrace();
            }
        }

        @Override
        public void onLoadCancel() {
            Message message = Message.obtain(null,CANCEL_MESSAGE);
            try {
                activityMessenger.send(message);
            }catch (RemoteException e){
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        MahasiswaHelper mahasiswaHelper = MahasiswaHelper.getInstance(getApplicationContext());
        AppPreference appPreference = new AppPreference(getApplicationContext());

        loadData = new LoadDataAsync(mahasiswaHelper,appPreference,myCallback,getResources());

        Log.d(TAG, "onCreate: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        loadData.cancel(true);
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public IBinder onBind(Intent intent) {
        activityMessenger = intent.getParcelableExtra(ACTIVITY_HANDLER);

        //jalankan service
        loadData.execute();
        return activityMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: ");
        loadData.cancel(true);
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind: ");
        super.onRebind(intent);
    }
}
