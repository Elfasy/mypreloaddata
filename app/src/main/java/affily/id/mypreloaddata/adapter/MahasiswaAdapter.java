package affily.id.mypreloaddata.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import affily.id.mypreloaddata.R;
import affily.id.mypreloaddata.model.MahasiswaModel;

public class MahasiswaAdapter extends RecyclerView.Adapter<MahasiswaAdapter.Holder> {
    ArrayList<MahasiswaModel> listMahasiswa = new ArrayList<>();

    public MahasiswaAdapter() {
    }

    public void setData(ArrayList<MahasiswaModel> listMahasiswa){
        if (listMahasiswa.size() >0){
            this.listMahasiswa.clear();
        }
        this.listMahasiswa.addAll(listMahasiswa);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mahasiswa_row,parent,false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.tvNim.setText(listMahasiswa.get(position).getNim());
        holder.tvName.setText(listMahasiswa.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return listMahasiswa.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvNim;
        public Holder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_nama);
            tvNim = itemView.findViewById(R.id.tv_nim);
        }
    }
}
