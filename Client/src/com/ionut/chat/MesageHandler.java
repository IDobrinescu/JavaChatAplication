package com.ionut.chat;

import java.util.ArrayList;

public interface MesageHandler {
    void onMesageReceived(String msg);
    void onArrayReceived(ArrayList<String> arrayList);
    void onFileSaved();
    String requestName();
}
