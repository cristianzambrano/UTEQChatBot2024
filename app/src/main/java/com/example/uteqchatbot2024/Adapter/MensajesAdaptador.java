package com.example.uteqchatbot2024.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.uteqchatbot2024.Models.Mensaje;
import com.example.uteqchatbot2024.R;

import java.util.ArrayList;
import java.util.List;


public class MensajesAdaptador extends RecyclerView.Adapter<MensajesAdaptador.MensajeViewHolder> {

    int mLastPosition = 0;

    private Context Ctx;
    private List<Mensaje> lstMensajes;

    public void notifyData(ArrayList<Mensaje> myList) {
        this.lstMensajes = myList;

        notifyDataSetChanged();
    }

    public MensajesAdaptador(Context mCtx, List<Mensaje> mensajes) {
        this.lstMensajes = mensajes;
        Ctx = mCtx;
    }

    @Override
    public MensajeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //inflating and returning our view holder
        LayoutInflater inflater = LayoutInflater.from(Ctx);
        View view = inflater.inflate(R.layout.lyitem_usuario, null);
        return new MensajeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MensajeViewHolder holder, int position) {
        Mensaje mensaje = lstMensajes.get(position);
        holder.textViewMensaje.setText(mensaje.getMensaje());
        holder.imageView.setImageResource(mensaje.getAvatar()=="U"?R.drawable.img:R.drawable.avatar);
        mLastPosition =position;
    }

    @Override
    public int getItemCount() {        return(null != lstMensajes?lstMensajes.size():0);    }

    class MensajeViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMensaje;
        ImageView imageView;
        public MensajeViewHolder(View itemView) {
            super(itemView);
            textViewMensaje = itemView.findViewById(R.id.txtpregunta);
            imageView = itemView.findViewById(R.id.imgAvatar);
        }
    }
}

