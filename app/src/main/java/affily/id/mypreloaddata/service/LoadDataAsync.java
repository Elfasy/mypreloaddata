package affily.id.mypreloaddata.service;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import affily.id.mypreloaddata.R;
import affily.id.mypreloaddata.database.MahasiswaHelper;
import affily.id.mypreloaddata.model.MahasiswaModel;
import affily.id.mypreloaddata.pref.AppPreference;

public class LoadDataAsync extends AsyncTask<Void, Integer, Boolean> {
    private final String TAG = LoadDataAsync.class.getSimpleName();
    private MahasiswaHelper mahasiswaHelper;
    private AppPreference appPreference;
    private WeakReference<LoadDataCallback> weakCallback;
    private WeakReference<Resources> weakResources;
    double progress;
    double maxProgress = 100;

    LoadDataAsync(MahasiswaHelper mahasiswaHelper, AppPreference appPreference, LoadDataCallback loadDataCallback, Resources resources) {
        this.mahasiswaHelper = mahasiswaHelper;
        this.appPreference = appPreference;
        this.weakCallback = new WeakReference<>(loadDataCallback);
        this.weakResources = new WeakReference<>(resources);
    }

    @Override
    protected void onPreExecute() {
        Log.e(TAG, "On pre");
        weakCallback.get().onPreLoad();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        weakCallback.get().onProgressUpdate(values[0]);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        Boolean firstRun = appPreference.getFirstRun();
        if (firstRun) {
            ArrayList<MahasiswaModel> mahasiswaModels = preLoadRaw();

            mahasiswaHelper.open();

            progress = 30;
            publishProgress((int) progress);
            Double progressMaxInsert = 80.0;
            Double progressDiff = (progressMaxInsert - progress) / mahasiswaModels.size();

            boolean isInsertSuccess;
            try {
//                for (MahasiswaModel model : mahasiswaModels) {
//                    mahasiswaHelper.insert(model);
//                    progress += progressDiff;
//                    publishProgress((int) progress);
//                }
                mahasiswaHelper.beginTransaction();
                for (MahasiswaModel model : mahasiswaModels) {
                    if (isCancelled()) {
                        break;
                    } else {
                        mahasiswaHelper.insertTransaction(model);
                        progress += progressDiff;
                        publishProgress((int) progress);
                    }
                }

                if (isCancelled()) {
                    isInsertSuccess = false;
                    appPreference.setFirstRun(true);
                    weakCallback.get().onLoadCancel();
                } else {
                    mahasiswaHelper.setTransactionSuccess();
                    isInsertSuccess = true;
                    appPreference.setFirstRun(false);
                }
            } catch (Exception e) {
                Log.e(TAG, "ada salah");
                isInsertSuccess = false;
            } finally {
                mahasiswaHelper.endTransaction();
            }

            mahasiswaHelper.close();

            publishProgress((int) maxProgress);
            return isInsertSuccess;
        } else {
            try {
                synchronized (this) {
                    this.wait(2000);
                    publishProgress(50);

                    this.wait(2000);
                    publishProgress((int) maxProgress);
                    return true;
                }
            } catch (Exception e) {
                return false;
            }
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (aBoolean) {
            weakCallback.get().onLoadSuccess();
        } else {
            weakCallback.get().onLoadFailed();
        }
    }

    //method untuk mengelola file raw
    private ArrayList<MahasiswaModel> preLoadRaw() {
        ArrayList<MahasiswaModel> mahasiswaModels = new ArrayList<>();
        String line;
        BufferedReader reader;
        try {
            Resources res = weakResources.get();
            InputStream raw_dict = res.openRawResource(R.raw.data_mahasiswa);

            reader = new BufferedReader(new InputStreamReader(raw_dict));
            do {
                line = reader.readLine();
                String[] splitStr = line.split("\t");

                MahasiswaModel mahasiswaModel;

                mahasiswaModel = new MahasiswaModel(splitStr[0], splitStr[1]);
                mahasiswaModels.add(mahasiswaModel);
            } while (line != null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mahasiswaModels;
    }

}
