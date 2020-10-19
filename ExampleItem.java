package com.W4ereT1ckRtB1tch.bibl.ui.prodlenie;

public class ExampleItem {
private String mItemBarcode;

//класс передачи значений в элемент списка, через конструктор
public ExampleItem(String itemBarcode){
   mItemBarcode = itemBarcode;
}

    public String getmItemBarcode() {
        return mItemBarcode;
    }

    public void setmItemBarcode(String mItemBarcode) {
        this.mItemBarcode = mItemBarcode;
    }
}
