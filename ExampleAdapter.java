package com.W4ereT1ckRtB1tch.bibl.ui.prodlenie;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.W4ereT1ckRtB1tch.bibl.R;

import java.util.ArrayList;

//класс формирующий список
public class ExampleAdapter extends RecyclerView.Adapter<ExampleAdapter.ExampleViewHolder> {
    private ArrayList<ExampleItem> mExampleList;
    //слушатель кнопки элемента
    private OnItemClickListener mListener;

    // интерфейс который будем имлементировать слушая адаптер, метод удаления элемента списка
    public interface OnItemClickListener {
        void onDeleteClick(int position);

    }

    //слушатель элемента
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class ExampleViewHolder extends RecyclerView.ViewHolder {
        //текст штрих кода на эелементе
        public TextView mItemBarcode;
        //кнопка удалить на элементе списка
        public Button delBookBtn;

        public ExampleViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            //находим элементы
            mItemBarcode = (TextView) itemView.findViewById(R.id.itembarcode);
            delBookBtn = (Button) itemView.findViewById(R.id.delBookBtn);

            //слушаем соответсвующую кнопку на соответсвующем элементе
            delBookBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(position);
                        }
                    }

                }
            });

        }
    }
    //методы сборки списка
    //метод создания списка первоначального
    public ExampleAdapter(ArrayList<ExampleItem> exampleList) {
        mExampleList = exampleList;
    }

    @NonNull
    @Override
    //раздуваем элемент списка из шаблона XML
    public ExampleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_recycler_item, parent, false);
        ExampleViewHolder exampleViewHolder = new ExampleViewHolder(view, mListener);
        return exampleViewHolder;
    }

    @Override
    //передаем значения в элемент
    public void onBindViewHolder(@NonNull ExampleViewHolder holder, int position) {
        ExampleItem currentItem = mExampleList.get(position);
        holder.mItemBarcode.setText(currentItem.getmItemBarcode());
    }

    //возврат количества элементов
    @Override
    public int getItemCount() {
        return mExampleList.size();
    }
}
